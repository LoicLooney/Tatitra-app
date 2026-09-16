package mg.itu.tatitra_app.domain

/**
 * Règles de saisie de l'écran de connexion.
 *
 * Elles doublent celles de Supabase Auth : refuser localement ce que le serveur
 * refuserait évite un aller-retour réseau — précieux sur une connexion lente — et
 * permet d'expliquer l'erreur dans la langue de l'application.
 *
 * Le prototype propose « téléphone ou email ». L'authentification par SMS exige chez
 * Supabase un fournisseur payant (Twilio et consorts), non retenu pour ce projet :
 * un identifiant qui ressemble à un numéro est donc reconnu comme tel, pour pouvoir
 * l'expliquer à l'utilisateur au lieu de lui opposer un « email invalide » obscur.
 */
object ReglesConnexion {

    /** Minimum imposé par Supabase Auth par défaut. */
    const val LONGUEUR_MOT_DE_PASSE_MIN = 6

    private val FORME_EMAIL = Regex("^[^@\\s]+@[^@\\s.]+\\.[^@\\s]+$")
    private val FORME_TELEPHONE = Regex("^[+0-9][0-9\\s.-]{6,}$")

    fun emailEstValide(email: String): Boolean = FORME_EMAIL.matches(email.trim())

    fun ressembleAUnNumero(identifiant: String): Boolean =
        FORME_TELEPHONE.matches(identifiant.trim())

    fun motDePasseEstValide(motDePasse: String): Boolean =
        motDePasse.length >= LONGUEUR_MOT_DE_PASSE_MIN

    /**
     * Message d'erreur à afficher sous le champ identifiant, ou null s'il est acceptable.
     * Retourne null tant que le champ est vide : on ne réprimande pas un formulaire à peine ouvert.
     */
    fun erreurIdentifiant(identifiant: String): String? = when {
        identifiant.isBlank() -> null
        ressembleAUnNumero(identifiant) ->
            "La connexion par numéro de téléphone n'est pas activée. Utilisez votre adresse e-mail."
        !emailEstValide(identifiant) -> "Adresse e-mail invalide."
        else -> null
    }

    fun erreurMotDePasse(motDePasse: String): String? = when {
        motDePasse.isEmpty() -> null
        !motDePasseEstValide(motDePasse) ->
            "Le mot de passe doit comporter au moins $LONGUEUR_MOT_DE_PASSE_MIN caractères."
        else -> null
    }

    /** Le formulaire est-il envoyable ? Sert à activer ou griser le bouton. */
    fun formulaireEstComplet(identifiant: String, motDePasse: String): Boolean =
        emailEstValide(identifiant) && motDePasseEstValide(motDePasse)
}
