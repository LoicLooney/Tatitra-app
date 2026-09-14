import { useCallback, useEffect, useState } from 'react';
import { getSignalements } from './api';

/**
 * Charge la liste des signalements depuis l'API.
 * Point d'accès commun au tableau de bord, aux statistiques et à la liste détaillée
 * pour éviter que chaque composant refasse son propre fetch.
 */
export function useSignalements() {
  const [signalements, setSignalements] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);

  const recharger = useCallback(async () => {
    setChargement(true);
    setErreur(null);
    try {
      const data = await getSignalements();
      setSignalements(Array.isArray(data) ? data : []);
    } catch (echec) {
      setErreur(echec.message);
      setSignalements([]);
    } finally {
      setChargement(false);
    }
  }, []);

  useEffect(() => {
    recharger();
  }, [recharger]);

  return { signalements, chargement, erreur, recharger };
}
