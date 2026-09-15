import { useState } from 'react';
import ConfirmStatutModal from '../components/ConfirmStatutModal';
import DetailSignalement from '../components/DetailSignalement';
import FiltresSignalements, {
  FILTRE_TOUS,
  filtrerSignalements,
} from '../components/FiltresSignalements';
import StatsPanel from '../components/StatsPanel';
import {
  LIBELLES_CATEGORIE,
  LIBELLES_STATUT,
  STATUTS_TRIAGE_ADMIN,
  estStatutTriage,
} from '../constants';
import { patchStatut } from '../services/api';
import { useSignalements } from '../services/useSignalements';

function DashboardPage({ signalementId, onOuvrirDetail, onFermerDetail }) {
  const { signalements, chargement, erreur, recharger } = useSignalements();
  const [statut, setStatut] = useState(FILTRE_TOUS);
  const [categorie, setCategorie] = useState(FILTRE_TOUS);
  const [messageAction, setMessageAction] = useState(null);
  const [confirmation, setConfirmation] = useState(null);
  const [confirmationEnCours, setConfirmationEnCours] = useState(false);

  if (signalementId) {
    return (
      <DetailSignalement
        id={signalementId}
        onRetour={onFermerDetail}
        onStatutChange={recharger}
      />
    );
  }

  const signalementsFiltres = filtrerSignalements(signalements, statut, categorie);
  const modalOuverte = Boolean(confirmation);

  function demanderChangementStatut(id, ancienStatut, nouveauStatut) {
    if (nouveauStatut === ancienStatut) return;
    if (!estStatutTriage(nouveauStatut)) return;
    setConfirmation({ id, ancienStatut, nouveauStatut });
  }

  async function confirmerChangementStatut() {
    if (!confirmation) return;
    setMessageAction(null);
    setConfirmationEnCours(true);
    try {
      await patchStatut(confirmation.id, confirmation.nouveauStatut);
      setMessageAction(`Statut mis à jour pour ${confirmation.id}.`);
      setConfirmation(null);
      await recharger();
    } catch (echec) {
      setMessageAction(echec.message);
    } finally {
      setConfirmationEnCours(false);
    }
  }

  return (
    <div className="page">
      <div className="page-entete">
        <h1>Administration TATITRA</h1>
        <button type="button" className="bouton" onClick={recharger} disabled={chargement}>
          {chargement ? 'Chargement…' : 'Actualiser'}
        </button>
      </div>

      {erreur && (
        <p className="message-erreur">
          {erreur} — vérifiez l'adresse de l'API dans l'onglet Paramètres.
        </p>
      )}

      {messageAction && <p className="message-info">{messageAction}</p>}

      <StatsPanel
        signalements={signalements}
        onFiltrerStatut={(code) => setStatut(code || FILTRE_TOUS)}
      />

      <section className="carte">
        <h2>Signalements</h2>
        <FiltresSignalements
          statut={statut}
          categorie={categorie}
          onStatutChange={setStatut}
          onCategorieChange={setCategorie}
        />

        <p className="compteur-resultats">
          {signalementsFiltres.length} signalement(s) affiché(s) sur {signalements.length}.
        </p>

        <ApercuSignalements
          signalements={signalementsFiltres}
          chargement={chargement}
          selectsDesactives={modalOuverte || confirmationEnCours}
          onDemanderChangementStatut={demanderChangementStatut}
          onOuvrirDetail={onOuvrirDetail}
        />
      </section>

      <ConfirmStatutModal
        ouvert={modalOuverte}
        ancienStatut={confirmation?.ancienStatut}
        nouveauStatut={confirmation?.nouveauStatut}
        enCours={confirmationEnCours}
        onConfirmer={confirmerChangementStatut}
        onAnnuler={() => !confirmationEnCours && setConfirmation(null)}
      />
    </div>
  );
}

function ApercuSignalements({
  signalements,
  chargement,
  selectsDesactives,
  onDemanderChangementStatut,
  onOuvrirDetail,
}) {
  if (chargement) return <p>Chargement des signalements…</p>;
  if (signalements.length === 0) return <p>Aucun signalement pour ces filtres.</p>;

  return (
    <table className="table-signalements">
      <thead>
        <tr>
          <th>Référence</th>
          <th>Catégorie</th>
          <th>Description</th>
          <th>Statut</th>
          <th>Reçu le</th>
          <th>Détail</th>
        </tr>
      </thead>
      <tbody>
        {signalements.map((signalement) => (
          <tr key={signalement.id} className={classeLigne(signalement.statut)}>
            <td className="cellule-reference">{signalement.id}</td>
            <td>{LIBELLES_CATEGORIE[signalement.categorie] || signalement.categorie}</td>
            <td className="cellule-description">
              {signalement.description}
              {signalement.isDemo && <span className="etiquette-demo">démonstration</span>}
              {signalement.statut === 'RESOLUTION_A_CONFIRMER' && (
                <span className="etiquette-alerte" title="Action de résolution requise">
                  À confirmer
                </span>
              )}
              {signalement.statut === 'REOUVERT_NON_RESOLU' && (
                <span className="etiquette-rouvert" title="Signalement rouvert">
                  Rouvert
                  {signalement.motifReouverture ? ` — ${signalement.motifReouverture}` : ''}
                </span>
              )}
            </td>
            <td>
              <CelluleStatutListe
                signalement={signalement}
                disabled={selectsDesactives}
                onDemander={onDemanderChangementStatut}
              />
            </td>
            <td>{formaterDate(signalement.createdAt)}</td>
            <td>
              <button
                type="button"
                className="bouton bouton-secondaire bouton-compact"
                onClick={() => onOuvrirDetail(signalement.id)}
              >
                Voir
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function classeLigne(statut) {
  if (statut === 'REOUVERT_NON_RESOLU') return 'ligne-rouvert';
  if (statut === 'RESOLUTION_A_CONFIRMER') return 'ligne-a-confirmer';
  return undefined;
}

function CelluleStatutListe({ signalement, disabled, onDemander }) {
  if (!estStatutTriage(signalement.statut)) {
    return (
      <span className="statut-lecture" title="Gérer la résolution depuis le détail">
        {LIBELLES_STATUT[signalement.statut] || signalement.statut}
      </span>
    );
  }

  return (
    <select
      key={`${signalement.id}-${signalement.statut}`}
      className="select-statut"
      value={signalement.statut}
      disabled={disabled}
      aria-label={`Statut de ${signalement.id}`}
      onClick={(e) => e.stopPropagation()}
      onChange={(e) =>
        onDemander(signalement.id, signalement.statut, e.target.value)
      }
    >
      {STATUTS_TRIAGE_ADMIN.map((code) => (
        <option key={code} value={code}>
          {LIBELLES_STATUT[code] || code}
        </option>
      ))}
    </select>
  );
}

function formaterDate(valeur) {
  if (!valeur) return '—';
  return new Date(valeur).toLocaleString('fr-FR');
}

export default DashboardPage;
