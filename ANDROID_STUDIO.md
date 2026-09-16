# Important — ouverture Android Studio

Ce dépôt contient **trois parties** :
- `app/` → module **Android Gradle**
- `backend/` → Node.js (pas un module Java)
- `admin-web/` → Vite/React (pas un module Java)

## Correct

**File → Open** → sélectionner la **racine du dépôt** :

`D:\ITU\Master\Master 1\Développement Mobile\Projet\Tatitra-app`

C'est bien la racine qu'il faut ouvrir, pas le sous-dossier `app/` : `settings.gradle.kts` s'y
trouve et déclare `include(":app")`. Ouvrir `app/` directement prive Gradle de sa configuration.

Puis **Trust Project** → laisser Gradle synchroniser.

## Juste pour démarrer l'émulateur

Aucun projet n'est nécessaire : sur l'écran d'accueil d'Android Studio,
**More Actions** (ou ⋮) → **Virtual Device Manager** → ▶ sur l'AVD voulu.

## Si le message d'erreur apparaît

> you can't have non-Gradle Java modules and Android-Gradle modules in one project

1. Cliquer sur l’option pour **retirer les modules non-Gradle**
2. Ou : **File → Close Project**, puis rouvrir **seulement** `Tatitra-app`
3. **File → Sync Project with Gradle Files**

Ne pas ouvrir le dossier parent `Développement Mobile` dans Android Studio (il mélange plusieurs projets).

Le backend et l’admin se lancent à part dans un terminal (`npm run dev`), pas comme modules Android.
