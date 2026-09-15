require('dotenv').config();
const express = require('express');
const cors = require('cors');

const healthRoutes = require('./src/routes/health');
const signalementsRoutes = require('./src/routes/signalements');
const uploadsRoutes = require('./src/routes/uploads');
const uploadService = require('./src/services/upload-service');
const { demarrerCronResolution } = require('./src/services/resolutionCron');
const { notFoundHandler, errorHandler } = require('./src/middleware/errorHandler');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
// Les corps JSON restent petits : les photos passent par /api/uploads, jamais en base64.
app.use(express.json({ limit: '1mb' }));

app.use('/health', healthRoutes);
app.use('/api/signalements', signalementsRoutes);
app.use('/api/uploads', uploadsRoutes);

// Photos servies directement quand Supabase Storage n'est pas configuré (mode démo local).
app.use('/uploads', express.static(uploadService.DOSSIER_LOCAL));

app.use(notFoundHandler);
app.use(errorHandler);

app.listen(PORT, '0.0.0.0', () => {
  console.log(`API TATITRA démarrée sur http://0.0.0.0:${PORT}`);
  console.log(`Santé : http://localhost:${PORT}/health`);
  console.log(`Stockage des photos : ${uploadService.modeStockage()}`);
  demarrerCronResolution();
});
