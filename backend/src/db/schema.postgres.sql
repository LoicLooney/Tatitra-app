-- Schéma PostgreSQL / Supabase (cahier des charges LALANA §15.3)
-- À exécuter aussi via : node src/db/init.js  OU  SQL Editor Supabase

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS signalements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  client_id UUID UNIQUE NOT NULL,
  categorie VARCHAR(40) NOT NULL,
  description TEXT NOT NULL,
  latitude NUMERIC(10, 7),
  longitude NUMERIC(10, 7),
  photo_url TEXT,
  statut VARCHAR(30) NOT NULL DEFAULT 'ENVOYE',
  resolution_proposee_par VARCHAR(20),
  resolution_proposee_le TIMESTAMPTZ,
  date_limite_confirmation TIMESTAMPTZ,
  resolution_confirmee_le TIMESTAMPTZ,
  date_reouverture TIMESTAMPTZ,
  motif_reouverture TEXT,
  is_demo BOOLEAN NOT NULL DEFAULT FALSE,
  date_creation TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  date_modification TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_signalements_date_creation
  ON signalements (date_creation DESC);
