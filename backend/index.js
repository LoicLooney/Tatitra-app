require('dotenv').config();
const express = require('express');
const cors = require('cors');

const healthRoutes = require('./src/routes/health');
const signalementsRoutes = require('./src/routes/signalements');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

app.use('/health', healthRoutes);
app.use('/api/signalements', signalementsRoutes);

app.listen(PORT, () => {
  console.log(`LALANA API démarrée sur http://localhost:${PORT}`);
});
