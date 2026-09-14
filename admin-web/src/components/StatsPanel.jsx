import { CATEGORIES, LIBELLES_CATEGORIE, LIBELLES_STATUT, STATUTS } from '../constants';

/**
 * Synthèse chiffrée des signalements reçus (F-ADM-10).
 * Tout est calculé côté client à partir de la liste déjà chargée : pas d'endpoint
 * de statistiques supplémentaire à maintenir pour le MVP.
 */
function StatsPanel({ signalements }) {
  const parStatut = compter(signalements, (item) => item.statut);
  const parCategorie = compter(signalements, (item) => item.categorie);
  const nombreDemo = signalements.filter((item) => item.isDemo).length;
  const nombreAvecPhoto = signalements.filter((item) => Boolean(item.photoUrl)).length;

  return (
    <section className="carte">
      <h2>Synthèse</h2>

      <div className="stats-resume">
        <ChiffreCle valeur={signalements.length} libelle="Signalements reçus" />
        <ChiffreCle
          valeur={parStatut.RESOLUTION_A_CONFIRMER || 0}
          libelle="Résolutions à confirmer"
        />
        <ChiffreCle valeur={parStatut.REOUVERT_NON_RESOLU || 0} libelle="Rouverts" />
        <ChiffreCle valeur={nombreAvecPhoto} libelle="Avec photo" />
        <ChiffreCle valeur={nombreDemo} libelle="Données de démonstration" />
      </div>

      <div className="stats-colonnes">
        <TableauRepartition
          titre="Par statut"
          lignes={STATUTS.map((statut) => ({
            libelle: LIBELLES_STATUT[statut],
            valeur: parStatut[statut] || 0,
          }))}
          total={signalements.length}
        />
        <TableauRepartition
          titre="Par catégorie"
          lignes={CATEGORIES.map((categorie) => ({
            libelle: LIBELLES_CATEGORIE[categorie],
            valeur: parCategorie[categorie] || 0,
          }))}
          total={signalements.length}
        />
      </div>
    </section>
  );
}

function ChiffreCle({ valeur, libelle }) {
  return (
    <div className="chiffre-cle">
      <span className="chiffre-cle-valeur">{valeur}</span>
      <span className="chiffre-cle-libelle">{libelle}</span>
    </div>
  );
}

function TableauRepartition({ titre, lignes, total }) {
  return (
    <div className="repartition">
      <h3>{titre}</h3>
      <table className="table-repartition">
        <tbody>
          {lignes.map((ligne) => (
            <tr key={ligne.libelle}>
              <td>{ligne.libelle}</td>
              <td className="repartition-valeur">{ligne.valeur}</td>
              <td className="repartition-part">{calculerPart(ligne.valeur, total)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function compter(signalements, extraireCle) {
  return signalements.reduce((accumulateur, item) => {
    const cle = extraireCle(item);
    if (!cle) return accumulateur;
    accumulateur[cle] = (accumulateur[cle] || 0) + 1;
    return accumulateur;
  }, {});
}

function calculerPart(valeur, total) {
  if (!total) return '—';
  return `${Math.round((valeur / total) * 100)} %`;
}

export default StatsPanel;
