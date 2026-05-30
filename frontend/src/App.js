import React from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { Toaster } from "react-hot-toast";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Prestataires from "./pages/Prestataires";
import PrestataireCreneaux from "./pages/PrestataireCreneaux";
import Reservation from "./pages/Reservation";
import MesRendezVous from "./pages/MesRendezVous";
import AdminDashboard from "./pages/AdminDashboard";
import PrestataireDashboard from "./pages/PrestataireDashboard";
import Profile from "./pages/Profile";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import "./index.css";

const PrivateRoute = ({ children, adminOnly = false, prestataireOnly = false }) => {
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[50vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  if (adminOnly && user?.role !== "ADMIN") {
    return <Navigate to="/" />;
  }

  if (prestataireOnly && user?.role !== "PRESTATAIRE") {
    return <Navigate to="/" />;
  }

  return children;
};

const AppContent = () => {
  const { isAuthenticated } = useAuth();

  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        {isAuthenticated && <Navbar />}
        <main className="container mx-auto px-4 py-8">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route path="/prestataires" element={<Prestataires />} />
            <Route path="/prestataires/:id" element={<PrestataireCreneaux />} />
            <Route
              path="/reservation"
              element={
                <PrivateRoute>
                  <Reservation />
                </PrivateRoute>
              }
            />
            <Route
              path="/mes-rendez-vous"
              element={
                <PrivateRoute>
                  <MesRendezVous />
                </PrivateRoute>
              }
            />
            <Route
              path="/profil"
              element={
                <PrivateRoute>
                  <Profile />
                </PrivateRoute>
              }
            />
            <Route
              path="/admin"
              element={
                <PrivateRoute adminOnly>
                  <AdminDashboard />
                </PrivateRoute>
              }
            />
            <Route
              path="/prestataire"
              element={
                <PrivateRoute prestataireOnly>
                  <PrestataireDashboard />
                </PrivateRoute>
              }
            />
          </Routes>
        </main>
        <Toaster position="top-right" />
      </div>
    </Router>
  );
};

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
