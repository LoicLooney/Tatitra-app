// Mémoire temporaire — remplacé plus tard par PostgreSQL / Supabase
const signalements = [];

exports.lister = async () => signalements;

exports.creer = async (payload) => {
  const item = {
    id: String(signalements.length + 1),
    ...payload,
    statut: payload.statut || 'ENVOYE',
    createdAt: new Date().toISOString(),
  };
  signalements.push(item);
  return item;
};
