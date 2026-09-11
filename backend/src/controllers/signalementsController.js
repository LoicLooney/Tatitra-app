const signalementsService = require('../services/signalementsService');

exports.lister = async (req, res) => {
  try {
    const data = await signalementsService.lister();
    res.json(data);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

exports.creer = async (req, res) => {
  try {
    const cree = await signalementsService.creer(req.body);
    res.status(201).json(cree);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
};
