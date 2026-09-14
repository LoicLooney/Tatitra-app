const db = require('../db');

exports.lister = async () => {
  const result = await db.query(
    `SELECT
       id,
       client_id,
       categorie,
       description,
       latitude,
       longitude,
       photo_url,
       statut,
       resolution_proposee_par,
       resolution_proposee_le,
       date_limite_confirmation,
       resolution_confirmee_le,
       date_reouverture,
       motif_reouverture,
       is_demo,
       date_creation,
       date_modification
     FROM signalements
     ORDER BY date_creation DESC`
  );
  return result.rows;
};
const crypto = require('crypto');
const { STATUT_PAR_DEFAUT } = require('../constants');

// Mémoire temporaire — remplacé plus tard par PostgreSQL / Supabase (src/db).
const signalements = [];

// Index clientId -> signalement : support de l'idempotence, remplacé côté base
// par la contrainte UNIQUE sur signalements.client_id.
const parClientId = new Map();

exports.lister = async () => signalements;

exports.recupererParClientId = async (clientId) => parClientId.get(clientId) || null;

/**
 * Crée un signalement à partir d'un corps déjà validé.
 *
 * Idempotence : une synchronisation rejouée (perte de réseau après l'envoi, reprise
 * du WorkManager) renvoie le signalement existant au lieu d'en créer un doublon.
 * `cree` indique au contrôleur s'il doit répondre 201 ou 200.
 */
exports.creer = async (payload) => {
  const {
    client_id,
    categorie,
    description,
    latitude = null,
    longitude = null,
    photo_url = null,
    statut = 'ENVOYE',
    is_demo = false,
  } = payload;

  if (!client_id || !categorie || !description) {
    throw new Error('client_id, categorie et description sont obligatoires');
  }

  const result = await db.query(
    `INSERT INTO signalements
       (client_id, categorie, description, latitude, longitude, photo_url, statut, is_demo)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
     RETURNING *`,
    [client_id, categorie, description, latitude, longitude, photo_url, statut, is_demo]
  );
  return result.rows[0];
  const existant = parClientId.get(payload.clientId);
  if (existant) {
    return { signalement: existant, cree: false };
  }

  const signalement = {
    id: genererIdentifiant(),
    clientId: payload.clientId,
    categorie: payload.categorie,
    description: payload.description,
    latitude: payload.latitude,
    longitude: payload.longitude,
    photoUrl: payload.photoUrl,
    statut: STATUT_PAR_DEFAUT,
    isDemo: payload.isDemo,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  signalements.push(signalement);
  parClientId.set(signalement.clientId, signalement);
  return { signalement, cree: true };
};

/** Identifiant lisible, du même format que celui prévu côté base (SIG-000125). */
function genererIdentifiant() {
  const numero = String(signalements.length + 1).padStart(6, '0');
  const suffixe = crypto.randomBytes(2).toString('hex');
  return `SIG-${numero}-${suffixe}`;
}
