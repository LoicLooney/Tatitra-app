const test = require('node:test');
const assert = require('node:assert/strict');

const {
  validerRoleResolution,
  validerMotifReouverture,
  exigerRoleAdmin,
} = require('../src/middleware/validate-resolution');
const { fausseRequete, executerMiddleware } = require('./helpers');

test('rôle : accepté depuis le corps JSON', () => {
  const { suivantAppele, requete } = executerMiddleware(
    validerRoleResolution,
    fausseRequete({ body: { role: 'CITOYEN' } })
  );

  assert.equal(suivantAppele, true);
  assert.equal(requete.roleResolution, 'CITOYEN');
});

test('rôle : accepté depuis l’en-tête X-Tatitra-Role, en minuscules', () => {
  const { suivantAppele, requete } = executerMiddleware(
    validerRoleResolution,
    fausseRequete({ headers: { 'X-Tatitra-Role': 'citoyen' } })
  );

  assert.equal(suivantAppele, true);
  assert.equal(requete.roleResolution, 'CITOYEN');
});

test('rôle : absent refusé en 400', () => {
  const { reponse, suivantAppele } = executerMiddleware(
    validerRoleResolution,
    fausseRequete({})
  );

  assert.equal(suivantAppele, false);
  assert.equal(reponse.codeStatut, 400);
});

test('rôle : inconnu refusé en 400', () => {
  const { reponse } = executerMiddleware(
    validerRoleResolution,
    fausseRequete({ body: { role: 'MAIRE' } })
  );

  assert.equal(reponse.codeStatut, 400);
});

test('rôle ADMIN : refusé en 403 sans la clé, quand TATITRA_ADMIN_KEY est configurée', () => {
  process.env.TATITRA_ADMIN_KEY = 'cle-de-test';
  try {
    const { reponse, suivantAppele } = executerMiddleware(
      validerRoleResolution,
      fausseRequete({ body: { role: 'ADMIN' } })
    );

    assert.equal(suivantAppele, false);
    assert.equal(reponse.codeStatut, 403);
  } finally {
    delete process.env.TATITRA_ADMIN_KEY;
  }
});

test('rôle ADMIN : accepté avec la bonne clé', () => {
  process.env.TATITRA_ADMIN_KEY = 'cle-de-test';
  try {
    const { suivantAppele, requete } = executerMiddleware(
      validerRoleResolution,
      fausseRequete({
        body: { role: 'ADMIN' },
        headers: { 'X-Tatitra-Admin-Key': 'cle-de-test' },
      })
    );

    assert.equal(suivantAppele, true);
    assert.equal(requete.roleResolution, 'ADMIN');
  } finally {
    delete process.env.TATITRA_ADMIN_KEY;
  }
});

test('rôle ADMIN : accepté sans clé quand TATITRA_ADMIN_KEY n’est pas configurée (mode démo)', () => {
  delete process.env.TATITRA_ADMIN_KEY;
  const { suivantAppele } = executerMiddleware(
    validerRoleResolution,
    fausseRequete({ body: { role: 'ADMIN' } })
  );

  assert.equal(suivantAppele, true);
});

test('motif : valeur par défaut « Toujours endommagé » si absent', () => {
  const { suivantAppele, requete } = executerMiddleware(
    validerMotifReouverture,
    fausseRequete({ body: {} })
  );

  assert.equal(suivantAppele, true);
  assert.equal(requete.motifReouverture, 'Toujours endommagé');
});

test('motif : texte fourni conservé et détouré', () => {
  const { requete } = executerMiddleware(
    validerMotifReouverture,
    fausseRequete({ body: { motif: '  Le trou est toujours là  ' } })
  );

  assert.equal(requete.motifReouverture, 'Le trou est toujours là');
});

test('motif : type invalide refusé en 400', () => {
  const { reponse, suivantAppele } = executerMiddleware(
    validerMotifReouverture,
    fausseRequete({ body: { motif: 42 } })
  );

  assert.equal(suivantAppele, false);
  assert.equal(reponse.codeStatut, 400);
});

// --- Endpoints réservés à l'administration (job J+7) -------------------------

test('job J+7 : sans rôle, refusé en 400', () => {
  const { reponse, suivantAppele } = executerMiddleware(exigerRoleAdmin, fausseRequete({}));

  assert.equal(suivantAppele, false);
  assert.equal(reponse.codeStatut, 400);
});

test('job J+7 : avec le rôle CITOYEN, refusé en 403', () => {
  const { reponse, suivantAppele } = executerMiddleware(
    exigerRoleAdmin,
    fausseRequete({ body: { role: 'CITOYEN' } })
  );

  assert.equal(suivantAppele, false);
  assert.equal(reponse.codeStatut, 403);
});

test('job J+7 : avec le rôle ADMIN, autorisé', () => {
  const { suivantAppele, requete } = executerMiddleware(
    exigerRoleAdmin,
    fausseRequete({ headers: { 'X-Tatitra-Role': 'ADMIN' } })
  );

  assert.equal(suivantAppele, true);
  assert.equal(requete.roleResolution, 'ADMIN');
});

test('job J+7 : la clé admin est exigée quand elle est configurée', () => {
  process.env.TATITRA_ADMIN_KEY = 'cle-de-test';
  try {
    const sansCle = executerMiddleware(
      exigerRoleAdmin,
      fausseRequete({ body: { role: 'ADMIN' } })
    );
    assert.equal(sansCle.reponse.codeStatut, 403);

    const avecCle = executerMiddleware(
      exigerRoleAdmin,
      fausseRequete({
        body: { role: 'ADMIN' },
        headers: { 'X-Tatitra-Admin-Key': 'cle-de-test' },
      })
    );
    assert.equal(avecCle.suivantAppele, true);
  } finally {
    delete process.env.TATITRA_ADMIN_KEY;
  }
});
