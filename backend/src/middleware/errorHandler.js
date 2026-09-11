// Exemple de middleware d'erreur
function errorHandler(err, req, res, next) {
  console.error(err);
  res.status(err.status || 500).json({ error: err.message || 'Erreur serveur' });
}

module.exports = { errorHandler };
