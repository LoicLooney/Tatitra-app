import { useEffect, useState } from 'react';

function CompteARebours({ echeanceIso }) {
  const [reste, setReste] = useState(() => calculerReste(echeanceIso));

  useEffect(() => {
    setReste(calculerReste(echeanceIso));
    const id = window.setInterval(() => {
      setReste(calculerReste(echeanceIso));
    }, 1000);
    return () => window.clearInterval(id);
  }, [echeanceIso]);

  if (!echeanceIso) {
    return (
      <div className="compte-rebours">
        <strong>Délai de confirmation (J+7)</strong>
        <p>Échéance inconnue.</p>
      </div>
    );
  }

  return (
    <div className={`compte-rebours${reste.expire ? ' compte-rebours-expire' : ''}`}>
      <strong>Délai de confirmation (J+7)</strong>
      <p className="aide">
        Échéance : {new Date(echeanceIso).toLocaleString('fr-FR')}
      </p>
      {reste.expire ? (
        <p>Délai dépassé — le job J+7 rouvrira ce dossier.</p>
      ) : (
        <p className="compte-rebours-chiffres" aria-live="polite">
          {reste.jours} j {pad(reste.heures)} h {pad(reste.minutes)} min {pad(reste.secondes)} s
        </p>
      )}
    </div>
  );
}

function calculerReste(echeanceIso) {
  if (!echeanceIso) return { jours: 0, heures: 0, minutes: 0, secondes: 0, expire: true };
  const diff = new Date(echeanceIso).getTime() - Date.now();
  if (Number.isNaN(diff) || diff <= 0) {
    return { jours: 0, heures: 0, minutes: 0, secondes: 0, expire: true };
  }
  const totalSec = Math.floor(diff / 1000);
  const jours = Math.floor(totalSec / 86400);
  const heures = Math.floor((totalSec % 86400) / 3600);
  const minutes = Math.floor((totalSec % 3600) / 60);
  const secondes = totalSec % 60;
  return { jours, heures, minutes, secondes, expire: false };
}

function pad(n) {
  return String(n).padStart(2, '0');
}

export default CompteARebours;
