import React, { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import axios from "axios";
import { User, Mail, Calendar, Clock, ArrowLeft } from "lucide-react";
import toast from "react-hot-toast";

const PrestataireCreneaux = () => {
  const { id } = useParams();
  const [prestataire, setPrestataire] = useState(null);
  const [creneaux, setCreneaux] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPrestataireAndCreneaux();
  }, [id]);

  const fetchPrestataireAndCreneaux = async () => {
    try {
      // Récupérer les informations du prestataire
      const prestataireResponse = await axios.get(`/prestataires`);
      const prestataireData = prestataireResponse.data.find((p) => p.id == id);
      setPrestataire(prestataireData);

      // Récupérer les créneaux disponibles
      const creneauxResponse = await axios.get(
        `/creneaux/prestataire/${id}/disponibles`
      );
      setCreneaux(creneauxResponse.data);
    } catch (error) {
      toast.error("Erreur lors du chargement des données");
      console.error("Erreur:", error);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("fr-FR", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const formatTime = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString("fr-FR", {
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

  if (!prestataire) {
    return (
      <div className="text-center py-12">
        <h3 className="text-lg font-medium text-gray-900">
          Prestataire non trouvé
        </h3>
        <Link
          to="/prestataires"
          className="text-primary-600 hover:text-primary-700"
        >
          Retour aux prestataires
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="mb-8">
        <Link
          to="/prestataires"
          className="inline-flex items-center text-primary-600 hover:text-primary-700 mb-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Retour aux prestataires
        </Link>

        <div className="bg-white rounded-lg shadow-md p-6 mb-8">
          <div className="flex items-center mb-4">
            <div className="flex-shrink-0">
              <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center">
                <User className="h-8 w-8 text-primary-600" />
              </div>
            </div>
            <div className="ml-4">
              <h1 className="text-2xl font-bold text-gray-900">
                {prestataire.nom}
              </h1>
              <p className="text-lg text-gray-600">{prestataire.specialite}</p>
            </div>
          </div>

          <div className="flex items-center text-sm text-gray-600">
            <Mail className="h-4 w-4 mr-2" />
            {prestataire.email}
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-6 flex items-center">
          <Clock className="h-5 w-5 mr-2" />
          Créneaux disponibles
        </h2>

        {creneaux.length === 0 ? (
          <div className="text-center py-8">
            <Calendar className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">
              Aucun créneau disponible
            </h3>
            <p className="mt-1 text-sm text-gray-500">
              Ce prestataire n'a pas de créneaux disponibles pour le moment.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {creneaux.map((creneau) => (
              <div
                key={creneau.id}
                className="border border-gray-200 rounded-lg p-4 hover:border-primary-300 transition-colors"
              >
                <div className="text-sm font-medium text-gray-900">
                  {formatDate(creneau.dateHeure)}
                </div>
                <div className="text-lg font-semibold text-primary-600">
                  {formatTime(creneau.dateHeure)}
                </div>
                <div className="mt-2">
                  <Link
                    to={`/reservation?prestataire=${prestataire.id}&creneau=${creneau.id}`}
                    className="inline-flex items-center px-3 py-1 bg-primary-600 text-white text-sm rounded-md hover:bg-primary-700 transition-colors"
                  >
                    <Calendar className="h-3 w-3 mr-1" />
                    Réserver
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default PrestataireCreneaux;
