const express = require('express');
const router = express.Router();
const signalementsController = require('../controllers/signalementsController');

router.get('/', signalementsController.lister);
router.post('/', signalementsController.creer);

module.exports = router;
