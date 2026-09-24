package ihm.sokoban.view;

public enum Theme {
    CLAIR("themeClair"),
    SOMBRE("themeSombre"),
    ROSE("themeRose"),
    BLEU("themeBleu"),
    MARUIGI("themeMaruigi");

    private final String classeCss;

    Theme(String classeCss) {
        this.classeCss = classeCss;
    }

    public String getClasseCss() {
        return classeCss;
    }
}