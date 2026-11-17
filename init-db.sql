-- Script manuel d'initialisation de la base PostgreSQL pour Yow Gifter
-- À exécuter sous psql ou pgAdmin en tant qu'utilisateur postgres si Docker n'est pas utilisé :

CREATE DATABASE yow_gifter_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'French_Cameroon.1252'
    LC_CTYPE = 'French_Cameroon.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE yow_gifter_db
    IS 'Base de données officielle Yow Gifter (ENSPY / UY1 4GI - UE Supervision Réseaux)';
