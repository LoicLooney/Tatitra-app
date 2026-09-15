require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { pool } = require('./index');

async function init() {
  const sqlPath = path.join(__dirname, 'schema.postgres.sql');
  const sql = fs.readFileSync(sqlPath, 'utf8');
  await pool.query(sql);
  console.log('Table signalements prête (CREATE IF NOT EXISTS).');
  await pool.end();
}

init().catch(async (err) => {
  console.error('Échec init DB:', err.message);
  try { await pool.end(); } catch (_) { /* ignore */ }
  process.exit(1);
});
