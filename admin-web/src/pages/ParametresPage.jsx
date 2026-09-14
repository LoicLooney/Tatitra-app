import { useState } from 'react';
import { getApiUrl, getApiUrlParDefaut, setApiUrl } from '../services/apiClient';
import { getHealth, lancerJobJ7 } from '../services/api';
import { fermerSession, ouvrirSession } from '../services/session';

/**
 * Paramètres de l'administration : adresse du backend et session agent simplifiée.
 * Permet de rebrancher l'interface sur l'API de démonstration sans rebuild (§20.2).
 *
 * La session vient de App : l'en-tête doit se mettre à jour en même temps que cette page.
 */
function ParametresPage({ session, onSessionChangee }) {
  const [urlSaisie, setUrlSaisie] = useState(getApiUrl());
  const [nomAgent, setNomAgent] = useState('');
  const [etatConnexion, setEtatConnexion] = useState(null);
  const [etatJobJ7, setEtatJobJ7] = useState(null);
  const [jobJ7EnCours, setJobJ7EnCours] = useState(false);

  const enregistrerUrl = (evenement) => {
    evenement.preventDefault();
    setApiUrl(urlSaisie);
    setUrlSaisie(getApiUrl());
    setEtatConnexion({ type: 'info', message: 'Adresse enregistrée.' });
  };

  const testerConnexion = async () => {
    setEtatConnexion({ type: 'info', message: 'Test en cours…' });
    try {
      const sante = await getHealth();
      setEtatConnexion({
        type: 'succes',
        message: `Backend joignable (status : ${sante.status}).`,
      });
    } catch (erreur) {
      setEtatConnexion({ type: 'erreur', message: `Backend injoignable — ${erreur.message}` });
    }
  };

  const connecter = (evenement) => {
    evenement.preventDefault();
    if (!nomAgent.trim()) return;
    onSessionChangee(ouvrirSession(nomAgent));
    setNomAgent('');
  };

  const deconnecter = () => {
    fermerSession();
    onSessionChangee(null);
  };

  const executerJobJ7 = async () => {
    setJobJ7EnCours(true);
    setEtatJobJ7({ type: 'info', message: 'Job J+7 en cours…' });
    try {
      const resultat = await lancerJobJ7();
      const n = resultat?.updated ?? resultat?.ids?.length ?? 0;
      setEtatJobJ7({
        type: 'succes',
        message: `Job terminé : ${n} signalement(s) rouvert(s) pour absence de confirmation.`,
      });
    } catch (erreur) {
      setEtatJobJ7({ type: 'erreur', message: erreur.message });
    } finally {
      setJobJ7EnCours(false);
    }
  };

  return (
    <div className="page">
      <h1>Paramètres</h1>

      <section className="carte">
        <h2>Connexion à l'API</h2>
        <form className="formulaire" onSubmit={enregistrerUrl}>
          <label htmlFor="url-api">Adresse du backend</label>
          <input
            id="url-api"
            type="url"
            value={urlSaisie}
            onChange={(evenement) => setUrlSaisie(evenement.target.value)}
            placeholder={getApiUrlParDefaut()}
          />
          <div className="formulaire-actions">
            <button type="submit" className="bouton">
              Enregistrer
            </button>
            <button type="button" className="bouton bouton-secondaire" onClick={testerConnexion}>
              Tester la connexion
            </button>
          </div>
        </form>
        <p className="aide">
          Valeur par défaut : <code>{getApiUrlParDefaut()}</code>. Pour une démonstration sur
          téléphone réel, utilisez l'adresse IP du poste qui exécute le backend.
        </p>
        {etatConnexion && (
          <p className={`message-${etatConnexion.type}`}>{etatConnexion.message}</p>
        )}
      </section>

      <section className="carte">
        <h2>Session agent</h2>
        {session ? (
          <>
            <p>
              Connecté en tant que <strong>{session.nomAgent}</strong> ({session.role}).
            </p>
            <button type="button" className="bouton bouton-secondaire" onClick={deconnecter}>
              Fermer la session
            </button>
          </>
        ) : (
          <form className="formulaire" onSubmit={connecter}>
            <label htmlFor="nom-agent">Nom de l'agent</label>
            <input
              id="nom-agent"
              type="text"
              value={nomAgent}
              onChange={(evenement) => setNomAgent(evenement.target.value)}
              placeholder="Ex. : Agent voirie"
            />
            <div className="formulaire-actions">
              <button type="submit" className="bouton">
                Ouvrir la session
              </button>
            </div>
          </form>
        )}
        <p className="aide">
          Identification locale de démonstration : elle n'autorise rien par elle-même.
          Le backend ne déduit jamais un rôle de ce que lui envoie le navigateur
          (authentification complète prévue en bonus).
        </p>
      </section>

      <section className="carte">
        <h2>Maintenance résolution (J+7)</h2>
        <p className="aide">
          Relance manuelle du job qui rouvre les signalements en{' '}
          <code>RESOLUTION_A_CONFIRMER</code> dont l'échéance de 7 jours est dépassée.
          Le backend exécute aussi ce job automatiquement chaque heure.
        </p>
        <div className="formulaire-actions">
          <button
            type="button"
            className="bouton bouton-secondaire"
            disabled={jobJ7EnCours}
            onClick={executerJobJ7}
          >
            {jobJ7EnCours ? 'Exécution…' : 'Lancer le job J+7'}
          </button>
        </div>
        {etatJobJ7 && <p className={`message-${etatJobJ7.type}`}>{etatJobJ7.message}</p>}
      </section>
    </div>
  );
}

export default ParametresPage;
