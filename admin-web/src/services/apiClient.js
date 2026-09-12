const CLE_URL_API = 'tatitra.apiUrl';
const URL_PAR_DEFAUT = import.meta.env.VITE_API_URL || 'http://localhost:3000';

/**
 * URL du backend. L'écran Paramètres peut la surcharger sans reconstruire l'application :
 * pratique quand la démo passe du poste de développement au réseau de la salle.
 */
export function getApiUrl() {
  try {
    return localStorage.getItem(CLE_URL_API) || URL_PAR_DEFAUT;
  } catch {
    // Navigation privée ou stockage bloqué : on retombe sur la valeur de build.
    return URL_PAR_DEFAUT;
  }
}

export function setApiUrl(url) {
  const valeur = (url || '').trim().replace(/\/$/, '');
  if (valeur) {
    localStorage.setItem(CLE_URL_API, valeur);
  } else {
    localStorage.removeItem(CLE_URL_API);
  }
}

export function getApiUrlParDefaut() {
  return URL_PAR_DEFAUT;
}

/**
 * Appel HTTP unique de l'admin : ajoute l'URL de base et transforme une réponse
 * d'erreur du backend en Error porteuse du message renvoyé par l'API.
 */
export async function requeteApi(chemin, options = {}) {
  const url = `${getApiUrl()}${chemin}`;
  let reponse;
  try {
    reponse = await fetch(url, {
      headers: { 'Content-Type': 'application/json', ...options.headers },
      ...options,
    });
  } catch {
    // fetch ne lève que « Failed to fetch » : on nomme la cause réelle pour l'agent.
    throw new Error(`Backend injoignable à l'adresse ${getApiUrl()}`);
  }

  if (!reponse.ok) {
    throw new Error(await lireMessageErreur(reponse));
  }

  return reponse.status === 204 ? null : reponse.json();
}

async function lireMessageErreur(reponse) {
  try {
    const corps = await reponse.json();
    const details = Array.isArray(corps.details) ? ` (${corps.details.join(', ')})` : '';
    return `${corps.error || reponse.statusText}${details}`;
  } catch {
    return `Erreur ${reponse.status} : ${reponse.statusText}`;
  }
}
