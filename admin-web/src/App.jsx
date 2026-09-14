import { useState } from 'react';
import './App.css';
import Layout from './components/Layout';
import DashboardPage from './pages/DashboardPage';
import ParametresPage from './pages/ParametresPage';
import { getSession } from './services/session';

/**
 * Navigation de l'administration (sans react-router).
 * Le détail J4 s'ouvre depuis le tableau de bord via signalementId.
 */
function App() {
  const [pageActive, setPageActive] = useState('tableau-de-bord');
  const [session, setSession] = useState(getSession);
  const [signalementId, setSignalementId] = useState(null);

  function changerPage(page) {
    setSignalementId(null);
    setPageActive(page);
  }

  return (
    <Layout pageActive={pageActive} onChangerPage={changerPage} session={session}>
      {pageActive === 'parametres' ? (
        <ParametresPage session={session} onSessionChangee={setSession} />
      ) : (
        <DashboardPage
          signalementId={signalementId}
          onOuvrirDetail={setSignalementId}
          onFermerDetail={() => setSignalementId(null)}
        />
      )}
    </Layout>
  );
}

export default App;
