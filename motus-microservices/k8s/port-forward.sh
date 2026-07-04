#!/bin/sh
# Ouvre 4 tunnels pour que le navigateur atteigne le front (8080)
# et les 3 backends (8081-8083), comme en local.
kubectl port-forward svc/frontend-service 8080:8080 &
kubectl port-forward svc/joueur-service   8081:8081 &
kubectl port-forward svc/partie-service   8082:8082 &
kubectl port-forward svc/score-service    8083:8083 &
echo "Tunnels ouverts -> ouvre http://localhost:8080"
echo "Pour arreter : pkill -f 'kubectl port-forward'"
wait
