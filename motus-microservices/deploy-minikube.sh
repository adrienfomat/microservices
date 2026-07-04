#!/usr/bin/env bash
# Lance tout Motus sur Minikube en une commande : bash deploy-minikube.sh
set -e

echo "==> 1/5 Demarrage de Minikube (si necessaire)"
minikube status >/dev/null 2>&1 || minikube start --driver=docker --memory=4096 --cpus=4

echo "==> 2/5 Construction des 4 images dans Minikube (plusieurs minutes la 1re fois)"
eval "$(minikube docker-env)"
docker build -f joueur-service/Dockerfile   -t motus/joueur-service:1.0 .
docker build -f score-service/Dockerfile    -t motus/score-service:1.0 .
docker build -f partie-service/Dockerfile   -t motus/partie-service:1.0 .
docker build -f frontend-service/Dockerfile -t motus/frontend-service:1.0 .

echo "==> 3/5 Deploiement des manifestes"
kubectl apply -f k8s/

echo "==> 4/5 Attente que les services soient prets"
kubectl wait --for=condition=available --timeout=300s \
  deploy/postgres deploy/joueur-service deploy/score-service \
  deploy/partie-service deploy/frontend-service || true

echo "==> 5/5 Ouverture des tunnels -> http://localhost:8080  (Ctrl+C pour arreter)"
kubectl port-forward svc/frontend-service 8080:8080 &
kubectl port-forward svc/joueur-service   8081:8081 &
kubectl port-forward svc/partie-service   8082:8082 &
kubectl port-forward svc/score-service    8083:8083 &
wait
