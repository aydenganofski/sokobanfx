package ihm.sokoban.model;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Classe principale représentant une partie de Sokoban.
 * Gère le plateau, le déplacement du joueur, la poussée des caisses,
 * la détection de victoire et l'annulation de coups.
 */
public class JeuSokoban {

    private TypeCase[][] grille;
    private int nbLignes;
    private int nbColonnes;
    private int joueurLigne;
    private int joueurColonne;
    private int nbCaisses;
    private int nbCaissesSurCible;
    private int nbMouvements;
    private int nbPoussees;
    private EtatPartie etat;
    private int niveauCourant;

    /** Banque de niveaux courante (textes) et leurs noms. Modifiable via setBanqueNiveaux. */
    private String[] banqueNiveaux;
    private String[] banqueNoms;

    /** Historique pour le undo : chaque entrée = {direction, aPoussé (0/1)} */
    private Deque<int[]> historique;

    /**
     * Crée un jeu Sokoban à partir d'une chaîne de caractères représentant un niveau.
     * Format : chaque ligne séparée par \n, avec les symboles standard.
     *
     * @param niveau La chaîne décrivant le niveau
     * @throws SokobanException si le niveau est invalide
     */
    public JeuSokoban(String niveau) {
        historique = new ArrayDeque<>();
        niveauCourant = -1;
        // Pas de banque associée : navigation entre niveaux désactivée.
        banqueNiveaux = new String[0];
        banqueNoms = new String[0];
        chargerNiveau(niveau);
    }

    /**
     * Crée un jeu Sokoban et charge le niveau d'index donné depuis NiveauxSokoban.
     *
     * @param indexNiveau L'index du niveau (à partir de 0)
     * @throws SokobanException si l'index est invalide
     */
    public JeuSokoban(int indexNiveau) {
        historique = new ArrayDeque<>();
        // Banque par défaut : les 10 niveaux standards.
        banqueNiveaux = ihm.sokoban.util.NiveauxSokoban.getNiveaux();
        banqueNoms = ihm.sokoban.util.NiveauxSokoban.getNoms();
        chargerNiveauParIndex(indexNiveau);
    }

    /**
     * Crée un jeu Sokoban à partir d'une banque de niveaux fournie par l'IHM
     * (par exemple : niveaux tutoriel, ou niveaux chargés depuis un dossier .xsb).
     *
     * @param niveaux    Tableau de niveaux (chaînes Sokoban). Doit être non vide.
     * @param noms       Tableau de noms de niveaux (peut être null ou plus court que niveaux).
     * @param indexInitial Index du niveau à charger en premier (0 par défaut).
     */
    public JeuSokoban(String[] niveaux, String[] noms, int indexInitial) {
        if (niveaux == null || niveaux.length == 0) {
            throw new SokobanException(SokobanException.TypeErreur.NIVEAU_INVALIDE,
                "La banque de niveaux est vide.");
        }
        historique = new ArrayDeque<>();
        banqueNiveaux = niveaux.clone();
        banqueNoms = (noms == null) ? new String[0] : noms.clone();
        chargerNiveauParIndex(indexInitial);
    }

    /**
     * Remplace la banque de niveaux courante et charge le premier niveau.
     * Utile pour basculer entre tutoriel / standard / dossier .xsb depuis l'IHM.
     *
     * @param niveaux Tableau de niveaux (non null, non vide).
     * @param noms    Tableau de noms (peut être null).
     */
    public void setBanqueNiveaux(String[] niveaux, String[] noms) {
        if (niveaux == null || niveaux.length == 0) {
            throw new SokobanException(SokobanException.TypeErreur.NIVEAU_INVALIDE,
                "La banque de niveaux est vide.");
        }
        banqueNiveaux = niveaux.clone();
        banqueNoms = (noms == null) ? new String[0] : noms.clone();
        chargerNiveauParIndex(0);
    }

    // ==================== Chargement de niveau ====================

    /**
     * Charge un niveau à partir de sa représentation textuelle.
     *
     * @param niveau La chaîne décrivant le niveau
     * @throws SokobanException si le niveau est invalide
     */
    public void chargerNiveau(String niveau) {
        if (niveau == null || niveau.isEmpty()) {
            throw new SokobanException(SokobanException.TypeErreur.NIVEAU_INVALIDE);
        }

        String[] lignes = niveau.split("\n");
        nbLignes = lignes.length;
        nbColonnes = 0;
        for (String ligne : lignes) {
            nbColonnes = Math.max(nbColonnes, ligne.length());
        }

        grille = new TypeCase[nbLignes][nbColonnes];
        int nbJoueurs = 0;
        nbCaisses = 0;
        int nbCibles = 0;
        nbCaissesSurCible = 0;

        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColonnes; j++) {
                char c = (j < lignes[i].length()) ? lignes[i].charAt(j) : ' ';
                TypeCase tc = TypeCase.fromChar(c);

                if (tc.estJoueur()) {
                    nbJoueurs++;
                    joueurLigne = i;
                    joueurColonne = j;
                }
                if (tc.estCaisse()) {
                    nbCaisses++;
                }
                if (tc.estCible()) {
                    nbCibles++;
                }
                if (tc == TypeCase.CAISSE_SUR_CIBLE) {
                    nbCaissesSurCible++;
                }

                grille[i][j] = tc;
            }
        }

        if (nbJoueurs == 0) {
            throw new SokobanException(SokobanException.TypeErreur.AUCUN_JOUEUR);
        }
        if (nbJoueurs > 1) {
            throw new SokobanException(SokobanException.TypeErreur.PLUSIEURS_JOUEURS);
        }
        if (nbCaisses != nbCibles) {
            throw new SokobanException(SokobanException.TypeErreur.CAISSES_CIBLES_DIFFERENTES,
                "Caisses: " + nbCaisses + ", Cibles: " + nbCibles);
        }

        nbMouvements = 0;
        nbPoussees = 0;
        historique.clear();
        etat = (nbCaissesSurCible == nbCaisses) ? EtatPartie.GAGNEE : EtatPartie.EN_COURS;
    }

    /**
     * Charge un niveau par son index depuis la banque de niveaux.
     *
     * @param index L'index du niveau (à partir de 0)
     */
    public void chargerNiveauParIndex(int index) {
        if (banqueNiveaux == null || banqueNiveaux.length == 0) {
            throw new SokobanException(SokobanException.TypeErreur.NIVEAU_INEXISTANT,
                "Aucune banque de niveaux associée à ce jeu.");
        }
        if (index < 0 || index >= banqueNiveaux.length) {
            throw new SokobanException(SokobanException.TypeErreur.NIVEAU_INEXISTANT,
                "Index " + index + " invalide. Niveaux disponibles : 0 à " + (banqueNiveaux.length - 1));
        }
        niveauCourant = index;
        chargerNiveau(banqueNiveaux[index]);
    }

    // ==================== Déplacement ====================

    /**
     * Déplace le joueur dans la direction donnée.
     * Pousse une caisse si elle se trouve dans la direction et que la case derrière est libre.
     *
     * @param direction La direction de déplacement
     * @return Le résultat du mouvement
     */
    public ResultatMouvement deplacer(Direction direction) {
        if (etat != EtatPartie.EN_COURS) {
            // Partie terminée (gagnée ou perdue) : on lève une exception pour
            // forcer l'IHM à empêcher tout déplacement après la fin.
            // L'IHM doit consulter getEtatPartie() / isNiveauTermine() / isPerdu()
            // avant d'appeler deplacer().
            throw new SokobanException(SokobanException.TypeErreur.MOUVEMENT_APRES_FIN,
                SokobanException.TypeErreur.MOUVEMENT_APRES_FIN.getMessage()
                + " (état actuel : " + etat + ")");
        }

        int nl = joueurLigne + direction.getDl();
        int nc = joueurColonne + direction.getDc();

        // Hors limites ou mur
        if (!estDansGrille(nl, nc) || grille[nl][nc].estMur()) {
            return ResultatMouvement.BLOQUE;
        }

        // Case libre (sol ou cible) → déplacement simple
        if (grille[nl][nc].estLibre()) {
            effectuerDeplacement(nl, nc, direction, false);
            return ResultatMouvement.DEPLACE;
        }

        // Caisse → tenter de pousser
        if (grille[nl][nc].estCaisse()) {
            int nlCaisse = nl + direction.getDl();
            int ncCaisse = nc + direction.getDc();

            // La case derrière la caisse doit être libre
            if (!estDansGrille(nlCaisse, ncCaisse) || !grille[nlCaisse][ncCaisse].estLibre()) {
                return ResultatMouvement.BLOQUE;
            }

            effectuerPoussee(nl, nc, nlCaisse, ncCaisse, direction);
            return ResultatMouvement.POUSSE;
        }

        return ResultatMouvement.BLOQUE;
    }

    /**
     * Effectue un déplacement simple du joueur (sans poussée).
     */
    private void effectuerDeplacement(int nl, int nc, Direction direction, boolean fromUndo) {
        // Retirer le joueur de l'ancienne position
        if (grille[joueurLigne][joueurColonne] == TypeCase.JOUEUR_SUR_CIBLE) {
            grille[joueurLigne][joueurColonne] = TypeCase.CIBLE;
        } else {
            grille[joueurLigne][joueurColonne] = TypeCase.SOL;
        }

        // Placer le joueur à la nouvelle position
        if (grille[nl][nc] == TypeCase.CIBLE) {
            grille[nl][nc] = TypeCase.JOUEUR_SUR_CIBLE;
        } else {
            grille[nl][nc] = TypeCase.JOUEUR;
        }

        joueurLigne = nl;
        joueurColonne = nc;

        if (!fromUndo) {
            nbMouvements++;
            historique.push(new int[]{direction.ordinal(), 0});
        }
    }

    /**
     * Effectue une poussée : déplace le joueur et la caisse.
     */
    private void effectuerPoussee(int nlJoueur, int ncJoueur,
                                   int nlCaisse, int ncCaisse, Direction direction) {
        // Retirer le joueur de l'ancienne position
        if (grille[joueurLigne][joueurColonne] == TypeCase.JOUEUR_SUR_CIBLE) {
            grille[joueurLigne][joueurColonne] = TypeCase.CIBLE;
        } else {
            grille[joueurLigne][joueurColonne] = TypeCase.SOL;
        }

        // Retirer la caisse de son ancienne position
        boolean caisseEtaitSurCible = (grille[nlJoueur][ncJoueur] == TypeCase.CAISSE_SUR_CIBLE);
        if (caisseEtaitSurCible) {
            nbCaissesSurCible--;
        }

        // Placer le joueur là où était la caisse
        if (caisseEtaitSurCible) {
            grille[nlJoueur][ncJoueur] = TypeCase.JOUEUR_SUR_CIBLE;
        } else {
            grille[nlJoueur][ncJoueur] = TypeCase.JOUEUR;
        }

        // Placer la caisse à sa nouvelle position
        if (grille[nlCaisse][ncCaisse] == TypeCase.CIBLE) {
            grille[nlCaisse][ncCaisse] = TypeCase.CAISSE_SUR_CIBLE;
            nbCaissesSurCible++;
        } else {
            grille[nlCaisse][ncCaisse] = TypeCase.CAISSE;
        }

        joueurLigne = nlJoueur;
        joueurColonne = ncJoueur;

        nbMouvements++;
        nbPoussees++;
        historique.push(new int[]{direction.ordinal(), 1});

        // Vérifier la victoire en priorité (gagner > perdre).
        if (nbCaissesSurCible == nbCaisses) {
            etat = EtatPartie.GAGNEE;
        } else if (estCaisseCoinceeApresPoussee(nlCaisse, ncCaisse)) {
            // La caisse vient d'être poussée dans un coin sans cible :
            // la partie est perdue (sauf undo).
            etat = EtatPartie.PERDU;
        }
    }

    // ==================== Détection de blocage (corner-trap simple) ====================

    /**
     * Détection "corner-trap" simple : la caisse qu'on vient de pousser est
     * sur un sol (pas une cible) et a deux murs perpendiculaires adjacents.
     * Aucun coup futur ne pourra la dégager → partie perdue.
     *
     * Volontairement très conservateur : pas de faux positif, mais ne détecte
     * pas tous les blocages (bord-mort, deadlocks combinés, etc.).
     */
    private boolean estCaisseCoinceeApresPoussee(int l, int c) {
        if (grille[l][c] != TypeCase.CAISSE) {
            return false; // Caisse sur cible → pas un blocage.
        }
        boolean murHaut  = !estDansGrille(l - 1, c) || grille[l - 1][c].estMur();
        boolean murBas   = !estDansGrille(l + 1, c) || grille[l + 1][c].estMur();
        boolean murGauche = !estDansGrille(l, c - 1) || grille[l][c - 1].estMur();
        boolean murDroite = !estDansGrille(l, c + 1) || grille[l][c + 1].estMur();
        boolean coinVertical   = (murHaut || murBas) && (murGauche || murDroite);
        return coinVertical;
    }

    // ==================== Annulation (Undo) ====================

    /**
     * Annule le dernier mouvement.
     *
     * @throws SokobanException s'il n'y a rien à annuler
     */
    public void annuler() {
        if (historique.isEmpty()) {
            throw new SokobanException(SokobanException.TypeErreur.RIEN_A_ANNULER);
        }

        int[] dernierCoup = historique.pop();
        Direction direction = Direction.values()[dernierCoup[0]];
        boolean aPousseCaisse = (dernierCoup[1] == 1);
        Direction opposee = direction.getOpposee();

        if (aPousseCaisse) {
            // Annuler la poussée : remettre la caisse et le joueur
            int nlCaisse = joueurLigne + direction.getDl();
            int ncCaisse = joueurColonne + direction.getDc();

            // Retirer la caisse de sa position actuelle
            boolean caisseSurCible = (grille[nlCaisse][ncCaisse] == TypeCase.CAISSE_SUR_CIBLE);
            if (caisseSurCible) {
                grille[nlCaisse][ncCaisse] = TypeCase.CIBLE;
                nbCaissesSurCible--;
            } else {
                grille[nlCaisse][ncCaisse] = TypeCase.SOL;
            }

            // Remettre la caisse à la position du joueur
            if (grille[joueurLigne][joueurColonne] == TypeCase.JOUEUR_SUR_CIBLE) {
                grille[joueurLigne][joueurColonne] = TypeCase.CAISSE_SUR_CIBLE;
                nbCaissesSurCible++;
            } else {
                grille[joueurLigne][joueurColonne] = TypeCase.CAISSE;
            }

            // Remettre le joueur à sa position précédente
            int ancLigne = joueurLigne + opposee.getDl();
            int ancColonne = joueurColonne + opposee.getDc();
            if (grille[ancLigne][ancColonne] == TypeCase.CIBLE) {
                grille[ancLigne][ancColonne] = TypeCase.JOUEUR_SUR_CIBLE;
            } else {
                grille[ancLigne][ancColonne] = TypeCase.JOUEUR;
            }

            joueurLigne = ancLigne;
            joueurColonne = ancColonne;
            nbPoussees--;
        } else {
            // Annuler un déplacement simple
            int ancLigne = joueurLigne + opposee.getDl();
            int ancColonne = joueurColonne + opposee.getDc();

            // Retirer le joueur de la position actuelle
            if (grille[joueurLigne][joueurColonne] == TypeCase.JOUEUR_SUR_CIBLE) {
                grille[joueurLigne][joueurColonne] = TypeCase.CIBLE;
            } else {
                grille[joueurLigne][joueurColonne] = TypeCase.SOL;
            }

            // Remettre le joueur à sa position précédente
            if (grille[ancLigne][ancColonne] == TypeCase.CIBLE) {
                grille[ancLigne][ancColonne] = TypeCase.JOUEUR_SUR_CIBLE;
            } else {
                grille[ancLigne][ancColonne] = TypeCase.JOUEUR;
            }

            joueurLigne = ancLigne;
            joueurColonne = ancColonne;
        }

        nbMouvements--;
        // Après une annulation, on revient toujours en cours :
        // c'est précisément l'usage attendu pour sortir d'un état PERDU.
        etat = EtatPartie.EN_COURS;
    }

    /**
     * Indique si un undo est possible.
     */
    public boolean peutAnnuler() {
        return !historique.isEmpty();
    }

    // ==================== Utilitaires ====================

    /**
     * Vérifie si des coordonnées sont dans la grille.
     */
    private boolean estDansGrille(int ligne, int colonne) {
        return ligne >= 0 && ligne < nbLignes && colonne >= 0 && colonne < nbColonnes;
    }

    /**
     * Réinitialise le niveau courant.
     */
    public void reset() {
        if (niveauCourant >= 0) {
            chargerNiveauParIndex(niveauCourant);
        }
    }

    // ==================== Accesseurs ====================

    /**
     * Retourne le type de case à la position donnée.
     */
    public TypeCase getCase(int ligne, int colonne) {
        if (!estDansGrille(ligne, colonne)) {
            return TypeCase.VIDE;
        }
        return grille[ligne][colonne];
    }

    /**
     * Retourne une copie de la grille.
     */
    public TypeCase[][] getGrille() {
        TypeCase[][] copie = new TypeCase[nbLignes][nbColonnes];
        for (int i = 0; i < nbLignes; i++) {
            copie[i] = grille[i].clone();
        }
        return copie;
    }

    public int getNbLignes() {
        return nbLignes;
    }

    public int getNbColonnes() {
        return nbColonnes;
    }

    public int getJoueurLigne() {
        return joueurLigne;
    }

    public int getJoueurColonne() {
        return joueurColonne;
    }

    public int getNbCaisses() {
        return nbCaisses;
    }

    public int getNbCaissesSurCible() {
        return nbCaissesSurCible;
    }

    public int getNbMouvements() {
        return nbMouvements;
    }

    public int getNbPoussees() {
        return nbPoussees;
    }

    public EtatPartie getEtatPartie() {
        return etat;
    }

    public boolean isNiveauTermine() {
        return etat == EtatPartie.GAGNEE;
    }

    /**
     * Indique si la partie est dans un état "perdu" (caisse coincée).
     * L'IHM doit alors proposer Undo ou Reset.
     */
    public boolean isPerdu() {
        return etat == EtatPartie.PERDU;
    }

    /**
     * Indique si une partie peut accepter de nouveaux déplacements.
     * Équivalent à etat == EN_COURS.
     */
    public boolean peutJouer() {
        return etat == EtatPartie.EN_COURS;
    }

    public int getNiveauCourant() {
        return niveauCourant;
    }

    /**
     * Retourne le nombre total de niveaux disponibles.
     */
    public int getNbNiveaux() {
        return banqueNiveaux == null ? 0 : banqueNiveaux.length;
    }

    /**
     * Retourne le nom du niveau courant (s'il est connu), sinon "Niveau N".
     */
    public String getNomNiveauCourant() {
        if (banqueNoms != null && niveauCourant >= 0 && niveauCourant < banqueNoms.length) {
            return banqueNoms[niveauCourant];
        }
        return "Niveau " + (niveauCourant + 1);
    }

    /**
     * Indique si le niveau courant est le dernier de la banque.
     */
    public boolean estDernierNiveau() {
        return niveauCourant >= 0 && niveauCourant == getNbNiveaux() - 1;
    }

    /**
     * Passe au niveau suivant.
     *
     * @return true si un niveau suivant existait, false sinon
     */
    public boolean niveauSuivant() {
        if (niveauCourant >= 0 && niveauCourant < getNbNiveaux() - 1) {
            chargerNiveauParIndex(niveauCourant + 1);
            return true;
        }
        return false;
    }

    /**
     * Revient au niveau précédent.
     *
     * @return true si un niveau précédent existait, false sinon
     */
    public boolean niveauPrecedent() {
        if (niveauCourant > 0) {
            chargerNiveauParIndex(niveauCourant - 1);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColonnes; j++) {
                sb.append(grille[i][j].getSymbole());
            }
            sb.append('\n');
        }
        sb.append("Mouvements: ").append(nbMouvements);
        sb.append(" | Poussées: ").append(nbPoussees);
        sb.append(" | Caisses: ").append(nbCaissesSurCible).append("/").append(nbCaisses);
        return sb.toString();
    }
}
