/** Ossature commune des pages d'administration : en-tête, navigation, contenu. */
function Layout({ pageActive, onChangerPage, session, children }) {
  const onglets = [
    { cle: 'tableau-de-bord', libelle: 'Tableau de bord' },
    { cle: 'parametres', libelle: 'Paramètres' },
  ];

  return (
    <div className="admin-layout">
      <header className="admin-header">
        <img src="/logo_tatitra_horizontal.png" alt="TATITRA" className="admin-logo" />
        <nav className="admin-nav">
          {onglets.map((onglet) => (
            <button
              key={onglet.cle}
              type="button"
              className={pageActive === onglet.cle ? 'nav-link nav-link-actif' : 'nav-link'}
              onClick={() => onChangerPage(onglet.cle)}
            >
              {onglet.libelle}
            </button>
          ))}
        </nav>
        <span className="admin-agent">
          {session ? `${session.nomAgent} · ${session.role}` : 'Session non ouverte'}
        </span>
      </header>

      <main className="admin-contenu">{children}</main>

      <footer className="admin-footer">
        TATITRA — Interface d'administration. Les signalements marqués « démonstration »
        sont des incidents simulés.
      </footer>
    </div>
  );
}

export default Layout;
