package ihm.sokoban.model;

/**
 * Énumération des types de cases du plateau Sokoban.
 */
public enum TypeCase {
    /** Mur infranchissable */
    MUR('#', "Mur"),

    /** Sol libre */
    SOL(' ', "Sol"),

    /** Emplacement cible (où poser une caisse) */
    CIBLE('.', "Cible"),

    /** Caisse à pousser */
    CAISSE('$', "Caisse"),

    /** Caisse posée sur une cible */
    CAISSE_SUR_CIBLE('*', "Caisse sur cible"),

    /** Le joueur */
    JOUEUR('@', "Joueur"),

    /** Le joueur debout sur une cible */
    JOUEUR_SUR_CIBLE('+', "Joueur sur cible"),

    /** Zone hors du plateau (vide extérieur) */
    VIDE(' ', "Vide");

    private final char symbole;
    private final String libelle;

    TypeCase(char symbole, String libelle) {
        this.symbole = symbole;
        this.libelle = libelle;
    }

    public char getSymbole() {
        return symbole;
    }

    public String getLibelle() {
        return libelle;
    }

    /**
     * Indique si cette case contient un mur.
     */
    public boolean estMur() {
        return this == MUR;
    }

    /**
     * Indique si cette case contient une caisse.
     */
    public boolean estCaisse() {
        return this == CAISSE || this == CAISSE_SUR_CIBLE;
    }

    /**
     * Indique si cette case est une cible (avec ou sans caisse/joueur).
     */
    public boolean estCible() {
        return this == CIBLE || this == CAISSE_SUR_CIBLE || this == JOUEUR_SUR_CIBLE;
    }

    /**
     * Indique si cette case contient le joueur.
     */
    public boolean estJoueur() {
        return this == JOUEUR || this == JOUEUR_SUR_CIBLE;
    }

    /**
     * Indique si cette case est franchissable (sol ou cible libre).
     */
    public boolean estLibre() {
        return this == SOL || this == CIBLE;
    }

    /**
     * Convertit un caractère en TypeCase.
     */
    public static TypeCase fromChar(char c) {
        switch (c) {
            case '#': return MUR;
            case ' ': return SOL;
            case '.': return CIBLE;
            case '$': return CAISSE;
            case '*': return CAISSE_SUR_CIBLE;
            case '@': return JOUEUR;
            case '+': return JOUEUR_SUR_CIBLE;
            default: return VIDE;
        }
    }

    @Override
    public String toString() {
        return libelle;
    }
}
