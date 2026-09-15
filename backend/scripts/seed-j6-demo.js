require('dotenv').config();
const { Client } = require('pg');

/** Seed J6 — Usage : npm run seed:j6 */
const DEMOS = [
  {
    clientId: 'a6a00001-aaaa-4aaa-8aaa-aaaaaaaa0001',
    categorie: 'ROUTE',
    description: 'J6 — Nid-de-poule (ENVOYE, à trier)',
    statut: 'ENVOYE',
    proposePar: null,
    limiteDansJours: null,
    motif: null,
  },
  {
    clientId: 'a6a00002-aaaa-4aaa-8aaa-aaaaaaaa0002',
    categorie: 'ECLAIRAGE',
    description: 'J6 — Lampadaire HS (PRIS_EN_CHARGE, prêt à proposer)',
    statut: 'PRIS_EN_CHARGE',
    proposePar: null,
    limiteDansJours: null,
    motif: null,
  },
  {
    clientId: 'a6a00003-aaaa-4aaa-8aaa-aaaaaaaa0003',
    categorie: 'DECHETS',
    description: 'J6 — Dépôt sauvage (ADMIN a proposé → confirmer sur mobile)',
    statut: 'RESOLUTION_A_CONFIRMER',
    proposePar: 'ADMIN',
    limiteDansJours: 7,
    motif: null,
  },
  {
    clientId: 'a6a00004-aaaa-4aaa-8aaa-aaaaaaaa0004',
    categorie: 'DRAINAGE',
    description: 'J6 — Caniveau bouché (CITOYEN a proposé → confirmer/refuser admin)',
    statut: 'RESOLUTION_A_CONFIRMER',
    proposePar: 'CITOYEN',
    limiteDansJours: 7,
    motif: null,
  },
  {
    clientId: 'a6a00005-aaaa-4aaa-8aaa-aaaaaaaa0005',
    categorie: 'PONT',
    description: 'J6 — Garde-corps endommagé (REOUVERT — affichage filtre)',
    statut: 'REOUVERT_NON_RESOLU',
    proposePar: null,
    limiteDansJours: null,
    motif: 'Toujours endommagé (démo J6)',
  },
  {
    clientId: 'a6a00006-aaaa-4aaa-8aaa-aaaaaaaa0006',
    categorie: 'ROUTE',
    description: 'J6 — Fissure (échéance passée → job J+7)',
    statut: 'RESOLUTION_A_CONFIRMER',
    proposePar: 'ADMIN',
    limiteDansJours: -1,
    motif: null,
  },
];

(async () => {
  const c = new Client({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false },
  });
  await c.connect();

  console.log('Seed J6 — scénario résolution…\n');

  for (const demo of DEMOS) {
    const proposeLe = demo.proposePar ? new Date() : null;
    const limite =
      demo.limiteDansJours == null
        ? null
        : new Date(Date.now() + demo.limiteDansJours * 24 * 60 * 60 * 1000);
    const dateReouverture = demo.statut === 'REOUVERT_NON_RESOLU' ? new Date() : null;

    const result = await c.query(
      `INSERT INTO signalements (
         client_id, categorie, description, latitude, longitude, photo_url, statut, is_demo,
         resolution_proposee_par, resolution_proposee_le, date_limite_confirmation,
         date_reouverture, motif_reouverture
       ) VALUES (
         $1, $2, $3, -18.8792, 47.5079, null, $4, true,
         $5, $6, $7, $8, $9
       )
       ON CONFLICT (client_id) DO UPDATE SET
         categorie = EXCLUDED.categorie,
         description = EXCLUDED.description,
         statut = EXCLUDED.statut,
         resolution_proposee_par = EXCLUDED.resolution_proposee_par,
         resolution_proposee_le = EXCLUDED.resolution_proposee_le,
         date_limite_confirmation = EXCLUDED.date_limite_confirmation,
         date_reouverture = EXCLUDED.date_reouverture,
         motif_reouverture = EXCLUDED.motif_reouverture,
         date_modification = NOW()
       RETURNING id, statut, client_id`,
      [
        demo.clientId,
        demo.categorie,
        demo.description,
        demo.statut,
        demo.proposePar,
        proposeLe,
        limite,
        dateReouverture,
        demo.motif,
      ]
    );
    const row = result.rows[0];
    console.log(`• ${String(row.statut).padEnd(24)} ${row.id}`);
    console.log(`  ${demo.description}`);
  }

  await c.end();
  console.log('\nOK. Relancez admin-web et sync mobile pour voir les données.');
  console.log('Job J+7 sur échéance passée : npm run job:j7');
})().catch((e) => {
  console.error(e.message);
  process.exit(1);
});
