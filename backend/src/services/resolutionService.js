const db = require('../db');
const {
  DELAI_CONFIRMATION_JOURS,
  STATUTS_PROPOSITION_RESOLUTION,
} = require('../constants');
const { ErreurApi } = require('../middleware/errorHandler');
const { versApi } = require('./signalementsService');

const SELECT_COMPLET = `
  SELECT
    id, client_id, categorie, description, latitude, longitude, photo_url, statut,
    resolution_proposee_par, resolution_proposee_le, date_limite_confirmation,
    resolution_confirmee_le, date_reouverture, motif_reouverture,
    is_demo, date_creation, date_modification
  FROM signalements
`;

async function chargerParId(id) {
  const result = await db.query(`${SELECT_COMPLET} WHERE id = $1`, [id]);
  return result.rows[0] || null;
}

/**
 * Propose une résolution (citoyen ou admin) → RESOLUTION_A_CONFIRMER + échéance J+7.
 */
exports.proposer = async (id, role) => {
  const row = await chargerParId(id);
  if (!row) {
    throw new ErreurApi(404, `Signalement introuvable : ${id}`);
  }
  if (!STATUTS_PROPOSITION_RESOLUTION.includes(row.statut)) {
    throw new ErreurApi(
      409,
      `Proposition impossible depuis le statut ${row.statut} ` +
        `(attendu : ${STATUTS_PROPOSITION_RESOLUTION.join(' ou ')})`
    );
  }

  const result = await db.query(
    `UPDATE signalements
        SET statut = 'RESOLUTION_A_CONFIRMER',
            resolution_proposee_par = $1,
            resolution_proposee_le = NOW(),
            date_limite_confirmation = NOW() + ($2::int * INTERVAL '1 day'),
            resolution_confirmee_le = NULL,
            date_modification = NOW()
      WHERE id = $3
      RETURNING *`,
    [role, DELAI_CONFIRMATION_JOURS, id]
  );
  return versApi(result.rows[0]);
};

/**
 * Confirme la résolution proposée par l'autre partie → RESOLU_CONFIRME.
 */
exports.confirmer = async (id, role) => {
  const row = await chargerParId(id);
  if (!row) {
    throw new ErreurApi(404, `Signalement introuvable : ${id}`);
  }
  if (row.statut !== 'RESOLUTION_A_CONFIRMER') {
    throw new ErreurApi(
      409,
      `Confirmation impossible : statut actuel ${row.statut} (attendu RESOLUTION_A_CONFIRMER)`
    );
  }
  if (!row.resolution_proposee_par) {
    throw new ErreurApi(409, 'Aucune proposition de résolution enregistrée');
  }
  if (row.resolution_proposee_par === role) {
    throw new ErreurApi(
      409,
      `Le ${role.toLowerCase()} a déjà proposé : c'est à l'autre partie de confirmer`
    );
  }

  const result = await db.query(
    `UPDATE signalements
        SET statut = 'RESOLU_CONFIRME',
            resolution_confirmee_le = NOW(),
            date_limite_confirmation = NULL,
            date_modification = NOW()
      WHERE id = $1
      RETURNING *`,
    [id]
  );
  return versApi(result.rows[0]);
};

/**
 * Rouvre le dossier (« toujours endommagé » ou refus) → REOUVERT_NON_RESOLU.
 * On conserve le motif ; on efface l'échéance pour ne pas relancer le cron à tort.
 */
exports.rouvrir = async (id, motif) => {
  const row = await chargerParId(id);
  if (!row) {
    throw new ErreurApi(404, `Signalement introuvable : ${id}`);
  }
  if (row.statut !== 'RESOLUTION_A_CONFIRMER') {
    throw new ErreurApi(
      409,
      `Réouverture impossible : statut actuel ${row.statut} (attendu RESOLUTION_A_CONFIRMER)`
    );
  }

  const motifFinal = (motif && String(motif).trim()) || 'Toujours endommagé';

  const result = await db.query(
    `UPDATE signalements
        SET statut = 'REOUVERT_NON_RESOLU',
            date_reouverture = NOW(),
            motif_reouverture = $1,
            date_limite_confirmation = NULL,
            date_modification = NOW()
      WHERE id = $2
      RETURNING *`,
    [motifFinal, id]
  );
  return versApi(result.rows[0]);
};

/**
 * Règle J+7 : sans confirmation avant date_limite_confirmation → réouverture automatique.
 * @returns {{ updated: number, ids: string[] }}
 */
exports.expirerResolutions = async () => {
  const result = await db.query(
    `UPDATE signalements
        SET statut = 'REOUVERT_NON_RESOLU',
            date_reouverture = NOW(),
            motif_reouverture = 'Absence de confirmation sous 7 jours',
            date_limite_confirmation = NULL,
            date_modification = NOW()
      WHERE statut = 'RESOLUTION_A_CONFIRMER'
        AND date_limite_confirmation IS NOT NULL
        AND date_limite_confirmation < NOW()
      RETURNING id`
  );
  const ids = result.rows.map((r) => r.id);
  return { updated: ids.length, ids };
};
