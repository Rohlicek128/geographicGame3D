import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Color primary = new Color(255, 165, 0);
        Color secondary = new Color(34, 86, 115);
        Color correct = new Color(27, 222, 102);
        Color wrong = new Color(193, 1, 47);

        LoadingWindow loadingWindow = new LoadingWindow(secondary, primary);
        Renderer renderer = new Renderer(secondary, primary, correct, wrong);

        loadingWindow.setVisible(false);
        loadingWindow.dispose();
    }
}