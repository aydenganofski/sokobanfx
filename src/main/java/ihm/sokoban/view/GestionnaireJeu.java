package ihm.sokoban.view;

import ihm.sokoban.model.Direction;
import ihm.sokoban.model.JeuSokoban;
import ihm.sokoban.model.SokobanException;
import ihm.sokoban.model.TypeCase;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Gestionnaire du jeu SobokanApp
 * @author Ayden GANOFSKI
 * @version 1.0
 */
public class GestionnaireJeu {

    // ==========================================
    // ATTRIBUTS
    // ==========================================
    private JeuSokoban jeu;
    private GridPane grilleJeu;
    private InterfaceJeuController controleurInterface;
    private final javafx.event.EventHandler<KeyEvent> ecouteurClavier = this::traiterClavier;
    private Direction directionJoueur = Direction.BAS;

    // Ressources images
    private final Image imgMur = new Image(getClass().getResourceAsStream("/ihm/sokoban/images/mur.png"));
    private final Image imgSol = new Image(getClass().getResourceAsStream("/ihm/sokoban/images/sol.png"));
    private final Image imgCible = new Image(getClass().getResourceAsStream("/ihm/sokoban/images/cible.png"));
    private final Image imgCaisse = new Image(getClass().getResourceAsStream("/ihm/sokoban/images/caisse.png"));
    private final Image imgCaisseValidee = new Image(getClass().getResourceAsStream("/ihm/sokoban/images/caisse_validee.png"));
    
    private final Image imgJoueurHaut = new Image(getClass().getResourceAsStream("/ihm/sokoban/images/joueur_haut.png"));
    private final Image imgJoueurBas = new Image(getClass().getResourceAsStream("/ihm/sokoban/images/joueur_bas.png"));
    private final Image imgJoueurGauche = new Image(getClass().getResourceAsStream("/ihm/sokoban/images/joueur_gauche.png"));
    private final Image imgJoueurDroite = new Image(getClass().getResourceAsStream("/ihm/sokoban/images/joueur_droite.png"));


    // ==========================================
    // CONSTRUCTEUR
    // ==========================================
    /**
     * Constructeur parametre
     * @param grilleJeu la GridPane de l'interface
     * @param controleurInterface l'interface
     */
    public GestionnaireJeu(GridPane grilleJeu, InterfaceJeuController controleurInterface) {
        this.grilleJeu = grilleJeu;
        this.controleurInterface = controleurInterface;
        activerClavier();
    }

    // ==========================================
    // GESTION DU NIVEAU
    // ==========================================
    /**
     * Charge un niveau à partir de sa chaîne de car, de son nom et de son index
     * @param niveaux liste des niveaux
     * @param noms liste des nomes de niveaux
     * @param index index du niveau souhaité
     */
    public void chargerNiveau(String[] niveaux, String[] noms, int index) {
        jeu = new JeuSokoban(niveaux, noms, index);
        controleurInterface.majAffichage();
        dessinerGrille();
    }

    /**
     * Getteur du jeu
     * @return le jeu
     */
    public JeuSokoban getJeu() {
        return jeu;
    }

    // ==========================================
    // AFFICHAGE GRAPHIQUE
    // ==========================================

    /**
     * Rafraîchit l'affichage de la grille de jeu dans l'interface utilisateur.
     * Cette méthode effectue les opérations suivantes :
     * - Nettoie les anciens composants de la {@link GridPane}.
     * - Recalcule la taille des cellules en fonction des dimensions du plateau.
     * - Parcourt la matrice du jeu pour superposer dynamiquement les images :
     * -- Un fond (Sol ou Mur).
     * -- Un élément optionnelle (Cible, Caisse, ou Joueur) par-dessus.
     */
    public void dessinerGrille() {
        //Suppression de l'ancienne grille
        grilleJeu.getChildren().clear();
        grilleJeu.getRowConstraints().clear();
        grilleJeu.getColumnConstraints().clear();
        grilleJeu.setAlignment(javafx.geometry.Pos.CENTER);

        //Parametrage des dimensions
        grilleJeu.setMinSize(300, 300); // Ou 300 selon tes réglages
        grilleJeu.setPrefSize(300, 300);
        grilleJeu.setMaxSize(300, 300);

        double espaceMaxLargeur = 300.0;
        double espaceMaxHauteur = 300.0;
        double tailleMaxLigne = espaceMaxHauteur / jeu.getNbLignes();
        double tailleMaxCol = espaceMaxLargeur / jeu.getNbColonnes();
        double tailleCase = Math.min(tailleMaxLigne, tailleMaxCol);

        //Complétage de la grille case par case
        for (int ligne = 0; ligne < jeu.getNbLignes(); ligne++) {
            for (int colonne = 0; colonne < jeu.getNbColonnes(); colonne++) {
                TypeCase tc = jeu.getCase(ligne, colonne);
                
                StackPane caseGraphique = new StackPane();
                caseGraphique.setPrefSize(tailleCase, tailleCase);
                caseGraphique.setMaxSize(tailleCase, tailleCase);
                caseGraphique.setMinSize(tailleCase, tailleCase);

                // --- 1. Sol
                ImageView fond = new ImageView(imgSol);
                fond.setFitWidth(tailleCase);
                fond.setFitHeight(tailleCase);
                caseGraphique.getChildren().add(fond);

                // --- 2. Autre élément
                ImageView entite = null;

                switch (tc) {
                    case MUR :
                        entite = new ImageView(imgMur);
                        break;
                    case CIBLE:
                        entite = new ImageView(imgCible);
                        break;
                        
                    case CAISSE:
                        entite = new ImageView(imgCaisse);
                        break;
                        
                    case CAISSE_SUR_CIBLE:
                        entite = new ImageView(imgCaisseValidee);
                        break;
                        
                    case JOUEUR:
                    case JOUEUR_SUR_CIBLE:
                        if (tc == TypeCase.JOUEUR_SUR_CIBLE) {
                            ImageView cibleJoueur = new ImageView(imgCible);
                            cibleJoueur.setFitWidth(tailleCase); cibleJoueur.setFitHeight(tailleCase);
                            caseGraphique.getChildren().add(cibleJoueur);
                        }

                        // Séléction du sprite du joueur selon la direction
                        if (directionJoueur == Direction.HAUT) entite = new ImageView(imgJoueurHaut);
                        else if (directionJoueur == Direction.GAUCHE) entite = new ImageView(imgJoueurGauche);
                        else if (directionJoueur == Direction.DROITE) entite = new ImageView(imgJoueurDroite);
                        else entite = new ImageView(imgJoueurBas);
                        break;
                        
                    default:
                        break;
                }

                // Si la case contient un élément, on l'ajoute par dessus le sol
                if (entite != null) {
                    entite.setFitWidth(tailleCase);
                    entite.setFitHeight(tailleCase);
                    caseGraphique.getChildren().add(entite);
                }

                grilleJeu.add(caseGraphique, colonne, ligne);
            }
        }
    }

    // ==========================================
    // CONTRÔLES CLAVIER
    // ==========================================
    
    /**
     * Permet d'activer le contrôle du clavier
     */
    private void activerClavier() {
        Platform.runLater(() -> {
            grilleJeu.requestFocus();
            if (grilleJeu.getScene() != null) {
                grilleJeu.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, ecouteurClavier);
                grilleJeu.getScene().addEventFilter(KeyEvent.KEY_PRESSED, ecouteurClavier);
            }
        });
    }

    public void desactiverClavier() {
        if (grilleJeu != null && grilleJeu.getScene() != null) {
            grilleJeu.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, ecouteurClavier);
        }
    }

    /**
     * Convertit une touche clavier pressée en une direction de déplacement correspondante.
     * Cette méthode réalise le mapping entre les touches directionnelles (flèches) 
     * ou les touches de jeu (Z, Q, S, D) et les valeurs de l'énumération {@link Direction}.
     *
     * @param code Le code de la touche récupéré depuis l'événement clavier ({@link KeyCode}).
     * @return La {@link Direction} associée à la touche, ou {@code null} si la touche ne correspond à aucun déplacement.
     */
    private Direction obtenirDirection(KeyCode code) {
        if (code == KeyCode.Z || code == KeyCode.UP) return Direction.HAUT;
        if (code == KeyCode.Q || code == KeyCode.LEFT) return Direction.GAUCHE;
        if (code == KeyCode.S || code == KeyCode.DOWN) return Direction.BAS;
        if (code == KeyCode.D || code == KeyCode.RIGHT) return Direction.DROITE;
        return null;
    }

    /**
     * Gère les événements clavier de l'utilisateur pour contrôler le personnage ou les actions du jeu.
     * Cette méthode intercepte les touches pressées et déclenche les actions correspondantes :
     * Touches de déplacement (Z, Q, S, D ou flèches) : oriente et déplace le joueur.
     * Touche 'Z' (avec Ctrl) : annule le dernier mouvement.
     * Touche 'R' : réinitialise le niveau actuel.
     * Touche 'N' : passe au niveau suivant.
     * Touche 'P' : revient au niveau précédent.
     *
     * @param event L'événement clavier capturé par le gestionnaire d'événements.
     */
    private void traiterClavier(KeyEvent event) {
        KeyCode code = event.getCode();
        
        // Redirection vers les méthodes de l'interface
        if ((event.isControlDown() && code == KeyCode.Z) || code == KeyCode.BACK_SPACE) {
            controleurInterface.annuler();
            event.consume();
            return;
        }
        if (code == KeyCode.R) {
            controleurInterface.recommencer();
            event.consume();
            return;
        }
        if (code == KeyCode.N) {
            controleurInterface.suivant();
            event.consume();
            return;
        }
        if (code == KeyCode.P) {
            controleurInterface.precedent();
            event.consume();
            return;
        }
        if (code == KeyCode.M) {
            controleurInterface.retourMP();
            event.consume();
            return;
        }
        if (code == KeyCode.ESCAPE) {
            controleurInterface.quitter();
            event.consume();
            return;
        }

        Direction direction = obtenirDirection(code);
        if (direction != null) {
            event.consume();

            directionJoueur = direction;
            if (!jeu.peutJouer()) {
                if (jeu.isPerdu() || jeu.isNiveauTermine()) return;
            }

            int nbCaissesSurCibleAvant = jeu.getNbCaissesSurCible();

            try {
                jeu.deplacer(direction);
            }catch (SokobanException e) {
                System.out.println("Erreur : " + e.getMessage());
            }

            //Gestion du son
            if (jeu.isNiveauTermine()) {
                controleurInterface.declencherVictoire();
            } 
            else if (jeu.isPerdu()) {
                controleurInterface.declencherDefaite();
            } 
            else if (jeu.getNbCaissesSurCible() > nbCaissesSurCibleAvant) {
                controleurInterface.cibleAtteint();
            }

            controleurInterface.majAffichage();
            dessinerGrille();

            if (jeu.isNiveauTermine()) {
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> controleurInterface.suivant());
                pause.play();
            }
        }
    }
}