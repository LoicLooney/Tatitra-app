import { useState } from 'react';
import FiltresSignalements, {
  FILTRE_TOUS,
  filtrerSignalements,
} from '../components/FiltresSignalements';
import StatsPanel from '../components/StatsPanel';
import { LIBELLES_CATEGORIE, LIBELLES_STATUT } from '../constants';
import { useSignalements } from '../services/useSignalements';

/**
 * Tableau de bord : synthèse, filtres et aperçu des signalements reçus (F-ADM-01/02/08).
 * La liste détaillée et l'écran de détail relèvent du parcours de traitement (Membre B)
 * et viendront remplacer l'aperçu ci-dessous.
 */
function DashboardPage() {
  const { signalements, chargement, erreur, recharger } = useSignalements();
  const [statut, setStatut] = useState(FILTRE_TOUS);
  const [categorie, setCategorie] = useState(FILTRE_TOUS);

  const signalementsFiltres = filtrerSignalements(signalements, statut, categorie);

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

        <ApercuSignalements signalements={signalementsFiltres} chargement={chargement} />
      </section>
    </div>
  );
}

function ApercuSignalements({ signalements, chargement }) {
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
            <td>{signalement.id}</td>
            <td>{LIBELLES_CATEGORIE[signalement.categorie] || signalement.categorie}</td>
            <td className="cellule-description">
              {signalement.description}
              {signalement.isDemo && <span className="etiquette-demo">démonstration</span>}
            </td>
            <td>{LIBELLES_STATUT[signalement.statut] || signalement.statut}</td>
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
