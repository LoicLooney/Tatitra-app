import { CATEGORIES, LIBELLES_CATEGORIE, LIBELLES_STATUT, STATUTS } from '../constants';

export const FILTRE_TOUS = 'TOUS';

/**
 * Barre de filtres par statut et catégorie (F-ADM-08).
 * Composant contrôlé : la sélection est détenue par la page qui l'utilise.
 */
function FiltresSignalements({ statut, categorie, onStatutChange, onCategorieChange }) {
  return (
    <div className="filtres">
      <div className="filtres-groupe">
        <span className="filtres-titre">Statut</span>
        <div className="filtres-boutons">
          <BoutonFiltre
            libelle="Tous"
            actif={statut === FILTRE_TOUS}
            onClick={() => onStatutChange(FILTRE_TOUS)}
          />
          {STATUTS.map((valeur) => (
            <BoutonFiltre
              key={valeur}
              libelle={LIBELLES_STATUT[valeur]}
              actif={statut === valeur}
              onClick={() => onStatutChange(valeur)}
            />
          ))}
        </div>
      </div>

      <div className="filtres-groupe">
        <span className="filtres-titre">Catégorie</span>
        <div className="filtres-boutons">
          <BoutonFiltre
            libelle="Toutes"
            actif={categorie === FILTRE_TOUS}
            onClick={() => onCategorieChange(FILTRE_TOUS)}
          />
          {CATEGORIES.map((valeur) => (
            <BoutonFiltre
              key={valeur}
              libelle={LIBELLES_CATEGORIE[valeur]}
              actif={categorie === valeur}
              onClick={() => onCategorieChange(valeur)}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

function BoutonFiltre({ libelle, actif, onClick }) {
  return (
    <button
      type="button"
      className={actif ? 'filtre-bouton filtre-bouton-actif' : 'filtre-bouton'}
      aria-pressed={actif}
      onClick={onClick}
    >
      {libelle}
    </button>
  );
}

/** Applique les filtres courants à une liste de signalements. */
export function filtrerSignalements(signalements, statut, categorie) {
  return signalements.filter((item) => {
    const statutOk = statut === FILTRE_TOUS || item.statut === statut;
    const categorieOk = categorie === FILTRE_TOUS || item.categorie === categorie;
    return statutOk && categorieOk;
  });
}

export default FiltresSignalements;
