import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

public class RenderPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener {

    Countries countries;
    boolean mouseHold;
    double xAng;
    double yAng;
    double xLastAng;
    double yLastAng;
    double xCurrent;
    double yCurrent;

    int mouseX;
    int mouseY;
    double zoomSize = 1;

    Random random = new Random();
    String randomCountry;
    String mouseOnCountry;
    int wrongAmount = 0;
    int wrongMax = 4;
    int rightCount = 0;

    public RenderPanel() {
        this.countries = new Countries("world-administrative-boundaries.csv");
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);

        this.randomCountry = "";
        nextRandomName("");
    }

    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(0,0,0));
        g2.fillRect(0, 0, getWidth(), getHeight());

        double[][] zBuffer = new double[getWidth()][getHeight()];
        double[][] zBufferBackup = new double[getWidth()][getHeight()];
        for (int x = 0; x < getWidth(); x++){
            for (int y = 0; y < getHeight(); y++){
                zBuffer[x][y] = Double.NEGATIVE_INFINITY;
                zBufferBackup[x][y] = Double.NEGATIVE_INFINITY;
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

        //Atm
        int atmSize = 300;
        Point2D center = new Point2D.Float(0, 0);
        float[]  dist = {0.0f, 0.5f, 0.75f, 1.0f};
        Color[] colors = {new Color(255, 255, 255, 255), new Color(255, 255, 255, 0), new Color(255, 255, 255, 0), new Color(255, 255, 255, 0)};

        RadialGradientPaint rgp = new RadialGradientPaint(center, (int) (atmSize * zoomSize), dist, colors);
        g2.setPaint(rgp);
        g2.fill(new Ellipse2D.Double((int) -(atmSize * zoomSize) / 2.0,(int) -(atmSize * zoomSize) / 2.0, (int) (atmSize * zoomSize), (int) (atmSize * zoomSize)));

        //Oceans
        g2.setColor(new Color(62, 137, 199));
        g2.fillOval((int) -(200 * zoomSize) / 2,(int) -(200 * zoomSize) / 2, (int) (200 * zoomSize), (int) (200 * zoomSize));

        //Oceans Shading
        dist = new float[]{0.0f, 0.5f, 0.9f,  1.0f};
        colors = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 175), new Color(0, 0, 0, 255), new Color(0, 0, 0, 255)};
        //colors = new Color[]{new Color(255, 255, 255, 0), new Color(255, 255, 255, 175), new Color(255, 255, 255, 255), new Color(255, 255, 255, 255)};

        rgp = new RadialGradientPaint(center, (int) (200 * zoomSize), dist, colors);
        g2.setPaint(rgp);
        g2.fill(new Ellipse2D.Double((int) -(200 * zoomSize) / 2.0,(int) -(200 * zoomSize) / 2.0, (int) (200 * zoomSize), (int) (200 * zoomSize)));

        boolean changeCursor = false;
        boolean mouseInCountry = false;
        int lastIDi = 0;
        int count = 0;
        for (int i = 0; i < countries.polygons.size() + (mouseInCountry ? 1 : 0); i++){
            if (i >= countries.polygons.size()) break;

            if (mouseInCountry && count == i + 1){
                i = lastIDi;
                mouseOnCountry = countries.polygons.get(i).name;
                changeCursor = true;
                zBuffer = zBufferBackup;
            }
            else if (countries.polygons.get(i).id != countries.polygons.get(lastIDi).id) {
                mouseInCountry = false;
            }

            if (countries.polygons.get(i).id != countries.polygons.get(lastIDi).id) lastIDi = i;

            for (Triangle t : countries.polygons.get(i).geoShapes.get(0).triangles){
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
                                if (depth > 0){
                                    if ((mouseX == x && mouseY == y) || mouseInCountry) {
                                        mouseInCountry = true;
                                        g2.setColor(Triangle.getShadow(t.color, Math.abs(normal.z)).brighter().brighter());
                                        //g2.setColor(new Color(255,0,0));
                                    }
                                    else g2.setColor(Triangle.getShadow(t.color, Math.abs(normal.z)));
                                    g2.drawRect(x - (getWidth() / 2), y - (getHeight() / 2), 1, 1);
                                }
                                zBuffer[x][y] = depth;
                            }
                        }
                    }
                }
            }
            count++;
            if (mouseInCountry) count++;
        }

        if (changeCursor) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            int fontSize = 20;
            g2.setColor(new Color(0,0,0,100));
            g2.fillRect((mouseX - getWidth() / 2) - (fontSize * 2 / 5), (mouseY - getHeight() / 2) - fontSize, mouseOnCountry.length() * fontSize*3/5 + (fontSize * 4 / 5), fontSize*7/5);

            g2.setColor(new Color(255,255,255));
            g2.setFont(new Font("Courier Prime", Font.BOLD, fontSize));
            g2.drawString(mouseOnCountry, mouseX - getWidth() / 2,mouseY - getHeight() / 2);
        }
        else if (!mouseHold) this.setCursor(Cursor.getDefaultCursor());

        g2.setColor(new Color(255,255,255));
        g2.setFont(new Font("Ariel", Font.BOLD, 20));
        g2.drawString("ZOOM: " + zoomSize + "x", -getWidth() / 3,getHeight() / 3);

        g2.setColor(new Color(255,255,255));
        g2.setFont(new Font("Ariel", Font.BOLD, 25));
        g2.drawString("[R:" + rightCount + "] Click on " + randomCountry.toUpperCase() + " [W:" + wrongAmount + "]", -getWidth() / 3, (getHeight() / 2) - (getHeight() / 10));
    }

    public void nextRandomName(String lastName){
        int id;
        do{
            id = random.nextInt(256);
            for (CountryPolygon c : countries.polygons){
                if (c.id == id){
                    randomCountry = c.name;
                    break;
                }
            }
        }
        while (randomCountry.equalsIgnoreCase(lastName));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        mouseHold = true;

        mouseX = Integer.MIN_VALUE;
        mouseY = Integer.MIN_VALUE;

        double dpi = 0.07;
        double xi = dpi / Math.max(2.5, zoomSize) * 8;
        double yi = dpi / Math.max(2.5, zoomSize) * 8;
        xAng = -((xCurrent - (e.getX())) * xi - xLastAng);
        yAng = (yCurrent - (e.getY())) * yi + yLastAng;
        this.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (mouseOnCountry.equalsIgnoreCase(randomCountry) || wrongAmount == wrongMax){
            rightCount++;
            wrongAmount = 0;

            nextRandomName(randomCountry);

            System.out.println("Correct.");
        }
        else {
            wrongAmount++;
            System.out.println("Wrong.");
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        xCurrent = e.getX();
        yCurrent = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.setCursor(Cursor.getDefaultCursor());
        mouseHold = false;
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
        double zoomDpi = 0.5;
        zoomSize -= e.getWheelRotation() * zoomDpi * (zoomSize / 4);
        zoomSize = Math.round(zoomSize * 10) / 10.0;
        if (zoomSize < 0.5) zoomSize = 0.5;
        if (zoomSize > 500000) zoomSize = 500000;
        repaint();
    }

}