const db = require('../db');
const { STATUT_PAR_DEFAUT } = require('../constants');
const { ErreurApi } = require('../middleware/errorHandler');

function versApi(row) {
  if (!row) return null;
  return {
    id: row.id,
    clientId: row.client_id,
    categorie: row.categorie,
    description: row.description,
    latitude: row.latitude !== null ? Number(row.latitude) : null,
    longitude: row.longitude !== null ? Number(row.longitude) : null,
    photoUrl: row.photo_url,
    statut: row.statut,
    isDemo: row.is_demo,
    createdAt: row.date_creation,
    updatedAt: row.date_modification,
    resolutionProposeePar: row.resolution_proposee_par || null,
    resolutionProposeeLe: row.resolution_proposee_le || null,
    dateLimiteConfirmation: row.date_limite_confirmation || null,
    resolutionConfirmeeLe: row.resolution_confirmee_le || null,
    dateReouverture: row.date_reouverture || null,
    motifReouverture: row.motif_reouverture || null,
  };
}

exports.versApi = versApi;

const SELECT_COMPLET = `
  SELECT
    id, client_id, categorie, description, latitude, longitude, photo_url, statut,
    resolution_proposee_par, resolution_proposee_le, date_limite_confirmation,
    resolution_confirmee_le, date_reouverture, motif_reouverture,
    is_demo, date_creation, date_modification
  FROM signalements
`;

exports.lister = async () => {
  const result = await db.query(`${SELECT_COMPLET} ORDER BY date_creation DESC`);
  return result.rows.map(versApi);
};

exports.recupererParId = async (id) => {
  const result = await db.query(`${SELECT_COMPLET} WHERE id = $1`, [id]);
  const signalement = versApi(result.rows[0]);
  if (!signalement) {
    throw new ErreurApi(404, `Signalement introuvable : ${id}`);
  }
  return signalement;
};

exports.creer = async (payload) => {
  // ON CONFLICT : évite 23505 si deux syncs concurrentes créent le même client_id.
  const result = await db.query(
    `INSERT INTO signalements
       (client_id, categorie, description, latitude, longitude, photo_url, statut, is_demo)
     VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
     ON CONFLICT (client_id) DO NOTHING
     RETURNING *`,
    [
      payload.clientId,
      payload.categorie,
      payload.description,
      payload.latitude,
      payload.longitude,
      payload.photoUrl,
      STATUT_PAR_DEFAUT,
      payload.isDemo === true,
    ]
  );

  if (result.rows[0]) {
    return { signalement: versApi(result.rows[0]), cree: true };
  }

  const existant = await db.query(`${SELECT_COMPLET} WHERE client_id = $1`, [
    payload.clientId,
  ]);
  if (!existant.rows[0]) {
    throw new ErreurApi(500, 'Création conflictuelle sans ligne existante');
  }
  return { signalement: versApi(existant.rows[0]), cree: false };
};

exports.mettreAJourStatut = async (id, statut) => {
  const result = await db.query(
    `UPDATE signalements
        SET statut = $1, date_modification = NOW()
      WHERE id = $2
      RETURNING *`,
    [statut, id]
  );
  const signalement = versApi(result.rows[0]);
  if (!signalement) {
    throw new ErreurApi(404, `Signalement introuvable : ${id}`);
  }
  return signalement;
};
