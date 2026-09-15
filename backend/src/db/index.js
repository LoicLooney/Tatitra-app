const fs = require('node:fs');
const { Pool } = require('pg');

const CHAINE_CONNEXION = process.env.DATABASE_URL || '';

if (!CHAINE_CONNEXION) {
  console.warn('[db] DATABASE_URL manquant — vérifie backend/.env');
}

/**
 * Choisit la configuration TLS de la connexion PostgreSQL.
 *
 * - Base locale (ou sslmode=disable) : pas de TLS. Sans ce cas, un PostgreSQL local
 *   refuse la connexion avec « The server does not support SSL connections ».
 * - DATABASE_CA_CERT fourni : TLS **vérifié** avec ce certificat d'autorité.
 *   Supabase publie le sien (Project Settings > Database > SSL configuration).
 * - Sinon : TLS chiffré mais non vérifié. La chaîne Supabase est auto-signée, donc la
 *   vérification standard échoue ; on accepte ce repli pour la démonstration, en le
 *   signalant, car il n'empêche pas une interception active.
 */
function optionsSsl(url, env = process.env) {
  const estLocale = /@(localhost|127\.0\.0\.1|\[::1\])/i.test(url);
  const sslDesactive = /[?&]sslmode=disable/i.test(url);
  if (estLocale || sslDesactive) return false;

  const cheminCa = env.DATABASE_CA_CERT;
  if (cheminCa) {
    return { ca: fs.readFileSync(cheminCa, 'utf8'), rejectUnauthorized: true };
  }

  return { rejectUnauthorized: false, verificationDesactivee: true };
}

const configurationSsl = optionsSsl(CHAINE_CONNEXION);

if (configurationSsl && configurationSsl.verificationDesactivee) {
  console.warn(
    '[db] TLS actif mais non vérifié (certificat Supabase auto-signé). ' +
      'Pour le vérifier : télécharger le certificat et définir DATABASE_CA_CERT.'
  );
  delete configurationSsl.verificationDesactivee;
}

const pool = new Pool({
  connectionString: CHAINE_CONNEXION,
  ssl: configurationSsl,
});

async function query(text, params) {
  return pool.query(text, params);
}

async function healthCheck() {
  const result = await pool.query('SELECT 1 AS ok');
  return result.rows[0]?.ok === 1;
}

module.exports = {
  pool,
  query,
  healthCheck,
  optionsSsl,
};
