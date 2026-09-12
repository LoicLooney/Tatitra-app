import { requeteApi } from './apiClient';

/** GET /api/signalements — liste complète des signalements reçus. */
export async function getSignalements() {
  return requeteApi('/api/signalements');
}

/** GET /api/signalements/:id — détail d'un signalement. */
export async function getSignalement(id) {
  return requeteApi(`/api/signalements/${id}`);
}

/** GET /health — vérifie que le backend répond (écran Paramètres, conditions de démo). */
export async function getHealth() {
  return requeteApi('/health');
}
