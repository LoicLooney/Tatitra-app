import { useState } from 'react';
import FiltresSignalements, {
  FILTRE_TOUS,
  filtrerSignalements,
} from '../components/FiltresSignalements';
import StatsPanel from '../components/StatsPanel';
import { LIBELLES_CATEGORIE, LIBELLES_STATUT, STATUTS } from '../constants';
import { patchStatut } from '../services/api';
import { useSignalements } from '../services/useSignalements';

/** Statuts modifiables depuis l'admin (pas EN_ATTENTE_SYNC, réservé au mobile). */
const STATUTS_ADMIN = STATUTS.filter((code) => code !== 'EN_ATTENTE_SYNC');

/**
 * Tableau de bord : synthèse, filtres et liste connectée à l'API (J3).
 * Le statut est modifiable via PATCH /api/signalements/:id/statut.
 */
function DashboardPage() {
  const { signalements, chargement, erreur, recharger } = useSignalements();
  const [statut, setStatut] = useState(FILTRE_TOUS);
  const [categorie, setCategorie] = useState(FILTRE_TOUS);
  const [messageAction, setMessageAction] = useState(null);

  const signalementsFiltres = filtrerSignalements(signalements, statut, categorie);

  async function onChangerStatut(id, nouveauStatut) {
    setMessageAction(null);
    try {
      await patchStatut(id, nouveauStatut);
      setMessageAction(`Statut mis à jour pour ${id}.`);
      await recharger();
    } catch (echec) {
      setMessageAction(echec.message);
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

      <StatsPanel signalements={signalements} />

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
          onChangerStatut={onChangerStatut}
        />
      </section>
    </div>
  );
}

function ApercuSignalements({ signalements, chargement, onChangerStatut }) {
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
        </tr>
      </thead>
      <tbody>
        {signalements.map((signalement) => (
          <tr key={signalement.id}>
            <td className="cellule-reference">{signalement.id}</td>
            <td>{LIBELLES_CATEGORIE[signalement.categorie] || signalement.categorie}</td>
            <td className="cellule-description">
              {signalement.description}
              {signalement.isDemo && <span className="etiquette-demo">démonstration</span>}
            </td>
            <td>
              <select
                className="select-statut"
                value={signalement.statut}
                aria-label={`Statut de ${signalement.id}`}
                onChange={(e) => onChangerStatut(signalement.id, e.target.value)}
              >
                {STATUTS_ADMIN.map((code) => (
                  <option key={code} value={code}>
                    {LIBELLES_STATUT[code] || code}
                  </option>
                ))}
              </select>
            </td>
            <td>{formaterDate(signalement.createdAt)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function formaterDate(valeur) {
  if (!valeur) return '—';
  return new Date(valeur).toLocaleString('fr-FR');
}

export default DashboardPage;
