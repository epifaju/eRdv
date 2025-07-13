import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { Calendar, Clock, Users, CheckCircle } from "lucide-react";

const Home = () => {
  const { isAuthenticated } = useAuth();

  const features = [
    {
      icon: Calendar,
      title: "Réservation Simple",
      description:
        "Prenez rendez-vous en quelques clics avec nos prestataires qualifiés.",
    },
    {
      icon: Clock,
      title: "Créneaux Flexibles",
      description:
        "Des créneaux disponibles 7j/7 pour s'adapter à votre emploi du temps.",
    },
    {
      icon: Users,
      title: "Prestataires Experts",
      description: "Une équipe de professionnels expérimentés à votre service.",
    },
    {
      icon: CheckCircle,
      title: "Confirmation Automatique",
      description: "Recevez une confirmation par email de votre rendez-vous.",
    },
  ];

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-primary-600 to-primary-800 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold mb-6">
              Prise de Rendez-vous
              <span className="block text-primary-200">Simplifiée</span>
            </h1>
            <p className="text-xl md:text-2xl mb-8 text-primary-100 max-w-3xl mx-auto">
              Réservez vos rendez-vous en ligne rapidement et facilement. Notre
              plateforme vous connecte avec les meilleurs prestataires.
            </p>
            <div className="space-x-4">
              {isAuthenticated ? (
                <Link
                  to="/reservation"
                  className="inline-flex items-center px-8 py-4 bg-white text-primary-600 font-semibold rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <Calendar className="mr-2 h-5 w-5" />
                  Prendre un Rendez-vous
                </Link>
              ) : (
                <>
                  <Link
                    to="/register"
                    className="inline-flex items-center px-8 py-4 bg-white text-primary-600 font-semibold rounded-lg hover:bg-gray-100 transition-colors"
                  >
                    Commencer
                  </Link>
                  <Link
                    to="/login"
                    className="inline-flex items-center px-8 py-4 border-2 border-white text-white font-semibold rounded-lg hover:bg-white hover:text-primary-600 transition-colors"
                  >
                    Se Connecter
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              Pourquoi choisir eRDV ?
            </h2>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Une expérience utilisateur exceptionnelle pour simplifier vos
              prises de rendez-vous.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map((feature, index) => (
              <div
                key={index}
                className="text-center p-6 rounded-lg hover:shadow-lg transition-shadow"
              >
                <div className="inline-flex items-center justify-center w-16 h-16 bg-primary-100 text-primary-600 rounded-full mb-4">
                  <feature.icon className="h-8 w-8" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  {feature.title}
                </h3>
                <p className="text-gray-600">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="bg-gray-50 py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            Prêt à commencer ?
          </h2>
          <p className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
            Rejoignez des milliers d'utilisateurs qui font confiance à eRDV pour
            leurs rendez-vous.
          </p>
          {isAuthenticated ? (
            <Link
              to="/reservation"
              className="inline-flex items-center px-8 py-4 bg-primary-600 text-white font-semibold rounded-lg hover:bg-primary-700 transition-colors"
            >
              <Calendar className="mr-2 h-5 w-5" />
              Prendre un Rendez-vous
            </Link>
          ) : (
            <Link
              to="/register"
              className="inline-flex items-center px-8 py-4 bg-primary-600 text-white font-semibold rounded-lg hover:bg-primary-700 transition-colors"
            >
              Créer un Compte
            </Link>
          )}
        </div>
      </div>
    </div>
  );
};

export default Home;
