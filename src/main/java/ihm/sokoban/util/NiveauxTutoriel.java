package ihm.sokoban.util;

/**
 * Banque de 5 niveaux "tutoriel" Sokoban, tous au format 8×8.
 *
 * Conçus pour le palier A du projet : un étudiant n'ayant codé que la
 * gestion d'une grille de taille fixe peut afficher et résoudre tous
 * ces niveaux sans gérer la variabilité de taille.
 *
 * Difficulté croissante très douce : 1 → 3 caisses.
 *
 * Symboles :
 *   # = mur, espace = sol, . = cible, $ = caisse,
 *   @ = joueur, * = caisse sur cible, + = joueur sur cible
 */
public class NiveauxTutoriel {

    private NiveauxTutoriel() {
        // Classe utilitaire
    }

    private static final String[] NIVEAUX = {

        // ===== Niveau 0 — Pousser une caisse (1 caisse) =====
        "########\n" +
        "#      #\n" +
        "#      #\n" +
        "# @$ . #\n" +
        "#      #\n" +
        "#      #\n" +
        "#      #\n" +
        "########",

        // ===== Niveau 1 — Choisir sa direction (1 caisse) =====
        "########\n" +
        "#      #\n" +
        "#  .   #\n" +
        "#      #\n" +
        "#  $   #\n" +
        "#  @   #\n" +
        "#      #\n" +
        "########",

        // ===== Niveau 2 — Tourner autour (2 caisses) =====
        "########\n" +
        "#      #\n" +
        "# .  . #\n" +
        "#      #\n" +
        "# $@$  #\n" +
        "#      #\n" +
        "#      #\n" +
        "########",

        // ===== Niveau 3 — Petit obstacle (2 caisses) =====
        "########\n" +
        "# .    #\n" +
        "#  $   #\n" +
        "#  ##  #\n" +
        "#   $  #\n" +
        "#   @  #\n" +
        "#    . #\n" +
        "########",

        // ===== Niveau 4 — Trois caisses (3 caisses) =====
        "########\n" +
        "#  ... #\n" +
        "#      #\n" +
        "# $$$  #\n" +
        "#      #\n" +
        "#  @   #\n" +
        "#      #\n" +
        "########"
    };

    private static final String[] NOMS = {
        "Pousser une caisse",
        "Choisir sa direction",
        "Tourner autour",
        "Petit obstacle",
        "Trois caisses"
    };

    /** Taille fixe de toutes les grilles tutoriel (en cases). */
    public static final int TAILLE = 8;

    public static String[] getNiveaux() {
        return NIVEAUX.clone();
    }

    public static String[] getNoms() {
        return NOMS.clone();
    }

    public static int getNbNiveaux() {
        return NIVEAUX.length;
    }

    public static String getNiveau(int index) {
        if (index < 0 || index >= NIVEAUX.length) {
            throw new IndexOutOfBoundsException("Niveau tutoriel " + index + " inexistant.");
        }
        return NIVEAUX[index];
    }

    public static String getNomNiveau(int index) {
        if (index >= 0 && index < NOMS.length) {
            return NOMS[index];
        }
        return "Tutoriel " + (index + 1);
    }
}
