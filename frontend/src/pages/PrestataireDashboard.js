import React, { useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import AdminCatalogue from "../components/AdminCatalogue";
import PrestataireAgenda from "../components/PrestataireAgenda";
import { Briefcase, CalendarDays } from "lucide-react";

const PrestataireDashboard = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState("agenda");

  if (!user?.prestataireId) {
    return (
      <div className="max-w-2xl mx-auto text-center py-12">
        <Briefcase className="mx-auto h-12 w-12 text-gray-400 mb-4" />
        <h2 className="text-xl font-semibold text-gray-900">
          Compte non rattaché
        </h2>
        <p className="text-gray-600 mt-2">
          Votre compte prestataire n&apos;est pas encore lié à une fiche
          établissement. Contactez l&apos;administrateur.
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          Espace prestataire
        </h1>
        <p className="text-lg text-gray-600">
          {user.prestataireNom || user.nom}
        </p>
      </div>

      <div className="flex justify-center border-b border-gray-200 mb-6">
        <button
          type="button"
          onClick={() => setActiveTab("agenda")}
          className={`flex items-center px-4 py-2 border-b-2 text-sm font-medium ${
            activeTab === "agenda"
              ? "border-primary-500 text-primary-600"
              : "border-transparent text-gray-500 hover:text-gray-700"
          }`}
        >
          <CalendarDays className="h-4 w-4 mr-2" />
          Rendez-vous
        </button>
        <button
          type="button"
          onClick={() => setActiveTab("catalogue")}
          className={`flex items-center px-4 py-2 border-b-2 text-sm font-medium ${
            activeTab === "catalogue"
              ? "border-primary-500 text-primary-600"
              : "border-transparent text-gray-500 hover:text-gray-700"
          }`}
        >
          <Briefcase className="h-4 w-4 mr-2" />
          Mon catalogue
        </button>
      </div>

      {activeTab === "agenda" && <PrestataireAgenda />}
      {activeTab === "catalogue" && (
        <AdminCatalogue
          lockedPrestataireId={user.prestataireId}
          hidePrestataireSelector
        />
      )}
    </div>
  );
};

export default PrestataireDashboard;
