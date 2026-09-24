package ihm.sokoban.util;

/**
 * Banque de niveaux Sokoban prédéfinis.
 * Les niveaux sont classés par difficulté croissante.
 *
 * Symboles :
 *   # = mur, espace = sol, . = cible, $ = caisse,
 *   @ = joueur, * = caisse sur cible, + = joueur sur cible
 */
public class NiveauxSokoban {

    private NiveauxSokoban() {
        // Classe utilitaire
    }

    private static final String[] NIVEAUX = {

        // ===== Niveau 0 : Tutoriel (1 caisse) =====
        "######\n" +
        "#    #\n" +
        "# @$ #\n" +
        "#  . #\n" +
        "#    #\n" +
        "######",

        // ===== Niveau 1 : Couloir (1 caisse) =====
        "  ####\n" +
        "###  #\n" +
        "#    #\n" +
        "# @$.#\n" +
        "#    #\n" +
        "######",

        // ===== Niveau 2 : En L (2 caisses) =====
        "#####\n" +
        "#   #\n" +
        "# $ #\n" +
        "#.@.#\n" +
        "# $ #\n" +
        "#   #\n" +
        "#####",

        // ===== Niveau 3 : Petit entrepôt (2 caisses) =====
        "  #####\n" +
        "###   #\n" +
        "#.@$  #\n" +
        "### $ #\n" +
        "#.## ##\n" +
        "#     #\n" +
        "# $ . #\n" +
        "#  ####\n" +
        "####",

        // ===== Niveau 4 : Croisement (3 caisses) =====
        "  ####\n" +
        "  #  #\n" +
        "  #  #\n" +
        "###  ###\n" +
        "#  $.  #\n" +
        "# $@$ #\n" +
        "#  .  #\n" +
        "###.###\n" +
        "  #  #\n" +
        "  ####",

        // ===== Niveau 5 : Salle double (3 caisses) =====
        "  ####\n" +
        "###  ####\n" +
        "#     . #\n" +
        "# #  #$ #\n" +
        "# . #.$ #\n" +
        "#   @ $ #\n" +
        "#  ######\n" +
        "####",

        // ===== Niveau 6 : Zigzag (3 caisses) =====
        "####\n" +
        "#  ####\n" +
        "# $$  #\n" +
        "#  $  #\n" +
        "## .  #\n" +
        " #@. ##\n" +
        " #.  #\n" +
        " #####",

        // ===== Niveau 7 : Classique (4 caisses) =====
        "  #####\n" +
        "  #   #\n" +
        "  # # ##\n" +
        "### #  #\n" +
        "#  $$ .#\n" +
        "#  $  .#\n" +
        "## $# .#\n" +
        " # @ .#\n" +
        " ######",

        // ===== Niveau 8 : Labyrinthe (4 caisses) =====
        "########\n" +
        "#   #  #\n" +
        "# $  $ #\n" +
        "# .#.  #\n" +
        "# .#.$ #\n" +
        "#  # $ #\n" +
        "#  @ ###\n" +
        "######",

        // ===== Niveau 9 : Expert (5 caisses) =====
        "  ######\n" +
        "  #    #\n" +
        "### ## ##\n" +
        "# $  $  #\n" +
        "#  .@.  #\n" +
        "# $.$.# #\n" +
        "##      #\n" +
        " ########"
    };

    private static final String[] NOMS = {
        "Tutoriel", "Couloir", "En L", "Petit entrepôt", "Croisement",
        "Salle double", "Zigzag", "Classique", "Labyrinthe", "Expert"
    };

    /**
     * Retourne tous les niveaux disponibles.
     */
    public static String[] getNiveaux() {
        return NIVEAUX.clone();
    }

    /**
     * Retourne tous les noms de niveaux (parallèles à getNiveaux()).
     */
    public static String[] getNoms() {
        return NOMS.clone();
    }

    /**
     * Retourne un niveau par son index (à partir de 0).
     *
     * @param index L'index du niveau
     * @return La chaîne décrivant le niveau
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    public static String getNiveau(int index) {
        if (index < 0 || index >= NIVEAUX.length) {
            throw new IndexOutOfBoundsException("Niveau " + index + " inexistant.");
        }
        return NIVEAUX[index];
    }

    /**
     * Retourne le nombre total de niveaux.
     */
    public static int getNbNiveaux() {
        return NIVEAUX.length;
    }

    /**
     * Retourne le nom descriptif d'un niveau.
     */
    public static String getNomNiveau(int index) {
        if (index >= 0 && index < NOMS.length) {
            return NOMS[index];
        }
        return "Niveau " + (index + 1);
    }
}
