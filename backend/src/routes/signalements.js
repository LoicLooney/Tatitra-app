const express = require('express');
const router = express.Router();
const signalementsController = require('../controllers/signalementsController');
const {
  validerCreationSignalement,
  validerChangementStatut,
} = require('../middleware/validate-signalement');

// GET /api/signalements — liste
router.get('/', signalementsController.lister);

// PATCH /api/signalements/:id/statut — avant GET /:id (chemin plus spécifique)
router.patch(
  '/:id/statut',
  validerChangementStatut,
  signalementsController.changerStatut
);

// GET /api/signalements/:id — détail
router.get('/:id', signalementsController.detail);

// POST /api/signalements — création (corps validé en amont)
router.post('/', validerCreationSignalement, signalementsController.creer);

module.exports = router;
