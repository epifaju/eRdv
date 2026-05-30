import React, { useState, useEffect } from "react";
import api from "../api/client";
import { Plus, Trash2, Edit, RefreshCw, Clock, Briefcase } from "lucide-react";
import toast from "react-hot-toast";

const JOURS = [
  { value: 1, label: "Lundi" },
  { value: 2, label: "Mardi" },
  { value: 3, label: "Mercredi" },
  { value: 4, label: "Jeudi" },
  { value: 5, label: "Vendredi" },
  { value: 6, label: "Samedi" },
  { value: 7, label: "Dimanche" },
];

const AdminCatalogue = ({ prestataires, lockedPrestataireId, hidePrestataireSelector = false }) => {
  const [prestataireId, setPrestataireId] = useState(
    lockedPrestataireId ? String(lockedPrestataireId) : ""
  );
  const [prestations, setPrestations] = useState([]);
  const [plages, setPlages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [showPrestationForm, setShowPrestationForm] = useState(false);
  const [showPlageForm, setShowPlageForm] = useState(false);
  const [editingPrestation, setEditingPrestation] = useState(null);
  const [editingPlage, setEditingPlage] = useState(null);
  const [prestationForm, setPrestationForm] = useState({
    nom: "",
    description: "",
    dureeMinutes: 30,
    prix: "",
    actif: true,
  });
  const [plageForm, setPlageForm] = useState({
    jourSemaine: 1,
    heureDebut: "09:00",
    heureFin: "12:00",
    actif: true,
  });

  useEffect(() => {
    if (lockedPrestataireId) {
      setPrestataireId(String(lockedPrestataireId));
    }
  }, [lockedPrestataireId]);

  useEffect(() => {
    if (prestataireId) {
      loadCatalogue(prestataireId);
    } else {
      setPrestations([]);
      setPlages([]);
    }
  }, [prestataireId]);

  const loadCatalogue = async (id) => {
    setLoading(true);
    try {
      const [prestRes, plagesRes] = await Promise.all([
        api.get(`/prestations/prestataire/${id}/toutes`),
        api.get(`/plages-recurrentes/prestataire/${id}`),
      ]);
      setPrestations(prestRes.data);
      setPlages(plagesRes.data);
    } catch {
      toast.error("Erreur chargement catalogue / plages");
    } finally {
      setLoading(false);
    }
  };

  const handleGenererCreneaux = async () => {
    if (!prestataireId) return;
    setGenerating(true);
    try {
      const { data } = await api.post(
        `/plages-recurrentes/prestataire/${prestataireId}/generer-creneaux`,
        null,
        { params: { jours: 28 } }
      );
      toast.success(
        `${data.creneauxCrees} créneaux générés (${data.granulariteMinutes} min / créneau)`
      );
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Impossible de générer les créneaux"
      );
    } finally {
      setGenerating(false);
    }
  };

  const submitPrestation = async (e) => {
    e.preventDefault();
    if (!prestataireId) return;
    const payload = {
      prestataireId: parseInt(prestataireId, 10),
      nom: prestationForm.nom,
      description: prestationForm.description,
      dureeMinutes: parseInt(prestationForm.dureeMinutes, 10),
      prix: prestationForm.prix ? parseFloat(prestationForm.prix) : null,
      actif: prestationForm.actif,
    };
    try {
      if (editingPrestation) {
        await api.put(`/prestations/${editingPrestation.id}`, payload);
        toast.success("Prestation modifiée");
      } else {
        await api.post("/prestations", payload);
        toast.success("Prestation créée");
      }
      setShowPrestationForm(false);
      setEditingPrestation(null);
      loadCatalogue(prestataireId);
    } catch (error) {
      toast.error(error.response?.data?.message || "Erreur sauvegarde");
    }
  };

  const submitPlage = async (e) => {
    e.preventDefault();
    if (!prestataireId) return;
    const payload = {
      prestataireId: parseInt(prestataireId, 10),
      jourSemaine: parseInt(plageForm.jourSemaine, 10),
      heureDebut: plageForm.heureDebut,
      heureFin: plageForm.heureFin,
      actif: plageForm.actif,
    };
    try {
      if (editingPlage) {
        await api.put(`/plages-recurrentes/${editingPlage.id}`, payload);
        toast.success("Plage modifiée");
      } else {
        await api.post("/plages-recurrentes", payload);
        toast.success("Plage créée");
      }
      setShowPlageForm(false);
      setEditingPlage(null);
      loadCatalogue(prestataireId);
    } catch (error) {
      toast.error(error.response?.data?.message || "Erreur sauvegarde");
    }
  };

  return (
    <div className="space-y-8">
      {!hidePrestataireSelector && (
      <div className="bg-white rounded-lg shadow-md p-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Prestataire
        </label>
        <select
          value={prestataireId}
          onChange={(e) => setPrestataireId(e.target.value)}
          className="w-full max-w-md border rounded-lg px-3 py-2"
        >
          <option value="">Choisir un prestataire…</option>
          {prestataires.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nom} — {p.specialite}
            </option>
          ))}
        </select>
        {prestataireId && (
          <button
            type="button"
            onClick={handleGenererCreneaux}
            disabled={generating}
            className="mt-4 inline-flex items-center px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
          >
            <RefreshCw
              className={`h-4 w-4 mr-2 ${generating ? "animate-spin" : ""}`}
            />
            Générer les créneaux (28 jours)
          </button>
        )}
      </div>
      )}

      {hidePrestataireSelector && prestataireId && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <button
            type="button"
            onClick={handleGenererCreneaux}
            disabled={generating}
            className="inline-flex items-center px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
          >
            <RefreshCw
              className={`h-4 w-4 mr-2 ${generating ? "animate-spin" : ""}`}
            />
            Générer les créneaux (28 jours)
          </button>
        </div>
      )}

      {loading && (
        <p className="text-center text-gray-500">Chargement…</p>
      )}

      {prestataireId && !loading && (
        <>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold flex items-center">
                <Briefcase className="h-5 w-5 mr-2" />
                Catalogue de prestations
              </h3>
              <button
                type="button"
                onClick={() => {
                  setEditingPrestation(null);
                  setPrestationForm({
                    nom: "",
                    description: "",
                    dureeMinutes: 30,
                    prix: "",
                    actif: true,
                  });
                  setShowPrestationForm(true);
                }}
                className="inline-flex items-center text-sm px-3 py-1.5 bg-primary-600 text-white rounded-lg"
              >
                <Plus className="h-4 w-4 mr-1" /> Ajouter
              </button>
            </div>
            <div className="space-y-2">
              {prestations.length === 0 && (
                <p className="text-gray-500 text-sm">Aucune prestation.</p>
              )}
              {prestations.map((pr) => (
                <div
                  key={pr.id}
                  className="flex justify-between items-center border rounded-lg p-3"
                >
                  <div>
                    <span className="font-medium">{pr.nom}</span>
                    <span className="text-gray-500 text-sm ml-2">
                      {pr.dureeMinutes} min
                      {pr.prix != null ? ` — ${pr.prix} €` : ""}
                      {!pr.actif && " (inactive)"}
                    </span>
                    {pr.description && (
                      <p className="text-sm text-gray-500">{pr.description}</p>
                    )}
                  </div>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => {
                        setEditingPrestation(pr);
                        setPrestationForm({
                          nom: pr.nom,
                          description: pr.description || "",
                          dureeMinutes: pr.dureeMinutes,
                          prix: pr.prix ?? "",
                          actif: pr.actif,
                        });
                        setShowPrestationForm(true);
                      }}
                    >
                      <Edit className="h-4 w-4 text-gray-500" />
                    </button>
                    <button
                      type="button"
                      onClick={async () => {
                        if (!window.confirm("Supprimer cette prestation ?"))
                          return;
                        await api.delete(`/prestations/${pr.id}`);
                        loadCatalogue(prestataireId);
                      }}
                    >
                      <Trash2 className="h-4 w-4 text-red-500" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold flex items-center">
                <Clock className="h-5 w-5 mr-2" />
                Plages horaires récurrentes
              </h3>
              <button
                type="button"
                onClick={() => {
                  setEditingPlage(null);
                  setPlageForm({
                    jourSemaine: 1,
                    heureDebut: "09:00",
                    heureFin: "12:00",
                    actif: true,
                  });
                  setShowPlageForm(true);
                }}
                className="inline-flex items-center text-sm px-3 py-1.5 bg-primary-600 text-white rounded-lg"
              >
                <Plus className="h-4 w-4 mr-1" /> Ajouter
              </button>
            </div>
            <div className="space-y-2">
              {plages.length === 0 && (
                <p className="text-gray-500 text-sm">
                  Aucune plage. Ex. lun–ven 9h–12h et 14h–18h.
                </p>
              )}
              {plages.map((pl) => (
                <div
                  key={pl.id}
                  className="flex justify-between items-center border rounded-lg p-3"
                >
                  <span>
                    <strong>{pl.jourLibelle || pl.jourSemaine}</strong> :{" "}
                    {String(pl.heureDebut).slice(0, 5)} –{" "}
                    {String(pl.heureFin).slice(0, 5)}
                    {!pl.actif && " (inactive)"}
                  </span>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => {
                        setEditingPlage(pl);
                        setPlageForm({
                          jourSemaine: pl.jourSemaine,
                          heureDebut: String(pl.heureDebut).slice(0, 5),
                          heureFin: String(pl.heureFin).slice(0, 5),
                          actif: pl.actif,
                        });
                        setShowPlageForm(true);
                      }}
                    >
                      <Edit className="h-4 w-4 text-gray-500" />
                    </button>
                    <button
                      type="button"
                      onClick={async () => {
                        if (!window.confirm("Supprimer cette plage ?")) return;
                        await api.delete(`/plages-recurrentes/${pl.id}`);
                        loadCatalogue(prestataireId);
                      }}
                    >
                      <Trash2 className="h-4 w-4 text-red-500" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}

      {showPrestationForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <form
            onSubmit={submitPrestation}
            className="bg-white rounded-lg p-6 w-full max-w-md space-y-3"
          >
            <h4 className="font-semibold">
              {editingPrestation ? "Modifier" : "Nouvelle"} prestation
            </h4>
            <input
              required
              placeholder="Nom (ex. Consultation, Coupe, Révision…)"
              className="w-full border rounded px-3 py-2"
              value={prestationForm.nom}
              onChange={(e) =>
                setPrestationForm({ ...prestationForm, nom: e.target.value })
              }
            />
            <textarea
              placeholder="Description"
              className="w-full border rounded px-3 py-2"
              rows={2}
              value={prestationForm.description}
              onChange={(e) =>
                setPrestationForm({
                  ...prestationForm,
                  description: e.target.value,
                })
              }
            />
            <input
              type="number"
              min={15}
              step={5}
              required
              placeholder="Durée (minutes)"
              className="w-full border rounded px-3 py-2"
              value={prestationForm.dureeMinutes}
              onChange={(e) =>
                setPrestationForm({
                  ...prestationForm,
                  dureeMinutes: e.target.value,
                })
              }
            />
            <input
              type="number"
              min={0}
              step="0.01"
              placeholder="Prix (€, optionnel)"
              className="w-full border rounded px-3 py-2"
              value={prestationForm.prix}
              onChange={(e) =>
                setPrestationForm({ ...prestationForm, prix: e.target.value })
              }
            />
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={prestationForm.actif}
                onChange={(e) =>
                  setPrestationForm({
                    ...prestationForm,
                    actif: e.target.checked,
                  })
                }
              />
              Active
            </label>
            <div className="flex gap-2 pt-2">
              <button
                type="button"
                className="flex-1 border rounded py-2"
                onClick={() => setShowPrestationForm(false)}
              >
                Annuler
              </button>
              <button
                type="submit"
                className="flex-1 bg-primary-600 text-white rounded py-2"
              >
                Enregistrer
              </button>
            </div>
          </form>
        </div>
      )}

      {showPlageForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <form
            onSubmit={submitPlage}
            className="bg-white rounded-lg p-6 w-full max-w-md space-y-3"
          >
            <h4 className="font-semibold">
              {editingPlage ? "Modifier" : "Nouvelle"} plage horaire
            </h4>
            <select
              className="w-full border rounded px-3 py-2"
              value={plageForm.jourSemaine}
              onChange={(e) =>
                setPlageForm({ ...plageForm, jourSemaine: e.target.value })
              }
            >
              {JOURS.map((j) => (
                <option key={j.value} value={j.value}>
                  {j.label}
                </option>
              ))}
            </select>
            <div className="flex gap-2">
              <input
                type="time"
                required
                className="flex-1 border rounded px-3 py-2"
                value={plageForm.heureDebut}
                onChange={(e) =>
                  setPlageForm({ ...plageForm, heureDebut: e.target.value })
                }
              />
              <input
                type="time"
                required
                className="flex-1 border rounded px-3 py-2"
                value={plageForm.heureFin}
                onChange={(e) =>
                  setPlageForm({ ...plageForm, heureFin: e.target.value })
                }
              />
            </div>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={plageForm.actif}
                onChange={(e) =>
                  setPlageForm({ ...plageForm, actif: e.target.checked })
                }
              />
              Active
            </label>
            <div className="flex gap-2 pt-2">
              <button
                type="button"
                className="flex-1 border rounded py-2"
                onClick={() => setShowPlageForm(false)}
              >
                Annuler
              </button>
              <button
                type="submit"
                className="flex-1 bg-primary-600 text-white rounded py-2"
              >
                Enregistrer
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default AdminCatalogue;
