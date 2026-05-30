import React, { useState, useEffect, useCallback } from "react";

import { useNavigate } from "react-router-dom";

import api from "../api/client";

import { RDV_MES, rdvReprogrammer } from "../api/paths";

import DatePicker from "react-datepicker";

import "react-datepicker/dist/react-datepicker.css";

import {

  Calendar,

  Clock,

  User,

  X,

  CheckCircle,

  AlertCircle,

  AlertTriangle,

  RefreshCw,

} from "lucide-react";

import toast from "react-hot-toast";



const MesRendezVous = () => {

  const navigate = useNavigate();

  const [rendezVous, setRendezVous] = useState([]);

  const [loading, setLoading] = useState(true);

  const [showCancelModal, setShowCancelModal] = useState(false);

  const [rendezVousToCancel, setRendezVousToCancel] = useState(null);

  const [cancelling, setCancelling] = useState(false);

  const [showRescheduleModal, setShowRescheduleModal] = useState(false);

  const [rdvToReschedule, setRdvToReschedule] = useState(null);

  const [creneauxDisponibles, setCreneauxDisponibles] = useState([]);

  const [creneaux, setCreneaux] = useState([]);

  const [selectedDate, setSelectedDate] = useState(null);

  const [selectedCreneau, setSelectedCreneau] = useState(null);

  const [loadingCreneaux, setLoadingCreneaux] = useState(false);

  const [rescheduling, setRescheduling] = useState(false);



  useEffect(() => {

    fetchRendezVous();

  }, []);



  const fetchRendezVous = async () => {

    try {

      const response = await api.get(RDV_MES);

      setRendezVous(response.data);

    } catch {

      toast.error("Erreur lors du chargement des rendez-vous");

    } finally {

      setLoading(false);

    }

  };



  const fetchCreneauxDisponibles = useCallback(async (prestataireId) => {

    if (!prestataireId) return;

    try {

      const { data } = await api.get(

        `/creneaux/prestataire/${prestataireId}/disponibles`

      );

      setCreneauxDisponibles(data);

    } catch {

      setCreneauxDisponibles([]);

      toast.error("Erreur lors du chargement des créneaux");

    }

  }, []);



  const fetchCreneauxForDate = useCallback(async (prestataireId, date, dureeMinutes) => {

    if (!prestataireId || !date) return;

    setLoadingCreneaux(true);

    try {

      const y = date.getFullYear();

      const m = String(date.getMonth() + 1).padStart(2, "0");

      const d = String(date.getDate()).padStart(2, "0");

      const dateStr = `${y}-${m}-${d}`;

      const params = { date: dateStr };

      if (dureeMinutes) params.dureeMinutes = dureeMinutes;

      const { data } = await api.get(

        `/creneaux/prestataire/${prestataireId}/disponibles/date`,

        { params }

      );

      setCreneaux(data);

    } catch {

      setCreneaux([]);

      toast.error("Erreur lors du chargement des horaires");

    } finally {

      setLoadingCreneaux(false);

    }

  }, []);



  useEffect(() => {

    if (showRescheduleModal && rdvToReschedule && selectedDate) {

      fetchCreneauxForDate(
        rdvToReschedule.prestataire?.id,
        selectedDate,
        rdvToReschedule.prestation?.dureeMinutes || rdvToReschedule.dureeTotaleMinutes
      );

    }

  }, [showRescheduleModal, rdvToReschedule, selectedDate, fetchCreneauxForDate]);



  const openCancelModal = (rdv) => {

    setRendezVousToCancel(rdv);

    setShowCancelModal(true);

  };



  const closeCancelModal = () => {

    setShowCancelModal(false);

    setRendezVousToCancel(null);

  };



  const openRescheduleModal = (rdv) => {

    setRdvToReschedule(rdv);

    setSelectedDate(null);

    setSelectedCreneau(null);

    setCreneaux([]);

    setShowRescheduleModal(true);

    fetchCreneauxDisponibles(rdv.prestataire?.id);

  };



  const closeRescheduleModal = () => {

    setShowRescheduleModal(false);

    setRdvToReschedule(null);

    setSelectedDate(null);

    setSelectedCreneau(null);

    setCreneaux([]);

    setCreneauxDisponibles([]);

  };



  const handleAnnuler = async () => {

    if (!rendezVousToCancel) return;



    setCancelling(true);

    try {

      await api.put(`/rendez-vous/${rendezVousToCancel.id}/annuler`);

      toast.success("Rendez-vous annulé avec succès");

      fetchRendezVous();

      closeCancelModal();

    } catch (error) {
      const msg = error.response?.data?.message;
      toast.error(msg || "Erreur lors de l'annulation");
    } finally {

      setCancelling(false);

    }

  };



  const handleReprogrammer = async () => {

    if (!rdvToReschedule || !selectedCreneau) {

      toast.error("Choisissez une date et un horaire");

      return;

    }



    setRescheduling(true);

    try {

      await api.put(rdvReprogrammer(rdvToReschedule.id), {

        creneauId: selectedCreneau.id,

      });

      toast.success("Rendez-vous reprogrammé avec succès");

      fetchRendezVous();

      closeRescheduleModal();

    } catch (error) {

      const msg = error.response?.data?.message;

      toast.error(msg || "Erreur lors de la reprogrammation");

    } finally {

      setRescheduling(false);

    }

  };



  const isFutureRdv = (dateHeure) => new Date(dateHeure) > new Date();



  const canModify = (rdv) =>
    (rdv.statut === "EN_ATTENTE" || rdv.statut === "CONFIRME") &&
    isFutureRdv(rdv.dateHeure);

  const canAnnuler = (rdv) => rdv.annulableParClient === true;

  const delaiAnnulationHeures = (rdv) => rdv.delaiAnnulationHeures ?? 24;



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



  const formatPlage = (rdv) => {

    const debut = formatTime(rdv.dateHeure);

    if (rdv.dateHeureFin) return `${debut} – ${formatTime(rdv.dateHeureFin)}`;

    if (rdv.dureeTotaleMinutes > 30) {

      const fin = new Date(

        new Date(rdv.dateHeure).getTime() + rdv.dureeTotaleMinutes * 60000

      );

      return `${debut} – ${formatTime(fin.toISOString())}`;

    }

    return debut;

  };



  const isDateSelectable = (date) => {

    if (!creneauxDisponibles.length) return false;

    const day = new Date(date);

    day.setHours(0, 0, 0, 0);

    return creneauxDisponibles.some((c) => {

      const slot = new Date(c.dateHeure);

      slot.setHours(0, 0, 0, 0);

      return slot.getTime() === day.getTime();

    });

  };



  const CancelConfirmationModal = () => {

    if (!showCancelModal || !rendezVousToCancel) return null;



    return (

      <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">

        <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">

          <div className="mt-3 text-center">

            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">

              <AlertTriangle className="h-6 w-6 text-red-600" />

            </div>

            <h3 className="text-lg font-medium text-gray-900 mt-4">

              Confirmer l&apos;annulation

            </h3>

            <div className="mt-2 px-7 py-3">

              <p className="text-sm text-gray-500 mb-4">
                Êtes-vous sûr de vouloir annuler ce rendez-vous ?
              </p>
              <p className="text-xs text-gray-500 mb-4">
                Rappel : annulation possible jusqu&apos;à{" "}
                {delaiAnnulationHeures(rendezVousToCancel)} h avant le rendez-vous.
              </p>

              <div className="bg-gray-50 rounded-lg p-4 text-left">

                <div className="flex items-center mb-2">

                  <User className="h-4 w-4 text-gray-400 mr-2" />

                  <span className="font-medium text-gray-900">

                    {rendezVousToCancel.prestataire?.nom}

                  </span>

                </div>

                <div className="flex items-center text-sm text-gray-600 mb-2">

                  <Calendar className="h-4 w-4 mr-2" />

                  {formatDate(rendezVousToCancel.dateHeure)}

                </div>

                <div className="flex items-center text-sm text-gray-600 mb-2">

                  <Clock className="h-4 w-4 mr-2" />

                  {formatTime(rendezVousToCancel.dateHeure)}

                </div>

                <div className="text-sm text-gray-600">

                  <span className="font-medium">Service :</span>{" "}

                  {rendezVousToCancel.service}

                </div>

              </div>

              <p className="text-xs text-red-500 mt-3">

                Cette action est irréversible.

              </p>

            </div>

            <div className="flex justify-center space-x-3 mt-6">

              <button

                onClick={closeCancelModal}

                disabled={cancelling}

                className="px-4 py-2 bg-gray-300 text-gray-700 text-base font-medium rounded-md shadow-sm hover:bg-gray-400 disabled:opacity-50"

              >

                Retour

              </button>

              <button

                onClick={handleAnnuler}

                disabled={cancelling}

                className="px-4 py-2 bg-red-600 text-white text-base font-medium rounded-md shadow-sm hover:bg-red-700 disabled:opacity-50"

              >

                {cancelling ? "Annulation..." : "Confirmer l'annulation"}

              </button>

            </div>

          </div>

        </div>

      </div>

    );

  };



  const RescheduleModal = () => {

    if (!showRescheduleModal || !rdvToReschedule) return null;



    return (

      <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">

        <div className="relative top-10 mx-auto p-5 border w-full max-w-lg shadow-lg rounded-md bg-white mb-10">

          <div className="flex items-center justify-between mb-4">

            <h3 className="text-lg font-medium text-gray-900 flex items-center">

              <RefreshCw className="h-5 w-5 mr-2 text-primary-600" />

              Reprogrammer le rendez-vous

            </h3>

            <button

              onClick={closeRescheduleModal}

              className="text-gray-400 hover:text-gray-600"

              aria-label="Fermer"

            >

              <X className="h-5 w-5" />

            </button>

          </div>



          <div className="bg-gray-50 rounded-lg p-4 mb-4 text-sm">

            <p className="font-medium text-gray-900 mb-1">

              {rdvToReschedule.prestataire?.nom}

            </p>

            <p className="text-gray-600 mb-2">{rdvToReschedule.service}</p>

            <p className="text-gray-500">

              Actuellement : {formatDate(rdvToReschedule.dateHeure)} à{" "}

              {formatTime(rdvToReschedule.dateHeure)}

            </p>

          </div>



          <div className="mb-4">

            <label className="block text-sm font-medium text-gray-700 mb-2">

              Nouvelle date

            </label>

            <div className="flex justify-center">

              <DatePicker

                selected={selectedDate}

                onChange={(date) => {

                  setSelectedDate(date);

                  setSelectedCreneau(null);

                }}

                minDate={new Date()}

                filterDate={isDateSelectable}

                dateFormat="dd/MM/yyyy"

                placeholderText="Sélectionnez une date"

                className="border rounded-lg px-4 py-2 w-full focus:outline-none focus:ring-2 focus:ring-primary-500"

              />

            </div>

            {creneauxDisponibles.length === 0 && (

              <p className="text-amber-700 text-sm mt-2">

                Aucun créneau disponible pour ce prestataire.

              </p>

            )}

          </div>



          {selectedDate && (

            <div className="mb-6">

              <label className="block text-sm font-medium text-gray-700 mb-2">

                Nouvel horaire

              </label>

              {loadingCreneaux ? (

                <p className="text-gray-500 text-center py-3">Chargement…</p>

              ) : creneaux.length === 0 ? (

                <p className="text-gray-500 text-sm">

                  Aucun créneau pour cette date.

                </p>

              ) : (

                <div className="grid grid-cols-3 sm:grid-cols-4 gap-2 max-h-48 overflow-y-auto">

                  {creneaux.map((creneau) => (

                    <button

                      key={creneau.id}

                      type="button"

                      onClick={() => setSelectedCreneau(creneau)}

                      className={`border rounded-lg p-2 text-sm transition-colors ${

                        selectedCreneau?.id === creneau.id

                          ? "border-primary-600 bg-primary-50 text-primary-700"

                          : "hover:border-primary-500 hover:bg-primary-50"

                      }`}

                    >

                      {formatTime(creneau.dateHeure)}

                    </button>

                  ))}

                </div>

              )}

            </div>

          )}



          <div className="flex justify-end space-x-3">

            <button

              onClick={closeRescheduleModal}

              disabled={rescheduling}

              className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 disabled:opacity-50"

            >

              Annuler

            </button>

            <button

              onClick={handleReprogrammer}

              disabled={rescheduling || !selectedCreneau}

              className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50"

            >

              {rescheduling ? "Reprogrammation..." : "Confirmer"}

            </button>

          </div>

        </div>

      </div>

    );

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

            Vous n&apos;avez pas encore de rendez-vous.

          </p>

          <div className="mt-6">

            <button

              onClick={() => navigate("/reservation")}

              className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"

            >

              <Calendar className="h-4 w-4 mr-2" />

              Prendre un rendez-vous

            </button>

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

                      {formatPlage(rdv)}

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



                  <div className="flex flex-wrap items-center gap-3 justify-between">

                    {getStatusBadge(rdv.statut)}



                    {canModify(rdv) && (

                      <div className="flex flex-wrap gap-2">

                        <button

                          onClick={() => openRescheduleModal(rdv)}

                          className="inline-flex items-center px-3 py-1.5 border border-primary-300 text-sm font-medium rounded-md text-primary-700 bg-white hover:bg-primary-50"

                        >

                          <RefreshCw className="h-4 w-4 mr-1" />

                          Reprogrammer

                        </button>

                        {canAnnuler(rdv) && (
                          <button
                            onClick={() => openCancelModal(rdv)}
                            className="inline-flex items-center px-3 py-1.5 border border-red-300 text-sm font-medium rounded-md text-red-700 bg-white hover:bg-red-50"
                          >
                            <X className="h-4 w-4 mr-1" />
                            Annuler
                          </button>
                        )}
                      </div>
                    )}

                    {canModify(rdv) && !canAnnuler(rdv) && (
                      <p className="text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded px-2 py-1 w-full sm:w-auto">
                        Annulation impossible : délai de {delaiAnnulationHeures(rdv)} h dépassé.
                        Contactez le prestataire.
                      </p>
                    )}

                  </div>

                </div>

              </div>

            </div>

          ))}

        </div>

      )}



      <CancelConfirmationModal />

      <RescheduleModal />

    </div>

  );

};



export default MesRendezVous;

