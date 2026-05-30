import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import api from "../api/client";
import { Building2, MapPin, Phone, Calendar, Users } from "lucide-react";
import toast from "react-hot-toast";

const Etablissements = () => {
  const [etablissements, setEtablissements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const { data } = await api.get("/etablissements");
        setEtablissements(data);
      } catch {
        toast.error("Erreur lors du chargement des établissements");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[40vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center mb-12">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">Nos établissements</h1>
        <p className="text-xl text-gray-600 max-w-2xl mx-auto">
          Choisissez un cabinet ou un site pour consulter les prestataires et réserver un rendez-vous.
        </p>
      </div>

      {etablissements.length === 0 ? (
        <p className="text-center text-gray-500">Aucun établissement disponible.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {etablissements.map((e) => (
            <div
              key={e.id}
              className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow overflow-hidden"
            >
              <div className="p-6">
                <div className="flex items-start gap-3 mb-4">
                  <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center shrink-0">
                    <Building2 className="h-6 w-6 text-primary-600" />
                  </div>
                  <div>
                    <h2 className="text-lg font-semibold text-gray-900">{e.nom}</h2>
                    {e.ville && (
                      <p className="text-sm text-gray-600 flex items-center gap-1 mt-1">
                        <MapPin className="h-4 w-4" />
                        {e.codePostal} {e.ville}
                      </p>
                    )}
                  </div>
                </div>
                {e.adresse && (
                  <p className="text-sm text-gray-600 mb-2">{e.adresse}</p>
                )}
                {e.telephone && (
                  <p className="text-sm text-gray-600 flex items-center gap-1 mb-6">
                    <Phone className="h-4 w-4" />
                    {e.telephone}
                  </p>
                )}
                <div className="flex flex-col sm:flex-row gap-2">
                  <Link
                    to={`/prestataires?etablissement=${e.id}`}
                    className="flex-1 inline-flex items-center justify-center gap-1 bg-gray-100 text-gray-800 px-4 py-2 rounded-md text-sm font-medium hover:bg-gray-200"
                  >
                    <Users className="h-4 w-4" />
                    Prestataires
                  </Link>
                  <Link
                    to={`/reservation?etablissement=${e.id}`}
                    className="flex-1 inline-flex items-center justify-center gap-1 bg-primary-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-primary-700"
                  >
                    <Calendar className="h-4 w-4" />
                    Réserver
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Etablissements;
