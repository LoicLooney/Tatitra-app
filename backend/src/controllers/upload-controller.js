const uploadService = require('../services/upload-service');
const { asyncHandler, ErreurApi } = require('../middleware/errorHandler');

// POST /api/uploads — reçoit un fichier image (champ "photo") et renvoie son URL de stockage.
exports.envoyerPhoto = asyncHandler(async (req, res) => {
  if (!req.file) {
    throw new ErreurApi(400, 'Aucun fichier reçu (champ attendu : photo)');
  }

  const url = await uploadService.stockerPhoto(req.file);
  res.status(201).json({ url, stockage: uploadService.modeStockage() });
});
