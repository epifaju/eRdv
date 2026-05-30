import React, { useEffect, useState } from "react";
import { loadStripe } from "@stripe/stripe-js";
import {
  Elements,
  PaymentElement,
  useElements,
  useStripe,
} from "@stripe/react-stripe-js";
import api from "../api/client";
import toast from "react-hot-toast";
import { CreditCard } from "lucide-react";

const CheckoutForm = ({ onSuccess, onBack, loading, setLoading }) => {
  const stripe = useStripe();
  const elements = useElements();

  const handlePay = async (e) => {
    e.preventDefault();
    if (!stripe || !elements) return;

    setLoading(true);
    try {
      const { error, paymentIntent } = await stripe.confirmPayment({
        elements,
        redirect: "if_required",
      });

      if (error) {
        toast.error(error.message || "Paiement refusé");
        return;
      }

      if (paymentIntent?.status === "succeeded") {
        const { data } = await api.post("/payments/complete", {
          paymentIntentId: paymentIntent.id,
        });
        onSuccess(data);
      } else {
        toast.error("Le paiement n'a pas pu être confirmé");
      }
    } catch (err) {
      const msg = err.response?.data?.message;
      toast.error(msg || "Erreur lors de la confirmation du paiement");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handlePay} className="space-y-4">
      <PaymentElement />
      <div className="flex space-x-4 pt-2">
        <button
          type="button"
          onClick={onBack}
          disabled={loading}
          className="flex-1 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-300 disabled:opacity-50"
        >
          ← Retour
        </button>
        <button
          type="submit"
          disabled={loading || !stripe}
          className="flex-1 bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 disabled:opacity-50"
        >
          {loading ? "Paiement..." : "Payer et confirmer"}
        </button>
      </div>
    </form>
  );
};

const PaymentCheckout = ({
  publishableKey,
  creneauId,
  prestationId,
  serviceNotes,
  montant,
  onSuccess,
  onBack,
}) => {
  const [stripePromise, setStripePromise] = useState(null);
  const [clientSecret, setClientSecret] = useState("");
  const [initError, setInitError] = useState("");
  const [loading, setLoading] = useState(false);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    if (publishableKey) {
      setStripePromise(loadStripe(publishableKey));
    }
  }, [publishableKey]);

  useEffect(() => {
    let cancelled = false;
    const init = async () => {
      setInitializing(true);
      setInitError("");
      try {
        const payload = { creneauId, prestationId };
        if (serviceNotes?.trim()) {
          payload.service = serviceNotes.trim();
        }
        const { data } = await api.post("/payments/intent", payload);
        if (!cancelled) {
          setClientSecret(data.clientSecret);
        }
      } catch (err) {
        if (!cancelled) {
          setInitError(
            err.response?.data?.message || "Impossible d'initialiser le paiement"
          );
        }
      } finally {
        if (!cancelled) {
          setInitializing(false);
        }
      }
    };
    init();
    return () => {
      cancelled = true;
    };
  }, [creneauId, prestationId, serviceNotes]);

  if (initializing) {
    return (
      <div className="flex justify-center py-8">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600" />
      </div>
    );
  }

  if (initError) {
    return (
      <div className="space-y-4">
        <p className="text-red-600 text-sm">{initError}</p>
        <button
          type="button"
          onClick={onBack}
          className="text-primary-600 hover:text-primary-700 text-sm"
        >
          ← Retour
        </button>
      </div>
    );
  }

  if (!clientSecret || !stripePromise) {
    return null;
  }

  const options = { clientSecret, locale: "fr" };

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2 text-sm font-medium text-gray-800">
        <CreditCard className="h-4 w-4" />
        Paiement sécurisé — {montant} €
      </div>
      <Elements stripe={stripePromise} options={options}>
        <CheckoutForm
          onSuccess={onSuccess}
          onBack={onBack}
          loading={loading}
          setLoading={setLoading}
        />
      </Elements>
    </div>
  );
};

export default PaymentCheckout;
