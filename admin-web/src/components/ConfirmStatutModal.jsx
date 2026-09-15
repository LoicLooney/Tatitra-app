import { LIBELLES_STATUT } from '../constants';

/**
 * Modal de confirmation avant un changement de statut.
 */
function ConfirmStatutModal({
  ouvert,
  ancienStatut,
  nouveauStatut,
  enCours,
  onConfirmer,
  onAnnuler,
}) {
  if (!ouvert) return null;

  const libelleAncien = LIBELLES_STATUT[ancienStatut] || ancienStatut;
  const libelleNouveau = LIBELLES_STATUT[nouveauStatut] || nouveauStatut;

  return (
    <div
      className="modal-fond"
      role="presentation"
      onClick={onAnnuler}
    >
      <div
        className="modal-boite"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-statut-titre"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="modal-statut-titre">Confirmer le changement de statut</h2>
        <p>
          Passer de <strong>{libelleAncien}</strong> à{' '}
          <strong>{libelleNouveau}</strong> ?
        </p>
        <div className="modal-actions">
          <button
            type="button"
            className="bouton bouton-secondaire"
            onClick={onAnnuler}
            disabled={enCours}
          >
            Annuler
          </button>
          <button
            type="button"
            className="bouton"
            onClick={onConfirmer}
            disabled={enCours}
          >
            {enCours ? 'Enregistrement…' : 'Confirmer'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ConfirmStatutModal;
