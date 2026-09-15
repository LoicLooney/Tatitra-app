require('dotenv').config();
const { Client } = require('pg');

(async () => {
  const clientId = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
  const c = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false },
  });
  await c.connect();
  const r = await c.query(
    `INSERT INTO signalements
      (client_id, categorie, description, latitude, longitude, photo_url, statut, is_demo)
     VALUES ($1, $2, $3, $4, $5, null, 'ENVOYE', true)
     ON CONFLICT (client_id) DO UPDATE SET description = EXCLUDED.description
     RETURNING id`,
    [clientId, 'ROUTE', 'Nid-de-poule test J2 detail API', -18.8792, 47.5079]
  );
  console.log(r.rows[0].id);
  await c.end();
})().catch((e) => {
  console.error(e.message);
  process.exit(1);
});
