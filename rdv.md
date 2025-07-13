# 💡 Prompt : Application de prise de rendez-vous complète

Crée une application complète de **prise de rendez-vous** avec une architecture **moderne** et un **design responsive**. Cette application doit permettre à des utilisateurs de réserver des créneaux disponibles chez des prestataires, avec gestion admin, confirmation par email, et API sécurisée.

---

## 🧱 Backend (Spring Boot + PostgreSQL)

- Utilise **Spring Boot (Java)** avec **Maven**
- Base de données : **PostgreSQL**
- Authentification JWT (connexion / inscription sécurisées)
- API RESTful structurée pour :

### 📦 Entités à gérer :
1. `Utilisateur` :
   - id
   - nom
   - email
   - téléphone
   - mot de passe (hashé)
   - rôle (`USER`, `ADMIN`)

2. `Prestataire` :
   - id
   - nom
   - spécialité
   - email

3. `CréneauHoraire` :
   - id
   - prestataire_id
   - date_heure
   - disponible (booléen)

4. `RendezVous` :
   - id
   - utilisateur_id
   - prestataire_id
   - date_heure
   - service
   - statut (`en_attente`, `confirmé`, `annulé`)

- Fonctionnalités :
  - L’administrateur peut :
    - Gérer les prestataires et les créneaux horaires
    - Consulter tous les rendez-vous
  - L’utilisateur peut :
    - Voir les prestataires disponibles
    - Réserver un rendez-vous à une date/heure
    - Voir ses rendez-vous réservés
    - Recevoir un email de confirmation de rendez-vous

- Configuration SMTP :
  - Utilise `JavaMailSender`
  - Email de confirmation avec détails du rendez-vous

- Sécurité :
  - JWT Authentication (login, register, token refresh)
  - Middleware de vérification des rôles

---

## 🎨 Frontend (React.js + Tailwind CSS)

- Design **moderne et responsive**, compatible **mobiles et tablettes**
- Utilise **React.js** avec **React Router** et **Axios**
- UI avec **Tailwind CSS**
- Authentification avec stockage du token JWT dans `localStorage`

### Pages à créer :
1. **Accueil** : Présentation + bouton “Prendre rendez-vous”
2. **Liste des prestataires**
3. **Réservation** : Sélection du prestataire, date, heure, formulaire
4. **Connexion / Inscription**
5. **Espace utilisateur** : Liste de ses rendez-vous
6. **Admin Dashboard** :
   - Voir tous les rendez-vous
   - Gérer les prestataires et les créneaux

### Composants UI :
- Datepicker
- Sélecteur d’heure
- Boutons conditionnels selon statut
- Modales de confirmation / succès
- Tableaux dynamiques

---

## 🔐 Authentification & Sécurité

- Inscription / Connexion avec JWT
- Pages protégées côté client selon rôle (`USER` ou `ADMIN`)
- Autorisation côté serveur via middleware

---

## 📬 Email de confirmation (SMTP)

- Lorsqu’un utilisateur réserve un rendez-vous, un **email est automatiquement envoyé** :
  - Détail du rendez-vous (prestataire, date, heure)
  - Lien vers l’espace personnel

---

## 🐳 Dockerisation

- Dockerfile pour le backend (Spring Boot)
- Dockerfile pour le frontend (React.js)
- Fichier `docker-compose.yml` avec :
  - PostgreSQL
  - Backend
  - Frontend

---

## 🧪 Tests

- Tests unitaires pour les services backend
- Instructions dans le README pour lancer l'app localement

---

## ✅ Contraintes techniques

- Utiliser les bonnes pratiques de séparation des couches (Controller, Service, Repository)
- Code propre, modulaire, lisible
- Architecture prête pour évoluer vers un SaaS

---

Génère tout le projet avec cette structure, prêt à déployer localement via Docker.
