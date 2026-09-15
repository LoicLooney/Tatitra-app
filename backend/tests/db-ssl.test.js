/**
 * Choix de la configuration TLS selon la base visée.
 * Sans ce choix, une base locale refuse la connexion (« The server does not support
 * SSL connections ») et le projet ne peut tourner que sur Supabase.
 */
const test = require('node:test');
const assert = require('node:assert/strict');

const { optionsSsl } = require('../src/db');

test('base locale : pas de TLS', () => {
  assert.equal(optionsSsl('postgresql://postgres:mdp@localhost:5432/tatitra'), false);
  assert.equal(optionsSsl('postgresql://postgres:mdp@127.0.0.1:5432/tatitra'), false);
});

test('sslmode=disable explicite : pas de TLS', () => {
  assert.equal(
    optionsSsl('postgresql://postgres:mdp@ailleurs.example:5432/tatitra?sslmode=disable'),
    false
  );
});

test('base distante sans certificat fourni : TLS actif mais non vérifié, et signalé', () => {
  const options = optionsSsl(
    'postgresql://postgres.ref:mdp@aws-0-eu-central-1.pooler.supabase.com:6543/postgres',
    {}
  );

  assert.equal(options.rejectUnauthorized, false);
  assert.equal(options.verificationDesactivee, true, 'le repli doit être signalé à l’appelant');
});

test('base distante avec DATABASE_CA_CERT : TLS vérifié', () => {
  const options = optionsSsl(
    'postgresql://postgres.ref:mdp@aws-0-eu-central-1.pooler.supabase.com:6543/postgres',
    { DATABASE_CA_CERT: __filename } // n'importe quel fichier lisible suffit ici
  );

  assert.equal(options.rejectUnauthorized, true);
  assert.ok(typeof options.ca === 'string' && options.ca.length > 0);
});
