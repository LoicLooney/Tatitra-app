# Tatitra / LALANA

Application citoyenne de signalement des problèmes d’infrastructures publiques  
(Master 1 Développement Mobile — ITU).

MVP : photo + GPS + Room offline + synchronisation + API + admin Web + validation croisée de résolution.

---

## Séance 8

Synthèse du module (Kotlin → coroutines → cycle de vie → Compose → navigation → MVVM → Room).  
Ouverture KMP. Pas de nouveau mini-TP : focus **projet d’examen LALANA / Tatitra**.

---

## Arborescence (Annexe C du cahier des charges)

```
Tatitra-app/
├── android-app/                 # Application Android (citoyen)
│   └── app/src/main/
│       ├── java/mg/itu/lalana/
│       │   ├── data/
│       │   │   ├── local/       # Room
│       │   │   ├── remote/      # API
│       │   │   └── repository/
│       │   ├── domain/
│       │   ├── ui/
│       │   │   ├── home/
│       │   │   ├── report/      # Nouveau signalement
│       │   │   └── reports/     # Mes signalements
│       │   └── worker/          # WorkManager
│       └── res/
│           ├── drawable/        # Logos Tatitra
│           └── mipmap-*/        # Icônes lanceur
├── backend/                     # API REST
│   └── src/
│       ├── routes/
│       ├── controllers/
│       ├── services/
│       ├── db/
│       └── middleware/
├── admin-web/                   # Interface admin
│   ├── public/                  # Logos pour le web
│   └── src/
│       ├── pages/
│       ├── components/
│       └── services/
├── docs/
│   ├── Cahier_des_charges_LALANA_v1.1.pdf
│   ├── branding/                # Copies des logos/icônes
│   └── prototypes/              # planche + prototype HTML
└── Photos/
    └── demo/                    # Photos de signalement (démo)
```

---

## Branding (photos renommées)

| Fichier | Rôle | Emplacement |
|---------|------|-------------|
| `logo_symbole.png` | Symbole seul (pin + point) | `drawable/`, `docs/branding/`, `admin-web/public/` |
| `logo_tatitra_vertical.png` | Logo vertical (icône + texte) | idem |
| `logo_tatitra_horizontal.png` | Logo horizontal | idem |
| `ic_launcher.png` | Icône app (fond bleu uni) | `mipmap-*/`, `docs/branding/` |
| `ic_launcher_round.png` | Icône app (fond dégradé) | `mipmap-*/`, `docs/branding/` |

`Photos/` est réservé aux **photos d’incidents** de démonstration, pas aux logos.
