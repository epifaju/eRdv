import React, { useState, useEffect } from "react";
import api from "../api/client";
import { RDV_MON_AGENDA } from "../api/paths";
import {
  Calendar,
  Clock,
  User,
  CheckCircle,
  X,
  AlertCircle,
  AlertTriangle,
} from "lucide-react";
import toast from "react-hot-toast";

const PrestataireAgenda = () => {
  const [rendezVous, setRendezVous] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("EN_ATTENTE");
  const [actionId, setActionId] = useState(null);

  useEffect(() => {
    fetchAgenda();
  }, []);

  const fetchAgenda = async () => {
    setLoading(true);
    try {
      const { data } = await api.get(RDV_MON_AGENDA);
      setRendezVous(data);
    } catch {
      toast.error("Erreur lors du chargement de l'agenda");
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmer = async (id) => {
    setActionId(id);
    try {
      await api.put(`/rendez-vous/${id}/confirmer`);
      toast.success("Rendez-vous confirmé");
      fetchAgenda();
    } catch (error) {
      toast.error(error.response?.data?.message || "Erreur lors de la confirmation");
    } finally {
      setActionId(null);
    }
  };

  const handleAnnuler = async (id) => {
    if (!window.confirm("Annuler ce rendez-vous ? Le client sera notifié.")) return;
    setActionId(id);
    try {
      await api.put(`/rendez-vous/${id}/annuler`);
      toast.success("Rendez-vous annulé");
      fetchAgenda();
    } catch {
      toast.error("Erreur lors de l'annulation");
    } finally {
      setActionId(null);
    }
  };

  const filtered = rendezVous.filter((rdv) =>
    filter === "TOUS" ? true : rdv.statut === filter
  );

  const formatDate = (dateString) =>
    new Date(dateString).toLocaleDateString("fr-FR", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });

  const formatTime = (dateString) =>
    new Date(dateString).toLocaleTimeString("fr-FR", {
      hour: "2-digit",
      minute: "2-digit",
    });

  const formatPlage = (rdv) => {
    const debut = formatTime(rdv.dateHeure);
    if (rdv.dateHeureFin) {
      return `${debut} – ${formatTime(rdv.dateHeureFin)}`;
    }
    if (rdv.dureeTotaleMinutes > 30) {
      const fin = new Date(
        new Date(rdv.dateHeure).getTime() + rdv.dureeTotaleMinutes * 60000
      );
      return `${debut} – ${fin.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" })}`;
    }
    return debut;
  };

  const statusBadge = (statut) => {
    switch (statut) {
      case "EN_ATTENTE":
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            <AlertCircle className="h-3 w-3 mr-1" /> En attente
          </span>
        );
      case "CONFIRME":
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            <CheckCircle className="h-3 w-3 mr-1" /> Confirmé
          </span>
        );
      case "ANNULE":
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            <X className="h-3 w-3 mr-1" /> Annulé
          </span>
        );
      default:
        return null;
    }
  };

  if (loading) {
    return <p className="text-center text-gray-500 py-8">Chargement de l&apos;agenda…</p>;
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-2">
        {[
          { key: "EN_ATTENTE", label: "En attente" },
          { key: "CONFIRME", label: "Confirmés" },
          { key: "ANNULE", label: "Annulés" },
          { key: "TOUS", label: "Tous" },
        ].map((f) => (
          <button
            key={f.key}
            type="button"
            onClick={() => setFilter(f.key)}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium ${
              filter === f.key
                ? "bg-primary-600 text-white"
                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
            }`}
          >
            {f.label}
            {f.key !== "TOUS" && (
              <span className="ml-1 opacity-80">
                ({rendezVous.filter((r) => r.statut === f.key).length})
              </span>
            )}
          </button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-10 bg-white rounded-lg shadow-md">
          <Calendar className="mx-auto h-10 w-10 text-gray-400 mb-2" />
          <p className="text-gray-600">Aucun rendez-vous pour ce filtre.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map((rdv) => (
            <div
              key={rdv.id}
              className="bg-white rounded-lg shadow-md p-4 border-l-4 border-primary-500"
            >
              <div className="flex flex-wrap justify-between gap-2 mb-2">
                {statusBadge(rdv.statut)}
                {rdv.dureeTotaleMinutes > 30 && (
                  <span className="text-xs text-gray-500">
                    {rdv.dureeTotaleMinutes} min
                    {rdv.nbCreneaux > 1 ? ` · ${rdv.nbCreneaux} créneaux` : ""}
                  </span>
                )}
              </div>

              <div className="flex items-center text-gray-900 font-medium mb-1">
                <User className="h-4 w-4 mr-2 text-gray-400" />
                {rdv.utilisateur?.nom}
                <span className="text-sm text-gray-500 ml-2">
                  {rdv.utilisateur?.email}
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-1 text-sm text-gray-600 mb-2">
                <span className="flex items-center">
                  <Calendar className="h-4 w-4 mr-1" />
                  {formatDate(rdv.dateHeure)}
                </span>
                <span className="flex items-center">
                  <Clock className="h-4 w-4 mr-1" />
                  {formatPlage(rdv)}
                </span>
              </div>

              <p className="text-sm bg-gray-50 rounded p-2 mb-3">
                <strong>Prestation :</strong> {rdv.service}
              </p>

              {rdv.statut !== "ANNULE" &&
                new Date(rdv.dateHeure) > new Date() && (
                  <div className="flex flex-wrap gap-2">
                    {rdv.statut === "EN_ATTENTE" && (
                      <button
                        type="button"
                        disabled={actionId === rdv.id}
                        onClick={() => handleConfirmer(rdv.id)}
                        className="inline-flex items-center px-3 py-1.5 text-sm bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
                      >
                        <CheckCircle className="h-4 w-4 mr-1" />
                        Confirmer
                      </button>
                    )}
                    <button
                      type="button"
                      disabled={actionId === rdv.id}
                      onClick={() => handleAnnuler(rdv.id)}
                      className="inline-flex items-center px-3 py-1.5 text-sm border border-red-300 text-red-700 rounded-md hover:bg-red-50 disabled:opacity-50"
                    >
                      <AlertTriangle className="h-4 w-4 mr-1" />
                      Annuler
                    </button>
                  </div>
                )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default PrestataireAgenda;
