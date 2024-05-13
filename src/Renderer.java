import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Renderer extends JFrame implements KeyListener {
    RenderPanel renderPanel;

    ImageIcon img = new ImageIcon("icon2.png");
    Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    boolean fullscreen = false;
    Dimension lastWindowSize;
    int[] lastWindowLocation;

    int sign = 1;
    double moveDPI = 2.5;

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

        this.setTitle("DEFCON ONE");
        this.setIconImage(img.getImage());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(this.lastWindowSize);
        this.setLocation(this.lastWindowLocation[0], this.lastWindowLocation[1]);
        this.setVisible(true);

        JPanel endPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0));
                g.fillRect(0, 0, getWidth(), getHeight());

                g.translate(getWidth() / 2, getHeight() / 2);

                g.setColor(new Color(255, 255, 255));
                g.setFont(new Font("Courier Prime", Font.BOLD, 50));
                g.drawString("YOU DIED.", -125, 25);
            }
        };

        Timer endWaiter = new Timer(3000, e -> {
            if (!renderPanel.isVisible()){
                pane.add(endPanel);
                endPanel.repaint();
                endPanel.revalidate();
                this.repaint();
            }
        });
        endWaiter.setRepeats(true);
        endWaiter.start();
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
        else if(e.getKeyCode() == KeyEvent.VK_F11) {
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
        else if (e.getKeyCode() == KeyEvent.VK_A) {
            double x = renderPanel.trajectories.get(0).end.v.x - moveDPI;
            double y = renderPanel.trajectories.get(0).end.v.y;
            if (x > 180) x = -180;
            if (x < -180) x = 180;

            renderPanel.trajectories.get(0).end.v = new Vertex(x, y, 0);
            renderPanel.trajectories.get(0).setSegments();
        }
        else if (e.getKeyCode() == KeyEvent.VK_D) {
            double x = renderPanel.trajectories.get(0).end.v.x + moveDPI;
            double y = renderPanel.trajectories.get(0).end.v.y;
            if (x > 180) x = -180;
            if (x < -180) x = 180;

            renderPanel.trajectories.get(0).end.v = new Vertex(x, y, 0);
            renderPanel.trajectories.get(0).setSegments();
        }
        else if (e.getKeyCode() == KeyEvent.VK_W) {
            double x = renderPanel.trajectories.get(0).end.v.x;
            double y = renderPanel.trajectories.get(0).end.v.y + moveDPI * sign;
            if (y > 90) sign *= -1;
            if (y < -90) sign *= -1;

            renderPanel.trajectories.get(0).end.v = new Vertex(x, y, 0);
            renderPanel.trajectories.get(0).setSegments();
        }
        else if (e.getKeyCode() == KeyEvent.VK_S) {
            double x = renderPanel.trajectories.get(0).end.v.x;
            double y = renderPanel.trajectories.get(0).end.v.y - moveDPI * sign;
            if (y > 90) sign *= -1;
            if (y < -90) sign *= -1;

            renderPanel.trajectories.get(0).end.v = new Vertex(x, y, 0);
            renderPanel.trajectories.get(0).setSegments();
        }
        else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            renderPanel.gameAudio.audioVolume += 10;
            //renderPanel.coreCount++;
            System.out.println("VOLUME UP, cores:" + renderPanel.coreCount);
        }
        else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            renderPanel.gameAudio.audioVolume -= 10;
            //renderPanel.coreCount--;
            System.out.println("VOLUME DOWN, cores:" + renderPanel.coreCount);
        }
        renderPanel.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
