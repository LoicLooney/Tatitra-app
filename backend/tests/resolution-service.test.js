const test = require('node:test');
const assert = require('node:assert/strict');

const db = require('../src/db');
const resolutionService = require('../src/services/resolutionService');

function ligneBase(surcharges = {}) {
  return {
    id: '11111111-1111-4111-8111-111111111111',
    client_id: '22222222-2222-4222-8222-222222222222',
    categorie: 'ROUTE',
    description: 'Chaussée dégradée',
    latitude: null,
    longitude: null,
    photo_url: null,
    statut: 'PRIS_EN_CHARGE',
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

/**
 * Simule la base : le premier appel charge la ligne, le second applique l'UPDATE.
 * Mémorise le SQL et les paramètres pour les vérifier.
 */
function simulerBase(ligneCourante, ligneApresUpdate) {
  const appels = [];
  test.mock.method(db, 'query', async (sql, params) => {
    appels.push({ sql, params });
    if (appels.length === 1) {
      return { rows: ligneCourante ? [ligneCourante] : [] };
    }
    return { rows: [ligneApresUpdate || ligneCourante] };
  });
  return appels;
}

test.afterEach(() => test.mock.restoreAll());

// --- Proposer ---------------------------------------------------------------

test('proposer : signalement inconnu → 404', async () => {
  simulerBase(null);

  await assert.rejects(
    () => resolutionService.proposer('inconnu', 'CITOYEN'),
    (erreur) => erreur.status === 404
  );
});

test('proposer : depuis PRIS_EN_CHARGE, passe en RESOLUTION_A_CONFIRMER avec échéance J+7', async () => {
  const appels = simulerBase(
    ligneBase(),
    ligneBase({ statut: 'RESOLUTION_A_CONFIRMER', resolution_proposee_par: 'CITOYEN' })
  );

  const resultat = await resolutionService.proposer('id', 'CITOYEN');

  assert.equal(resultat.statut, 'RESOLUTION_A_CONFIRMER');
  assert.equal(resultat.resolutionProposeePar, 'CITOYEN');
  assert.match(appels[1].sql, /date_limite_confirmation = NOW\(\) \+ \(\$2::int \* INTERVAL '1 day'\)/);
  assert.equal(appels[1].params[1], 7, 'le délai doit être de 7 jours');
});

test('proposer : depuis REOUVERT_NON_RESOLU, autorisé', async () => {
  simulerBase(
    ligneBase({ statut: 'REOUVERT_NON_RESOLU' }),
    ligneBase({ statut: 'RESOLUTION_A_CONFIRMER', resolution_proposee_par: 'ADMIN' })
  );

  const resultat = await resolutionService.proposer('id', 'ADMIN');
  assert.equal(resultat.statut, 'RESOLUTION_A_CONFIRMER');
});

test('proposer : depuis ENVOYE, refusé en 409', async () => {
  simulerBase(ligneBase({ statut: 'ENVOYE' }));

  await assert.rejects(
    () => resolutionService.proposer('id', 'CITOYEN'),
    (erreur) => erreur.status === 409
  );
});

test('proposer : depuis RESOLU_CONFIRME, refusé en 409 (dossier déjà clos)', async () => {
  simulerBase(ligneBase({ statut: 'RESOLU_CONFIRME' }));

  await assert.rejects(
    () => resolutionService.proposer('id', 'ADMIN'),
    (erreur) => erreur.status === 409
  );
});

// --- Confirmer --------------------------------------------------------------

test('confirmer : l’autre partie confirme → RESOLU_CONFIRME', async () => {
  simulerBase(
    ligneBase({ statut: 'RESOLUTION_A_CONFIRMER', resolution_proposee_par: 'ADMIN' }),
    ligneBase({ statut: 'RESOLU_CONFIRME', resolution_proposee_par: 'ADMIN' })
  );

  const resultat = await resolutionService.confirmer('id', 'CITOYEN');
  assert.equal(resultat.statut, 'RESOLU_CONFIRME');
});

test('confirmer : celui qui a proposé ne peut pas confirmer lui-même (validation croisée)', async () => {
  simulerBase(ligneBase({ statut: 'RESOLUTION_A_CONFIRMER', resolution_proposee_par: 'CITOYEN' }));

  await assert.rejects(
    () => resolutionService.confirmer('id', 'CITOYEN'),
    (erreur) => erreur.status === 409
  );
});

test('confirmer : impossible si le statut n’est pas RESOLUTION_A_CONFIRMER', async () => {
  simulerBase(ligneBase({ statut: 'PRIS_EN_CHARGE' }));

  await assert.rejects(
    () => resolutionService.confirmer('id', 'ADMIN'),
    (erreur) => erreur.status === 409
  );
});

test('confirmer : signalement inconnu → 404', async () => {
  simulerBase(null);

  await assert.rejects(
    () => resolutionService.confirmer('inconnu', 'ADMIN'),
    (erreur) => erreur.status === 404
  );
});

// --- Rouvrir ----------------------------------------------------------------

test('rouvrir : « toujours endommagé » repasse en REOUVERT_NON_RESOLU', async () => {
  const appels = simulerBase(
    ligneBase({ statut: 'RESOLUTION_A_CONFIRMER', resolution_proposee_par: 'ADMIN' }),
    ligneBase({ statut: 'REOUVERT_NON_RESOLU', motif_reouverture: 'Toujours endommagé' })
  );

  const resultat = await resolutionService.rouvrir('id', null);

  assert.equal(resultat.statut, 'REOUVERT_NON_RESOLU');
  assert.equal(appels[1].params[0], 'Toujours endommagé');
  assert.match(appels[1].sql, /date_limite_confirmation = NULL/);
});

test('rouvrir : un motif personnalisé est conservé', async () => {
  const appels = simulerBase(
    ligneBase({ statut: 'RESOLUTION_A_CONFIRMER', resolution_proposee_par: 'CITOYEN' }),
    ligneBase({ statut: 'REOUVERT_NON_RESOLU' })
  );

  await resolutionService.rouvrir('id', '  Le trou est revenu  ');
  assert.equal(appels[1].params[0], 'Le trou est revenu');
});

test('rouvrir : impossible hors RESOLUTION_A_CONFIRMER', async () => {
  simulerBase(ligneBase({ statut: 'ENVOYE' }));

  await assert.rejects(
    () => resolutionService.rouvrir('id', null),
    (erreur) => erreur.status === 409
  );
});

// --- Règle des 7 jours ------------------------------------------------------

test('expiration J+7 : ne vise que les propositions dont l’échéance est dépassée', async () => {
  let sqlRecu = '';
  test.mock.method(db, 'query', async (sql) => {
    sqlRecu = sql;
    return { rows: [{ id: 'a' }, { id: 'b' }] };
  });

  const resultat = await resolutionService.expirerResolutions();

  assert.equal(resultat.updated, 2);
  assert.deepEqual(resultat.ids, ['a', 'b']);
  assert.match(sqlRecu, /statut = 'REOUVERT_NON_RESOLU'/);
  assert.match(sqlRecu, /WHERE statut = 'RESOLUTION_A_CONFIRMER'/);
  assert.match(sqlRecu, /date_limite_confirmation < NOW\(\)/);
  assert.match(sqlRecu, /motif_reouverture = 'Absence de confirmation sous 7 jours'/);
});

test('expiration J+7 : sans échéance dépassée, ne touche rien', async () => {
  test.mock.method(db, 'query', async () => ({ rows: [] }));

  const resultat = await resolutionService.expirerResolutions();

  assert.equal(resultat.updated, 0);
  assert.deepEqual(resultat.ids, []);
});
