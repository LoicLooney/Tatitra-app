import { useEffect, useState } from 'react';
import ConfirmStatutModal from './ConfirmStatutModal';
import { LIBELLES_CATEGORIE, LIBELLES_STATUT, STATUTS } from '../constants';
import { getSignalement, patchStatut } from '../services/api';
import { getApiUrl } from '../services/apiClient';

const STATUTS_ADMIN = STATUTS.filter((code) => code !== 'EN_ATTENTE_SYNC');

/**
 * Détail d'un signalement (J4) : photo, catégorie, statut + infos utiles.
 * Charge GET /api/signalements/:id pour garantir des données à jour.
 */
function DetailSignalement({ id, onRetour, onStatutChange }) {
  const [signalement, setSignalement] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);
  const [message, setMessage] = useState(null);
  const [confirmation, setConfirmation] = useState(null);
  const [enregistrementStatut, setEnregistrementStatut] = useState(false);

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
                    <select
                      className="select-statut"
                      value={signalement.statut}
                      disabled={enregistrementStatut}
                      aria-label="Statut du signalement"
                      onChange={(e) => demanderChangementStatut(e.target.value)}
                    >
                      {STATUTS_ADMIN.map((code) => (
                        <option key={code} value={code}>
                          {LIBELLES_STATUT[code] || code}
                        </option>
                      ))}
                    </select>
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
              </dl>
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
