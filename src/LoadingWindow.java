import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class LoadingWindow extends JFrame {

    LoadingPanel loadingPanel;
    ImageIcon img = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("icons/whiteIcon.png")));
    Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

    public LoadingWindow(Color p, Color s) {
        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());

        loadingPanel = new LoadingPanel(p, s);
        pane.add(loadingPanel, BorderLayout.CENTER);

        this.setTitle("LOADING SCREEN");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(860, 230);
        this.setIconImage(img.getImage());
        this.setResizable(false);
        this.setUndecorated(true);
        this.setAlwaysOnTop(false);
        this.setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
        this.setVisible(true);
    }

}
