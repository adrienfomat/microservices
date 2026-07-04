-- Execute une seule fois, a la premiere initialisation du volume Postgres.
-- Le role "motus" existe deja (POSTGRES_USER), on cree juste les 3 bases.
CREATE DATABASE motus_joueur OWNER motus;
CREATE DATABASE motus_partie OWNER motus;
CREATE DATABASE motus_score  OWNER motus;
