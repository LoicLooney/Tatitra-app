export const STATUTS = [
  'EN_ATTENTE_SYNC',
  'ENVOYE',
  'A_VERIFIER',
  'PRIS_EN_CHARGE',
  'REJETE',
  'RESOLUTION_A_CONFIRMER',
  'RESOLU_CONFIRME',
  'REOUVERT_NON_RESOLU',
];

// Les statuts de résolution passent par POST /resolution, pas PATCH.
export const STATUTS_TRIAGE_ADMIN = [
  'ENVOYE',
  'A_VERIFIER',
  'PRIS_EN_CHARGE',
  'REJETE',
];

export const STATUTS_PROPOSITION_RESOLUTION = [
  'PRIS_EN_CHARGE',
  'REOUVERT_NON_RESOLU',
];

export const ROLES_RESOLUTION = {
  CITOYEN: 'CITOYEN',
  ADMIN: 'ADMIN',
};

export const CATEGORIES = ['ROUTE', 'DECHETS', 'ECLAIRAGE', 'DRAINAGE', 'PONT'];

export const LIBELLES_STATUT = {
  EN_ATTENTE_SYNC: 'En attente de synchronisation',
  ENVOYE: 'Envoyé',
  A_VERIFIER: 'À vérifier',
  PRIS_EN_CHARGE: 'Pris en charge',
  REJETE: 'Rejeté',
  RESOLUTION_A_CONFIRMER: 'Résolution à confirmer',
  RESOLU_CONFIRME: 'Résolu',
  REOUVERT_NON_RESOLU: 'Rouvert - non résolu',
};

export const LIBELLES_CATEGORIE = {
  ROUTE: 'Route',
  DECHETS: 'Déchets',
  ECLAIRAGE: 'Éclairage public',
  DRAINAGE: 'Drainage',
  PONT: 'Pont / ouvrage',
};

export function estStatutTriage(statut) {
  return STATUTS_TRIAGE_ADMIN.includes(statut);
}
