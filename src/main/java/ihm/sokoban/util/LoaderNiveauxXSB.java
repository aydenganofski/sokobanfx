package ihm.sokoban.util;

import ihm.sokoban.model.SokobanException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Chargeur de niveaux au format XSB (extension .xsb), le format standard
 * des Sokobanistes.
 *
 * Chaque fichier .xsb du dossier représente UN niveau (convention :
 * 001.xsb, 002.xsb, ...). L'ordre alphabétique est utilisé.
 *
 * Format d'un fichier :
 *   - lignes Sokoban avec les caractères standards (#, $, ., @, +, *, espace)
 *   - lignes commençant par ';' = commentaires (ignorées)
 *   - première ligne de commentaire = nom du niveau (si présente)
 *   - lignes vides en tête/queue ignorées
 *
 * Exemple de contenu pour 001.xsb :
 *   ; Mon premier niveau
 *   ######
 *   #@$ .#
 *   ######
 */
public class LoaderNiveauxXSB {

    private LoaderNiveauxXSB() {
        // Classe utilitaire
    }

    /** Résultat du chargement : niveaux + noms (parallèles). */
    public static class Banque {
        public final String[] niveaux;
        public final String[] noms;

        Banque(String[] niveaux, String[] noms) {
            this.niveaux = niveaux;
            this.noms = noms;
        }
    }

    /**
     * Charge tous les fichiers *.xsb d'un dossier (triés par nom).
     *
     * @param dossier Chemin du dossier (existant et lisible).
     * @return Une banque (niveaux + noms) prête à passer à JeuSokoban.
     * @throws SokobanException si le dossier est invalide ou ne contient aucun .xsb
     */
    public static Banque chargerDepuisDossier(Path dossier) {
        if (dossier == null || !Files.isDirectory(dossier)) {
            throw new SokobanException(SokobanException.TypeErreur.NIVEAU_INVALIDE,
                "Dossier introuvable : " + dossier);
        }

        List<Path> fichiers = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dossier)) {
            stream.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xsb"))
                  .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                  .forEach(fichiers::add);
        } catch (IOException e) {
            throw new SokobanException(SokobanException.TypeErreur.NIVEAU_INVALIDE,
                "Erreur de lecture du dossier : " + e.getMessage());
        }

        if (fichiers.isEmpty()) {
            throw new SokobanException(SokobanException.TypeErreur.NIVEAU_INVALIDE,
                "Aucun fichier .xsb dans : " + dossier);
        }

        String[] niveaux = new String[fichiers.size()];
        String[] noms = new String[fichiers.size()];
        for (int i = 0; i < fichiers.size(); i++) {
            Path f = fichiers.get(i);
            String[] niveauEtNom = lireFichier(f);
            niveaux[i] = niveauEtNom[0];
            noms[i] = (niveauEtNom[1] != null) ? niveauEtNom[1]
                                               : nomDepuisFichier(f);
        }
        return new Banque(niveaux, noms);
    }

    /**
     * Lit un fichier .xsb et retourne {niveau, nom (peut être null)}.
     */
    private static String[] lireFichier(Path fichier) {
        List<String> lignes;
        try {
            lignes = Files.readAllLines(fichier);
        } catch (IOException e) {
            throw new SokobanException(SokobanException.TypeErreur.NIVEAU_INVALIDE,
                "Erreur de lecture du fichier " + fichier + " : " + e.getMessage());
        }

        StringBuilder niveau = new StringBuilder();
        String nom = null;

        for (String ligne : lignes) {
            if (ligne.startsWith(";")) {
                // Premier commentaire = nom du niveau
                if (nom == null) {
                    nom = ligne.substring(1).trim();
                }
                continue;
            }
            if (ligne.isEmpty() && niveau.length() == 0) {
                // Lignes vides en tête : on les saute
                continue;
            }
            if (ligne.isEmpty()) {
                // Première ligne vide après le contenu : on s'arrête
                break;
            }
            if (niveau.length() > 0) {
                niveau.append('\n');
            }
            niveau.append(ligne);
        }

        if (niveau.length() == 0) {
            throw new SokobanException(SokobanException.TypeErreur.NIVEAU_INVALIDE,
                "Fichier vide ou sans niveau : " + fichier);
        }
        return new String[] { niveau.toString(), nom };
    }

    private static String nomDepuisFichier(Path fichier) {
        String nom = fichier.getFileName().toString();
        int dot = nom.lastIndexOf('.');
        return (dot > 0) ? nom.substring(0, dot) : nom;
    }
}
