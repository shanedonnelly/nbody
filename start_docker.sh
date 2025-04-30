#!/bin/bash

# Nom du conteneur
CONTAINER_NAME="nbody-jvm"

echo "🔧 Build de l'image Docker..."
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/$CONTAINER_NAME .

echo "🚀 Lancement du conteneur..."
docker run -d --rm -p 8080:8080 --name $CONTAINER_NAME quarkus/$CONTAINER_NAME
echo "🌐 Accédez à l'application à l'adresse : http://localhost:8080"