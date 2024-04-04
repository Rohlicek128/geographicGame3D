import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class Renderer extends JFrame implements KeyListener {
    RenderPanel renderPanel;

    ImageIcon img = new ImageIcon("icon2.png");
    Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    boolean fullscreen = false;
    Dimension lastWindowSize;
    int[] lastWindowLocation;

    public Renderer(Color p, Color s, Color c, Color w) {
        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());

        this.renderPanel = new RenderPanel(p, s);
        renderPanel.correctColor = c;
        renderPanel.wrongColor = w;
        pane.add(renderPanel, BorderLayout.CENTER);

        this.addKeyListener(this);
        this.setFocusable(true);
        this.lastWindowSize = new Dimension(720, 720);
        this.lastWindowLocation = new int[]{screenSize.width / 2 - lastWindowSize.width / 2, screenSize.height / 2 - lastWindowSize.height / 2};

        this.setTitle("Earth Game");
        this.setIconImage(img.getImage());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(this.lastWindowSize);
        this.setLocation(this.lastWindowLocation[0], this.lastWindowLocation[1]);
        this.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_V) {
            renderPanel.view = !renderPanel.view;
            if (renderPanel.view) System.out.println("View: ENABLED");
            else System.out.println("View: DISABLED");
        }
        else if(e.getKeyCode() == KeyEvent.VK_N) {
            System.out.println("SKIPPED: " + renderPanel.randomCountry.toUpperCase());
            renderPanel.nextRandomName(renderPanel.randomCountry);
        }
        if(e.getKeyCode() == KeyEvent.VK_F11) {
            fullscreen = !fullscreen;
            if (fullscreen) {
                lastWindowSize.setSize(getWidth(), getHeight());
                lastWindowLocation[0] = getX();
                lastWindowLocation[1] = getY();

                this.setSize(screenSize.width, screenSize.height + 48);
                this.setLocation(0, 0);
                System.out.println("FULLSCREEN");
            }
            else {
                this.setSize(lastWindowSize);
                this.setLocation(lastWindowLocation[0], lastWindowLocation[1]);
                System.out.println("WINDOW");
            }
            this.dispose();
            this.setUndecorated(fullscreen);
            this.setAlwaysOnTop(fullscreen);
            this.setVisible(true);
        }
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
        renderPanel.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

    /*public void defObjectSquare(){
        polygons.add(new Triangle(
                new Vertex(-100, 100, 100),
                new Vertex(-100, 100, -100),
                new Vertex(-100, -100, 100),
                new Color(0,255,0)));
        polygons.add(new Triangle(
                new Vertex(-100, -100, -100),
                new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100),
                new Color(0,255,0)));

        polygons.add(new Triangle(
                new Vertex(100, 100, 100),
                new Vertex(100, 100, -100),
                new Vertex(100, -100, 100),
                new Color(0,0,255)));
        polygons.add(new Triangle(
                new Vertex(100, -100, -100),
                new Vertex(100, -100, 100),
                new Vertex(100, 100, -100),
                new Color(0,0,255)));

        polygons.add(new Triangle(
                new Vertex(100, 100, 100),
                new Vertex(-100, 100, 100),
                new Vertex(100, -100, 100),
                new Color(255,255,0)));
        polygons.add(new Triangle(
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, 100),
                new Vertex(-100, 100, 100),
                new Color(255,255,0)));

        polygons.add(new Triangle(
                new Vertex(100, 100, -100),
                new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Color(255,0,255)));
        polygons.add(new Triangle(
                new Vertex(-100, -100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, 100, -100),
                new Color(255,0,255)));

        polygons.add(new Triangle(
                new Vertex(-100, 100, 100),
                new Vertex(100, 100, 100),
                new Vertex(-100, 100, -100),
                new Color(255,255,255)));
        polygons.add(new Triangle(
                new Vertex(100, 100, -100),
                new Vertex(-100, 100, -100),
                new Vertex(100, 100, 100),
                new Color(255,255,255)));

        polygons.add(new Triangle(
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, 100),
                new Vertex(-100, -100, -100),
                new Color(255,0,0)));
        polygons.add(new Triangle(
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, -100),
                new Vertex(100, -100, 100),
                new Color(255,0,0)));
    }

    public void defObjectPyramid(){
        polygons.add(new Triangle(
                new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100),
                new Color(255,255,255)));

        polygons.add(new Triangle(
                new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, -100),
                new Color(255,0,0)));

        polygons.add(new Triangle(
                new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(100, 100, 100),
                new Color(0,255,0)));

        polygons.add(new Triangle(
                new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, 100),
                new Color(0,0,255)));
    }*/
