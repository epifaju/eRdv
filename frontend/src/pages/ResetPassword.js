import React, { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import api from "../api/client";
import toast from "react-hot-toast";

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") || "";
  const navigate = useNavigate();
  const [nouveauMotDePasse, setNouveauMotDePasse] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!token) {
      toast.error("Lien invalide (token manquant)");
      return;
    }
    if (nouveauMotDePasse !== confirmation) {
      toast.error("Les mots de passe ne correspondent pas");
      return;
    }
    setLoading(true);
    try {
      await api.post("/auth/reset-password", {
        token,
        nouveauMotDePasse,
      });
      toast.success("Mot de passe réinitialisé. Vous pouvez vous connecter.");
      navigate("/login");
    } catch (error) {
      const msg =
        error.response?.data?.message ||
        "Impossible de réinitialiser le mot de passe";
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-lg shadow-md">
        <h2 className="text-2xl font-bold text-center text-gray-900">
          Nouveau mot de passe
        </h2>
        {!token && (
          <p className="text-red-600 text-sm text-center">
            Lien incomplet. Utilisez le lien reçu par email.
          </p>
        )}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">
              Nouveau mot de passe
            </label>
            <input
              type="password"
              required
              minLength={6}
              value={nouveauMotDePasse}
              onChange={(e) => setNouveauMotDePasse(e.target.value)}
              className="mt-1 w-full border rounded-lg px-3 py-2 border-gray-300"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">
              Confirmation
            </label>
            <input
              type="password"
              required
              value={confirmation}
              onChange={(e) => setConfirmation(e.target.value)}
              className="mt-1 w-full border rounded-lg px-3 py-2 border-gray-300"
            />
          </div>
          <button
            type="submit"
            disabled={loading || !token}
            className="w-full py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
          >
            {loading ? "Enregistrement..." : "Valider"}
          </button>
        </form>
        <div className="text-center">
          <Link to="/login" className="text-primary-600 text-sm">
            Connexion
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ResetPassword;
