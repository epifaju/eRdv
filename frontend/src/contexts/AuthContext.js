import React, { createContext, useContext, useState, useEffect } from "react";
import axios from "axios";
import toast from "react-hot-toast";

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

  // Configuration axios - toujours sur localhost pour accès navigateur
  const baseURL = "http://localhost:8080/api";
  axios.defaults.baseURL = baseURL;

  // Configuration CORS pour axios
  axios.defaults.withCredentials = false;
  axios.defaults.headers.common["Content-Type"] = "application/json";
  axios.defaults.headers.common["Accept"] = "application/json";

  // Intercepteur pour ajouter le token aux requêtes
  axios.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem("token");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  // Intercepteur pour gérer les erreurs d'authentification et CORS
  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      console.error("Erreur axios:", error);

      if (error.response?.status === 401) {
        logout();
        toast.error("Session expirée. Veuillez vous reconnecter.");
      } else if (
        error.code === "ERR_NETWORK" ||
        error.message.includes("CORS")
      ) {
        toast.error(
          "Erreur de connexion au serveur. Vérifiez que le backend est démarré."
        );
        console.error("Erreur CORS/Network:", error);
      } else if (error.response?.status === 403) {
        toast.error("Accès refusé. Vérifiez vos permissions.");
      } else if (error.response?.status >= 500) {
        toast.error("Erreur serveur. Veuillez réessayer plus tard.");
      }
      return Promise.reject(error);
    }
  );

  useEffect(() => {
    const token = localStorage.getItem("token");
    const userData = localStorage.getItem("user");

    if (token && userData) {
      try {
        setUser(JSON.parse(userData));
        setIsAuthenticated(true);
      } catch (error) {
        console.error("Erreur lors du parsing des données utilisateur:", error);
        logout();
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, motDePasse) => {
    try {
      const response = await axios.post("/auth/login", { email, motDePasse });
      const { token, ...userData } = response.data;

      localStorage.setItem("token", token);
      localStorage.setItem("user", JSON.stringify(userData));

      setUser(userData);
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
      const response = await axios.post("/auth/register", userData);
      const { token, ...user } = response.data;

      localStorage.setItem("token", token);
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

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
    setIsAuthenticated(false);
    toast.success("Déconnexion réussie");
  };

  const value = {
    user,
    isAuthenticated,
    loading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
