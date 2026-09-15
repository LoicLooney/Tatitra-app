const { ROLES_RESOLUTION } = require('../constants');

/**
 * Extrait et valide le rôle (corps JSON ou en-tête X-Tatitra-Role).
 *
 * Pourquoi une clé admin : le cahier interdit de faire confiance au rôle client
 * sans contrôle. En démo, ADMIN exige TATITRA_ADMIN_KEY (header X-Tatitra-Admin-Key)
 * si la variable d'environnement est définie ; sinon mode permissif (salle de TP).
 */
function validerRoleResolution(req, res, next) {
  const depuisCorps = req.body && req.body.role;
  const depuisEntete = req.get('X-Tatitra-Role');
  const role = String(depuisCorps || depuisEntete || '')
    .trim()
    .toUpperCase();

  if (!ROLES_RESOLUTION.includes(role)) {
    return res.status(400).json({
      error: 'Rôle de résolution invalide',
      details: [`role manquant ou inconnu (valeurs : ${ROLES_RESOLUTION.join(', ')})`],
    });
  }

  const cleAttendue = process.env.TATITRA_ADMIN_KEY;
  if (role === 'ADMIN' && cleAttendue) {
    const cleFournie = req.get('X-Tatitra-Admin-Key') || '';
    if (cleFournie !== cleAttendue) {
      return res.status(403).json({
        error: 'Rôle ADMIN refusé',
        details: ['X-Tatitra-Admin-Key manquante ou incorrecte'],
      });
    }
  }

  req.roleResolution = role;
  return next();
}

/**
 * Motif optionnel pour POST …/resolution/reopen.
 */
function validerMotifReouverture(req, res, next) {
  const motif = req.body && req.body.motif;
  if (motif !== undefined && motif !== null && typeof motif !== 'string') {
    return res.status(400).json({
      error: 'Réouverture invalide',
      details: ['motif doit être une chaîne de caractères'],
    });
  }
  req.motifReouverture =
    typeof motif === 'string' && motif.trim() ? motif.trim() : 'Toujours endommagé';
  return next();
}

/**
 * Réserve un endpoint au rôle ADMIN (job de maintenance J+7).
 *
 * Pourquoi : ce job change l'état de dossiers en base. Sans contrôle, n'importe qui
 * pouvait le déclencher par un simple POST. Le niveau d'exigence suit la même règle que
 * les autres actions ADMIN : clé obligatoire si TATITRA_ADMIN_KEY est définie.
 */
function exigerRoleAdmin(req, res, next) {
  return validerRoleResolution(req, res, () => {
    if (req.roleResolution !== 'ADMIN') {
      return res.status(403).json({
        error: 'Action réservée à l’administration',
        details: [`rôle ${req.roleResolution} insuffisant pour cette opération`],
      });
    }
    return next();
  });
}

module.exports = {
  validerRoleResolution,
  validerMotifReouverture,
  exigerRoleAdmin,
};
