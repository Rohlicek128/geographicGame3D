import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class RenderPanel extends JPanel implements MouseMotionListener, MouseListener {

    ArrayList<Triangle> polygons;
    int xAng;
    int yAng;
    int xLastAng;
    int yLastAng;
    int xCurrent;
    int yCurrent;
    boolean isHeld;

    public RenderPanel(ArrayList<Triangle> p) {
        this.polygons = p;
        this.isHeld = false;
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }

    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(0,0,0));
        g2.fillRect(0, 0, getWidth(), getHeight());

        double[][] zBuffer = new double[getWidth()][getHeight()];
        for (int x = 0; x < getWidth(); x++){
            for (int y = 0; y < getHeight(); y++){
                zBuffer[x][y] = Double.NEGATIVE_INFINITY;
            }
        }

        double heading = Math.toRadians(xAng);
        Matrix3 headingTransform = new Matrix3(new double[][]{
                {Math.cos(heading), 0, -Math.sin(heading)},
                {0, 1, 0},
                {Math.sin(heading), 0, Math.cos(heading)}
        });
        double pitch = Math.toRadians(yAng);
        Matrix3 pitchTransform = new Matrix3(new double[][]{
                {1, 0, 0},
                {0, Math.cos(pitch), Math.sin(pitch)},
                {0, -Math.sin(pitch), Math.cos(pitch)}
        });
        Matrix3 transform = headingTransform.multiply(pitchTransform);

        g2.translate(getWidth() / 2, getHeight() / 2);
        g2.setColor(new Color(255,255,255));
        for (Triangle t : polygons){
            Vertex v1 = transform.transform(t.v1);
            Vertex v2 = transform.transform(t.v2);
            Vertex v3 = transform.transform(t.v3);

            v1.x += getWidth() / 2.0;
            v1.y += getHeight() / 2.0;
            v2.x += getWidth() / 2.0;
            v2.y += getHeight() / 2.0;
            v3.x += getWidth() / 2.0;
            v3.y += getHeight() / 2.0;

            int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
            int maxX = (int) Math.min(getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
            int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
            int maxY = (int) Math.min(getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    Vertex p = new Vertex(x, y, 0);

                    boolean V1 = Vertex.crossProduct(v1, v2, v3, p);
                    boolean V2 = Vertex.crossProduct(v2, v3, v1, p);
                    boolean V3 = Vertex.crossProduct(v3, v1, v2, p);
                    if (V1 && V2 && V3){
                        double depth = v1.z + v2.z + v3.z;
                        if (zBuffer[x][y] < depth){
                            g2.setColor(t.color);
                            g2.drawRect(x - (getWidth() / 2), y - (getHeight() / 2), 1, 1);
                            zBuffer[x][y] = depth;
                        }
                    }
                }
            }
        }

    }



    @Override
    public void mouseDragged(MouseEvent e) {
        if (isHeld){
            double dpi = 0.03;
            double xi = 180.0 / this.getWidth();
            double yi = 180.0 / this.getHeight();
            xAng = (int) ((xCurrent - (e.getX())) * xi + xLastAng);
            yAng = -(int) ((yCurrent - (e.getY())) * yi - yLastAng);

            System.out.println(Math.toRadians(xAng));
            System.out.println(Math.toRadians(yAng));
            this.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        xCurrent = e.getX();
        yCurrent = e.getY();
        isHeld = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        xLastAng = xAng;
        yLastAng = yAng;
        isHeld = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}