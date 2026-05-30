import React, { useState, useEffect, useCallback } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import api from "../api/client";
import SlotCalendar from "../components/SlotCalendar";
import { addDays, dateKey, formatSlotPlage } from "../utils/slotTime";
import { User, CheckCircle, Briefcase, Calendar } from "lucide-react";
import toast from "react-hot-toast";

const Reservation = () => {
  const [prestataires, setPrestataires] = useState([]);
  const [selectedPrestataire, setSelectedPrestataire] = useState(null);
  const [prestations, setPrestations] = useState([]);
  const [selectedPrestation, setSelectedPrestation] = useState(null);
  const [creneaux, setCreneaux] = useState([]);
  const [creneauxDisponibles, setCreneauxDisponibles] = useState([]);
  const [weekSlots, setWeekSlots] = useState({});
  const [loadingCreneaux, setLoadingCreneaux] = useState(false);
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedCreneau, setSelectedCreneau] = useState(null);
  const [service, setService] = useState("");
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState(1);

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const dureeMinutes = selectedPrestation?.dureeMinutes;

  const fetchPrestataires = async () => {
    try {
      const response = await api.get("/prestataires");
      setPrestataires(response.data);
    } catch {
      toast.error("Erreur lors du chargement des prestataires");
    }
  };

  const fetchPrestations = useCallback(async (prestataireId) => {
    if (!prestataireId) return;
    try {
      const { data } = await api.get(`/prestations/prestataire/${prestataireId}`);
      setPrestations(data);
    } catch {
      setPrestations([]);
      toast.error("Erreur lors du chargement des prestations");
    }
  }, []);

  const fetchCreneauxDisponibles = useCallback(async (prestataireId) => {
    if (!prestataireId) return;
    try {
      const response = await api.get(
        `/creneaux/prestataire/${prestataireId}/disponibles`
      );
      setCreneauxDisponibles(response.data);
    } catch {
      setCreneauxDisponibles([]);
      toast.error("Erreur lors du chargement des créneaux");
    }
  }, []);

  const fetchCreneauxForDate = useCallback(
    async (date) => {
      if (!selectedPrestataire || !date) return [];
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, "0");
      const d = String(date.getDate()).padStart(2, "0");
      const params = { date: `${y}-${m}-${d}` };
      if (dureeMinutes) params.dureeMinutes = dureeMinutes;
      const response = await api.get(
        `/creneaux/prestataire/${selectedPrestataire}/disponibles/date`,
        { params }
      );
      return response.data;
    },
    [selectedPrestataire, dureeMinutes]
  );

  const fetchCreneaux = useCallback(async () => {
    if (!selectedPrestataire || !selectedDate) return;
    setLoadingCreneaux(true);
    try {
      const data = await fetchCreneauxForDate(selectedDate);
      setCreneaux(data);
    } catch {
      setCreneaux([]);
      toast.error("Erreur lors du chargement des créneaux");
    } finally {
      setLoadingCreneaux(false);
    }
  }, [selectedPrestataire, selectedDate, fetchCreneauxForDate]);

  const loadWeekSlots = useCallback(
    async (weekStartDate) => {
      if (!selectedPrestataire) return;
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const map = {};
      const tasks = [];
      for (let i = 0; i < 7; i++) {
        const day = addDays(weekStartDate, i);
        if (day < today) continue;
        const key = dateKey(day);
        tasks.push(
          fetchCreneauxForDate(day)
            .then((data) => {
              map[key] = data;
            })
            .catch(() => {
              map[key] = [];
            })
        );
      }
      await Promise.all(tasks);
      setWeekSlots(map);
    },
    [selectedPrestataire, fetchCreneauxForDate]
  );

  useEffect(() => {
    fetchPrestataires();
    const prestataireId = searchParams.get("prestataire");
    if (prestataireId) {
      setSelectedPrestataire(prestataireId);
      fetchPrestations(prestataireId);
      fetchCreneauxDisponibles(prestataireId);
      setStep(2);
    }
  }, [searchParams, fetchCreneauxDisponibles, fetchPrestations]);

  useEffect(() => {
    if (selectedPrestataire && selectedDate) {
      fetchCreneaux();
    }
  }, [selectedPrestataire, selectedDate, selectedPrestation, fetchCreneaux]);

  const handlePrestataireSelect = (prestataireId) => {
    setSelectedPrestataire(prestataireId);
    setSelectedPrestation(null);
    setSelectedDate(null);
    setSelectedCreneau(null);
    setCreneaux([]);
    setCreneauxDisponibles([]);
    setWeekSlots({});
    setService("");
    fetchPrestations(prestataireId);
    fetchCreneauxDisponibles(prestataireId);
    setStep(2);
  };

  const handlePrestationSelect = (prestation) => {
    setSelectedPrestation(prestation);
    setService("");
    setSelectedDate(null);
    setSelectedCreneau(null);
    setWeekSlots({});
    setStep(3);
  };

  const handleCalendarDate = (date) => {
    setSelectedDate(date);
    setSelectedCreneau(null);
  };

  const handleSlotSelect = (creneau) => {
    setSelectedCreneau(creneau);
    if (creneau) setStep(4);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedPrestataire || !selectedPrestation || !selectedCreneau) {
      toast.error("Veuillez compléter toutes les étapes");
      return;
    }

    setLoading(true);
    try {
      const payload = {
        creneauId: selectedCreneau.id,
        prestationId: selectedPrestation.id,
      };
      if (service.trim()) {
        payload.service = service.trim();
      }
      await api.post("/rendez-vous", payload);
      toast.success(
        "Demande de rendez-vous enregistrée. Vous serez notifié après validation."
      );
      navigate("/mes-rendez-vous");
    } catch (error) {
      const msg = error.response?.data?.message;
      toast.error(msg || "Erreur lors de la création du rendez-vous");
    } finally {
      setLoading(false);
    }
  };

  const getPrestataireById = (id) =>
    prestataires.find((p) => p.id === parseInt(id, 10));

  const formatDate = (value) => {
    const date = value instanceof Date ? value : new Date(value);
    return date.toLocaleDateString("fr-FR", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const stepLabels = ["Prestataire", "Prestation", "Date & horaire", "Confirmation"];

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

      <div className="mb-8">
        <div className="flex items-center justify-center">
          {[1, 2, 3, 4].map((stepNumber) => (
            <div key={stepNumber} className="flex items-center">
              <div
                className={`flex items-center justify-center w-8 h-8 rounded-full text-sm ${
                  step >= stepNumber
                    ? "bg-primary-600 text-white"
                    : "bg-gray-200 text-gray-600"
                }`}
              >
                {stepNumber}
              </div>
              {stepNumber < 4 && (
                <div
                  className={`w-12 h-1 mx-1 ${
                    step > stepNumber ? "bg-primary-600" : "bg-gray-200"
                  }`}
                />
              )}
            </div>
          ))}
        </div>
        <div className="flex flex-wrap justify-center gap-x-4 gap-y-1 mt-2 text-xs sm:text-sm text-gray-600">
          {stepLabels.map((label, i) => (
            <span key={label} className={step >= i + 1 ? "text-primary-600" : ""}>
              {label}
            </span>
          ))}
        </div>
      </div>

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

      {step === 2 && selectedPrestataire && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-4 flex items-center">
            <Briefcase className="h-5 w-5 mr-2" />
            Choisissez une prestation
          </h2>
          <p className="text-gray-600 mb-4">
            Prestataire :{" "}
            <strong>{getPrestataireById(selectedPrestataire)?.nom}</strong>
          </p>
          {prestations.length === 0 ? (
            <p className="text-amber-700 bg-amber-50 border border-amber-200 rounded-lg py-3 px-4">
              Aucune prestation disponible. Contactez l&apos;établissement.
            </p>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {prestations.map((pr) => (
                <button
                  key={pr.id}
                  type="button"
                  onClick={() => handlePrestationSelect(pr)}
                  className="text-left border rounded-lg p-4 hover:border-primary-500 hover:bg-primary-50 transition-colors"
                >
                  <div className="font-semibold">{pr.nom}</div>
                  <div className="text-sm text-gray-600">
                    {pr.dureeMinutes} min
                    {pr.prix != null ? ` · ${pr.prix} €` : ""}
                  </div>
                  {pr.description && (
                    <p className="text-sm text-gray-500 mt-1">{pr.description}</p>
                  )}
                </button>
              ))}
            </div>
          )}
          <button
            onClick={() => setStep(1)}
            className="mt-4 text-primary-600 hover:text-primary-700"
          >
            ← Retour
          </button>
        </div>
      )}

      {step === 3 && selectedPrestation && (
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold mb-2 flex items-center">
            <Calendar className="h-5 w-5 mr-2" />
            Choisissez une date et un horaire
          </h2>
          <p className="text-gray-600 mb-4 text-sm">
            Prestation : <strong>{selectedPrestation.nom}</strong> (
            {selectedPrestation.dureeMinutes} min)
          </p>
          {creneauxDisponibles.length === 0 ? (
            <p className="text-center text-amber-700 bg-amber-50 border border-amber-200 rounded-lg py-3">
              Aucun créneau disponible pour ce prestataire.
            </p>
          ) : (
            <SlotCalendar
              creneauxDisponibles={creneauxDisponibles}
              slots={creneaux}
              selectedDate={selectedDate}
              onSelectDate={handleCalendarDate}
              selectedSlot={selectedCreneau}
              onSelectSlot={handleSlotSelect}
              dureeMinutes={dureeMinutes}
              loading={loadingCreneaux}
              weekSlots={weekSlots}
              onWeekChange={loadWeekSlots}
            />
          )}
          <button
            onClick={() => setStep(2)}
            className="mt-4 text-primary-600 hover:text-primary-700"
          >
            ← Retour
          </button>
        </div>
      )}

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
                <strong>Prestation :</strong> {selectedPrestation?.nom}
              </p>
              <p>
                <strong>Date :</strong> {formatDate(selectedCreneau.dateHeure)}
              </p>
              <p>
                <strong>Horaire :</strong>{" "}
                {formatSlotPlage(selectedCreneau, dureeMinutes)}
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label
                htmlFor="service"
                className="block text-sm font-medium text-gray-700 mb-2"
              >
                Précisions (optionnel)
              </label>
              <textarea
                id="service"
                value={service}
                onChange={(e) => setService(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary-500"
                rows="3"
                placeholder="Motif, demande particulière…"
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
                disabled={loading}
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
