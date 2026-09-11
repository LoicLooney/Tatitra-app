package mg.itu.tatitra_app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Gestion des fichiers photo d'un signalement.
 *
 * Les images sont conservées dans le stockage privé de l'application et compressées
 * avant tout envoi : un upload d'image est le point de fragilité principal identifié
 * au §19 du cahier des charges.
 */
object PhotoStorage {

    private const val TAG = "PhotoStorage"
    private const val DOSSIER_PHOTOS = "photos"
    private const val LARGEUR_MAX_PIXELS = 1280
    private const val QUALITE_JPEG = 80

    /** Crée un fichier vide destiné à recevoir la prise de vue de l'appareil photo. */
    fun creerFichierPhoto(context: Context): File {
        val dossier = File(context.filesDir, DOSSIER_PHOTOS).apply { mkdirs() }
        return File(dossier, "signalement_${System.currentTimeMillis()}.jpg")
    }

    /** URI temporaire partagée avec l'application appareil photo (voir FileProvider du manifeste). */
    fun obtenirUriPartage(context: Context, fichier: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fichier)

    /**
     * Copie une image choisie dans la galerie vers le stockage de l'application.
     * Retourne le chemin local, ou null si l'image n'a pas pu être lue.
     */
    suspend fun importerDepuisGalerie(context: Context, source: Uri): String? =
        withContext(Dispatchers.IO) {
            val destination = creerFichierPhoto(context)
            try {
                context.contentResolver.openInputStream(source)?.use { entree ->
                    destination.outputStream().use { sortie -> entree.copyTo(sortie) }
                } ?: return@withContext null
            } catch (erreur: IOException) {
                Log.w(TAG, "Import galerie impossible : ${erreur.message}")
                destination.delete()
                return@withContext null
            }
            optimiser(destination)
            destination.absolutePath
        }

    /**
     * Réduit et redresse une photo déjà présente sur le disque.
     * En cas d'échec, le fichier d'origine est conservé tel quel plutôt que perdu.
     */
    suspend fun optimiser(fichier: File): Boolean = withContext(Dispatchers.IO) {
        if (!fichier.exists() || fichier.length() == 0L) return@withContext false
        try {
            val rotation = lireRotationExif(fichier)
            val image = decoderReduite(fichier) ?: return@withContext false
            val imageRedressee = appliquerRotation(image, rotation)
            fichier.outputStream().use { sortie ->
                imageRedressee.compress(Bitmap.CompressFormat.JPEG, QUALITE_JPEG, sortie)
            }
            imageRedressee.recycle()
            true
        } catch (erreur: IOException) {
            Log.w(TAG, "Compression impossible, photo conservée telle quelle : ${erreur.message}")
            false
        } catch (erreur: OutOfMemoryError) {
            Log.w(TAG, "Image trop volumineuse pour être compressée : ${erreur.message}")
            false
        }
    }

    fun supprimer(chemin: String?) {
        if (chemin == null) return
        val fichier = File(chemin)
        if (fichier.exists() && !fichier.delete()) {
            Log.w(TAG, "Suppression de la photo locale impossible : $chemin")
        }
    }

    /** Décode l'image en la sous-échantillonnant pour ne jamais charger une photo pleine résolution. */
    private fun decoderReduite(fichier: File): Bitmap? {
        val dimensions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(fichier.absolutePath, dimensions)
        val plusGrandCote = maxOf(dimensions.outWidth, dimensions.outHeight)
        if (plusGrandCote <= 0) return null

        var facteur = 1
        while (plusGrandCote / facteur > LARGEUR_MAX_PIXELS) {
            facteur *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = facteur }
        return BitmapFactory.decodeFile(fichier.absolutePath, options)
    }

    private fun lireRotationExif(fichier: File): Float =
        when (
            ExifInterface(fichier.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

    private fun appliquerRotation(image: Bitmap, degres: Float): Bitmap {
        if (degres == 0f) return image
        val matrice = Matrix().apply { postRotate(degres) }
        val pivotee = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrice, true)
        if (pivotee != image) image.recycle()
        return pivotee
    }
}
