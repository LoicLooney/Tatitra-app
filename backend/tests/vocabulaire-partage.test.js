/**
 * Test de contrat entre les trois parties du projet (§2.6 des règles de code).
 *
 * Statuts, catégories et bornes de saisie doivent être identiques dans le backend,
 * l'application Android et l'admin web. Ce test lit directement les fichiers source
 * des trois côtés : une divergence introduite par l'un des deux membres échoue ici,
 * au lieu de se manifester par un bug de correspondance en démonstration.
 */
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const backend = require('../src/constants');

const RACINE = path.join(__dirname, '..', '..');
const KOTLIN_DOMAINE = path.join(
  RACINE,
  'app/src/main/java/mg/itu/tatitra_app/domain'
);

function lire(fichier) {
  return fs.readFileSync(fichier, 'utf8');
}

/** Extrait les noms des constantes d'une enum Kotlin (une par ligne, suivie de « ( »). */
function entreesEnumKotlin(source) {
  const corps = source.slice(source.indexOf('{'), source.lastIndexOf('}'));
  return [...corps.matchAll(/^\s{4}([A-Z][A-Z0-9_]*)\(/gm)].map((m) => m[1]);
}

/** Extrait une constante numérique d'un objet Kotlin. */
function constanteKotlin(source, nom) {
  const trouve = source.match(new RegExp(`const val ${nom}\\s*=\\s*(-?[0-9.]+)`));
  return trouve ? Number(trouve[1]) : null;
}

test('les statuts sont identiques côté backend et côté Android', () => {
  const kotlin = entreesEnumKotlin(lire(path.join(KOTLIN_DOMAINE, 'StatutSignalement.kt')));

  assert.deepEqual(kotlin, backend.STATUTS);
});

test('les catégories sont identiques côté backend et côté Android', () => {
  const kotlin = entreesEnumKotlin(lire(path.join(KOTLIN_DOMAINE, 'Categorie.kt')));

  assert.deepEqual(kotlin, backend.CATEGORIES);
});

test('les statuts et catégories sont identiques côté backend et côté admin web', async () => {
  const admin = await import('../../admin-web/src/constants.js');

  assert.deepEqual(admin.STATUTS, backend.STATUTS);
  assert.deepEqual(admin.CATEGORIES, backend.CATEGORIES);
});

test('chaque statut et catégorie a un libellé affichable dans l’admin', async () => {
  const admin = await import('../../admin-web/src/constants.js');

  for (const statut of backend.STATUTS) {
    assert.ok(admin.LIBELLES_STATUT[statut], `libellé manquant pour le statut ${statut}`);
  }
  for (const categorie of backend.CATEGORIES) {
    assert.ok(
      admin.LIBELLES_CATEGORIE[categorie],
      `libellé manquant pour la catégorie ${categorie}`
    );
  }
});

test('les bornes de saisie sont identiques côté backend et côté Android', () => {
  const regles = lire(path.join(KOTLIN_DOMAINE, 'ReglesSignalement.kt'));

  assert.equal(
    constanteKotlin(regles, 'LONGUEUR_DESCRIPTION_MIN'),
    backend.LONGUEUR_DESCRIPTION_MIN
  );
  assert.equal(
    constanteKotlin(regles, 'LONGUEUR_DESCRIPTION_MAX'),
    backend.LONGUEUR_DESCRIPTION_MAX
  );
  assert.equal(constanteKotlin(regles, 'LATITUDE_MIN'), backend.LATITUDE_MIN);
  assert.equal(constanteKotlin(regles, 'LATITUDE_MAX'), backend.LATITUDE_MAX);
  assert.equal(constanteKotlin(regles, 'LONGITUDE_MIN'), backend.LONGITUDE_MIN);
  assert.equal(constanteKotlin(regles, 'LONGITUDE_MAX'), backend.LONGITUDE_MAX);
});

test('les rôles de résolution sont identiques côté backend et côté Android', () => {
  const kotlin = lire(path.join(KOTLIN_DOMAINE, 'RoleResolution.kt'));
  const valeurs = [...kotlin.matchAll(/const val [A-Z_]+ = "([A-Z]+)"/g)].map((m) => m[1]);

  assert.deepEqual(valeurs.sort(), [...backend.ROLES_RESOLUTION].sort());
});

test('les rôles de résolution sont identiques côté backend et côté admin web', async () => {
  const admin = await import('../../admin-web/src/constants.js');

  // Côté admin c'est un objet ({ CITOYEN: 'CITOYEN' }) : l'interface compare
  // resolutionProposeePar à ROLES_RESOLUTION.CITOYEN pour décider qui peut confirmer.
  // Si cette forme change en tableau, la comparaison vaudrait undefined en silence et
  // le bouton « Confirmer » ne s'afficherait jamais.
  for (const role of backend.ROLES_RESOLUTION) {
    assert.equal(
      admin.ROLES_RESOLUTION[role],
      role,
      `ROLES_RESOLUTION.${role} doit valoir "${role}" côté admin`
    );
  }
});

test('les statuts de proposition de résolution sont identiques backend / admin web', async () => {
  const admin = await import('../../admin-web/src/constants.js');

  assert.deepEqual(admin.STATUTS_PROPOSITION_RESOLUTION, backend.STATUTS_PROPOSITION_RESOLUTION);
  assert.deepEqual(admin.STATUTS_TRIAGE_ADMIN, backend.STATUTS_TRIAGE_ADMIN);
});

test('le délai de confirmation est bien de 7 jours', () => {
  assert.equal(backend.DELAI_CONFIRMATION_JOURS, 7);
});

test('les statuts de triage admin sont un sous-ensemble des statuts connus', () => {
  for (const statut of backend.STATUTS_TRIAGE_ADMIN) {
    assert.ok(backend.STATUTS.includes(statut), `statut de triage inconnu : ${statut}`);
  }
  // Aucun statut de résolution ne doit être modifiable par PATCH libre.
  for (const statut of ['RESOLUTION_A_CONFIRMER', 'RESOLU_CONFIRME', 'REOUVERT_NON_RESOLU']) {
    assert.ok(
      !backend.STATUTS_TRIAGE_ADMIN.includes(statut),
      `${statut} ne doit pas être accessible par PATCH libre`
    );
  }
});
