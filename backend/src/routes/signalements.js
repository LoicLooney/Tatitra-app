const express = require('express');
const router = express.Router();
const signalementsController = require('../controllers/signalementsController');
const resolutionController = require('../controllers/resolutionController');
const {
  validerCreationSignalement,
  validerChangementStatut,
} = require('../middleware/validate-signalement');
const {
  validerRoleResolution,
  validerMotifReouverture,
} = require('../middleware/validate-resolution');

// GET /api/signalements — liste
router.get('/', signalementsController.lister);

// Job J+7 (avant /:id pour ne pas capturer "jobs" comme id)
router.post(
  '/jobs/expiration-resolution',
  resolutionController.expirer
);

// Résolution (chemins spécifiques avant GET /:id)
router.post(
  '/:id/resolution/confirm',
  validerRoleResolution,
  resolutionController.confirmer
);
router.post(
  '/:id/resolution/reopen',
  validerMotifReouverture,
  resolutionController.rouvrir
);
router.post(
  '/:id/resolution',
  validerRoleResolution,
  resolutionController.proposer
);

// PATCH /api/signalements/:id/statut
router.patch(
  '/:id/statut',
  validerChangementStatut,
  signalementsController.changerStatut
);

// GET /api/signalements/:id — détail
router.get('/:id', signalementsController.detail);

// POST /api/signalements — création
router.post('/', validerCreationSignalement, signalementsController.creer);

module.exports = router;
