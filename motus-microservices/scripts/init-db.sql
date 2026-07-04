-- Creation du role applicatif et d'UNE base par microservice (database-per-service).
-- A lancer une seule fois en tant que superutilisateur postgres :
--   sudo -u postgres psql -f scripts/init-db.sql

CREATE USER motus WITH PASSWORD 'motus';

CREATE DATABASE motus_joueur OWNER motus;
CREATE DATABASE motus_partie OWNER motus;
CREATE DATABASE motus_score  OWNER motus;
