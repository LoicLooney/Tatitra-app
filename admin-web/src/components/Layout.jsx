/** Ossature commune : en-tête TATITRA (logos public/), navigation, contenu. */
function Layout({ pageActive, onChangerPage, session, children }) {
  const onglets = [
    { cle: 'tableau-de-bord', libelle: 'Tableau de bord' },
    { cle: 'parametres', libelle: 'Paramètres' },
  ];

  return (
    <div className="admin-layout">
      <header className="admin-header">
        <a href="/" className="admin-marque" aria-label="TATITRA — accueil administration">
          <img
            src="/logo_symbole.png"
            alt=""
            className="admin-logo-symbole"
            width={32}
            height={32}
          />
          <img
            src="/logo_tatitra_horizontal.png"
            alt="TATITRA"
            className="admin-logo"
          />
        </a>
        <nav className="admin-nav" aria-label="Navigation principale">
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
        <img src="/logo_symbole.png" alt="" className="admin-footer-symbole" width={20} height={20} />
        <span>
          TATITRA — Interface d&apos;administration. Les signalements marqués « démonstration »
          sont des incidents simulés.
        </span>
      </footer>
    </div>
  );
}

export default Layout;
