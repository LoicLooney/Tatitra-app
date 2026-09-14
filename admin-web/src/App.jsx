import { useState } from 'react';
import './App.css';
import Layout from './components/Layout';
import DashboardPage from './pages/DashboardPage';
import ParametresPage from './pages/ParametresPage';
import { getSession } from './services/session';

/**
 * Navigation de l'administration.
 * Deux pages suffisent au MVP : aucune bibliothèque de routage n'est ajoutée tant
 * que l'arborescence reste plate (l'écran de détail s'ouvrira depuis le tableau de bord).
 *
 * La session est détenue ici : l'en-tête et la page Paramètres doivent afficher
 * le même agent au même moment.
 */
function App() {
  const [pageActive, setPageActive] = useState('tableau-de-bord');
  const [session, setSession] = useState(getSession);

  return (
    <Layout pageActive={pageActive} onChangerPage={setPageActive} session={session}>
      {pageActive === 'parametres' ? (
        <ParametresPage session={session} onSessionChangee={setSession} />
      ) : (
        <DashboardPage />
      )}
    </Layout>
  );
}

export default App;
