import { useEffect, useState } from 'react';
import ConfirmResolutionModal from './ConfirmResolutionModal';
import ConfirmStatutModal from './ConfirmStatutModal';
import CompteARebours from './CompteARebours';
import {
  LIBELLES_CATEGORIE,
  LIBELLES_STATUT,
  STATUTS_PROPOSITION_RESOLUTION,
  STATUTS_TRIAGE_ADMIN,
  ROLES_RESOLUTION,
  estStatutTriage,
} from '../constants';
import {
  confirmerResolution,
  getSignalement,
  patchStatut,
  proposerResolution,
  rouvrirResolution,
} from '../services/api';
import { getApiUrl } from '../services/apiClient';

function DetailSignalement({ id, onRetour, onStatutChange }) {
  const [signalement, setSignalement] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);
  const [message, setMessage] = useState(null);
  const [confirmation, setConfirmation] = useState(null);
  const [enregistrementStatut, setEnregistrementStatut] = useState(false);
  const [actionResolution, setActionResolution] = useState(false);
  const [modalResolution, setModalResolution] = useState(null);

  useEffect(() => {
    let annule = false;
    async function charger() {
      setChargement(true);
      setErreur(null);
      try {
        const data = await getSignalement(id);
        if (!annule) setSignalement(data);
      } catch (echec) {
        if (!annule) {
          setErreur(echec.message);
          setSignalement(null);
        }
      } finally {
        if (!annule) setChargement(false);
      }
    }
    charger();
    return () => {
      annule = true;
    };
  }, [id]);

  function demanderChangementStatut(nouveauStatut) {
    if (!signalement || nouveauStatut === signalement.statut) return;
    if (!estStatutTriage(nouveauStatut)) return;
    setConfirmation({
      ancienStatut: signalement.statut,
      nouveauStatut,
    });
  }

  async function confirmerChangementStatut() {
    if (!confirmation) return;
    setMessage(null);
    setEnregistrementStatut(true);
    try {
      const maj = await patchStatut(id, confirmation.nouveauStatut);
      setSignalement(maj);
      setMessage('Statut mis à jour.');
      setConfirmation(null);
      onStatutChange?.();
    } catch (echec) {
      setMessage(echec.message);
    } finally {
      setEnregistrementStatut(false);
    }
  }

  async function executerResolution(action, messageSucces) {
    setMessage(null);
    setActionResolution(true);
    try {
      const maj = await action();
      setSignalement(maj);
      setMessage(messageSucces);
      setModalResolution(null);
      onStatutChange?.();
    } catch (echec) {
      setMessage(echec.message);
    } finally {
      setActionResolution(false);
    }
  }

  const occupe = enregistrementStatut || actionResolution || Boolean(confirmation) || Boolean(modalResolution);

  return (
    <div className="page">
      <div className="page-entete">
        <h1>Détail du signalement</h1>
        <button type="button" className="bouton bouton-secondaire" onClick={onRetour}>
          Retour à la liste
        </button>
      </div>

      {chargement && <p>Chargement du détail…</p>}
      {erreur && <p className="message-erreur">{erreur}</p>}
      {message && <p className="message-info">{message}</p>}

      {signalement && (
        <section className="carte detail-signalement">
          <div className="detail-grille">
            <div className="detail-photo-bloc">
              <h2>Photo</h2>
              <PhotoSignalement photoUrl={signalement.photoUrl} />
            </div>

            <div className="detail-infos">
              <h2>Informations</h2>
              <dl className="detail-liste">
                <div>
                  <dt>Catégorie</dt>
                  <dd>{LIBELLES_CATEGORIE[signalement.categorie] || signalement.categorie}</dd>
                </div>
                <div>
                  <dt>Statut</dt>
                  <dd>
                    <SelectTriageStatut
                      statut={signalement.statut}
                      disabled={occupe}
                      onDemander={demanderChangementStatut}
                    />
                  </dd>
                </div>
                <div>
                  <dt>Description</dt>
                  <dd>
                    {signalement.description}
                    {signalement.isDemo && (
                      <span className="etiquette-demo">démonstration</span>
                    )}
                  </dd>
                </div>
                <div>
                  <dt>Localisation (GPS)</dt>
                  <dd>{formaterGps(signalement.latitude, signalement.longitude)}</dd>
                </div>
                <div>
                  <dt>Référence serveur</dt>
                  <dd className="detail-mono">{signalement.id}</dd>
                </div>
                <div>
                  <dt>Id client</dt>
                  <dd className="detail-mono">{signalement.clientId || '—'}</dd>
                </div>
                <div>
                  <dt>Reçu le</dt>
                  <dd>{formaterDate(signalement.createdAt)}</dd>
                </div>
                <div>
                  <dt>Modifié le</dt>
                  <dd>{formaterDate(signalement.updatedAt)}</dd>
                </div>
                {signalement.resolutionProposeePar && (
                  <div>
                    <dt>Résolution proposée par</dt>
                    <dd>{signalement.resolutionProposeePar}</dd>
                  </div>
                )}
                {signalement.dateLimiteConfirmation && (
                  <div>
                    <dt>Confirmer avant</dt>
                    <dd>{formaterDate(signalement.dateLimiteConfirmation)}</dd>
                  </div>
                )}
                {signalement.dateReouverture && (
                  <div>
                    <dt>Rouvert le</dt>
                    <dd>{formaterDate(signalement.dateReouverture)}</dd>
                  </div>
                )}
                {signalement.motifReouverture && (
                  <div>
                    <dt>Motif de réouverture</dt>
                    <dd>{signalement.motifReouverture}</dd>
                  </div>
                )}
              </dl>

              {signalement.statut === 'RESOLUTION_A_CONFIRMER' && (
                <CompteARebours echeanceIso={signalement.dateLimiteConfirmation} />
              )}

              <SectionResolutionAdmin
                signalement={signalement}
                disabled={occupe}
                onProposer={() =>
                  executerResolution(
                    () => proposerResolution(id, 'ADMIN'),
                    'Résolution proposée. En attente de confirmation citoyenne (7 jours).'
                  )
                }
                onDemanderConfirmer={() => setModalResolution('confirmer')}
                onDemanderRefuser={() => setModalResolution('refuser')}
              />
            </div>
          </div>
        </section>
      )}

      <ConfirmStatutModal
        ouvert={Boolean(confirmation)}
        ancienStatut={confirmation?.ancienStatut}
        nouveauStatut={confirmation?.nouveauStatut}
        enCours={enregistrementStatut}
        onConfirmer={confirmerChangementStatut}
        onAnnuler={() => !enregistrementStatut && setConfirmation(null)}
      />

      <ConfirmResolutionModal
        ouvert={Boolean(modalResolution)}
        mode={modalResolution}
        signalement={signalement}
        enCours={actionResolution}
        onConfirmer={() =>
          executerResolution(
            () => confirmerResolution(id, 'ADMIN'),
            'Résolution confirmée.'
          )
        }
        onRefuser={(motif) =>
          executerResolution(
            () => rouvrirResolution(id, motif),
            'Signalement rouvert (non résolu).'
          )
        }
        onAnnuler={() => !actionResolution && setModalResolution(null)}
      />
    </div>
  );
}

function SelectTriageStatut({ statut, disabled, onDemander }) {
  if (!estStatutTriage(statut)) {
    return (
      <span className="statut-lecture">
        {LIBELLES_STATUT[statut] || statut}
        <span className="aide-inline"> — via le parcours résolution</span>
      </span>
    );
  }

  return (
    <select
      key={statut}
      className="select-statut"
      value={statut}
      disabled={disabled}
      aria-label="Statut du signalement (triage)"
      onChange={(e) => onDemander(e.target.value)}
    >
      {STATUTS_TRIAGE_ADMIN.map((code) => (
        <option key={code} value={code}>
          {LIBELLES_STATUT[code] || code}
        </option>
      ))}
    </select>
  );
}

function SectionResolutionAdmin({
  signalement,
  disabled,
  onProposer,
  onDemanderConfirmer,
  onDemanderRefuser,
}) {
  const statut = signalement.statut;
  const proposePar = signalement.resolutionProposeePar;

  if (statut === 'REOUVERT_NON_RESOLU') {
    return (
      <div className="detail-resolution detail-resolution-rouvert">
        <h3>Signalement rouvert</h3>
        <p className="aide">
          Ce dossier a été rouvert
          {signalement.motifReouverture ? ` : ${signalement.motifReouverture}` : ''}. Vous
          pouvez le reprendre via le triage ou proposer à nouveau une résolution.
        </p>
        <div className="detail-resolution-actions">
          <button type="button" className="bouton" disabled={disabled} onClick={onProposer}>
            Proposer une résolution
          </button>
        </div>
      </div>
    );
  }

  if (STATUTS_PROPOSITION_RESOLUTION.includes(statut)) {
    return (
      <div className="detail-resolution">
        <h3>Résolution</h3>
        <p className="aide">
          Proposez une résolution si le problème paraît corrigé. Le citoyen devra confirmer sous
          7 jours.
        </p>
        <div className="detail-resolution-actions">
          <button type="button" className="bouton" disabled={disabled} onClick={onProposer}>
            Proposer une résolution
          </button>
        </div>
      </div>
    );
  }

  if (statut === 'RESOLUTION_A_CONFIRMER') {
    const peutConfirmer = proposePar === ROLES_RESOLUTION.CITOYEN;
    return (
      <div className="detail-resolution">
        <h3>Confirmation / refus</h3>
        {peutConfirmer ? (
          <>
            <p className="aide">
              Le citoyen a proposé une résolution. Confirmez ou refusez avec un motif.
            </p>
            <div className="detail-resolution-actions">
              <button
                type="button"
                className="bouton"
                disabled={disabled}
                onClick={onDemanderConfirmer}
              >
                Confirmer
              </button>
              <button
                type="button"
                className="bouton bouton-secondaire"
                disabled={disabled}
                onClick={onDemanderRefuser}
              >
                Refuser
              </button>
            </div>
          </>
        ) : (
          <>
            <p className="aide">
              Proposition admin en attente de confirmation citoyenne. Vous pouvez encore
              rouvrir le dossier si le problème n&apos;est pas corrigé.
            </p>
            <div className="detail-resolution-actions">
              <button
                type="button"
                className="bouton bouton-secondaire"
                disabled={disabled}
                onClick={onDemanderRefuser}
              >
                Refuser / rouvrir
              </button>
            </div>
          </>
        )}
      </div>
    );
  }

  return null;
}

function PhotoSignalement({ photoUrl }) {
  const url = resoudreUrlPhoto(photoUrl);
  if (!url) {
    return <p className="detail-sans-photo">Aucune photo jointe.</p>;
  }
  return (
    <img
      className="detail-photo"
      src={url}
      alt="Photo du signalement"
    />
  );
}

function resoudreUrlPhoto(photoUrl) {
  if (!photoUrl) return null;
  if (/^https?:\/\//i.test(photoUrl)) return photoUrl;
  const base = getApiUrl().replace(/\/$/, '');
  return `${base}${photoUrl.startsWith('/') ? '' : '/'}${photoUrl}`;
}

function formaterDate(valeur) {
  if (!valeur) return '—';
  return new Date(valeur).toLocaleString('fr-FR');
}

function formaterGps(latitude, longitude) {
  if (latitude == null || longitude == null) return 'Non renseignée';
  return `${Number(latitude).toFixed(6)}, ${Number(longitude).toFixed(6)}`;
}

export default DetailSignalement;
