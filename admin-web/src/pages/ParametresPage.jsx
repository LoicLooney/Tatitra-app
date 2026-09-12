import { useState } from 'react';
import { getApiUrl, getApiUrlParDefaut, setApiUrl } from '../services/apiClient';
import { getHealth } from '../services/api';
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
    </div>
  );
}

export default ParametresPage;
