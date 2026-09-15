import { useEffect, useState } from 'react';
import { LIBELLES_STATUT } from '../constants';

const MOTIF_PAR_DEFAUT = 'Toujours endommagé';

function ConfirmResolutionModal({
  ouvert,
  mode,
  signalement,
  enCours,
  onConfirmer,
  onRefuser,
  onAnnuler,
}) {
  const [motif, setMotif] = useState(MOTIF_PAR_DEFAUT);

  useEffect(() => {
    if (ouvert) {
      setMotif(MOTIF_PAR_DEFAUT);
    }
  }, [ouvert, mode]);

  if (!ouvert || !signalement) return null;

  const estRefus = mode === 'refuser';
  const titre = estRefus ? 'Refuser la résolution' : 'Confirmer la résolution';

  return (
    <div className="modal-fond" role="presentation" onClick={onAnnuler}>
      <div
        className="modal-boite"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-resolution-titre"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="modal-resolution-titre">{titre}</h2>
        <p>
          Signalement <strong>{LIBELLES_STATUT[signalement.statut] || signalement.statut}</strong>
          {signalement.resolutionProposeePar
            ? ` — proposé par ${signalement.resolutionProposeePar}`
            : ''}
          .
        </p>
        {signalement.dateLimiteConfirmation && (
          <p className="aide">
            Échéance J+7 :{' '}
            {new Date(signalement.dateLimiteConfirmation).toLocaleString('fr-FR')}
          </p>
        )}

        {estRefus ? (
          <>
            <label htmlFor="motif-refus">Motif du refus / réouverture</label>
            <textarea
              id="motif-refus"
              className="champ-motif"
              rows={3}
              value={motif}
              disabled={enCours}
              onChange={(e) => setMotif(e.target.value)}
            />
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
                onClick={() => onRefuser(motif.trim() || MOTIF_PAR_DEFAUT)}
                disabled={enCours}
              >
                {enCours ? 'Enregistrement…' : 'Refuser et rouvrir'}
              </button>
            </div>
          </>
        ) : (
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
              {enCours ? 'Enregistrement…' : 'Confirmer la résolution'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default ConfirmResolutionModal;
