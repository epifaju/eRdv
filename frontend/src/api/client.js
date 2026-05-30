import axios from "axios";
import toast from "react-hot-toast";

const baseURL = process.env.REACT_APP_API_URL || "http://localhost:8080/api";

export const api = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
  withCredentials: false,
});

/** Évite les appels refresh en parallèle */
let refreshInFlight = null;

function runRefresh() {
  if (refreshInFlight) {
    return refreshInFlight;
  }
  const rt = localStorage.getItem("refreshToken");
  if (!rt) {
    return Promise.reject(new Error("no refresh token"));
  }
  refreshInFlight = api
    .post(
      "/auth/refresh",
      { refreshToken: rt },
      { skipAuth: true, skipRefresh: true }
    )
    .then((res) => {
      const { token, refreshToken } = res.data;
      if (token) localStorage.setItem("token", token);
      if (refreshToken) localStorage.setItem("refreshToken", refreshToken);
      return res.data;
    })
    .finally(() => {
      refreshInFlight = null;
    });
  return refreshInFlight;
}

let onSessionExpired = () => {};

export function setSessionExpiredHandler(fn) {
  onSessionExpired = typeof fn === "function" ? fn : () => {};
}

api.interceptors.request.use(
  (config) => {
    if (config.skipAuth) {
      return config;
    }
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    if (status === 401 && originalRequest && !originalRequest._retry) {
      const skipRefresh =
        originalRequest.skipRefresh ||
        originalRequest.url?.includes("/auth/login") ||
        originalRequest.url?.includes("/auth/register") ||
        originalRequest.url?.includes("/auth/refresh");

      if (!skipRefresh && localStorage.getItem("refreshToken")) {
        originalRequest._retry = true;
        try {
          await runRefresh();
          const token = localStorage.getItem("token");
          if (token) {
            originalRequest.headers.Authorization = `Bearer ${token}`;
          }
          return api(originalRequest);
        } catch {
          // chute vers déconnexion
        }
      }

      if (!skipRefresh) {
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("user");
        onSessionExpired();
        toast.error("Session expirée. Veuillez vous reconnecter.");
      }
      return Promise.reject(error);
    }

    if (status === 401) {
      return Promise.reject(error);
    }

    if (error.code === "ERR_NETWORK" || error.message?.includes("CORS")) {
      toast.error(
        "Erreur de connexion au serveur. Vérifiez que le backend est démarré."
      );
    } else if (status === 403) {
      toast.error("Accès refusé. Vérifiez vos permissions.");
    } else if (status === 409) {
      toast.error(
        error.response?.data?.message ||
          "Ce créneau n'est plus disponible. Choisissez un autre horaire."
      );
    } else if (status >= 500) {
      toast.error("Erreur serveur. Veuillez réessayer plus tard.");
    }

    return Promise.reject(error);
  }
);

export default api;
