const express = require('express');
const multer = require('multer');
const router = express.Router();
const uploadController = require('../controllers/upload-controller');
const { ErreurApi } = require('../middleware/errorHandler');
const { MAX_UPLOAD_SIZE_MB, TYPES_IMAGE_ACCEPTES } = require('../constants');

const OCTETS_PAR_MEGAOCTET = 1024 * 1024;

// Stockage en mémoire : le fichier part ensuite vers Supabase Storage ou le disque,
// jamais dans un dossier temporaire accessible depuis l'extérieur.
const reception = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_UPLOAD_SIZE_MB * OCTETS_PAR_MEGAOCTET, files: 1 },
  fileFilter: (req, fichier, callback) => {
    if (!TYPES_IMAGE_ACCEPTES.includes(fichier.mimetype)) {
      callback(new ErreurApi(400, `Type d'image non accepté : ${fichier.mimetype}`));
      return;
    }
    callback(null, true);
  },
});

router.post('/', receptionnerPhoto, uploadController.envoyerPhoto);

/** Traduit les erreurs de multer en réponses HTTP cohérentes plutôt qu'en 500. */
function receptionnerPhoto(req, res, next) {
  reception.single('photo')(req, res, (erreur) => {
    if (!erreur) return next();
    if (erreur instanceof multer.MulterError && erreur.code === 'LIMIT_FILE_SIZE') {
      return next(new ErreurApi(413, `Image trop volumineuse (maximum ${MAX_UPLOAD_SIZE_MB} Mo)`));
    }
    return next(erreur);
  });
}

module.exports = router;
