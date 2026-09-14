const express = require('express');
const router = express.Router();
const signalementsController = require('../controllers/signalementsController');
const { validerCreationSignalement } = require('../middleware/validate-signalement');

// GET /api/signalements — liste
router.get('/', signalementsController.lister);

// GET /api/signalements/:id — détail
router.get('/:id', signalementsController.detail);

// POST /api/signalements — création (corps validé en amont)
router.post('/', validerCreationSignalement, signalementsController.creer);

module.exports = router;
