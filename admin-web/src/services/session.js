const CLE_SESSION = 'tatitra.session';

/**
 * Session administrateur simplifiée (authentification complète = bonus, §3.3).
 *
 * Elle identifie l'agent pendant la démonstration mais ne protège rien :
 * le backend ne doit jamais déduire un rôle de ce qui est stocké ici (§11.4).
 */
export function getSession() {
  try {
    const brut = localStorage.getItem(CLE_SESSION);
    return brut ? JSON.parse(brut) : null;
  } catch {
    return null;
  }
}

export function ouvrirSession(nomAgent) {
  const session = {
    nomAgent: nomAgent.trim(),
    role: 'ADMIN',
    ouvertureLe: new Date().toISOString(),
  };
  localStorage.setItem(CLE_SESSION, JSON.stringify(session));
  return session;
}

export function fermerSession() {
  localStorage.removeItem(CLE_SESSION);
}
