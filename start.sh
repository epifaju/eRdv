#!/bin/bash

echo "🚀 Démarrage de l'application eRDV..."

# Vérifier si Docker est installé
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé. Veuillez installer Docker d'abord."
    exit 1
fi

# Vérifier si Docker Compose est installé
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose n'est pas installé. Veuillez installer Docker Compose d'abord."
    exit 1
fi

echo "📦 Construction et démarrage des conteneurs..."
docker-compose up --build -d

echo "⏳ Attente du démarrage des services..."
sleep 30

echo "✅ Application démarrée avec succès !"
echo ""
echo "🌐 Accès à l'application :"
echo "   Frontend : http://localhost:3001"
echo "   Backend API : http://localhost:8084"
echo "   Base de données : localhost:5432"
echo ""
echo "📝 Pour arrêter l'application : docker-compose down"
echo "📝 Pour voir les logs : docker-compose logs -f" 