const crypto = require('crypto');
const fs = require('fs/promises');
const path = require('path');
const { ErreurApi } = require('../middleware/errorHandler');

const BUCKET_SUPABASE = process.env.SUPABASE_BUCKET || 'signalements';
const DOSSIER_LOCAL = process.env.UPLOADS_DIR || path.join(__dirname, '..', '..', 'uploads');

const EXTENSIONS_PAR_TYPE = {
  'image/jpeg': 'jpg',
  'image/png': 'png',
  'image/webp': 'webp',
};

let clientSupabase = null;

/**
 * Stocke la photo d'un signalement et renvoie son URL publique.
 *
 * Supabase Storage est utilisé dès que SUPABASE_URL et SUPABASE_SERVICE_KEY sont
 * configurés ; sinon le fichier est écrit dans backend/uploads et servi par Express.
 * Ce repli permet de démontrer le parcours complet sans dépendre du réseau (§20.2).
 */
exports.stockerPhoto = async (fichier) => {
  const nomFichier = genererNomFichier(fichier.mimetype);
  const supabase = obtenirClientSupabase();

  if (!supabase) {
    return stockerEnLocal(nomFichier, fichier.buffer);
  }

  const { error } = await supabase.storage
    .from(BUCKET_SUPABASE)
    .upload(nomFichier, fichier.buffer, { contentType: fichier.mimetype, upsert: false });

  if (error) {
    throw new ErreurApi(502, "Stockage de l'image impossible", error.message);
  }

  const { data } = supabase.storage.from(BUCKET_SUPABASE).getPublicUrl(nomFichier);
  return data.publicUrl;
};

/** Indique quel mode de stockage est actif (affiché au démarrage et par /health). */
exports.modeStockage = () => (obtenirClientSupabase() ? 'supabase' : 'local');

exports.DOSSIER_LOCAL = DOSSIER_LOCAL;

async function stockerEnLocal(nomFichier, contenu) {
  await fs.mkdir(DOSSIER_LOCAL, { recursive: true });
  await fs.writeFile(path.join(DOSSIER_LOCAL, nomFichier), contenu);

  const baseUrl = process.env.PUBLIC_BASE_URL || `http://localhost:${process.env.PORT || 3000}`;
  return `${baseUrl}/uploads/${nomFichier}`;
}

/**
 * Le nom est généré côté serveur : celui envoyé par le client n'est jamais utilisé
 * pour construire un chemin de fichier.
 */
function genererNomFichier(mimetype) {
  const extension = EXTENSIONS_PAR_TYPE[mimetype] || 'jpg';
  return `${Date.now()}-${crypto.randomBytes(6).toString('hex')}.${extension}`;
}

function obtenirClientSupabase() {
  if (clientSupabase) return clientSupabase;
  if (!process.env.SUPABASE_URL || !process.env.SUPABASE_SERVICE_KEY) return null;

  // Chargement paresseux : le paquet n'est requis que si Supabase est réellement configuré.
  const { createClient } = require('@supabase/supabase-js');
  clientSupabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_KEY);
  return clientSupabase;
}
