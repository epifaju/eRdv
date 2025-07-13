# 📅 Application de Prise de Rendez-vous

Une application complète de prise de rendez-vous avec Spring Boot (backend) et React.js (frontend).

## 🚀 Fonctionnalités

- **Authentification JWT** : Inscription et connexion sécurisées
- **Gestion des prestataires** : Création et gestion des prestataires de services
- **Réservation de rendez-vous** : Interface intuitive pour réserver des créneaux
- **Espace utilisateur** : Consultation et gestion des rendez-vous personnels
- **Dashboard administrateur** : Gestion complète des prestataires et rendez-vous
- **Notifications par email** : Confirmation automatique des réservations
- **Interface responsive** : Compatible mobile, tablette et desktop

## 🏗️ Architecture

### Backend (Spring Boot)

- **Framework** : Spring Boot 3.x
- **Base de données** : PostgreSQL
- **Authentification** : JWT
- **Email** : SMTP avec JavaMailSender
- **API** : RESTful

### Frontend (React.js)

- **Framework** : React.js 18
- **Styling** : Tailwind CSS
- **Routing** : React Router
- **HTTP Client** : Axios
- **État** : React Hooks

## 🐳 Démarrage rapide avec Docker

### Prérequis

- Docker
- Docker Compose

### Lancement

```bash
# Cloner le projet
git clone <repository-url>
cd eRDV

# Lancer l'application
docker-compose up -d

# L'application sera disponible sur :
# Frontend : http://localhost:3000
# Backend API : http://localhost:8080
# PostgreSQL : localhost:5432
```

## 🛠️ Développement local

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm start
```

## 📊 Base de données

L'application utilise PostgreSQL avec les tables suivantes :

- `utilisateurs` : Gestion des utilisateurs et rôles
- `prestataires` : Informations des prestataires de services
- `creneaux_horaires` : Créneaux disponibles par prestataire
- `rendez_vous` : Réservations des utilisateurs

## 🔐 Authentification

- **JWT** pour l'authentification
- **Rôles** : USER et ADMIN
- **Protection des routes** côté client et serveur

## 📧 Configuration Email

L'application envoie automatiquement des emails de confirmation lors des réservations. Configurez les paramètres SMTP dans `application.properties`.

## 🧪 Tests

```bash
# Tests backend
cd backend
./mvnw test

# Tests frontend
cd frontend
npm test
```

## 📱 Interface utilisateur

- **Design moderne** avec Tailwind CSS
- **Responsive** : Mobile-first approach
- **Composants réutilisables** : Modales, formulaires, tableaux
- **Navigation intuitive** : React Router

## 🔧 Configuration

### Variables d'environnement

- `DB_URL` : URL de la base de données PostgreSQL
- `JWT_SECRET` : Clé secrète pour les tokens JWT
- `SMTP_HOST` : Serveur SMTP
- `SMTP_PORT` : Port SMTP
- `SMTP_USERNAME` : Nom d'utilisateur SMTP
- `SMTP_PASSWORD` : Mot de passe SMTP

## 📝 API Endpoints

### Authentification

- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion
- `POST /api/auth/refresh` - Rafraîchissement du token

### Utilisateurs

- `GET /api/users/profile` - Profil utilisateur
- `PUT /api/users/profile` - Mise à jour du profil

### Prestataires

- `GET /api/prestataires` - Liste des prestataires
- `POST /api/prestataires` - Créer un prestataire (ADMIN)
- `PUT /api/prestataires/{id}` - Modifier un prestataire (ADMIN)
- `DELETE /api/prestataires/{id}` - Supprimer un prestataire (ADMIN)

### Créneaux

- `GET /api/creneaux` - Créneaux disponibles
- `POST /api/creneaux` - Créer un créneau (ADMIN)
- `PUT /api/creneaux/{id}` - Modifier un créneau (ADMIN)
- `DELETE /api/creneaux/{id}` - Supprimer un créneau (ADMIN)

### Rendez-vous

- `GET /api/rendez-vous` - Liste des rendez-vous
- `POST /api/rendez-vous` - Créer un rendez-vous
- `PUT /api/rendez-vous/{id}` - Modifier un rendez-vous
- `DELETE /api/rendez-vous/{id}` - Annuler un rendez-vous

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.
