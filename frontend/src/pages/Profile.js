import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/client";
import { User, Mail, Phone, Lock, Download, Trash2, MessageSquare } from "lucide-react";
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
  const [deletePassword, setDeletePassword] = useState("");
  const [deleting, setDeleting] = useState(false);
  const [form, setForm] = useState({
    nom: "",
    email: "",
    telephone: "",
    consentementSmsRappels: false,
  });
  const [consentementSmsRappelsAt, setConsentementSmsRappelsAt] = useState(null);
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
          consentementSmsRappels: Boolean(data.consentementSmsRappels),
        });
        setConsentementSmsRappelsAt(data.consentementSmsRappelsAt || null);
        initialEmailRef.current = data.email || "";
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
    if (form.consentementSmsRappels && !form.telephone.trim()) {
      toast.error("Indiquez un numéro de mobile pour activer les SMS");
      return;
    }
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
      const { data } = await api.get("/users/me");
      setConsentementSmsRappelsAt(data.consentementSmsRappelsAt || null);
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

  const handleExportData = async () => {
    try {
      const { data } = await api.get("/users/me/export");
      const blob = new Blob([JSON.stringify(data, null, 2)], {
        type: "application/json",
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `erdv-export-${new Date().toISOString().slice(0, 10)}.json`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success("Export téléchargé");
    } catch (error) {
      toast.error(getApiMessage(error));
    }
  };

  const handleDeleteAccount = async (e) => {
    e.preventDefault();
    if (
      !window.confirm(
        "Supprimer définitivement votre compte ? Cette action est irréversible."
      )
    ) {
      return;
    }
    setDeleting(true);
    try {
      await api.delete("/users/me", { data: { motDePasse: deletePassword } });
      toast.success("Compte supprimé");
      logout({ silent: true });
      navigate("/");
    } catch (error) {
      toast.error(getApiMessage(error));
    } finally {
      setDeleting(false);
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
        <div className="rounded-lg border border-gray-200 bg-gray-50 p-4 space-y-2">
          <label className="flex items-start gap-3 cursor-pointer">
            <input
              type="checkbox"
              checked={form.consentementSmsRappels}
              onChange={(e) =>
                setForm({
                  ...form,
                  consentementSmsRappels: e.target.checked,
                })
              }
              className="mt-1 h-4 w-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
            />
            <span className="text-sm text-gray-700">
              <span className="font-medium flex items-center gap-1">
                <MessageSquare className="h-4 w-4" />
                Recevoir des SMS de rappel de rendez-vous
              </span>
              <span className="block mt-1 text-gray-600">
                J&apos;accepte de recevoir par SMS des rappels liés à mes rendez-vous
                (J-1 et 2 h avant). Ce consentement est facultatif, révocable à tout
                moment depuis ce profil. Les e-mails de rappel restent envoyés
                indépendamment de ce choix.
              </span>
            </span>
          </label>
          {consentementSmsRappelsAt && form.consentementSmsRappels && (
            <p className="text-xs text-gray-500 pl-7">
              Consentement enregistré le{" "}
              {new Date(consentementSmsRappelsAt).toLocaleString("fr-FR")}
            </p>
          )}
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

      <div className="bg-white rounded-lg shadow-md p-6 space-y-4 border border-gray-100">
        <h2 className="text-lg font-semibold">Données personnelles (RGPD)</h2>
        <p className="text-sm text-gray-600">
          Exportez vos données ou supprimez votre compte. Les rendez-vous passés
          restent conservés de façon anonymisée.
        </p>
        <button
          type="button"
          onClick={handleExportData}
          className="inline-flex items-center gap-2 bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 text-sm"
        >
          <Download className="h-4 w-4" />
          Télécharger mes données (JSON)
        </button>

        <form onSubmit={handleDeleteAccount} className="pt-4 border-t space-y-3">
          <h3 className="text-sm font-medium text-red-700 flex items-center gap-1">
            <Trash2 className="h-4 w-4" /> Supprimer mon compte
          </h3>
          <input
            type="password"
            required
            placeholder="Mot de passe pour confirmer"
            value={deletePassword}
            onChange={(e) => setDeletePassword(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm"
          />
          <button
            type="submit"
            disabled={deleting}
            className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 disabled:opacity-50 text-sm"
          >
            {deleting ? "Suppression..." : "Supprimer définitivement"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Profile;
