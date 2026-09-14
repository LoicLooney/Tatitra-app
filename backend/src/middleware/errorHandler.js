/**
 * Gestion centralisée des erreurs HTTP (§11.4 : 400, 401/403, 404, 409, 500).
 * Aucune erreur ne doit être avalée silencieusement : tout passe par ici et est journalisé.
 */

/** Enveloppe un contrôleur asynchrone pour que ses rejets arrivent au gestionnaire d'erreurs. */
function asyncHandler(controleur) {
  return (req, res, next) => Promise.resolve(controleur(req, res, next)).catch(next);
}

/** Erreur métier porteuse d'un code HTTP, levée depuis les services. */
class ErreurApi extends Error {
  constructor(status, message, details) {
    super(message);
    this.name = 'ErreurApi';
    this.status = status;
    this.details = details;
  }
}

/** Route inconnue : 404 explicite plutôt qu'une page HTML par défaut. */
function notFoundHandler(req, res) {
  res.status(404).json({ error: `Ressource introuvable : ${req.method} ${req.originalUrl}` });
}

// eslint-disable-next-line no-unused-vars -- Express identifie le gestionnaire d'erreurs à ses 4 arguments.
function errorHandler(err, req, res, next) {
  const status = err.status || 500;

  // Les 5xx sont des incidents serveur : trace complète. Les 4xx restent des erreurs d'appel.
  if (status >= 500) {
    console.error(`[${req.method} ${req.originalUrl}]`, err);
  } else {
    console.warn(`[${req.method} ${req.originalUrl}] ${status} - ${err.message}`);
  }

  res.status(status).json({
    error: status >= 500 ? 'Erreur serveur' : err.message || 'Requête invalide',
    details: err.details,
  });
}

module.exports = { asyncHandler, ErreurApi, notFoundHandler, errorHandler };
