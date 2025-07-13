import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Calendar,
  Clock,
  User,
  X,
  CheckCircle,
  AlertCircle,
} from "lucide-react";
import toast from "react-hot-toast";

const MesRendezVous = () => {
  const [rendezVous, setRendezVous] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRendezVous();
  }, []);

  const fetchRendezVous = async () => {
    try {
      const response = await axios.get("/rendez-vous/mes-rendez-vous");
      setRendezVous(response.data);
    } catch (error) {
      toast.error("Erreur lors du chargement des rendez-vous");
    } finally {
      setLoading(false);
    }
  };

  const handleAnnuler = async (id) => {
    if (!window.confirm("Êtes-vous sûr de vouloir annuler ce rendez-vous ?")) {
      return;
    }

    try {
      await axios.put(`/rendez-vous/${id}/annuler`);
      toast.success("Rendez-vous annulé avec succès");
      fetchRendezVous();
    } catch (error) {
      toast.error("Erreur lors de l'annulation");
    }
  };

  const getStatusBadge = (statut) => {
    switch (statut) {
      case "EN_ATTENTE":
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            <AlertCircle className="h-3 w-3 mr-1" />
            En attente
          </span>
        );
      case "CONFIRME":
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
            <CheckCircle className="h-3 w-3 mr-1" />
            Confirmé
          </span>
        );
      case "ANNULE":
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
            <X className="h-3 w-3 mr-1" />
            Annulé
          </span>
        );
      default:
        return null;
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("fr-FR", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const formatTime = (dateString) => {
    return new Date(dateString).toLocaleTimeString("fr-FR", {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-4">
          Mes Rendez-vous
        </h1>
        <p className="text-lg text-gray-600">
          Gérez vos rendez-vous et suivez leur statut
        </p>
      </div>

      {rendezVous.length === 0 ? (
        <div className="text-center py-12">
          <Calendar className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            Aucun rendez-vous
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            Vous n'avez pas encore de rendez-vous.
          </p>
          <div className="mt-6">
            <a
              href="/reservation"
              className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
            >
              <Calendar className="h-4 w-4 mr-2" />
              Prendre un rendez-vous
            </a>
          </div>
        </div>
      ) : (
        <div className="space-y-6">
          {rendezVous.map((rdv) => (
            <div
              key={rdv.id}
              className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center mb-2">
                    <User className="h-5 w-5 text-gray-400 mr-2" />
                    <h3 className="text-lg font-semibold text-gray-900">
                      {rdv.prestataire?.nom}
                    </h3>
                    <span className="ml-2 text-sm text-gray-500">
                      ({rdv.prestataire?.specialite})
                    </span>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div className="flex items-center text-sm text-gray-600">
                      <Calendar className="h-4 w-4 mr-2" />
                      {formatDate(rdv.dateHeure)}
                    </div>
                    <div className="flex items-center text-sm text-gray-600">
                      <Clock className="h-4 w-4 mr-2" />
                      {formatTime(rdv.dateHeure)}
                    </div>
                  </div>

                  <div className="mb-4">
                    <h4 className="text-sm font-medium text-gray-700 mb-1">
                      Service :
                    </h4>
                    <p className="text-sm text-gray-600 bg-gray-50 rounded p-2">
                      {rdv.service}
                    </p>
                  </div>

                  <div className="flex items-center justify-between">
                    {getStatusBadge(rdv.statut)}

                    {rdv.statut === "EN_ATTENTE" && (
                      <button
                        onClick={() => handleAnnuler(rdv.id)}
                        className="inline-flex items-center px-3 py-1.5 border border-red-300 text-sm font-medium rounded-md text-red-700 bg-white hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                      >
                        <X className="h-4 w-4 mr-1" />
                        Annuler
                      </button>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MesRendezVous;
