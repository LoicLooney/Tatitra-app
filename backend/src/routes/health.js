const express = require('express');
const router = express.Router();
const db = require('../db');

router.get('/', async (req, res) => {
  try {
    const dbOk = await db.healthCheck();
    res.json({
      status: 'ok',
      db: dbOk ? 'up' : 'down',
    });
  } catch (err) {
    res.status(503).json({
      status: 'degraded',
      db: 'down',
      error: err.message,
    });
  }
});

module.exports = router;
