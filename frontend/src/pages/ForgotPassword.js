import React, { useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/client";
import { Mail } from "lucide-react";
import toast from "react-hot-toast";

const ForgotPassword = () => {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { data } = await api.post("/auth/forgot-password", { email });
      toast.success(data.message || "Demande enregistrée");
      setSent(true);
    } catch (error) {
      const msg =
        error.response?.data?.message || "Impossible d'envoyer la demande";
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <Mail className="mx-auto h-12 w-12 text-primary-600" />
          <h2 className="mt-4 text-2xl font-bold text-gray-900">
            Mot de passe oublié
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            Indiquez votre adresse e-mail. Si un compte existe, vous recevrez un
            lien de réinitialisation.
          </p>
        </div>

        {sent ? (
          <p className="text-center text-gray-700">
            Si un compte correspond à cet email, un message a été envoyé.
            Consultez votre boîte de réception.
          </p>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Votre email"
              className="w-full border rounded-lg px-3 py-2 border-gray-300"
            />
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
            >
              {loading ? "Envoi..." : "Envoyer le lien"}
            </button>
          </form>
        )}

        <div className="text-center">
          <Link
            to="/login"
            className="text-primary-600 hover:text-primary-500 text-sm"
          >
            Retour à la connexion
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
