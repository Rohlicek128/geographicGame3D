import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class RenderPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, ChangeListener {

    JSlider slider;
    ArrayList<Triangle> polygons;
    Countries countries;
    int xAng;
    int yAng;
    int xLastAng;
    int yLastAng;
    int xCurrent;
    int yCurrent;
    double zoomSize = 1;

    public RenderPanel(ArrayList<Triangle> p) {
        slider = new JSlider(0, 50, 1);
        slider.setPaintTrack(true);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(5);
        slider.setOrientation(SwingConstants.VERTICAL);
        slider.addChangeListener(this);
        slider.setFocusable(true);
        this.add(slider);

        this.polygons = p;
        this.countries = new Countries("world-administrative-boundaries-testSmall.csv");
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
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
        //Triangle t : polygons
        for (Triangle t : countries.polygons.get(0).geoShape.triangles){
            Vertex v1 = transform.transform(t.v1);
            Vertex v2 = transform.transform(t.v2);
            Vertex v3 = transform.transform(t.v3);

            v1 = new Vertex(v1.x * zoomSize, v1.y * zoomSize, v1.z * zoomSize);
            v2 = new Vertex(v2.x * zoomSize, v2.y * zoomSize, v2.z * zoomSize);
            v3 = new Vertex(v3.x * zoomSize, v3.y * zoomSize, v3.z * zoomSize);

            Vertex normal = Vertex.normalVector(v1, v2, v3);

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
                            g2.setColor(Triangle.getShadow(t.color, Math.abs(normal.z)));
                            g2.drawRect(x - (getWidth() / 2), y - (getHeight() / 2), 1, 1);
                            zBuffer[x][y] = depth;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < countries.polygons.size(); i++) {
            ArrayList<Vertex> vertices = countries.polygons.get(i).geoShape.vertices;
            //if (countries.polygons.get(i).type.equalsIgnoreCase(" \"\"type\"\": \"\"MultiPolygon\"\"}\"")) continue;

            g2.setColor(new Color(255,255,255));
            Path2D path = new Path2D.Double();
            double x = vertices.get(0).x * zoomSize;
            double y = -vertices.get(0).y * zoomSize;
            path.moveTo(x, y);
            for (int j = 1; j < vertices.size(); j++) {
                x = vertices.get(j).x * zoomSize;
                y = -vertices.get(j).y * zoomSize;
                path.lineTo(x, y);
                //g2.setColor(new Color(255,255,255));
                //g2.fillOval((int) ((int) v.x * zoomSize), (int) -((int) v.y * zoomSize), (int) Math.ceil(zoomSize * 2), (int) Math.ceil(zoomSize * 2));
            }
            path.closePath();
            g2.draw(path);
        }


        g2.setColor(new Color(255,255,255));
        g2.setFont(new Font("Ariel", Font.BOLD, 20));
        g2.drawString("ZOOM: " + zoomSize + "x", -getWidth() / 3,getHeight() / 3);
        //slider.paint(g);
    }



    @Override
    public void mouseDragged(MouseEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        double dpi = 0.03;
        double xi = 180.0 / this.getWidth();
        double yi = 180.0 / this.getHeight();
        xAng = -(int) ((xCurrent - (e.getX())) * xi - xLastAng);
        yAng = (int) ((yCurrent - (e.getY())) * yi + yLastAng);
        this.repaint();
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
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.setCursor(Cursor.getDefaultCursor());
        xLastAng = xAng;
        yLastAng = yAng;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoomSize -= e.getWheelRotation() * 1;
        zoomSize = Math.round(zoomSize * 100) / 100.0;
        if (zoomSize < 0.1) zoomSize = 0.1;
        //if (zoomSize > 10) zoomSize = 10;
        slider.setValue((int) zoomSize);
        repaint();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        zoomSize = slider.getValue();
        repaint();
    }
}