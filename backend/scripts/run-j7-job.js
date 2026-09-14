require('dotenv').config();
const resolutionService = require('../src/services/resolutionService');

/**
 * Exécution manuelle du job J+7 (livrable J5).
 * Usage : npm run job:j7
 */
(async () => {
  try {
    const resultat = await resolutionService.expirerResolutions();
    console.log(JSON.stringify(resultat, null, 2));
    process.exit(0);
  } catch (erreur) {
    console.error(erreur.message);
    process.exit(1);
  }
})();
