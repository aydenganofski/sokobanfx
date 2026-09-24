package ihm.sokoban.model;

/**
 * Énumération des 4 directions de déplacement.
 */
public enum Direction {
    HAUT(-1, 0, "Haut"),
    BAS(1, 0, "Bas"),
    GAUCHE(0, -1, "Gauche"),
    DROITE(0, 1, "Droite");

    private final int dl;
    private final int dc;
    private final String libelle;

    Direction(int dl, int dc, String libelle) {
        this.dl = dl;
        this.dc = dc;
        this.libelle = libelle;
    }

    /** Décalage en ligne (-1 = haut, +1 = bas) */
    public int getDl() {
        return dl;
    }

    /** Décalage en colonne (-1 = gauche, +1 = droite) */
    public int getDc() {
        return dc;
    }

    /**
     * Retourne la direction opposée.
     */
    public Direction getOpposee() {
        switch (this) {
            case HAUT: return BAS;
            case BAS: return HAUT;
            case GAUCHE: return DROITE;
            case DROITE: return GAUCHE;
            default: return this;
        }
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
