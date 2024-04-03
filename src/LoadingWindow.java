import javax.swing.*;
import java.awt.*;

public class LoadingWindow extends JFrame {

    public LoadingWindow(Color p, Color s) {
        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());

        LoadingPanel loadingPanel = new LoadingPanel(p, s);
        pane.add(loadingPanel, BorderLayout.CENTER);

        this.setTitle("LOADING SCREEN");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(860, 230);
        this.setResizable(false);
        this.setUndecorated(true);
        this.setLocation(2560 / 2 - getWidth() / 2, 1440 / 2 - getHeight() / 2);
        this.setVisible(true);
    }

}
