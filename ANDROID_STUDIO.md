# Important — ouverture Android Studio

Ce dépôt contient **trois parties** :
- `app/` → projet **Android Gradle** (à ouvrir)
- `backend/` → Node.js (pas un module Java)
- `admin-web/` → Vite/React (pas un module Java)

## Correct

**File → Open** → sélectionner uniquement le dossier :

`d:\Master\Développement Mobile\Tatitra-app`

Puis **Trust Project** → laisser Gradle synchroniser.

## Si le message d'erreur apparaît

> you can't have non-Gradle Java modules and Android-Gradle modules in one project

1. Cliquer sur l’option pour **retirer les modules non-Gradle**
2. Ou : **File → Close Project**, puis rouvrir **seulement** `Tatitra-app`
3. **File → Sync Project with Gradle Files**

Ne pas ouvrir le dossier parent `Développement Mobile` dans Android Studio (il mélange plusieurs projets).

Le backend et l’admin se lancent à part dans un terminal (`npm run dev`), pas comme modules Android.
