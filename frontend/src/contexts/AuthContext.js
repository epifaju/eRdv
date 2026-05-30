import React, { createContext, useContext, useState, useEffect } from "react";
import toast from "react-hot-toast";
import api, { setSessionExpiredHandler } from "../api/client";

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setSessionExpiredHandler(() => {
      setUser(null);
      setIsAuthenticated(false);
    });
  }, []);

  /** Rôle et profil alignés sur le backend (évite ADMIN en localStorage alors que le compte est USER). */
  useEffect(() => {
    let cancelled = false;
    (async () => {
      const token = localStorage.getItem("token");
      if (!token) {
        setLoading(false);
        return;
      }
      try {
        const { data } = await api.get("/users/me");
        if (cancelled) return;
        const u = {
          id: data.id,
          nom: data.nom,
          email: data.email,
          role: data.role,
          telephone: data.telephone,
          prestataireId: data.prestataireId ?? null,
          prestataireNom: data.prestataireNom ?? null,
        };
        localStorage.setItem("user", JSON.stringify(u));
        setUser(u);
        setIsAuthenticated(true);
      } catch (error) {
        if (cancelled) return;
        const status = error.response?.status;
        if (status === 401 || status === 403) {
          localStorage.removeItem("token");
          localStorage.removeItem("refreshToken");
          localStorage.removeItem("user");
          setUser(null);
          setIsAuthenticated(false);
        } else {
          const raw = localStorage.getItem("user");
          if (raw) {
            try {
              setUser(JSON.parse(raw));
              setIsAuthenticated(true);
            } catch {
              localStorage.removeItem("user");
            }
          }
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const login = async (email, motDePasse) => {
    try {
      const response = await api.post("/auth/login", { email, motDePasse });
      const { token, refreshToken, ...userData } = response.data;

      localStorage.setItem("token", token);
      if (refreshToken) {
        localStorage.setItem("refreshToken", refreshToken);
      }
      localStorage.setItem("user", JSON.stringify(userData));

      setUser({
        ...userData,
        prestataireId: userData.prestataireId ?? null,
        prestataireNom: userData.prestataireNom ?? null,
      });
      setIsAuthenticated(true);

      toast.success("Connexion réussie !");
      return true;
    } catch (error) {
      const message =
        error.response?.data?.message || "Erreur lors de la connexion";
      toast.error(message);
      return false;
    }
  };

  const register = async (userData) => {
    try {
      const response = await api.post("/auth/register", userData);
      const { token, refreshToken, ...user } = response.data;

      localStorage.setItem("token", token);
      if (refreshToken) {
        localStorage.setItem("refreshToken", refreshToken);
      }
      localStorage.setItem("user", JSON.stringify(user));

      setUser(user);
      setIsAuthenticated(true);

      toast.success("Inscription réussie !");
      return true;
    } catch (error) {
      const message =
        error.response?.data?.message || "Erreur lors de l'inscription";
      toast.error(message);
      return false;
    }
  };

  const logout = async (options = {}) => {
    const refreshToken = localStorage.getItem("refreshToken");
    if (refreshToken) {
      try {
        await api.post("/auth/logout", { refreshToken });
      } catch {
        // Déconnexion locale même si l'API échoue
      }
    }
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    setUser(null);
    setIsAuthenticated(false);
    if (!options.silent) {
      toast.success("Déconnexion réussie");
    }
  };

  const refreshUser = async () => {
    const { data } = await api.get("/users/me");
    const u = {
      id: data.id,
      nom: data.nom,
      email: data.email,
      role: data.role,
      telephone: data.telephone,
      prestataireId: data.prestataireId ?? null,
      prestataireNom: data.prestataireNom ?? null,
    };
    localStorage.setItem("user", JSON.stringify(u));
    setUser(u);
  };

  const value = {
    user,
    isAuthenticated,
    loading,
    login,
    register,
    logout,
    refreshUser,
  };

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
};
