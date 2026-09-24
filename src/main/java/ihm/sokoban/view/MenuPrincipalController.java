package ihm.sokoban.view;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 * Contrôleur du menu principal du jeu.
 * Gère la navigation vers l'interface de jeu et la configuration des thèmes.
 * @author Ayden GANOFSKI
 * @version 1.0
 */
public class MenuPrincipalController implements Initializable {

    // ATTRIBUTS FXML
    @FXML private BorderPane paneMP;
    @FXML private Slider sliderVolume;
    @FXML private CheckMenuItem checkMusique, checkBruitages;

    // ATTRIBUTS PRIVÉS
    private Theme themeActuel = Theme.CLAIR;
    private MediaPlayer mediaPlayerMenu;

    // INITIALISATION
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            String musicPath = "/ihm/sokoban/sons/menuMusic.mp3"; 
            String musicURI = getClass().getResource(musicPath).toExternalForm();
            
            Media sound = new Media(musicURI);
            
            javafx.application.Platform.runLater(() -> {
                mediaPlayerMenu = new MediaPlayer(sound);
                mediaPlayerMenu.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayerMenu.setVolume(sliderVolume.getValue());
                mediaPlayerMenu.setMute(checkMusique.isSelected());
                mediaPlayerMenu.play();
            });
            
        } catch (Exception e) {
            e.getMessage();
        }

        // ========================================================
        // GESTION DU SON
        // ========================================================
        
        sliderVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
            double volume = newValue.doubleValue();
            
            if (mediaPlayerMenu != null) {
                mediaPlayerMenu.setVolume(volume);
            }
        });

        // Couper/activer la musique
        checkMusique.selectedProperty().addListener((observable, oldValue, newValue) -> {
            boolean musiqueMuette = newValue; 
            if (mediaPlayerMenu != null) {
                mediaPlayerMenu.setMute(musiqueMuette);
            }
        });
        changerTheme(themeActuel);

    }

    // ==========================================
    // NAVIGATION VERS LE JEU
    // ==========================================

    @FXML public void actionLancerTutoriel() { lancerJeu(ModeJeu.TUTORIEL); }
    @FXML public void actionLancerSokoban()  { lancerJeu(ModeJeu.SOKOBAN); }
    @FXML public void actionLancerDossier()  { lancerJeu(ModeJeu.CHARGER); }

    /**
     * Charge l'interface de jeu et initialise le contrôleur cible.
     * @param mode Le mode de jeu à lancer.
     */
    private void lancerJeu(ModeJeu mode) {
        //Gestion de la musique
        if (mediaPlayerMenu != null) {
            mediaPlayerMenu.stop();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ihm/sokoban/view/interfaceJeu.fxml"));
            Parent racineJeu = loader.load();

            InterfaceJeuController controleurJeu = loader.getController();
            Stage stageActuel = (Stage) paneMP.getScene().getWindow();
            
            controleurJeu.setDialogStage(stageActuel);
            controleurJeu.changerTheme(themeActuel);

            double volumeEnCours = sliderVolume.getValue();
            boolean musiqueCoupee = checkMusique.isSelected();
            boolean bruitagesCoupes = checkBruitages.isSelected();
            controleurJeu.appliquerParametresAudio(volumeEnCours, musiqueCoupee, bruitagesCoupes);
            
            paneMP.getScene().setRoot(racineJeu);

            if (mode != ModeJeu.CHARGER) {
                controleurJeu.selectNiveau(0, mode);
            } else {
                Platform.runLater(() -> controleurJeu.chargerDossierXSB());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // ACTIONS UTILISATEUR (FXML)
    // ==========================================

    @FXML
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
    // ==========================================
    // THÈMES
    // ==========================================

    /**
     * Change le thème de l'application dynamiquement.
     * @param nouveauTheme Le thème à appliquer.
     */
    public void changerTheme(Theme nouveauTheme) {
        for (Theme t : Theme.values()) {
            paneMP.getStyleClass().remove(t.getClasseCss());
        }
        paneMP.getStyleClass().add(nouveauTheme.getClasseCss());
        this.themeActuel = nouveauTheme;
    }

    @FXML public void actionThemeClair()  { changerTheme(Theme.CLAIR); }
    @FXML public void actionThemeSombre() { changerTheme(Theme.SOMBRE); }
    @FXML public void actionThemeRose()   { changerTheme(Theme.ROSE); }
    @FXML public void actionThemeBleu()   { changerTheme(Theme.BLEU); }
    @FXML public void actionThemeMaruigi(){ changerTheme(Theme.MARUIGI); }

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
}