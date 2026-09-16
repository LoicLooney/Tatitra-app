# Installation et lancement

Comment faire tourner TATITRA sur un poste vierge, après un `git clone`.
Pour la présentation du projet et l’identité visuelle, voir [README.md](README.md).

Le projet contient **trois parties** qui se lancent séparément :

| Dossier | Rôle | Technologie | Port |
|---------|------|-------------|------|
| `app/` | Application Android citoyenne | Kotlin + Jetpack Compose | — |
| `backend/` | API REST commune mobile + admin | Node.js + Express | 3000 |
| `admin-web/` | Interface d’administration | React + Vite | 5173 |

---

## 1. Prérequis

| Outil | Version utilisée | Remarque |
|-------|------------------|----------|
| Node.js | 22.x (testé en 22.13.1) | avec npm 10.x |
| JDK | 17 ou plus | Gradle télécharge ensuite tout seul le JDK 25 dont il a besoin |
| Android SDK | plateforme **android-37** | via Android Studio ou `cmdline-tools` |
| Git | — | — |

Le projet utilise le **Gradle Wrapper** : il n’y a pas de Gradle à installer, `./gradlew` s’en
charge (Gradle 9.5, AGP 9.3.2, Kotlin 2.2.10, `compileSdk 37`, `minSdk 24`).

---

## 2. Cloner le projet

```bash
git clone https://github.com/LoicLooney/Tatitra-app.git
cd Tatitra-app
```

Rien à configurer à la racine : `local.properties` (chemin du SDK Android) est propre à chaque
poste et n’est pas versionné — Android Studio le crée à la première ouverture du projet. Si tu
compiles en ligne de commande sans passer par Studio, crée-le à la main :

```
sdk.dir=C\:\\Users\\TON_NOM\\AppData\\Local\\Android\\Sdk
```

---

## 3. Backend (à lancer en premier)

```bash
cd backend
npm install
cp .env.example .env   # puis renseigner DATABASE_URL — obligatoire
npm run db:init        # crée la table signalements (une seule fois)
npm run dev            # ou : npm start
```

Vérification : `http://localhost:3000/health` doit répondre `{"status":"ok","db":"up"}`
(ou `npm run health` dans `backend/`).

Le serveur écoute sur `0.0.0.0` : accessible depuis le téléphone de démo sur le même Wi-Fi
(`http://IP_DU_PC:3000/health`).

### Variables d’environnement (`backend/.env`)

| Variable | Rôle | Si absente |
|----------|------|------------|
| `PORT` | Port d’écoute | `3000` |
| `SUPABASE_URL` | URL du projet Supabase | ⬇ |
| `SUPABASE_SERVICE_KEY` | Clé **secrète** Supabase (`sb_secret_…`) | les photos sont écrites dans `backend/uploads/` et servies par Express — la démo fonctionne quand même |
| `SUPABASE_BUCKET` | Nom du bucket de stockage | `signalements` |
| `MAX_UPLOAD_SIZE_MB` | Taille maximale d’une photo | `5` |
| `PUBLIC_BASE_URL` | URL publique du backend, pour les liens des photos en stockage local | `http://localhost:$PORT` |
| **`DATABASE_URL`** | PostgreSQL / Supabase — **obligatoire** | toute l’API répond 500 et `/health` renvoie 503 |
| `TATITRA_ADMIN_KEY` | Clé exigée pour agir en tant qu’ADMIN sur les endpoints de résolution | rôle ADMIN accepté sans contrôle (mode salle de TP) |
| `DATABASE_CA_CERT` | Chemin du certificat d’autorité de la base, pour vérifier réellement TLS | TLS actif mais **non vérifié** (chaîne Supabase auto-signée) — un avertissement est affiché au démarrage |
| `DISABLE_CRON` | `1` désactive le job horaire J+7 | job actif |

**Les valeurs réelles ne sont jamais dans le dépôt.** Demande-les à ton binôme par un canal
privé, ou mieux : fais-toi inviter sur le projet Supabase (*Organization settings → Team →
Invite member*) et lis les clés toi-même dans *Project Settings → API Keys*.

> Les signalements sont stockés dans **PostgreSQL (Supabase)** : ils survivent au redémarrage
> du backend. Les photos vont dans Supabase Storage, ou dans `backend/uploads/` si les clés
> Supabase ne sont pas configurées.

### Tests du backend

```bash
npm test               # lanceur intégré de Node, aucune dépendance supplémentaire
```

55 tests couvrent la validation, l’idempotence, la validation croisée de résolution, la règle
J+7 et la cohérence du vocabulaire métier entre les trois parties du projet. Ils utilisent une
base simulée : aucune connexion PostgreSQL n’est nécessaire pour les lancer.

---

## 4. Admin Web

```bash
cd admin-web
npm install
npm run dev          # http://localhost:5173 (+ URL Network pour le téléphone)
```

Aucune configuration n’est nécessaire : l’interface vise `http://localhost:3000` par défaut, et
l’adresse de l’API est modifiable **à chaud** dans l’onglet *Paramètres* (bouton « Tester la
connexion » pour vérifier). Pour changer la valeur par défaut, copie `.env.example` en
`.env.local` et renseigne `VITE_API_URL`.

Autres commandes : `npm run lint`, `npm run build`.

---

## 5. Application Android

```bash
./gradlew :app:assembleDebug      # APK dans app/build/outputs/apk/debug/
./gradlew :app:installDebug       # installe sur l’appareil ou l’émulateur connecté
```

### Quelle adresse d’API choisir ?

L’URL est injectée à la compilation dans `BuildConfig.API_BASE_URL`. Elle est lue dans cet ordre :
option `-P` en ligne de commande, puis `gradle.properties`, puis `local.properties`.

`gradle.properties` (versionné) fixe `tatitra.apiBaseUrl=http://127.0.0.1:3000/`. **Cette valeur
suppose `adb reverse`**, qui fonctionne aussi bien sur émulateur que sur téléphone :

```bash
adb reverse tcp:3000 tcp:3000     # à refaire après chaque redémarrage de l’appareil
./gradlew :app:installDebug
```

| Situation | Commande |
|-----------|----------|
| **Émulateur ou téléphone USB** (le plus fiable) | `adb reverse tcp:3000 tcp:3000` puis `./gradlew :app:installDebug` |
| Émulateur sans `adb reverse` | `./gradlew :app:installDebug -Ptatitra.apiBaseUrl=http://10.0.2.2:3000/` |
| Téléphone en Wi-Fi | `./gradlew :app:installDebug -Ptatitra.apiBaseUrl=http://IP_DU_PC:3000/` (même réseau) |

`adb reverse` fait passer le port 3000 par le câble USB ou le canal de l’émulateur : pas d’IP à
chercher, et ça marche même sans Wi-Fi. `10.0.2.2` est l’adresse de l’hôte vue **uniquement** depuis
un émulateur ; `127.0.0.1` sans `adb reverse` désignerait l’appareil lui-même et échouerait.

### Connexion Supabase (écran de login)

L’application s’ouvre sur un écran de connexion relié à **Supabase Auth** (e-mail + mot de passe).
Il reste franchissable : le bouton **« Continuer en mode démonstration »** entre dans l’application
sans compte — l’offline-first est préservé, on peut signaler sans réseau dès le premier lancement.

Deux valeurs sont à placer dans `local.properties` (fichier **jamais versionné**) :

```properties
tatitra.supabaseUrl=https://<ref-projet>.supabase.co
tatitra.supabaseAnonKey=sb_publishable_xxxxxxxxxxxxxxxx
```

⚠️ La clé attendue est la clé **publiable** (*Project Settings → API Keys → Publishable key*,
anciennement « anon »). Elle est conçue pour être embarquée dans une application cliente. La clé
`sb_secret_…` du backend ne doit **jamais** figurer ici : elle contourne toutes les règles d’accès.

Côté Supabase, activer *Authentication → Sign In / Providers → **Email***.
Si l’option **Confirm email** y est cochée, « Créer un compte » n’ouvre pas de session
immédiatement : l’application affiche alors qu’un lien de confirmation a été envoyé.

Sans ces deux valeurs, l’application se lance quand même : l’écran de connexion annonce que
l’authentification n’est pas configurée et seul le mode démonstration est proposé.

L’authentification par **téléphone** affichée dans le prototype n’est pas activée : Supabase exige
pour cela un fournisseur SMS payant. Saisir un numéro affiche un message qui l’explique.

### Tests unitaires

```bash
./gradlew :app:testDebugUnitTest
```

94 tests : règles de saisie, conversions Room ↔ domaine, comportement du Repository en ligne /
hors ligne / face à un refus serveur, règles d’affichage des actions de résolution, et
authentification (validation du formulaire, ouverture de session, confirmation d’e-mail requise,
traduction des refus Supabase, mode démonstration). Ils tournent sur la JVM avec une base, une API
et un stockage simulés — ni téléphone, ni serveur, ni compte Supabase requis.

⚠️ Ils échouent si le projet est stocké dans un dossier **contenant des accents**
(`Développement Mobile`) : le worker de test Gradle ne retrouve pas ses classes. Les mêmes tests
passent (94/94) depuis un chemin sans accent, par exemple `C:\dev\Tatitra-app`.

---

## 6. Vérifier que tout fonctionne (5 minutes)

1. Backend lancé → `GET /health` répond `ok`.
2. Admin ouvert sur `localhost:5173` → le tableau de bord s’affiche (0 signalement au départ).
3. Application installée → créer un signalement avec catégorie, description et position.
4. Le signalement apparaît dans l’admin après quelques secondes (WorkManager).
5. **Test hors ligne** : activer le mode avion, créer un signalement → il s’affiche
   « En attente de synchronisation ». Désactiver le mode avion → il part tout seul.

---

## 7. Dépannage

**« Git a supprimé mon `local.properties` et mon dossier `app/build/` après un `git pull` »**

C’est voulu, et ce n’est pas un projet cassé : 585 fichiers générés par le build
(`app/build/`, `.gradle/`) plus `local.properties` étaient versionnés par erreur. Ils ont été
retirés du suivi Git. Comme ils étaient suivis chez toi aussi, le `pull` qui apporte ce commit
les efface de ton disque.

**Avant de tirer ce commit**, mets ton `local.properties` de côté et libère le dossier de build —
sinon Git refusera la fusion sur les fichiers que ton dernier build a modifiés :

```bash
./gradlew --stop                          # libère les fichiers tenus par le daemon
cp local.properties local.properties.bak  # PowerShell : Copy-Item local.properties local.properties.bak
rm -rf app/build .gradle                  # PowerShell : Remove-Item -Recurse -Force app\build, .gradle
git pull
cp local.properties.bak local.properties  # PowerShell : Copy-Item local.properties.bak local.properties
```

**Si tu as déjà tiré** et que le fichier a disparu, recrée-le avec le chemin de **ton** SDK :

```
sdk.dir=C\:\\Users\\TON_NOM\\AppData\\Local\\Android\\Sdk
```

Ouvrir le projet dans Android Studio le régénère aussi tout seul. Pour `app/build/`, il n’y a
rien à faire : `./gradlew :app:assembleDebug` le reconstruit.

Ces deux chemins restent désormais locaux à chaque poste : vos SDK peuvent être installés à des
endroits différents sans que cela crée de conflit.

**Le build Gradle échoue sur `Using kotlin.sourceSets DSL … is not allowed with built-in Kotlin`**
Déjà réglé dans `gradle.properties` (`android.disallowKotlinSourceSets=false`), nécessaire pour
que KSP (le compilateur d’annotations de Room) fonctionne avec AGP 9. Si l’erreur revient dans
l’IDE, relance simplement la synchronisation Gradle.

**L’émulateur reste sur un écran noir**
L’AVD a `hw.gpu.enabled = no`. Soit tu l’actives (`hw.gpu.enabled = yes`, `hw.gpu.mode = host`
dans `~/.android/avd/<nom>.avd/config.ini`), soit tu lances avec
`emulator -avd <nom> -gpu swiftshader_indirect` — mais le rendu logiciel consomme énormément de
CPU. **Un téléphone réel en USB reste plus simple et plus rapide.**

**L’application n’atteint pas le backend**
Vérifie l’adresse utilisée à la compilation (section 5). En build `debug`, le HTTP en clair est
autorisé ; en `release`, seul le HTTPS passe (voir `res/xml/network_security_config.xml`).

**Les photos ne s’affichent pas dans l’admin**
Sans clés Supabase, les images sont servies par le backend via `PUBLIC_BASE_URL` : mets-y l’IP du
PC si tu consultes l’admin depuis une autre machine.

---

## 8. Arborescence

```
Tatitra-app/
├── app/                              # Application Android (citoyen)
│   └── src/main/java/mg/itu/tatitra_app/
│       ├── data/local/               # Room : Entity, DAO, Database
│       ├── data/remote/              # Retrofit : API, DTO
│       ├── data/repository/          # arbitrage local / distant
│       ├── domain/                   # modèles et vocabulaire métier
│       ├── ui/                       # navigation, home, report, reports, components, theme
│       ├── worker/                   # WorkManager (synchronisation différée)
│       └── util/                     # photo, GPS, formatage
├── backend/
│   └── src/ routes/ controllers/ services/ db/ middleware/
├── admin-web/
│   ├── public/                       # logos Tatitra
│   └── src/ pages/ components/ services/
├── docs/
│   ├── Cahier_des_charges_TATITRA_v1.2.pdf
│   ├── branding/
│   └── prototypes/
└── Photos/demo/                      # photos d’incidents de démonstration
```

---

## 9. Endpoints

| Méthode | Endpoint | État |
|---------|----------|------|
| `GET` | `/health` | disponible |
| `GET` | `/api/signalements` | disponible |
| `POST` | `/api/signalements` | disponible — validation + idempotence par `clientId` |
| `POST` | `/api/uploads` | disponible — champ `photo`, Supabase Storage ou disque local |
| `GET` | `/api/signalements/:id` | disponible |
| `PATCH` | `/api/signalements/:id/statut` | disponible — triage uniquement (les statuts de résolution sont refusés) |
| `POST` | `/api/signalements/:id/resolution` | disponible — **rôle obligatoire** |
| `POST` | `/api/signalements/:id/resolution/confirm` | disponible — **rôle obligatoire**, refusé si c'est l'auteur de la proposition |
| `POST` | `/api/signalements/:id/resolution/reopen` | disponible — **rôle obligatoire** |
| `POST` | `/api/signalements/jobs/expiration-resolution` | disponible — job J+7 manuel, **réservé au rôle ADMIN** |
| `GET` | `/api/notifications` | à venir |

### Le rôle sur les endpoints de résolution

Les quatre endpoints ci-dessus exigent un rôle, transmis au choix dans le corps JSON
(`{"role": "CITOYEN"}`) ou dans l'en-tête `X-Tatitra-Role`. Les valeurs acceptées sont
`CITOYEN` et `ADMIN` ; toute autre valeur, ou son absence, est refusée en 400.

Si `TATITRA_ADMIN_KEY` est définie dans `backend/.env`, agir en tant qu'`ADMIN` exige en plus
l'en-tête `X-Tatitra-Admin-Key`. Sans cette variable, le rôle `ADMIN` est accepté sans contrôle :
pratique en salle de TP, à définir pour une démonstration publique.

---

## 10. Vocabulaire métier

Les mêmes chaînes exactes sont utilisées partout (Android, backend, base, admin).
Toute nouvelle valeur doit être ajoutée des deux côtés le même jour.

```
Statuts    : EN_ATTENTE_SYNC | ENVOYE | A_VERIFIER | PRIS_EN_CHARGE | REJETE
             RESOLUTION_A_CONFIRMER | RESOLU_CONFIRME | REOUVERT_NON_RESOLU
Catégories : ROUTE | DECHETS | ECLAIRAGE | DRAINAGE | PONT
```

Source de vérité : `app/.../domain/StatutSignalement.kt`, `backend/src/constants.js`,
`admin-web/src/constants.js`.

---

## 11. Identifiants et sécurité

Aucun identifiant (mot de passe PostgreSQL, clé Supabase, jeton) ne doit figurer dans ce dépôt :
tout part sur GitHub. Ils vivent uniquement dans `backend/.env`, ignoré par Git.

- La clé `sb_secret_…` donne un accès complet au projet Supabase : **backend uniquement**, jamais
  dans l’application Android ni dans l’admin web.
- Les clés doivent être régénérées après la soutenance
  (*Settings → API Keys*, *Settings → Database → Reset password*).

Pour l’identité visuelle et les fichiers de branding, voir [README.md](README.md).
