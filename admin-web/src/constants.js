// Vocabulaire métier partagé avec le mobile et le backend (§2.6 des règles de code).

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
