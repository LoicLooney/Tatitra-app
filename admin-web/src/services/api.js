const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:3000';

export async function getSignalements() {
  const res = await fetch(`${API_URL}/api/signalements`);
  if (!res.ok) throw new Error('Impossible de charger les signalements');
  return res.json();
}

export async function getHealth() {
  const res = await fetch(`${API_URL}/health`);
  if (!res.ok) throw new Error('API indisponible');
  return res.json();
}
