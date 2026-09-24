package ihm.sokoban.model;

/**
 * Énumération représentant l'état actuel d'une partie de Sokoban.
 */
public enum EtatPartie {
    /** Le niveau est en cours de résolution */
    EN_COURS("En cours"),

    /** Le niveau est résolu (toutes les caisses sur les cibles) */
    GAGNEE("Niveau résolu !"),

    /** Le niveau est dans une situation sans issue (au moins une caisse coincée). */
    PERDU("Partie perdue (caisse coincée)");

    private final String libelle;

    EtatPartie(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
