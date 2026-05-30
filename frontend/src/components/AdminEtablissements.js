import React, { useState, useEffect } from "react";
import api from "../api/client";
import { Building2, Plus, Edit, Trash2 } from "lucide-react";
import toast from "react-hot-toast";

const AdminEtablissements = () => {
  const [etablissements, setEtablissements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({
    nom: "",
    adresse: "",
    ville: "",
    codePostal: "",
    telephone: "",
    actif: true,
  });

  const load = async () => {
    try {
      const { data } = await api.get("/etablissements/admin/all");
      setEtablissements(data);
    } catch {
      toast.error("Impossible de charger les établissements");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const resetForm = () => {
    setForm({
      nom: "",
      adresse: "",
      ville: "",
      codePostal: "",
      telephone: "",
      actif: true,
    });
    setEditing(null);
    setShowForm(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editing) {
        await api.put(`/etablissements/${editing.id}`, form);
        toast.success("Établissement modifié");
      } else {
        await api.post("/etablissements", form);
        toast.success("Établissement créé");
      }
      resetForm();
      load();
    } catch (error) {
      toast.error(error.response?.data?.message || "Erreur de sauvegarde");
    }
  };

  const handleEdit = (e) => {
    setEditing(e);
    setForm({
      nom: e.nom || "",
      adresse: e.adresse || "",
      ville: e.ville || "",
      codePostal: e.codePostal || "",
      telephone: e.telephone || "",
      actif: e.actif,
    });
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Supprimer cet établissement ?")) return;
    try {
      await api.delete(`/etablissements/${id}`);
      toast.success("Établissement supprimé");
      load();
    } catch (error) {
      toast.error(error.response?.data?.message || "Suppression impossible");
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold flex items-center gap-2">
          <Building2 className="h-5 w-5" /> Établissements
        </h2>
        <button
          type="button"
          onClick={() => {
            resetForm();
            setShowForm(true);
          }}
          className="inline-flex items-center gap-1 bg-primary-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-primary-700"
        >
          <Plus className="h-4 w-4" /> Ajouter
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-gray-50 border rounded-lg p-4 space-y-3">
          <input
            required
            placeholder="Nom"
            value={form.nom}
            onChange={(ev) => setForm({ ...form, nom: ev.target.value })}
            className="w-full border rounded px-3 py-2"
          />
          <input
            placeholder="Adresse"
            value={form.adresse}
            onChange={(ev) => setForm({ ...form, adresse: ev.target.value })}
            className="w-full border rounded px-3 py-2"
          />
          <div className="grid grid-cols-2 gap-3">
            <input
              placeholder="Code postal"
              value={form.codePostal}
              onChange={(ev) => setForm({ ...form, codePostal: ev.target.value })}
              className="border rounded px-3 py-2"
            />
            <input
              placeholder="Ville"
              value={form.ville}
              onChange={(ev) => setForm({ ...form, ville: ev.target.value })}
              className="border rounded px-3 py-2"
            />
          </div>
          <input
            placeholder="Téléphone"
            value={form.telephone}
            onChange={(ev) => setForm({ ...form, telephone: ev.target.value })}
            className="w-full border rounded px-3 py-2"
          />
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={form.actif}
              onChange={(ev) => setForm({ ...form, actif: ev.target.checked })}
            />
            Actif (visible publiquement)
          </label>
          <div className="flex gap-2">
            <button type="submit" className="bg-primary-600 text-white px-4 py-2 rounded-lg text-sm">
              {editing ? "Enregistrer" : "Créer"}
            </button>
            <button type="button" onClick={resetForm} className="text-gray-600 px-4 py-2 text-sm">
              Annuler
            </button>
          </div>
        </form>
      )}

      <div className="overflow-x-auto">
        <table className="min-w-full bg-white border rounded-lg">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-2 text-left text-sm font-medium">Nom</th>
              <th className="px-4 py-2 text-left text-sm font-medium">Ville</th>
              <th className="px-4 py-2 text-left text-sm font-medium">Actif</th>
              <th className="px-4 py-2 text-right text-sm font-medium">Actions</th>
            </tr>
          </thead>
          <tbody>
            {etablissements.map((e) => (
              <tr key={e.id} className="border-t">
                <td className="px-4 py-2">{e.nom}</td>
                <td className="px-4 py-2">{e.ville || "—"}</td>
                <td className="px-4 py-2">{e.actif ? "Oui" : "Non"}</td>
                <td className="px-4 py-2 text-right space-x-2">
                  <button type="button" onClick={() => handleEdit(e)} className="text-primary-600">
                    <Edit className="h-4 w-4 inline" />
                  </button>
                  <button type="button" onClick={() => handleDelete(e.id)} className="text-red-600">
                    <Trash2 className="h-4 w-4 inline" />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminEtablissements;
