import { useEffect, useState } from 'react';
import ConfirmStatutModal from './ConfirmStatutModal';
import {
  LIBELLES_CATEGORIE,
  LIBELLES_STATUT,
  STATUTS_PROPOSITION_RESOLUTION,
  STATUTS_TRIAGE_ADMIN,
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

/**
 * Détail d'un signalement (J4) + actions résolution (J5).
 * Charge GET /api/signalements/:id pour garantir des données à jour.
 * Le select ne propose que le triage ; résolution via boutons dédiés.
 */
function DetailSignalement({ id, onRetour, onStatutChange }) {
  const [signalement, setSignalement] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);
  const [message, setMessage] = useState(null);
  const [confirmation, setConfirmation] = useState(null);
  const [enregistrementStatut, setEnregistrementStatut] = useState(false);
  const [actionResolution, setActionResolution] = useState(false);

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
      onStatutChange?.();
    } catch (echec) {
      setMessage(echec.message);
    } finally {
      setActionResolution(false);
    }
  }

  const occupe = enregistrementStatut || actionResolution || Boolean(confirmation);

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
                {signalement.motifReouverture && (
                  <div>
                    <dt>Motif de réouverture</dt>
                    <dd>{signalement.motifReouverture}</dd>
                  </div>
                )}
              </dl>

              <SectionResolutionAdmin
                signalement={signalement}
                disabled={occupe}
                onProposer={() =>
                  executerResolution(
                    () => proposerResolution(id, 'ADMIN'),
                    'Résolution proposée. En attente de confirmation citoyenne (7 jours).'
                  )
                }
                onConfirmer={() =>
                  executerResolution(
                    () => confirmerResolution(id, 'ADMIN'),
                    'Résolution confirmée.'
                  )
                }
                onRouvrir={() =>
                  executerResolution(
                    () => rouvrirResolution(id, 'Toujours endommagé'),
                    'Signalement rouvert (non résolu).'
                  )
                }
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
    </div>
  );
}

/**
 * Select triage uniquement. Hors triage : libellé en lecture seule
 * (parcours résolution via les boutons ci-dessous).
 */
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

function SectionResolutionAdmin({ signalement, disabled, onProposer, onConfirmer, onRouvrir }) {
  const statut = signalement.statut;
  const proposePar = signalement.resolutionProposeePar;

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
    const peutConfirmer = proposePar === 'CITOYEN';
    return (
      <div className="detail-resolution">
        <h3>Confirmation</h3>
        {peutConfirmer ? (
          <>
            <p className="aide">
              Le citoyen a proposé une résolution. Confirmez ou signalez que le problème est
              toujours présent.
            </p>
            <div className="detail-resolution-actions">
              <button type="button" className="bouton" disabled={disabled} onClick={onConfirmer}>
                Confirmer la résolution
              </button>
              <button
                type="button"
                className="bouton bouton-secondaire"
                disabled={disabled}
                onClick={onRouvrir}
              >
                Toujours endommagé
              </button>
            </div>
          </>
        ) : (
          <>
            <p className="aide">
              Proposition admin en attente de confirmation citoyenne
              {signalement.dateLimiteConfirmation
                ? ` (avant le ${formaterDate(signalement.dateLimiteConfirmation)})`
                : ''}
              .
            </p>
            <div className="detail-resolution-actions">
              <button
                type="button"
                className="bouton bouton-secondaire"
                disabled={disabled}
                onClick={onRouvrir}
              >
                Rouvrir (toujours endommagé)
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

/** Accepte une URL absolue ou un chemin relatif servi par le backend. */
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
