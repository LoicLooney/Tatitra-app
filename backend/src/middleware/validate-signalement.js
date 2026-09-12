const {
  CATEGORIES,
  LONGUEUR_DESCRIPTION_MIN,
  LONGUEUR_DESCRIPTION_MAX,
  LATITUDE_MIN,
  LATITUDE_MAX,
  LONGITUDE_MIN,
  LONGITUDE_MAX,
} = require('../constants');

// Le cahier des charges définit clientId comme « UUID/String » : on accepte donc tout
// identifiant opaque raisonnable, pas seulement des caractères hexadécimaux.
const CLIENT_ID_REGEX = /^[A-Za-z0-9_-]{8,64}$/;

/**
 * Valide le corps d'une création de signalement avant tout accès à la base (§11.4).
 * Répond 400 avec la liste complète des champs fautifs plutôt qu'une erreur à la fois.
 */
function validerCreationSignalement(req, res, next) {
  const erreurs = [];
  const corps = req.body || {};

  if (!corps.clientId || !CLIENT_ID_REGEX.test(String(corps.clientId))) {
    erreurs.push('clientId manquant ou invalide');
  }

  if (!CATEGORIES.includes(corps.categorie)) {
    erreurs.push(`categorie inconnue (valeurs acceptées : ${CATEGORIES.join(', ')})`);
  }

  const description = typeof corps.description === 'string' ? corps.description.trim() : '';
  if (description.length < LONGUEUR_DESCRIPTION_MIN) {
    erreurs.push(`description trop courte (minimum ${LONGUEUR_DESCRIPTION_MIN} caractères)`);
  } else if (description.length > LONGUEUR_DESCRIPTION_MAX) {
    erreurs.push(`description trop longue (maximum ${LONGUEUR_DESCRIPTION_MAX} caractères)`);
  }

  // La position est facultative : un signalement créé sans GPS reste recevable (F-CIT-05).
  const latitudeFournie = corps.latitude !== undefined && corps.latitude !== null;
  const longitudeFournie = corps.longitude !== undefined && corps.longitude !== null;

  if (latitudeFournie !== longitudeFournie) {
    erreurs.push('latitude et longitude doivent être fournies ensemble');
  }

  if (latitudeFournie && !estDansIntervalle(corps.latitude, LATITUDE_MIN, LATITUDE_MAX)) {
    erreurs.push(`latitude hors bornes (${LATITUDE_MIN} à ${LATITUDE_MAX})`);
  }

  if (longitudeFournie && !estDansIntervalle(corps.longitude, LONGITUDE_MIN, LONGITUDE_MAX)) {
    erreurs.push(`longitude hors bornes (${LONGITUDE_MIN} à ${LONGITUDE_MAX})`);
  }

  if (corps.isDemo !== undefined && typeof corps.isDemo !== 'boolean') {
    erreurs.push('isDemo doit être un booléen');
  }

  if (corps.photoUrl !== undefined && corps.photoUrl !== null && typeof corps.photoUrl !== 'string') {
    erreurs.push('photoUrl doit être une chaîne de caractères');
  }

  if (erreurs.length > 0) {
    return res.status(400).json({ error: 'Signalement invalide', details: erreurs });
  }

  // Le corps normalisé remplace l'entrée brute : le reste de la chaîne ne relit plus req.body.
  req.signalementValide = {
    clientId: String(corps.clientId),
    categorie: corps.categorie,
    description,
    latitude: latitudeFournie ? Number(corps.latitude) : null,
    longitude: longitudeFournie ? Number(corps.longitude) : null,
    photoUrl: corps.photoUrl || null,
    isDemo: corps.isDemo === undefined ? true : corps.isDemo,
  };

  return next();
}

function estDansIntervalle(valeur, minimum, maximum) {
  const nombre = Number(valeur);
  return Number.isFinite(nombre) && nombre >= minimum && nombre <= maximum;
}

module.exports = { validerCreationSignalement };
