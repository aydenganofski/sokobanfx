package ihm.sokoban.model;

/**
 * Exception levée lors d'une mauvaise utilisation du jeu Sokoban.
 */
public class SokobanException extends RuntimeException {

    /**
     * Types d'erreurs possibles.
     */
    public enum TypeErreur {
        NIVEAU_INVALIDE("Le format du niveau est invalide."),
        AUCUN_JOUEUR("Le niveau ne contient pas de joueur."),
        PLUSIEURS_JOUEURS("Le niveau contient plusieurs joueurs."),
        CAISSES_CIBLES_DIFFERENTES("Le nombre de caisses ne correspond pas au nombre de cibles."),
        NIVEAU_INEXISTANT("Ce numéro de niveau n'existe pas."),
        RIEN_A_ANNULER("Aucun mouvement à annuler."),
        MOUVEMENT_APRES_FIN("Impossible de déplacer le joueur : la partie est terminée (gagnée ou perdue).");

        private final String message;

        TypeErreur(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private final TypeErreur typeErreur;

    public SokobanException(TypeErreur typeErreur) {
        super(typeErreur.getMessage());
        this.typeErreur = typeErreur;
    }

    public SokobanException(TypeErreur typeErreur, String message) {
        super(message);
        this.typeErreur = typeErreur;
    }

    public TypeErreur getTypeErreur() {
        return typeErreur;
    }
}
