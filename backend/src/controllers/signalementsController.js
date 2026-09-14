const signalementsService = require('../services/signalementsService');
const { asyncHandler } = require('../middleware/errorHandler');

// GET /api/signalements — liste des signalements (admin Web et rafraîchissement mobile).
exports.lister = asyncHandler(async (req, res) => {
  const data = await signalementsService.lister();
  res.json(data);
});

// GET /api/signalements/:id — détail d'un signalement.
exports.detail = asyncHandler(async (req, res) => {
  const signalement = await signalementsService.recupererParId(req.params.id);
  res.json(signalement);
});

// POST /api/signalements — crée un signalement. Corps validé en amont par validerCreationSignalement.
// Renvoie 201 à la création, 200 si le clientId a déjà été reçu (rejeu de synchronisation).
exports.creer = asyncHandler(async (req, res) => {
  const { signalement, cree } = await signalementsService.creer(req.signalementValide);
  res.status(cree ? 201 : 200).json(signalement);
});
