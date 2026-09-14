const cron = require('node-cron');
const resolutionService = require('./resolutionService');

/**
 * Planifie le job J+7 (toutes les heures).
 * Désactivable avec DISABLE_CRON=1 (tests / environnement sans cron).
 */
function demarrerCronResolution() {
  if (process.env.DISABLE_CRON === '1') {
    console.log('Cron J+7 désactivé (DISABLE_CRON=1)');
    return null;
  }

  // Chaque heure, à la minute 0.
  const tache = cron.schedule('0 * * * *', async () => {
    try {
      const { updated, ids } = await resolutionService.expirerResolutions();
      if (updated > 0) {
        console.log(`Cron J+7 : ${updated} signalement(s) rouvert(s) — ${ids.join(', ')}`);
      }
    } catch (erreur) {
      console.error('Cron J+7 en échec :', erreur.message);
    }
  });

  console.log('Cron J+7 planifié (chaque heure)');
  return tache;
}

module.exports = { demarrerCronResolution };
