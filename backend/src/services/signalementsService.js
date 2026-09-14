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
};
