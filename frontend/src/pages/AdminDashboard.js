import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Users,
  Calendar,
  Settings,
  Plus,
  Edit,
  Trash2,
  CheckCircle,
  X,
  Clock,
} from "lucide-react";
import toast from "react-hot-toast";

const AdminDashboard = () => {
  const [activeTab, setActiveTab] = useState("rendez-vous");
  const [rendezVous, setRendezVous] = useState([]);
  const [prestataires, setPrestataires] = useState([]);
  const [creneaux, setCreneaux] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showPrestataireForm, setShowPrestataireForm] = useState(false);
  const [showCreneauForm, setShowCreneauForm] = useState(false);
  const [editingPrestataire, setEditingPrestataire] = useState(null);
  const [editingCreneau, setEditingCreneau] = useState(null);
  const [formData, setFormData] = useState({
    nom: "",
    specialite: "",
    email: "",
  });
  const [creneauFormData, setCreneauFormData] = useState({
    prestataireId: "",
    dateHeure: "",
    disponible: true,
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [rdvResponse, prestatairesResponse, creneauxResponse] =
        await Promise.all([
          axios.get("/rendez-vous"),
          axios.get("/prestataires"),
          axios.get("/creneaux"),
        ]);
      setRendezVous(rdvResponse.data);
      setPrestataires(prestatairesResponse.data);
      setCreneaux(creneauxResponse.data);
    } catch (error) {
      toast.error("Erreur lors du chargement des données");
    } finally {
      setLoading(false);
    }
  };

  const handlePrestataireSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingPrestataire) {
        await axios.put(`/prestataires/${editingPrestataire.id}`, formData);
        toast.success("Prestataire modifié avec succès");
      } else {
        await axios.post("/prestataires", formData);
        toast.success("Prestataire créé avec succès");
      }
      setShowPrestataireForm(false);
      setEditingPrestataire(null);
      setFormData({ nom: "", specialite: "", email: "" });
      fetchData();
    } catch (error) {
      toast.error("Erreur lors de la sauvegarde");
    }
  };

  const handleEditPrestataire = (prestataire) => {
    setEditingPrestataire(prestataire);
    setFormData({
      nom: prestataire.nom,
      specialite: prestataire.specialite,
      email: prestataire.email,
    });
    setShowPrestataireForm(true);
  };

  const handleDeletePrestataire = async (id) => {
    if (
      !window.confirm("Êtes-vous sûr de vouloir supprimer ce prestataire ?")
    ) {
      return;
    }
    try {
      await axios.delete(`/prestataires/${id}`);
      toast.success("Prestataire supprimé avec succès");
      fetchData();
    } catch (error) {
      toast.error("Erreur lors de la suppression");
    }
  };

  const handleCreneauSubmit = async (e) => {
    e.preventDefault();
    try {
      const creneauData = {
        prestataire: { id: parseInt(creneauFormData.prestataireId) },
        dateHeure: creneauFormData.dateHeure,
        disponible: creneauFormData.disponible,
      };

      if (editingCreneau) {
        await axios.put(`/creneaux/${editingCreneau.id}`, creneauData);
        toast.success("Créneau modifié avec succès");
      } else {
        await axios.post("/creneaux", creneauData);
        toast.success("Créneau créé avec succès");
      }
      setShowCreneauForm(false);
      setEditingCreneau(null);
      setCreneauFormData({
        prestataireId: "",
        dateHeure: "",
        disponible: true,
      });
      fetchData();
    } catch (error) {
      toast.error("Erreur lors de la sauvegarde du créneau");
    }
  };

  const handleEditCreneau = (creneau) => {
    setEditingCreneau(creneau);
    setCreneauFormData({
      prestataireId: creneau.prestataire.id.toString(),
      dateHeure: creneau.dateHeure.slice(0, 16), // Format pour input datetime-local
      disponible: creneau.disponible,
    });
    setShowCreneauForm(true);
  };

  const handleDeleteCreneau = async (id) => {
    if (!window.confirm("Êtes-vous sûr de vouloir supprimer ce créneau ?")) {
      return;
    }
    try {
      await axios.delete(`/creneaux/${id}`);
      toast.success("Créneau supprimé avec succès");
      fetchData();
    } catch (error) {
      toast.error("Erreur lors de la suppression");
    }
  };

  const handleConfirmerRendezVous = async (id) => {
    try {
      await axios.put(`/rendez-vous/${id}/confirmer`);
      toast.success("Rendez-vous confirmé");
      fetchData();
    } catch (error) {
      toast.error("Erreur lors de la confirmation");
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

  const getStatusBadge = (statut) => {
    switch (statut) {
      case "EN_ATTENTE":
        return (
          <span className="px-2 py-1 text-xs font-medium bg-yellow-100 text-yellow-800 rounded-full">
            En attente
          </span>
        );
      case "CONFIRME":
        return (
          <span className="px-2 py-1 text-xs font-medium bg-green-100 text-green-800 rounded-full">
            Confirmé
          </span>
        );
      case "ANNULE":
        return (
          <span className="px-2 py-1 text-xs font-medium bg-red-100 text-red-800 rounded-full">
            Annulé
          </span>
        );
      default:
        return null;
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
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          Dashboard Administrateur
        </h1>
        <p className="text-gray-600">
          Gérez les prestataires et les rendez-vous
        </p>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200 mb-8">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab("rendez-vous")}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === "rendez-vous"
                ? "border-primary-500 text-primary-600"
                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
            }`}
          >
            <Calendar className="h-4 w-4 inline mr-2" />
            Rendez-vous
          </button>
          <button
            onClick={() => setActiveTab("prestataires")}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === "prestataires"
                ? "border-primary-500 text-primary-600"
                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
            }`}
          >
            <Users className="h-4 w-4 inline mr-2" />
            Prestataires
          </button>
          <button
            onClick={() => setActiveTab("creneaux")}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === "creneaux"
                ? "border-primary-500 text-primary-600"
                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
            }`}
          >
            <Clock className="h-4 w-4 inline mr-2" />
            Créneaux
          </button>
        </nav>
      </div>

      {/* Rendez-vous Tab */}
      {activeTab === "rendez-vous" && (
        <div className="bg-white shadow-md rounded-lg overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">
              Tous les Rendez-vous
            </h2>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Client
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Prestataire
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date & Heure
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Service
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Statut
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {rendezVous.map((rdv) => (
                  <tr key={rdv.id}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {rdv.utilisateur?.nom}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {rdv.prestataire?.nom}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      <div>{formatDate(rdv.dateHeure)}</div>
                      <div className="text-gray-500">
                        {formatTime(rdv.dateHeure)}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900 max-w-xs truncate">
                      {rdv.service}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(rdv.statut)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      {rdv.statut === "EN_ATTENTE" && (
                        <button
                          onClick={() => handleConfirmerRendezVous(rdv.id)}
                          className="text-green-600 hover:text-green-900 mr-2"
                        >
                          <CheckCircle className="h-4 w-4" />
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Prestataires Tab */}
      {activeTab === "prestataires" && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-lg font-semibold text-gray-900">
              Gestion des Prestataires
            </h2>
            <button
              onClick={() => {
                setEditingPrestataire(null);
                setFormData({ nom: "", specialite: "", email: "" });
                setShowPrestataireForm(true);
              }}
              className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
            >
              <Plus className="h-4 w-4 mr-2" />
              Ajouter un prestataire
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {prestataires.map((prestataire) => (
              <div
                key={prestataire.id}
                className="bg-white rounded-lg shadow-md p-6"
              >
                <div className="flex items-center mb-4">
                  <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                    <Users className="h-5 w-5 text-primary-600" />
                  </div>
                  <div className="ml-3">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {prestataire.nom}
                    </h3>
                    <p className="text-sm text-gray-600">
                      {prestataire.specialite}
                    </p>
                  </div>
                </div>
                <p className="text-sm text-gray-600 mb-4">
                  {prestataire.email}
                </p>
                <div className="flex space-x-2">
                  <button
                    onClick={() => handleEditPrestataire(prestataire)}
                    className="flex-1 inline-flex items-center justify-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                  >
                    <Edit className="h-4 w-4 mr-1" />
                    Modifier
                  </button>
                  <button
                    onClick={() => handleDeletePrestataire(prestataire.id)}
                    className="flex-1 inline-flex items-center justify-center px-3 py-2 border border-red-300 shadow-sm text-sm font-medium rounded-md text-red-700 bg-white hover:bg-red-50"
                  >
                    <Trash2 className="h-4 w-4 mr-1" />
                    Supprimer
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Créneaux Tab */}
      {activeTab === "creneaux" && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-lg font-semibold text-gray-900">
              Gestion des Créneaux Horaires
            </h2>
            <button
              onClick={() => {
                setEditingCreneau(null);
                setCreneauFormData({
                  prestataireId: "",
                  dateHeure: "",
                  disponible: true,
                });
                setShowCreneauForm(true);
              }}
              className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
            >
              <Plus className="h-4 w-4 mr-2" />
              Ajouter un créneau
            </button>
          </div>

          <div className="bg-white shadow-md rounded-lg overflow-hidden">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Prestataire
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Date & Heure
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Disponible
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {creneaux.map((creneau) => (
                    <tr key={creneau.id}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {creneau.prestataire?.nom}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        <div>{formatDate(creneau.dateHeure)}</div>
                        <div className="text-gray-500">
                          {formatTime(creneau.dateHeure)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {creneau.disponible ? (
                          <span className="px-2 py-1 text-xs font-medium bg-green-100 text-green-800 rounded-full">
                            Disponible
                          </span>
                        ) : (
                          <span className="px-2 py-1 text-xs font-medium bg-red-100 text-red-800 rounded-full">
                            Indisponible
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <button
                          onClick={() => handleEditCreneau(creneau)}
                          className="text-indigo-600 hover:text-indigo-900 mr-2"
                        >
                          <Edit className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteCreneau(creneau.id)}
                          className="text-red-600 hover:text-red-900"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Prestataire Form Modal */}
      {showPrestataireForm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {editingPrestataire
                  ? "Modifier le prestataire"
                  : "Ajouter un prestataire"}
              </h3>
              <form onSubmit={handlePrestataireSubmit}>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Nom
                    </label>
                    <input
                      type="text"
                      value={formData.nom}
                      onChange={(e) =>
                        setFormData({ ...formData, nom: e.target.value })
                      }
                      className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Spécialité
                    </label>
                    <input
                      type="text"
                      value={formData.specialite}
                      onChange={(e) =>
                        setFormData({ ...formData, specialite: e.target.value })
                      }
                      className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Email
                    </label>
                    <input
                      type="email"
                      value={formData.email}
                      onChange={(e) =>
                        setFormData({ ...formData, email: e.target.value })
                      }
                      className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                      required
                    />
                  </div>
                </div>
                <div className="flex space-x-3 mt-6">
                  <button
                    type="submit"
                    className="flex-1 bg-primary-600 text-white px-4 py-2 rounded-md hover:bg-primary-700"
                  >
                    {editingPrestataire ? "Modifier" : "Ajouter"}
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowPrestataireForm(false)}
                    className="flex-1 bg-gray-300 text-gray-700 px-4 py-2 rounded-md hover:bg-gray-400"
                  >
                    Annuler
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Créneau Form Modal */}
      {showCreneauForm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {editingCreneau ? "Modifier le créneau" : "Ajouter un créneau"}
              </h3>
              <form onSubmit={handleCreneauSubmit}>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Prestataire
                    </label>
                    <select
                      value={creneauFormData.prestataireId}
                      onChange={(e) =>
                        setCreneauFormData({
                          ...creneauFormData,
                          prestataireId: e.target.value,
                        })
                      }
                      className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                      required
                    >
                      <option value="">Sélectionner un prestataire</option>
                      {prestataires.map((prestataire) => (
                        <option key={prestataire.id} value={prestataire.id}>
                          {prestataire.nom} - {prestataire.specialite}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700">
                      Date & Heure
                    </label>
                    <input
                      type="datetime-local"
                      value={creneauFormData.dateHeure}
                      onChange={(e) =>
                        setCreneauFormData({
                          ...creneauFormData,
                          dateHeure: e.target.value,
                        })
                      }
                      className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                      required
                    />
                  </div>
                  <div>
                    <label className="flex items-center">
                      <input
                        type="checkbox"
                        checked={creneauFormData.disponible}
                        onChange={(e) =>
                          setCreneauFormData({
                            ...creneauFormData,
                            disponible: e.target.checked,
                          })
                        }
                        className="rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-300 focus:ring focus:ring-primary-200 focus:ring-opacity-50"
                      />
                      <span className="ml-2 text-sm text-gray-700">
                        Disponible
                      </span>
                    </label>
                  </div>
                </div>
                <div className="flex space-x-3 mt-6">
                  <button
                    type="submit"
                    className="flex-1 bg-primary-600 text-white px-4 py-2 rounded-md hover:bg-primary-700"
                  >
                    {editingCreneau ? "Modifier" : "Ajouter"}
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowCreneauForm(false)}
                    className="flex-1 bg-gray-300 text-gray-700 px-4 py-2 rounded-md hover:bg-gray-400"
                  >
                    Annuler
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
