package ihm.sokoban;

import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Classe principale de l'application Sokoban.
 * Gère le cycle de vie du stage principal et le chargement du menu initial.
 */
public class SokobanApp extends Application {

    private BorderPane rootPane;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.rootPane = new BorderPane();

        Scene scene = new Scene(rootPane, 700, 500);
        
        chargerStyles(scene);
        configurerFenetre();
        
        primaryStage.setScene(scene);
        loadMenuPrincipal();
        
        primaryStage.setOnCloseRequest(event -> {
            event.consume();

            Alert confirmation = new Alert(AlertType.CONFIRMATION);
            confirmation.setTitle("Quitter");
            confirmation.setHeaderText("Voulez-vous vraiment quitter ?");
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Platform.exit();
                System.exit(0);
            }
        });

        primaryStage.show();
    }

    /**
     * Charge le fichier FXML du menu principal et l'injecte dans la scène.
     */
    public void loadMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ihm/sokoban/view/menuPrincipal.fxml"));
            rootPane.setCenter(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerStyles(Scene scene) {
        try {
            scene.getStylesheets().add(getClass().getResource("/ihm/sokoban/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Attention : Fichier style.css introuvable.");
        }
    }

    private void configurerFenetre() {
        primaryStage.setTitle("SokobanApp");
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/ihm/sokoban/images/icon.png")));
        } catch (Exception e) {
            System.err.println("Icône introuvable.");
        }
    }
    

    public static void main2(String[] args) {
        launch(args);
    }
}