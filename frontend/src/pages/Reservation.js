import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { Calendar, Clock, User, CheckCircle } from "lucide-react";
import toast from "react-hot-toast";

const Reservation = () => {
  const [prestataires, setPrestataires] = useState([]);
  const [selectedPrestataire, setSelectedPrestataire] = useState(null);
  const [creneaux, setCreneaux] = useState([]);
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedCreneau, setSelectedCreneau] = useState(null);
  const [service, setService] = useState("");
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState(1);

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  useEffect(() => {
    fetchPrestataires();
    const prestataireId = searchParams.get("prestataire");
    if (prestataireId) {
      setSelectedPrestataire(prestataireId);
    }
  }, [searchParams]);

  useEffect(() => {
    if (selectedPrestataire && selectedDate) {
      fetchCreneaux();
    }
  }, [selectedPrestataire, selectedDate]);

  const fetchPrestataires = async () => {
    try {
      const response = await axios.get("/prestataires");
      setPrestataires(response.data);
    } catch (error) {
      toast.error("Erreur lors du chargement des prestataires");
    }
  };

  const fetchCreneaux = async () => {
    try {
      const response = await axios.get(
        `/creneaux/prestataire/${selectedPrestataire}/disponibles`
      );
      setCreneaux(response.data);
    } catch (error) {
      toast.error("Erreur lors du chargement des créneaux");
    }
  };

  const handlePrestataireSelect = (prestataireId) => {
    setSelectedPrestataire(prestataireId);
    setSelectedDate(null);
    setSelectedCreneau(null);
    setStep(2);
  };

  const handleDateSelect = (date) => {
    setSelectedDate(date);
    setSelectedCreneau(null);
    setStep(3);
  };

  const handleCreneauSelect = (creneau) => {
    setSelectedCreneau(creneau);
    setStep(4);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedPrestataire || !selectedCreneau || !service.trim()) {
      toast.error("Veuillez remplir tous les champs");
      return;
    }

    setLoading(true);
    try {
      const rendezVous = {
        prestataireId: parseInt(selectedPrestataire),
        dateHeure: selectedCreneau.dateHeure,
        service: service,
      };

      await axios.post("/rendez-vous", rendezVous);
      toast.success("Rendez-vous créé avec succès !");
      navigate("/mes-rendez-vous");
    } catch (error) {
      console.error("Erreur axios:", error);
      toast.error("Erreur lors de la création du rendez-vous");
    } finally {
      setLoading(false);
    }
  };

  const getPrestataireById = (id) => {
    return prestataires.find((p) => p.id === parseInt(id));
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

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-4">
          Prendre un Rendez-vous
        </h1>
        <p className="text-lg text-gray-600">
          Suivez les étapes pour réserver votre créneau
        </p>
      </div>

      {/* Progress Steps */}
      <div className="mb-8">
        <div className="flex items-center justify-center">
          {[1, 2, 3, 4].map((stepNumber) => (
            <div key={stepNumber} className="flex items-center">
              <div
                className={`flex items-center justify-center w-8 h-8 rounded-full ${
                  step >= stepNumber
                    ? "bg-primary-600 text-white"
                    : "bg-gray-200 text-gray-600"
                }`}
              >
                {stepNumber}
              </div>
              {stepNumber < 4 && (
                <div
                  className={`w-16 h-1 mx-2 ${
                    step > stepNumber ? "bg-primary-600" : "bg-gray-200"
                  }`}
                ></div>
              )}
            </div>
          ))}
        </div>
        <div className="flex justify-center mt-2 text-sm text-gray-600">
          <span className={step >= 1 ? "text-primary-600" : ""}>
            Prestataire
          </span>
          <span className={`mx-4 ${step >= 2 ? "text-primary-600" : ""}`}>
            Date
          </span>
          <span className={`mx-4 ${step >= 3 ? "text-primary-600" : ""}`}>
            Heure
          </span>
          <span className={`mx-4 ${step >= 4 ? "text-primary-600" : ""}`}>
            Confirmation
          </span>
        </div>
      </div>

      {/* Step 1: Prestataire Selection */}
      {step === 1 && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 flex items-center">
            <User className="h-5 w-5 mr-2" />
            Choisissez un prestataire
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {prestataires.map((prestataire) => (
              <div
                key={prestataire.id}
                onClick={() => handlePrestataireSelect(prestataire.id)}
                className="border rounded-lg p-4 cursor-pointer hover:border-primary-500 hover:bg-primary-50 transition-colors"
              >
                <h3 className="font-semibold">{prestataire.nom}</h3>
                <p className="text-gray-600">{prestataire.specialite}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Step 2: Date Selection */}
      {step === 2 && selectedPrestataire && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 flex items-center">
            <Calendar className="h-5 w-5 mr-2" />
            Choisissez une date
          </h2>
          <div className="mb-4">
            <p className="text-gray-600 mb-2">
              Prestataire sélectionné :{" "}
              <strong>{getPrestataireById(selectedPrestataire)?.nom}</strong>
            </p>
          </div>
          <div className="flex justify-center">
            <DatePicker
              selected={selectedDate}
              onChange={handleDateSelect}
              minDate={new Date()}
              dateFormat="dd/MM/yyyy"
              placeholderText="Sélectionnez une date"
              className="border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <button
            onClick={() => setStep(1)}
            className="mt-4 text-primary-600 hover:text-primary-700"
          >
            ← Retour
          </button>
        </div>
      )}

      {/* Step 3: Time Selection */}
      {step === 3 && selectedDate && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 flex items-center">
            <Clock className="h-5 w-5 mr-2" />
            Choisissez une heure
          </h2>
          <div className="mb-4">
            <p className="text-gray-600">
              Date sélectionnée : <strong>{formatDate(selectedDate)}</strong>
            </p>
          </div>
          {creneaux.length === 0 ? (
            <p className="text-gray-500 text-center py-4">
              Aucun créneau disponible pour cette date
            </p>
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
              {creneaux.map((creneau) => (
                <button
                  key={creneau.id}
                  onClick={() => handleCreneauSelect(creneau)}
                  className="border rounded-lg p-3 hover:border-primary-500 hover:bg-primary-50 transition-colors"
                >
                  {formatTime(creneau.dateHeure)}
                </button>
              ))}
            </div>
          )}
          <button
            onClick={() => setStep(2)}
            className="mt-4 text-primary-600 hover:text-primary-700"
          >
            ← Retour
          </button>
        </div>
      )}

      {/* Step 4: Confirmation */}
      {step === 4 && selectedCreneau && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 flex items-center">
            <CheckCircle className="h-5 w-5 mr-2" />
            Confirmez votre rendez-vous
          </h2>

          <div className="bg-gray-50 rounded-lg p-4 mb-6">
            <h3 className="font-semibold mb-2">Récapitulatif :</h3>
            <div className="space-y-2 text-sm">
              <p>
                <strong>Prestataire :</strong>{" "}
                {getPrestataireById(selectedPrestataire)?.nom}
              </p>
              <p>
                <strong>Date :</strong> {formatDate(selectedCreneau.dateHeure)}
              </p>
              <p>
                <strong>Heure :</strong> {formatTime(selectedCreneau.dateHeure)}
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label
                htmlFor="service"
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Service demandé *
              </label>
              <textarea
                id="service"
                value={service}
                onChange={(e) => setService(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary-500"
                rows="3"
                placeholder="Décrivez le service que vous souhaitez..."
                required
              />
            </div>

            <div className="flex space-x-4">
              <button
                type="button"
                onClick={() => setStep(3)}
                className="flex-1 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-300 transition-colors"
              >
                ← Retour
              </button>
              <button
                type="submit"
                disabled={loading || !service.trim()}
                className="flex-1 bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50"
              >
                {loading ? "Création..." : "Confirmer le rendez-vous"}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default Reservation;
