const resolutionService = require('../services/resolutionService');
const { asyncHandler } = require('../middleware/errorHandler');

// POST /api/signalements/:id/resolution
exports.proposer = asyncHandler(async (req, res) => {
  const signalement = await resolutionService.proposer(req.params.id, req.roleResolution);
  res.json(signalement);
});

// POST /api/signalements/:id/resolution/confirm
exports.confirmer = asyncHandler(async (req, res) => {
  const signalement = await resolutionService.confirmer(req.params.id, req.roleResolution);
  res.json(signalement);
});

// POST /api/signalements/:id/resolution/reopen
exports.rouvrir = asyncHandler(async (req, res) => {
  const signalement = await resolutionService.rouvrir(req.params.id, req.motifReouverture);
  res.json(signalement);
});

// POST /api/signalements/jobs/expiration-resolution — exécution manuelle du job J+7
exports.expirer = asyncHandler(async (req, res) => {
  const resultat = await resolutionService.expirerResolutions();
  res.json(resultat);
});
