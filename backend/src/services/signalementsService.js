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
