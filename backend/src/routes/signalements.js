const express = require('express');
const router = express.Router();
const signalementsController = require('../controllers/signalementsController');
const { validerCreationSignalement } = require('../middleware/validate-signalement');

router.get('/', signalementsController.lister);
router.post('/', validerCreationSignalement, signalementsController.creer);

module.exports = router;
