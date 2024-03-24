import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Renderer extends JFrame {

    ArrayList<Triangle> polygons = new ArrayList<>();

    public Renderer() {
        //defObjectPyramid();
        defObjectSquare();

        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());

        RenderPanel renderPanel = new RenderPanel();
        pane.add(renderPanel, BorderLayout.CENTER);

        this.setTitle("Renderer3D");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(720, 720);
        this.setVisible(true);
    }



    public void defObjectSquare(){
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
    }
}

/*
JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0,0,0));
                g2.fillRect(0, 0, getWidth(), getHeight());

                double heading = Math.toRadians(x);
                Matrix3 headingTransform = new Matrix3(new double[][]{
                        {Math.cos(heading), 0, -Math.sin(heading)},
                        {0, 1, 0},
                        {Math.sin(heading), 0, Math.cos(heading)}
                });
                double pitch = Math.toRadians(y);
                Matrix3 pitchTransform = new Matrix3(new double[][]{
                        {1, 0, 0},
                        {0, Math.cos(pitch), Math.sin(pitch)},
                        {0, -Math.sin(pitch), Math.cos(pitch)}
                });
                Matrix3 transform = headingTransform.multiply(pitchTransform);

                g2.translate(getWidth() / 2, getHeight() / 2);
                g.setColor(new Color(255,255,255));
                for (Triangle t : pyramid){
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);

                    Path2D path = new Path2D.Double();
                    path.moveTo(t.v1.x, t.v1.y);
                    path.lineTo(t.v2.x, t.v2.y);
                    path.lineTo(t.v3.x, t.v3.y);
                    path.closePath();
                    g2.draw(path);
                }
            }
        };
        renderPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                double xi = 180.0 / renderPanel.getWidth();
                double yi = 180.0 / renderPanel.getHeight();
                x = (int) (e.getX() * xi);
                y = -(int) (e.getY() * yi);
                renderPanel.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
 */
