package ihm.sokoban.model;

/**
 * Énumération des résultats possibles d'un mouvement.
 */
public enum ResultatMouvement {
    /** Le joueur s'est déplacé sans pousser de caisse */
    DEPLACE("Déplacement effectué."),

    /** Le joueur s'est déplacé en poussant une caisse */
    POUSSE("Caisse poussée."),

    /** Le mouvement est bloqué (mur ou caisse non poussable) */
    BLOQUE("Mouvement impossible."),

    /** Le niveau est déjà terminé */
    NIVEAU_TERMINE("Le niveau est déjà terminé.");

    private final String message;

    ResultatMouvement(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
