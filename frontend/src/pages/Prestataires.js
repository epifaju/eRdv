import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import axios from "axios";
import { User, Mail, Calendar, Clock } from "lucide-react";
import toast from "react-hot-toast";

const Prestataires = () => {
  const [prestataires, setPrestataires] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPrestataires();
  }, []);

  const fetchPrestataires = async () => {
    try {
      const response = await axios.get("/prestataires");
      setPrestataires(response.data);
    } catch (error) {
      toast.error("Erreur lors du chargement des prestataires");
      console.error("Erreur:", error);
    } finally {
      setLoading(false);
    }
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
      <div className="text-center mb-12">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">
          Nos Prestataires
        </h1>
        <p className="text-xl text-gray-600 max-w-2xl mx-auto">
          Découvrez notre équipe de professionnels qualifiés prêts à vous
          accompagner.
        </p>
      </div>

      {prestataires.length === 0 ? (
        <div className="text-center py-12">
          <User className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            Aucun prestataire
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            Aucun prestataire n'est disponible pour le moment.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {prestataires.map((prestataire) => (
            <div
              key={prestataire.id}
              className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-300 overflow-hidden"
            >
              <div className="p-6">
                <div className="flex items-center mb-4">
                  <div className="flex-shrink-0">
                    <div className="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
                      <User className="h-6 w-6 text-primary-600" />
                    </div>
                  </div>
                  <div className="ml-4">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {prestataire.nom}
                    </h3>
                    <p className="text-sm text-gray-600">
                      {prestataire.specialite}
                    </p>
                  </div>
                </div>

                <div className="space-y-2 mb-6">
                  <div className="flex items-center text-sm text-gray-600">
                    <Mail className="h-4 w-4 mr-2" />
                    {prestataire.email}
                  </div>
                </div>

                <div className="flex space-x-2">
                  <Link
                    to={`/reservation?prestataire=${prestataire.id}`}
                    className="flex-1 bg-primary-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-primary-700 transition-colors text-center"
                  >
                    <Calendar className="h-4 w-4 inline mr-1" />
                    Prendre RDV
                  </Link>
                  <Link
                    to={`/prestataires/${prestataire.id}`}
                    className="flex-1 bg-gray-100 text-gray-700 px-4 py-2 rounded-md text-sm font-medium hover:bg-gray-200 transition-colors text-center"
                  >
                    <Clock className="h-4 w-4 inline mr-1" />
                    Voir créneaux
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="mt-12 text-center">
        <Link
          to="/reservation"
          className="inline-flex items-center px-6 py-3 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 transition-colors"
        >
          <Calendar className="h-5 w-5 mr-2" />
          Prendre un Rendez-vous
        </Link>
      </div>
    </div>
  );
};

export default Prestataires;
