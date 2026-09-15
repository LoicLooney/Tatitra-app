import { requeteApi } from './apiClient';

/** GET /api/signalements — liste complète des signalements reçus. */
export async function getSignalements() {
  return requeteApi('/api/signalements');
}

/** GET /api/signalements/:id — détail d'un signalement. */
export async function getSignalement(id) {
  return requeteApi(`/api/signalements/${id}`);
}

/**
 * PATCH /api/signalements/:id/statut — change le statut (J3).
 * @param {string} id UUID serveur
 * @param {string} statut code métier (ex. PRIS_EN_CHARGE)
 */
export async function patchStatut(id, statut) {
  return requeteApi(`/api/signalements/${id}/statut`, {
    method: 'PATCH',
    body: JSON.stringify({ statut }),
  });
}

/** POST /api/signalements/:id/resolution — propose (J5). */
export async function proposerResolution(id, role = 'ADMIN') {
  return requeteApi(`/api/signalements/${id}/resolution`, {
    method: 'POST',
    headers: { 'X-Tatitra-Role': role },
    body: JSON.stringify({ role }),
  });
}

/** POST /api/signalements/:id/resolution/confirm */
export async function confirmerResolution(id, role = 'ADMIN') {
  return requeteApi(`/api/signalements/${id}/resolution/confirm`, {
    method: 'POST',
    headers: { 'X-Tatitra-Role': role },
    body: JSON.stringify({ role }),
  });
}

/** POST /api/signalements/:id/resolution/reopen */
export async function rouvrirResolution(id, motif = 'Toujours endommagé', role = 'ADMIN') {
  return requeteApi(`/api/signalements/${id}/resolution/reopen`, {
    method: 'POST',
    headers: { 'X-Tatitra-Role': role },
    body: JSON.stringify({ motif, role }),
  });
}

/** POST /api/signalements/jobs/expiration-resolution — job J+7 manuel */
export async function lancerJobJ7(role = 'ADMIN') {
  return requeteApi('/api/signalements/jobs/expiration-resolution', {
    method: 'POST',
    headers: { 'X-Tatitra-Role': role },
    body: JSON.stringify({ role }),
  });
}

/** GET /health — vérifie que le backend répond (écran Paramètres, conditions de démo). */
export async function getHealth() {
  return requeteApi('/health');
}
