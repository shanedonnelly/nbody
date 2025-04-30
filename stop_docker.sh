#!/bin/bash

# Nom du conteneur
CONTAINER_NAME="nbody-jvm"

echo "🛑 Arrêt du conteneur..."
docker stop $CONTAINER_NAME 2>/dev/null || echo "Conteneur déjà arrêté ou inexistant."

echo "🧹 Nettoyage des images associées..."
docker rmi quarkus/$CONTAINER_NAME
