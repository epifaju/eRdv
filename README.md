# eRDV — Application de prise de rendez-vous

Application multi-secteurs (cabinet, salon, garage…) : réservation en ligne, espace prestataire, administration, notifications e-mail.

**Stack :** Spring Boot 3 · React 18 · PostgreSQL · Docker · Flyway

---

## Fonctionnalités (V2)

| Domaine | Contenu |
|--------|---------|
| **Utilisateur** | Inscription, profil, mot de passe oublié, réservation par prestation, calendrier semaine/mois, reprogrammation, annulation (délai 24 h) |
| **Prestataire** | Espace `/prestataire` : agenda, confirmer/refuser les demandes, gestion du catalogue (prestations & plages récurrentes) |
| **Admin** | Dashboard : prestataires, créneaux, RDV paginés, catalogue |
| **Métier** | Catalogue prestations, plages récurrentes, génération créneaux, multi-slots (prestations longues), anti double-booking |
| **Notifications** | E-mails HTML (client + prestataire), rappels J-1 / H-2 |
| **Infra** | Flyway, profils dev/prod, scripts Docker & backup PostgreSQL |

## Industrialisation (V3)

| Domaine | Contenu |
|--------|---------|
| **CI/CD** | GitHub Actions : tests, build frontend, build images Docker |
| **Sécurité auth** | Rate limiting login/register/forgot-password, refresh tokens révocables (`POST /auth/logout`) |
| **Observabilité** | Métriques Prometheus, logs JSON (Logstash) avec corrélation `X-Request-Id` |
| **Documentation** | OpenAPI / Swagger UI en dev (`APP_OPENAPI_ENABLED=true`) |

## Multi-établissement (V4)

| Domaine | Contenu |
|--------|---------|
| **Établissements** | Cabinets / sites, prestataires et RDV rattachés, parcours réservation en 5 étapes |
| **API** | `GET /etablissements`, `GET /etablissements/{id}/prestataires`, CRUD admin |
| **RGPD** | `GET /users/me/export`, `DELETE /users/me` (anonymisation + révocation sessions) |
| **Paiement** | Stripe : intent + confirmation, RDV auto-confirmé, remboursement à l'annulation |
| **SMS** | Twilio : rappels J-1 / H-2 (en plus des e-mails), désactivé par défaut |
| **Roadmap** | — |

---

### Prérequis

- Docker & Docker Compose
- PowerShell (Windows) ou bash équivalent

### 1. Configuration

```powershell
Copy-Item .env.example .env
# Éditer .env : JWT_SECRET (généré automatiquement par le script), SMTP optionnel
```

### 2. Lancement

```powershell
.\scripts\start-docker.ps1
```

Ou manuellement :

```powershell
docker compose up -d --build
```

### 3. URLs

| Service | URL / port |
|---------|------------|
| **Frontend** | http://localhost:3001 |
| **API backend** | http://localhost:8084/api |
| **Santé API** | http://localhost:8084/api/actuator/health |
| **Swagger UI** | http://localhost:8084/api/swagger-ui.html (si `APP_OPENAPI_ENABLED=true`) |
| **Prometheus** | http://localhost:8084/api/actuator/prometheus (admin JWT requis) |
| **PostgreSQL** | `localhost:5436` (utilisateur `postgres`, base `erdv_db`) |

Le frontend Docker est compilé avec `REACT_APP_API_URL=http://localhost:8084/api`. Après modification de l’URL API, reconstruire l’image frontend.

---

## Comptes de démo

Créés au démarrage si `APP_SEED_DEMO_USERS=true` (défaut en Docker local).

| Rôle | E-mail | Mot de passe | Accès |
|------|--------|--------------|--------|
| **Admin** | `admin@erdv.com` | `admin123` | Dashboard admin, tous les RDV |
| **Utilisateur** | `user@erdv.com` | `user123` | Réservation, mes RDV |
| **Prestataire** | `martin@erdv.com` | `prestataire123` | Espace prestataire (Dr. Martin) |
| **Prestataire** | `dubois@erdv.com` | `prestataire123` | Espace prestataire |
| **Prestataire** | `laurent@erdv.com` | `prestataire123` | Espace prestataire |

---

## Rôles

- **USER** — Réserver, voir/annuler/reprogrammer ses RDV (annulation client ≥ 24 h avant, configurable).
- **PRESTATAIRE** — Compte lié à une fiche prestataire : agenda, confirmation/refus, catalogue.
- **ADMIN** — Gestion globale (prestataires, créneaux, tous les RDV).

---

## Développement local

### Backend

```powershell
cd backend
mvn spring-boot:run
# Profil dev par défaut — port 8080, context-path /api → http://localhost:8080/api
```

### Frontend

```powershell
cd frontend
npm install
# Créer frontend/.env.local si besoin :
# REACT_APP_API_URL=http://localhost:8084/api
npm start
# Port 3001 (voir package.json)
```

### Tests backend

```powershell
cd backend
mvn test
```

---

## Configuration (.env)

Variables principales (voir `.env.example`) :

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | Clé HS256 (≥ 32 caractères) — obligatoire |
| `POSTGRES_PASSWORD` | Mot de passe PostgreSQL |
| `POSTGRES_HOST_PORT` | Port hôte PostgreSQL (défaut `5436`) |
| `APP_SEED_DEMO_USERS` | `true` en local, `false` en prod réelle |
| `APP_FRONTEND_BASE_URL` | Liens dans les e-mails (défaut `http://localhost:3001`) |
| `SPRING_MAIL_*` | SMTP pour envoi réel des e-mails |
| `APP_REMINDERS_ENABLED` | Rappels automatiques J-1 / H-2 |
| `APP_RDV_DELAI_ANNULATION_HEURES` | Délai min. avant annulation client (défaut `24`) |
| `APP_OPENAPI_ENABLED` | Swagger UI (`true` en dev/Docker local) |
| `APP_AUTH_RATE_LIMIT_MAX` | Tentatives auth max. par IP / fenêtre (défaut `20`) |
| `APP_AUTH_RATE_LIMIT_WINDOW` | Fenêtre rate limit en secondes (défaut `60`) |
| `APP_AUTH_REFRESH_TOKEN_PURGE_ENABLED` | Purge nocturne des refresh tokens (défaut `true`) |
| `APP_AUTH_REFRESH_TOKEN_PURGE_CRON` | Cron Spring de la purge (défaut `0 0 3 * * *`) |
| `APP_PAYMENT_ENABLED` | Paiement Stripe (`false` par défaut) |
| `STRIPE_SECRET_KEY` | Clé secrète Stripe (`sk_test_…`) |
| `STRIPE_PUBLISHABLE_KEY` | Clé publique (`pk_test_…`) |
| `STRIPE_WEBHOOK_SECRET` | Secret webhook (`whsec_…`, optionnel en local) |
| `APP_SMS_ENABLED` | Rappels SMS Twilio (`false` par défaut) |
| `TWILIO_ACCOUNT_SID` | SID compte Twilio (`AC…`) |
| `TWILIO_AUTH_TOKEN` | Token auth Twilio |
| `TWILIO_FROM_NUMBER` | Numéro expéditeur E.164 (`+33…`) |
| **Logs JSON** | Profil `prod` seul → JSON Logstash ; ajouter `plain-logs` pour texte (Docker local) |

### Paiement en ligne (Stripe)

1. Créer un compte [Stripe](https://stripe.com) et récupérer les clés **test**.
2. Dans `.env` : `APP_PAYMENT_ENABLED=true`, renseigner `STRIPE_SECRET_KEY` et `STRIPE_PUBLISHABLE_KEY`.
3. Redémarrer : `docker compose up -d --build`.
4. Réserver une prestation **avec prix** → formulaire Stripe à l’étape confirmation.
5. Carte test : `4242 4242 4242 4242`, date future, CVC quelconque.

Webhook (prod ou tests avancés) :

```powershell
stripe listen --forward-to localhost:8084/api/payments/webhook
```

Copier le `whsec_…` affiché dans `STRIPE_WEBHOOK_SECRET`.

Sans paiement activé, les prestations avec prix restent réservables sans encaissement (comportement actuel).

### Rappels SMS (Twilio)

Les rappels J-1 et H-2 sont déjà envoyés par **e-mail** si `APP_REMINDERS_ENABLED=true`. Avec Twilio, un **SMS complémentaire** part vers le téléphone du compte utilisateur (champ profil).

1. Créer un compte [Twilio](https://www.twilio.com) et un numéro SMS (ou utiliser le numéro d’essai).
2. Dans `.env` :
   - `APP_SMS_ENABLED=true`
   - `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER` (format E.164, ex. `+33123456789`)
3. Redémarrer : `docker compose up -d --build`.
4. Vérifier que l’utilisateur a un **numéro mobile valide** dans son profil (06… normalisé en +33…) et a **activé le consentement SMS** dans Mon profil.

Sans SMS activé, seuls les e-mails de rappel sont envoyés. Sans consentement explicite de l’utilisateur, aucun SMS n’est envoyé (RGPD). Un numéro absent ou invalide n’empêche pas l’e-mail et ne marque pas le SMS comme envoyé.

### Scripts utiles

```powershell
.\scripts\generate-docker-env.ps1   # JWT_SECRET dans .env
.\scripts\backup-postgres.ps1       # Sauvegarde PostgreSQL
.\scripts\reset-docker-volumes.ps1  # Reset volumes (données effacées)
```

---

## Architecture

```
frontend/          React + Tailwind, api/client.js, SlotCalendar
backend/           Spring Boot, Flyway (db/migration), JWT
docker-compose.yml postgres + backend + frontend
scripts/           Docker, backup, env
```

### Migrations Flyway

Schéma versionné (`V1` … `V10`) : auth, reset mot de passe, prestations, plages récurrentes, lien prestataire/utilisateur, créneaux multiples, rappels e-mail, refresh tokens révocables.

---

## API (aperçu)

Préfixe : `/api`

| Zone | Exemples |
|------|----------|
| Auth | `POST /auth/login`, `/auth/register`, `/auth/refresh`, `/auth/logout`, `/auth/logout-all`, `/auth/forgot-password` |
| Profil | `GET/PUT /users/me`, `PUT /users/me/password` |
| Prestataires | `GET /prestataires` |
| Prestations / plages | `GET /prestations/prestataire/{id}`, plages récurrentes |
| Créneaux | `GET /creneaux/prestataire/{id}/disponibles`, `.../disponibles/date?date=&dureeMinutes=` |
| RDV | `POST /rendez-vous`, `GET /rendez-vous/mes-rendez-vous`, `PUT .../confirmer`, `.../annuler`, `.../reprogrammer` |
| Prestataire | `GET /rendez-vous/mon-agenda` |

---

## E-mails

Sans SMTP configuré, les envois échouent silencieusement (logs backend). Pour un envoi réel, renseigner `SPRING_MAIL_USERNAME` et `SPRING_MAIL_PASSWORD` dans `.env`, puis redémarrer le backend.

Types d’e-mails : accusé de réception, confirmation/refus, annulation, notification prestataire, rappels J-1 / H-2, reset mot de passe — templates HTML avec lien vers l’application.

---

## Licence

MIT — voir `LICENSE` si présent.
