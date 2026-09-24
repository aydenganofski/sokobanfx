package ihm.sokoban.view;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

import ihm.sokoban.model.JeuSokoban;
import ihm.sokoban.model.SokobanException;
import ihm.sokoban.util.LoaderNiveauxXSB;
import ihm.sokoban.util.LoaderNiveauxXSB.Banque;
import ihm.sokoban.util.NiveauxSokoban;
import ihm.sokoban.util.NiveauxTutoriel;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


/**
 * Contrôleur de l'interface du jeu
 * @author Ayden GANOFSKI
 * @version 1.0
 */
public class InterfaceJeuController implements Initializable {

    //Attributs FXML
    @FXML private BorderPane pane;
    @FXML private GridPane grilleJeu;
    @FXML private Button btnAnnuler, btnRecommencer, btnSuivant, btnPrecedent;
    @FXML private Menu menuTutoriels, menuSokoban, menuXSB;
    @FXML private Label nomNiveau, nbMouv, nbPoussee, caissesObjectif, statutPartie;
    @FXML private Slider sliderVolume;
    @FXML private CheckMenuItem checkMusique, checkBruitages;
       
    //Autres attributs
    private Stage dialogStage;
    private String[] niveauxTutoriels, niveauxSokoban, nomsTutoriels, nomsSokoban;
    private ModeJeu mode;
    private GestionnaireJeu gestionnaire;
    private Theme themeActuel = Theme.CLAIR; 
    private MediaPlayer mediaPlayer;
    private AudioClip winSound, loseSound, goalSound;
    
    

    // INITIALISATION

    /**
     * Initialise le jeu
     */
    @Override
    public void initialize(java.net.URL location, ResourceBundle resources) {

        // ========================================================
        // GESTIONS DES NIVEAUX
        // ========================================================

        niveauxTutoriels = NiveauxTutoriel.getNiveaux();
        niveauxSokoban = NiveauxSokoban.getNiveaux();
        nomsTutoriels = NiveauxTutoriel.getNoms();
        nomsSokoban = NiveauxSokoban.getNoms();

        // On crée le gestionnaire en lui passant la grille FXML et le contrôleur
        gestionnaire = new GestionnaireJeu(grilleJeu, this);

        for (int i = 0; i < NiveauxTutoriel.getNiveaux().length; i++) {
            MenuItem mi = new MenuItem(nomsTutoriels[i]);
            final int index = i;
            mi.setOnAction(e -> selectNiveau(index, ModeJeu.TUTORIEL));
            menuTutoriels.getItems().add(mi);
        }
        
        for (int i = 0; i < NiveauxSokoban.getNiveaux().length; i++) {
            MenuItem mi = new MenuItem(nomsSokoban[i]);
            final int index = i;
            mi.setOnAction(e -> selectNiveau(index, ModeJeu.SOKOBAN));
            menuSokoban.getItems().add(mi);
        }

        // ========================================================
        // GESTION DES FICHIERS SONS
        // ========================================================

        String musicPath = "/ihm/sokoban/sons/backgroundmusic.mp3";
        String musicURI = getClass().getResource(musicPath).toExternalForm();

        Media sound = new Media(musicURI);

        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setVolume(sliderVolume.getValue());
        mediaPlayer.setMute(checkMusique.isSelected());
        mediaPlayer.play();

        try {
            String winFile = "/ihm/sokoban/sons/winSound.wav";
            winSound = new AudioClip(getClass().getResource(winFile).toExternalForm());

            String loseFile = "/ihm/sokoban/sons/loseSound.wav";
            loseSound = new AudioClip(getClass().getResource(loseFile).toExternalForm());

            String goalFile = "/ihm/sokoban/sons/goalSound.wav";
            goalSound = new AudioClip(getClass().getResource(goalFile).toExternalForm());
        } catch (Exception e) {
            System.err.println("Erreur de chargement audio : " + e.getMessage());
        }

        // ========================================================
        // GESTION DU SON
        // ========================================================
        
        sliderVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
            double volume = newValue.doubleValue();
            
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume);
            }
            
            if (!checkBruitages.isSelected()) {
                if (winSound != null) winSound.setVolume(volume);
                if (loseSound != null) loseSound.setVolume(volume);
                if (goalSound != null) goalSound.setVolume(volume);
            }
        });

        // Couper/activer la musique
        checkMusique.selectedProperty().addListener((observable, oldValue, newValue) -> {
            boolean musiqueMuette = newValue; 
            if (mediaPlayer != null) {
                mediaPlayer.setMute(musiqueMuette);
            }
        });

        // Couper/activer les bruitages
        checkBruitages.selectedProperty().addListener((observable, oldValue, newValue) -> {
            boolean bruitagesMuets = newValue; 
            double volumeActuel = bruitagesMuets ? 0.0 : sliderVolume.getValue();
            
            if (winSound != null) winSound.setVolume(volumeActuel);
            if (loseSound != null) loseSound.setVolume(volumeActuel);
            if (goalSound != null) goalSound.setVolume(volumeActuel);
        });
        
        // ========================================================
        
        selectNiveau(0, ModeJeu.TUTORIEL);
        changerTheme(themeActuel);
    }
    
    // ==========================================
    // CHARGEMENT DE DOSSIERS XSB
    // ==========================================
    @FXML
    /**
     * Permet de charger les niveaux sous format .xsb à partir d'un dossier
     */
    public void chargerDossierXSB() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionner un dossier de niveaux (.xsb)");
        
        File dossierChoisi = directoryChooser.showDialog(dialogStage); 

        //Si on annule le chargement du dossier
        if (dossierChoisi == null) {
            return;
        }

        //Si on a déjà chargé le dossier
        if (isDossierExistant(dossierChoisi)) {
            alerteDossierExistant(dossierChoisi);
            return;
        } else {
            //Sinon on le charge
            try {
                Banque banque = LoaderNiveauxXSB.chargerDepuisDossier(dossierChoisi.toPath());

                // 1. On crée un sous-menu qui porte le nom du dossier
                Menu sousMenuDossier = new Menu("📁 " + dossierChoisi.getName());

                // 2. On remplit ce sous-menu avec les niveaux
                for (int i = 0; i < banque.niveaux.length; i++) {
                    MenuItem mi = new MenuItem(banque.noms[i]);
                    final int index = i;
                    mi.setOnAction(e -> {
                        this.mode = ModeJeu.CHARGER;
                        gestionnaire.chargerNiveau(banque.niveaux, banque.noms, index);
                    });
                    sousMenuDossier.getItems().add(mi);
                }

                // 3. On ajoute ce nouveau sous-menu bien rempli au menu principal 'Charger dossier')
                menuXSB.getItems().add(sousMenuDossier);

                // 4. On lance automatiquement le tout premier niveau du dossier chargé
                this.mode = ModeJeu.CHARGER;
                gestionnaire.chargerNiveau(banque.niveaux, banque.noms, 0);

            } catch (SokobanException e) {
                Alert erreur = new Alert(AlertType.ERROR);
                erreur.setTitle("Erreur d'importation");
                erreur.setHeaderText("Impossible de charger les niveaux");
                erreur.setContentText(e.getMessage());
                erreur.showAndWait();
            }
        }
    }

    /**
     * Vérifie si un dossier a déjà été chargé dans la barre de menu
     * @param dossier Le dossier à vérifier
     * @return true si le dossier existe déjà dans le menu, false sinon
     */
    private boolean isDossierExistant(File dossier) {
        String nomSousMenu = "📁 " + dossier.getName();
        
        for (MenuItem itemExistant : menuXSB.getItems()) {
            if (itemExistant.getText().equals(nomSousMenu)) {
                return true; // Le dossier a été trouvé
            }
        }
        return false; // Le dossier n'a pas été trouvé
    }

    /**
     * Vérifie si un dossier a déjà été chargé dans la barre de menu
     * @param dossier Le dossier à vérifier
     * @return true si le dossier existe déjà dans le menu, false sinon
     */
    private void alerteDossierExistant(File dossier) {
        Alert alerte = new Alert(AlertType.WARNING);
        alerte.setTitle("Chargement annulé");
        alerte.setHeaderText("Dossier existant");
        alerte.setContentText("Le dossier '" + dossier.getName() + "' a déjà été importé.");
        alerte.showAndWait();
    }
    // ==========================================
    // NAVIGATION DES NIVEAUX
    // ==========================================

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Permet de selectionner le niveau à partir de son index et de son mode
     * @param index l'index du niveau
     * @param mode TUTORIEL ou SOKOBAN
     * @see #chargerDossierXSB()
     */
    public void selectNiveau(int index, ModeJeu mode) {
        this.mode = mode;
        switch (mode) {
            case TUTORIEL:
                gestionnaire.chargerNiveau(niveauxTutoriels, nomsTutoriels, index);
                break;
            case SOKOBAN:
                gestionnaire.chargerNiveau(niveauxSokoban, nomsSokoban, index);
                break;
            case CHARGER:
                break;
        }
    }

    // Getteur/Setteur
    /**
     * Permet d'avoir le mode du jeu courant
     * @return TUTORIEL, SOKOBAN ou CHARGER
     */
    public ModeJeu getMode() { 
        return mode; 
    }

    /**
     * Permet de changer le mode du jeu courant
     * @param mode TUTORIEL ou SOKOBAN 
     */
    public void setMode(ModeJeu mode) { 
        this.mode = mode; 
    }

    // ==========================================
    // AFFICHAGE
    // ==========================================
    
    /**
     * Permet d'afficher le jeu sur l'interface
     */
    public void majAffichage() {

        //Récupèration du jeu
        JeuSokoban jeu = gestionnaire.getJeu();
        if (jeu == null) return;

        //Mise à jour de la GridPane et des labels
        nomNiveau.setText(jeu.getNomNiveauCourant());
        nbMouv.setText("Mouvements : " + jeu.getNbMouvements());
        nbPoussee.setText("Poussées : "+ jeu.getNbPoussees());
        caissesObjectif.setText("Caisses sur objectif : " + jeu.getNbCaissesSurCible() + "/" + jeu.getNbCaisses());
        
        //Si le jeu est terminé
        if (jeu.isNiveauTermine()) {
            if (jeu.estDernierNiveau()) {
                if (mode == ModeJeu.TUTORIEL) {
                    statutPartie.setText("Tutoriel terminé ! Passage au mode principal...");
                } else {
                    statutPartie.setText("Félicitations ! Vous avez terminé tous les niveaux !");
                }

            } else {
                statutPartie.setText("Niveau terminé ! Prochain niveau. . .");
            }
            statutPartie.setVisible(true);
            statutPartie.setManaged(true);
        } else 
            //Si le jeu est perdu
            if (jeu.isPerdu()) {
            statutPartie.setText("Perdu ! (R) Recommencer ou (Ctrl+Z) Annuler");
            statutPartie.setVisible(true);
            statutPartie.setManaged(true);
        } else {
            //Cas normal
            statutPartie.setText("");
            statutPartie.setVisible(false);
            statutPartie.setManaged(false);
        }

        //Gestion des boutons Précédent/Suivant
        btnSuivant.setDisable(jeu.estDernierNiveau() && mode != ModeJeu.TUTORIEL);
        btnPrecedent.setDisable(jeu.getNiveauCourant() <= 0);
    }

    // ==========================================
    // ACTIONS DU JEU
    // ==========================================
    
    @FXML
    /**
     * Permet de passer au niveau suivant
     */
    public void suivant() {
        if (!gestionnaire.getJeu().estDernierNiveau()) {
            gestionnaire.getJeu().niveauSuivant();
            majAffichage();
            gestionnaire.dessinerGrille();
        } else if (mode == ModeJeu.TUTORIEL) {
            selectNiveau(0, ModeJeu.SOKOBAN);
        }
    }

    @FXML
    /**
     * Permet de passer au niveau précédent
     */
    public void precedent() {
        if (gestionnaire.getJeu().getNiveauCourant() > 0) {
            gestionnaire.getJeu().niveauPrecedent();
            majAffichage();
            gestionnaire.dessinerGrille();
        }
    }

    /**
     * Permet d'annuler une action
     */
    @FXML
    public void annuler() {
        if (gestionnaire.getJeu().peutAnnuler()) {
            gestionnaire.getJeu().annuler();
            majAffichage();
            gestionnaire.dessinerGrille();
        }
    }

    /**
     * Permet de recommencer le niveau
     */
    @FXML
    public void recommencer() {
        gestionnaire.getJeu().reset();
        majAffichage();
        gestionnaire.dessinerGrille();
    }

    // ==========================================
    // GESTION DES MUSIQUES
    // ==========================================

    public void declencherVictoire() {
        winSound.play();
    }

    public void declencherDefaite() {
        loseSound.play();
    }

    public void cibleAtteint() {
        goalSound.play();
    }


    // ==========================================
    // GESTION DES THEMES
    // ==========================================
    public void changerTheme(Theme nouveauTheme) {
        for (Theme t : Theme.values()) {
            pane.getStyleClass().remove(t.getClasseCss());
        }
        pane.getStyleClass().add(nouveauTheme.getClasseCss());
        this.themeActuel = nouveauTheme; 
    }

    @FXML public void actionThemeClair() { changerTheme(Theme.CLAIR); }
    @FXML public void actionThemeSombre() { changerTheme(Theme.SOMBRE); }
    @FXML public void actionThemeRose() { changerTheme(Theme.ROSE); }
    @FXML public void actionThemeBleu() { changerTheme(Theme.BLEU); }
    @FXML public void actionThemeMaruigi() { changerTheme(Theme.MARUIGI); }
    
    // ==========================================
    // TRANSFERT DES DONNÉES AUDIO
    // ==========================================
    /**
     * Permet d'importer les réglages audio depuis le menu principal
     */
    public void appliquerParametresAudio(double volume, boolean musiqueMuette, boolean bruitagesMuets) {
        // Le simple fait de modifier ces valeurs graphiquement va déclencher 
        // vos Listeners et mettre à jour les MediaPlayer et AudioClip automatiquement !
        if (sliderVolume != null) sliderVolume.setValue(volume);
        if (checkMusique != null) checkMusique.setSelected(musiqueMuette);
        if (checkBruitages != null) checkBruitages.setSelected(bruitagesMuets);
    }

    // ==========================================
    // INFOS ET AIDE
    // ==========================================

    @FXML
    /**
     * Affiche l'aide du jeu
     */
    public void aide() {
        Alert aide = new Alert(AlertType.INFORMATION);
        aide.setTitle("Aide");
        aide.setHeaderText("Comment jouer ?");
        aide.setContentText(
            "But du jeu :\n" +
            "Dans SokobanApp, il faut aider Maruigi à pousser toutes les caisses sur les cases cibles.\n" +
            "À vous de l'aider à travers 15 niveaux inclus dans l'application !\n\n"+
            "Contrôles :\n" +
            "Déplacements : Z/Q/S/D ou Flèches\n" +
            "Annuler : Ctrl +  ou Annuler\n" +
            "Recommencer : R\n" +
            "Niveau suivant : N\n" +
            "Niveau précédent : P\n"+
            "Menu principal : M\n" +
            "Quitter : Echap\n\n"+

            "Charger un niveau :\n" +
            "Pour charger vos propres niveaux au format .xsb, allez dans Charger un niveau > Charger un niveau à partir d'un dossier."
        );
        aide.showAndWait();
    }

    @FXML
    /**
     * Affiche les crédits du jeu
     */
    public void credit() {
        Alert credit = new Alert(AlertType.INFORMATION);
        credit.setTitle("Crédits");
        credit.setHeaderText("Crédits du projet");
        credit.setContentText(
            "Ce projet a été réalisé par Ayden GANOFSKI dans le cadre du cours IHM à l'IUT de Blagnac.\n\n" +
            "Les images utilisées pour les éléments du jeu sont issues de la ressource 'Sokoban' du site Kenney.\n\n"+
            "Musique de fond :\n" +
            "Uplifting City! Happy Groovy Game Music by HeatleyBros\n"+
            "Option Menu! Chill Calm Game Music by HeatleyBros\n\n" +
            "Bruitages : Ressources libres de droits"
        );
        credit.showAndWait();
    }

    @FXML
    /**
     * Retour au menu principal
     */
    public void retourMP() {
        //On coupe la musique
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ihm/sokoban/view/menuPrincipal.fxml"));
            Parent root = loader.load();

            MenuPrincipalController controleurMenu = loader.getController();
            
            controleurMenu.changerTheme(this.themeActuel);

            double volumeEnCours = sliderVolume.getValue();
            boolean musiqueCoupee = checkMusique.isSelected();
            boolean bruitagesCoupes = checkBruitages.isSelected();
            controleurMenu.appliquerParametresAudio(volumeEnCours, musiqueCoupee, bruitagesCoupes);

            if (pane.getScene() != null) {
                gestionnaire.desactiverClavier();
                pane.getScene().setRoot(root);
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du menu principal : " + e.getMessage());
            e.printStackTrace();
        }

    }
    
    // ==========================================
    // FERMETURE
    // ==========================================

    @FXML
    /**
     * Permet de quitter l'apllication
     */
    public void quitter() {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Quitter");
        confirmation.setHeaderText("Voulez-vous vraiment quitter ?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
            System.exit(0);
        }
    }

    

    
}