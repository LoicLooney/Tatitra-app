const test = require('node:test');
const assert = require('node:assert/strict');

const db = require('../src/db');
const signalementsService = require('../src/services/signalementsService');

/** Ligne PostgreSQL telle que la renvoie le pilote pg (NUMERIC arrive en chaîne). */
function ligneBase(surcharges = {}) {
  return {
    id: '11111111-1111-4111-8111-111111111111',
    client_id: '22222222-2222-4222-8222-222222222222',
    categorie: 'ROUTE',
    description: 'Chaussée dégradée',
    latitude: '-18.8792000',
    longitude: '47.5079000',
    photo_url: null,
    statut: 'ENVOYE',
    resolution_proposee_par: null,
    resolution_proposee_le: null,
    date_limite_confirmation: null,
    resolution_confirmee_le: null,
    date_reouverture: null,
    motif_reouverture: null,
    is_demo: true,
    date_creation: '2026-09-11T14:30:00.000Z',
    date_modification: '2026-09-11T14:30:00.000Z',
    ...surcharges,
  };
}

test.afterEach(() => test.mock.restoreAll());

test('versApi : les NUMERIC de PostgreSQL redeviennent des nombres', () => {
  const api = signalementsService.versApi(ligneBase());

  assert.equal(typeof api.latitude, 'number');
  assert.equal(api.latitude, -18.8792);
  assert.equal(api.longitude, 47.5079);
});

test('versApi : une position absente reste null, pas 0', () => {
  const api = signalementsService.versApi(ligneBase({ latitude: null, longitude: null }));

  assert.equal(api.latitude, null);
  assert.equal(api.longitude, null);
});

test('versApi : renvoie null pour une ligne absente', () => {
  assert.equal(signalementsService.versApi(undefined), null);
});

test('créer : un nouveau clientId est créé (cree = true)', async () => {
  test.mock.method(db, 'query', async () => ({ rows: [ligneBase()] }));

  const { signalement, cree } = await signalementsService.creer({
    clientId: '22222222-2222-4222-8222-222222222222',
    categorie: 'ROUTE',
    description: 'Chaussée dégradée',
    latitude: -18.8792,
    longitude: 47.5079,
    photoUrl: null,
    isDemo: true,
  });

  assert.equal(cree, true);
  assert.equal(signalement.clientId, '22222222-2222-4222-8222-222222222222');
});

test('créer : un clientId déjà connu renvoie l’existant sans doublon (idempotence)', async () => {
  let appels = 0;
  test.mock.method(db, 'query', async () => {
    appels += 1;
    // 1er appel : INSERT ... ON CONFLICT DO NOTHING → aucune ligne.
    // 2e appel : relecture de la ligne existante.
    return appels === 1 ? { rows: [] } : { rows: [ligneBase()] };
  });

  const { signalement, cree } = await signalementsService.creer({
    clientId: '22222222-2222-4222-8222-222222222222',
    categorie: 'ROUTE',
    description: 'Chaussée dégradée',
    latitude: null,
    longitude: null,
    photoUrl: null,
    isDemo: true,
  });

  assert.equal(cree, false);
  assert.equal(appels, 2);
  assert.equal(signalement.id, '11111111-1111-4111-8111-111111111111');
});

test('créer : conflit sans ligne existante → erreur 500 explicite', async () => {
  test.mock.method(db, 'query', async () => ({ rows: [] }));

  await assert.rejects(
    () =>
      signalementsService.creer({
        clientId: '22222222-2222-4222-8222-222222222222',
        categorie: 'ROUTE',
        description: 'Chaussée dégradée',
        latitude: null,
        longitude: null,
        photoUrl: null,
        isDemo: true,
      }),
    (erreur) => erreur.status === 500
  );
});

test('détail : identifiant inconnu → 404', async () => {
  test.mock.method(db, 'query', async () => ({ rows: [] }));

  await assert.rejects(
    () => signalementsService.recupererParId('inconnu'),
    (erreur) => erreur.status === 404
  );
});

test('statut : mise à jour d’un signalement inexistant → 404', async () => {
  test.mock.method(db, 'query', async () => ({ rows: [] }));

  await assert.rejects(
    () => signalementsService.mettreAJourStatut('inconnu', 'PRIS_EN_CHARGE'),
    (erreur) => erreur.status === 404
  );
});

test('statut : mise à jour renvoie le signalement à jour', async () => {
  test.mock.method(db, 'query', async () => ({
    rows: [ligneBase({ statut: 'PRIS_EN_CHARGE' })],
  }));

  const signalement = await signalementsService.mettreAJourStatut('id', 'PRIS_EN_CHARGE');
  assert.equal(signalement.statut, 'PRIS_EN_CHARGE');
});

test('lister : trie du plus récent au plus ancien', async () => {
  let sqlRecu = '';
  test.mock.method(db, 'query', async (sql) => {
    sqlRecu = sql;
    return { rows: [ligneBase()] };
  });

  const liste = await signalementsService.lister();

  assert.equal(liste.length, 1);
  assert.match(sqlRecu, /ORDER BY date_creation DESC/);
});
