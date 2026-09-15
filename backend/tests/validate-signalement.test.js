const test = require('node:test');
const assert = require('node:assert/strict');

const {
  validerCreationSignalement,
  validerChangementStatut,
} = require('../src/middleware/validate-signalement');
const { fausseRequete, executerMiddleware } = require('./helpers');

const CORPS_VALIDE = {
  clientId: '8f71aaaa-1111-2222-3333-444455556666',
  categorie: 'ROUTE',
  description: 'Chaussée dégradée devant l’arrêt de bus',
  latitude: -18.8792,
  longitude: 47.5079,
  isDemo: true,
};

test('création : un corps valide passe et est normalisé', () => {
  const { reponse, suivantAppele, requete } = executerMiddleware(
    validerCreationSignalement,
    fausseRequete({ body: { ...CORPS_VALIDE, description: '  Chaussée dégradée ici  ' } })
  );

  assert.equal(suivantAppele, true);
  assert.equal(reponse.codeStatut, null);
  assert.equal(requete.signalementValide.description, 'Chaussée dégradée ici');
  assert.equal(requete.signalementValide.latitude, -18.8792);
});

test('création : catégorie inconnue refusée en 400', () => {
  const { reponse, suivantAppele } = executerMiddleware(
    validerCreationSignalement,
    fausseRequete({ body: { ...CORPS_VALIDE, categorie: 'INCONNUE' } })
  );

  assert.equal(suivantAppele, false);
  assert.equal(reponse.codeStatut, 400);
  assert.ok(reponse.corps.details.some((d) => d.includes('categorie inconnue')));
});

test('création : description trop courte refusée', () => {
  const { reponse } = executerMiddleware(
    validerCreationSignalement,
    fausseRequete({ body: { ...CORPS_VALIDE, description: 'trou' } })
  );

  assert.equal(reponse.codeStatut, 400);
  assert.ok(reponse.corps.details.some((d) => d.includes('trop courte')));
});

test('création : latitude hors bornes refusée', () => {
  const { reponse } = executerMiddleware(
    validerCreationSignalement,
    fausseRequete({ body: { ...CORPS_VALIDE, latitude: -120 } })
  );

  assert.equal(reponse.codeStatut, 400);
  assert.ok(reponse.corps.details.some((d) => d.includes('latitude hors bornes')));
});

test('création : latitude sans longitude refusée', () => {
  const { reponse } = executerMiddleware(
    validerCreationSignalement,
    fausseRequete({ body: { ...CORPS_VALIDE, longitude: undefined } })
  );

  assert.equal(reponse.codeStatut, 400);
  assert.ok(reponse.corps.details.some((d) => d.includes('ensemble')));
});

test('création : position absente acceptée (GPS facultatif, F-CIT-05)', () => {
  const { suivantAppele, requete } = executerMiddleware(
    validerCreationSignalement,
    fausseRequete({
      body: { ...CORPS_VALIDE, latitude: undefined, longitude: undefined },
    })
  );

  assert.equal(suivantAppele, true);
  assert.equal(requete.signalementValide.latitude, null);
  assert.equal(requete.signalementValide.longitude, null);
});

test('création : un clientId qui n’est pas un UUID est refusé en 400, pas en 500', () => {
  // La colonne client_id est de type UUID : sans ce garde-fou, PostgreSQL lèverait
  // une erreur 22P02 qui remonterait en « Erreur serveur » au lieu d’un refus lisible.
  const { reponse, suivantAppele } = executerMiddleware(
    validerCreationSignalement,
    fausseRequete({ body: { ...CORPS_VALIDE, clientId: 'demo-001-pas-un-uuid' } })
  );

  assert.equal(suivantAppele, false);
  assert.equal(reponse.codeStatut, 400);
  assert.ok(reponse.corps.details.some((d) => d.includes('clientId')));
});

test('création : un UUID en majuscules reste accepté', () => {
  const { suivantAppele } = executerMiddleware(
    validerCreationSignalement,
    fausseRequete({
      body: { ...CORPS_VALIDE, clientId: '8F71AAAA-1111-2222-3333-444455556666' },
    })
  );

  assert.equal(suivantAppele, true);
});

test('création : plusieurs erreurs sont signalées d’un coup', () => {
  const { reponse } = executerMiddleware(
    validerCreationSignalement,
    fausseRequete({ body: { categorie: 'X', description: 'a' } })
  );

  assert.equal(reponse.codeStatut, 400);
  assert.equal(reponse.corps.details.length, 3); // clientId + categorie + description
});

test('statut : un statut de triage est accepté', () => {
  const { suivantAppele, requete } = executerMiddleware(
    validerChangementStatut,
    fausseRequete({ body: { statut: 'PRIS_EN_CHARGE' } })
  );

  assert.equal(suivantAppele, true);
  assert.equal(requete.statutValide, 'PRIS_EN_CHARGE');
});

test('statut : un statut de résolution est refusé (pas de contournement de la validation croisée)', () => {
  const { reponse, suivantAppele } = executerMiddleware(
    validerChangementStatut,
    fausseRequete({ body: { statut: 'RESOLU_CONFIRME' } })
  );

  assert.equal(suivantAppele, false);
  assert.equal(reponse.codeStatut, 400);
  assert.ok(reponse.corps.details.some((d) => d.includes('parcours résolution')));
});

test('statut : EN_ATTENTE_SYNC est refusé (statut purement local au mobile)', () => {
  const { reponse } = executerMiddleware(
    validerChangementStatut,
    fausseRequete({ body: { statut: 'EN_ATTENTE_SYNC' } })
  );

  assert.equal(reponse.codeStatut, 400);
  assert.ok(reponse.corps.details.some((d) => d.includes('statut inconnu')));
});

test('statut : statut manquant refusé', () => {
  const { reponse } = executerMiddleware(
    validerChangementStatut,
    fausseRequete({ body: {} })
  );

  assert.equal(reponse.codeStatut, 400);
  assert.ok(reponse.corps.details.some((d) => d.includes('statut manquant')));
});
