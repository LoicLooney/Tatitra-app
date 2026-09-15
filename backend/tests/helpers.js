/**
 * Outils communs aux tests : faux objets req / res d'Express.
 * Aucune dépendance externe — on utilise le lanceur intégré de Node (`node --test`).
 */

/** Construit une fausse requête Express. */
function fausseRequete({ body = {}, params = {}, headers = {} } = {}) {
  const entetes = {};
  for (const [cle, valeur] of Object.entries(headers)) {
    entetes[cle.toLowerCase()] = valeur;
  }
  return {
    body,
    params,
    method: 'POST',
    originalUrl: '/api/test',
    get(nom) {
      return entetes[String(nom).toLowerCase()];
    },
  };
}

/** Fausse réponse Express qui mémorise le code et le corps au lieu de les envoyer. */
function fausseReponse() {
  return {
    codeStatut: null,
    corps: null,
    status(code) {
      this.codeStatut = code;
      return this;
    },
    json(corps) {
      this.corps = corps;
      return this;
    },
  };
}

/** Exécute un middleware et renvoie ce qu'il a produit. */
function executerMiddleware(middleware, requete) {
  const reponse = fausseReponse();
  let suivantAppele = false;
  middleware(requete, reponse, () => {
    suivantAppele = true;
  });
  return { requete, reponse, suivantAppele };
}

module.exports = { fausseRequete, fausseReponse, executerMiddleware };
