import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/client";
import { User, Mail, Phone, Lock } from "lucide-react";
import toast from "react-hot-toast";
import { useAuth } from "../contexts/AuthContext";

const getApiMessage = (error) => {
  const d = error.response?.data;
  if (!d) return "Une erreur est survenue";
  if (typeof d.message === "string") return d.message;
  return "Une erreur est survenue";
};

const Profile = () => {
  const { refreshUser, logout } = useAuth();
  const navigate = useNavigate();
  const initialEmailRef = useRef("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [passwordSaving, setPasswordSaving] = useState(false);
  const [form, setForm] = useState({
    nom: "",
    email: "",
    telephone: "",
  });
  const [passwordForm, setPasswordForm] = useState({
    motDePasseActuel: "",
    nouveauMotDePasse: "",
    confirmation: "",
  });

  useEffect(() => {
    const load = async () => {
      try {
        const { data } = await api.get("/users/me");
        setForm({
          nom: data.nom || "",
          email: data.email || "",
          telephone: data.telephone || "",
        });
      } catch (e) {
        toast.error(getApiMessage(e));
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await api.put("/users/me", form);
      if (form.email !== initialEmailRef.current) {
        toast.success("Email modifié — reconnectez-vous avec votre nouvel email.");
        logout({ silent: true });
        navigate("/login");
        return;
      }
      await refreshUser();
      toast.success("Profil mis à jour");
    } catch (error) {
      toast.error(getApiMessage(error));
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    if (passwordForm.nouveauMotDePasse !== passwordForm.confirmation) {
      toast.error("Les mots de passe ne correspondent pas");
      return;
    }
    setPasswordSaving(true);
    try {
      await api.put("/users/me/password", {
        motDePasseActuel: passwordForm.motDePasseActuel,
        nouveauMotDePasse: passwordForm.nouveauMotDePasse,
      });
      setPasswordForm({
        motDePasseActuel: "",
        nouveauMotDePasse: "",
        confirmation: "",
      });
      toast.success("Mot de passe modifié");
    } catch (error) {
      toast.error(getApiMessage(error));
    } finally {
      setPasswordSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-24">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600" />
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-10">
      <h1 className="text-3xl font-bold text-gray-900">Mon profil</h1>

      <form
        onSubmit={handleProfileSubmit}
        className="bg-white rounded-lg shadow-md p-6 space-y-4"
      >
        <h2 className="text-lg font-semibold flex items-center gap-2">
          <User className="h-5 w-5" /> Coordonnées
        </h2>
        <div>
          <label className="block text-sm font-medium text-gray-700">Nom</label>
          <input
            type="text"
            required
            value={form.nom}
            onChange={(e) => setForm({ ...form, nom: e.target.value })}
            className="mt-1 w-full border rounded-lg px-3 py-2 border-gray-300"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 flex items-center gap-1">
            <Mail className="h-4 w-4" /> Email
          </label>
          <input
            type="email"
            required
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            className="mt-1 w-full border rounded-lg px-3 py-2 border-gray-300"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 flex items-center gap-1">
            <Phone className="h-4 w-4" /> Téléphone
          </label>
          <input
            type="text"
            required
            value={form.telephone}
            onChange={(e) => setForm({ ...form, telephone: e.target.value })}
            className="mt-1 w-full border rounded-lg px-3 py-2 border-gray-300"
          />
        </div>
        <button
          type="submit"
          disabled={saving}
          className="bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 disabled:opacity-50"
        >
          {saving ? "Enregistrement..." : "Enregistrer"}
        </button>
      </form>

      <form
        onSubmit={handlePasswordSubmit}
        className="bg-white rounded-lg shadow-md p-6 space-y-4"
      >
        <h2 className="text-lg font-semibold flex items-center gap-2">
          <Lock className="h-5 w-5" /> Changer le mot de passe
        </h2>
        <div>
          <label className="block text-sm font-medium text-gray-700">
            Mot de passe actuel
          </label>
          <input
            type="password"
            required
            value={passwordForm.motDePasseActuel}
            onChange={(e) =>
              setPasswordForm({
                ...passwordForm,
                motDePasseActuel: e.target.value,
              })
            }
            className="mt-1 w-full border rounded-lg px-3 py-2 border-gray-300"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">
            Nouveau mot de passe
          </label>
          <input
            type="password"
            required
            minLength={6}
            value={passwordForm.nouveauMotDePasse}
            onChange={(e) =>
              setPasswordForm({
                ...passwordForm,
                nouveauMotDePasse: e.target.value,
              })
            }
            className="mt-1 w-full border rounded-lg px-3 py-2 border-gray-300"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">
            Confirmer le nouveau mot de passe
          </label>
          <input
            type="password"
            required
            value={passwordForm.confirmation}
            onChange={(e) =>
              setPasswordForm({
                ...passwordForm,
                confirmation: e.target.value,
              })
            }
            className="mt-1 w-full border rounded-lg px-3 py-2 border-gray-300"
          />
        </div>
        <button
          type="submit"
          disabled={passwordSaving}
          className="bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-900 disabled:opacity-50"
        >
          {passwordSaving ? "Modification..." : "Mettre à jour le mot de passe"}
        </button>
      </form>
    </div>
  );
};

export default Profile;
