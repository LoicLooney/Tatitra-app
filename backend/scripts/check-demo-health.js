require('dotenv').config();

/**
 * Vérifie GET /health en conditions de démo (J7).
 * Usage : npm run health  ou  node scripts/check-demo-health.js [url]
 */
const base = (process.argv[2] || process.env.PUBLIC_BASE_URL || `http://127.0.0.1:${process.env.PORT || 3000}`)
  .replace(/\/$/, '');

(async () => {
  const url = `${base}/health`;
  console.log(`Vérification ${url} …`);
  try {
    const reponse = await fetch(url);
    const corps = await reponse.json();
    console.log(JSON.stringify(corps, null, 2));
    if (!reponse.ok || corps.status !== 'ok') {
      process.exitCode = 1;
      console.error('Échec : le backend n’est pas prêt pour la démo.');
      return;
    }
    console.log('OK — backend accessible pour la démo.');
  } catch (erreur) {
    process.exitCode = 1;
    console.error(`Injoignable : ${erreur.message}`);
    console.error('Lancez le backend (npm run dev) et vérifiez le pare-feu / adb reverse.');
  }
})();
