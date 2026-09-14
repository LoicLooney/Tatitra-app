// Vocabulaire métier partagé mobile / backend / admin (§2.6 des règles de code).
// Toute nouvelle valeur doit être ajoutée des deux côtés le même jour.

const CATEGORIES = ['ROUTE', 'DECHETS', 'ECLAIRAGE', 'DRAINAGE', 'PONT'];

const STATUTS = [
  'EN_ATTENTE_SYNC',
  'ENVOYE',
  'A_VERIFIER',
  'PRIS_EN_CHARGE',
  'REJETE',
  'RESOLUTION_A_CONFIRMER',
  'RESOLU_CONFIRME',
  'REOUVERT_NON_RESOLU',
];

const STATUT_PAR_DEFAUT = 'ENVOYE';

/** Rôles pour la double validation de résolution (J5). */
const ROLES_RESOLUTION = ['CITOYEN', 'ADMIN'];

/** Statuts depuis lesquels on peut proposer une résolution. */
const STATUTS_PROPOSITION_RESOLUTION = ['PRIS_EN_CHARGE', 'REOUVERT_NON_RESOLU'];

/**
 * Statuts modifiables via PATCH libre (triage admin).
 * Les statuts de résolution passent uniquement par /resolution (évite un bypass).
 */
const STATUTS_TRIAGE_ADMIN = ['ENVOYE', 'A_VERIFIER', 'PRIS_EN_CHARGE', 'REJETE'];

const DELAI_CONFIRMATION_JOURS = 7;

// Mêmes bornes que mg.itu.tatitra_app.domain.ReglesSignalement côté Android.
const LONGUEUR_DESCRIPTION_MIN = 10;
const LONGUEUR_DESCRIPTION_MAX = 500;
const LATITUDE_MIN = -90;
const LATITUDE_MAX = 90;
const LONGITUDE_MIN = -180;
const LONGITUDE_MAX = 180;

const MAX_UPLOAD_SIZE_MB = Number(process.env.MAX_UPLOAD_SIZE_MB || 5);
const TYPES_IMAGE_ACCEPTES = ['image/jpeg', 'image/png', 'image/webp'];

module.exports = {
  CATEGORIES,
  STATUTS,
  STATUT_PAR_DEFAUT,
  ROLES_RESOLUTION,
  STATUTS_PROPOSITION_RESOLUTION,
  STATUTS_TRIAGE_ADMIN,
  DELAI_CONFIRMATION_JOURS,
  LONGUEUR_DESCRIPTION_MIN,
  LONGUEUR_DESCRIPTION_MAX,
  LATITUDE_MIN,
  LATITUDE_MAX,
  LONGITUDE_MIN,
  LONGITUDE_MAX,
  MAX_UPLOAD_SIZE_MB,
  TYPES_IMAGE_ACCEPTES,
};
