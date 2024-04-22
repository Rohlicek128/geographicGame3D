import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class RenderPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener {

    Countries countries;
    ArrayList<Dot> dots = new ArrayList<>();
    ArrayList<Trajectory> trajectories = new ArrayList<>();

    static final int EARTH_RADIUS = 200;

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
    int mouseOnCountryID;
    int wrongAmount = 0;
    int wrongMax = 4;
    int rightCount = 0;
    int colorState = 0;
    boolean view = false;

    Color[][] pixels;
    Color primary;
    Color secondary;
    Color correctColor;
    Color wrongColor;

    int frames = 0;
    int printFrames = 0;

    public RenderPanel(Color p, Color s) {
        this.countries = new Countries("world-administrative-boundaries-testSmall.csv", false, s);
        this.dots.add(new Dot(new Vertex(35, 60,0), 1, new Color(253, 147, 25)));
        this.dots.add(new Dot(new Vertex(0, 51, 0), 1, new Color(246, 8, 66)));
        this.trajectories.add(new Trajectory(dots.get(0), dots.get(1), 50));

        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
        this.setFocusable(true);

        this.primary = p;
        this.secondary = s;
        recolorCountries(s);

        this.mouseOnCountry = "";
        this.randomCountry = "";
        nextRandomName("");

        Timer fpsCounter = new Timer(1000, e -> {
            printFrames = frames;
            frames = 0;
        });
        fpsCounter.restart();
        fpsCounter.start();

        Timer updating = new Timer(1, e -> {
            repaint();
            frames++;
        });
        updating.restart();
        updating.start();
    }

    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(0,0,0));
        g2.fillRect(0, 0, getWidth(), getHeight());

        pixels = new Color[getWidth()][getHeight()];
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
        int atmSize = EARTH_RADIUS * 3/2;
        Point2D center = new Point2D.Float(0, 0);
        float[]  dist = {0.0f, 0.5f, 0.75f, 1.0f};
        Color[] colors = {new Color(255, 255, 255, 255), new Color(255, 255, 255, 0), new Color(255, 255, 255, 0), new Color(255, 255, 255, 0)};

        RadialGradientPaint rgp = new RadialGradientPaint(center, (int) (atmSize * zoomSize), dist, colors);
        g2.setPaint(rgp);
        g2.fill(new Ellipse2D.Double((int) -(atmSize * zoomSize) / 2.0,(int) -(atmSize * zoomSize) / 2.0, (int) (atmSize * zoomSize), (int) (atmSize * zoomSize)));

        //Oceans
        g2.setColor(primary);
        g2.fillOval((int) -(EARTH_RADIUS * zoomSize) / 2,(int) -(EARTH_RADIUS * zoomSize) / 2, (int) (EARTH_RADIUS * zoomSize), (int) (EARTH_RADIUS * zoomSize));

        //Oceans Shading
        dist = new float[]{0.0f, 0.5f, 0.9f,  1.0f};
        colors = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 175), new Color(0, 0, 0, 255), new Color(0, 0, 0, 255)};

        rgp = new RadialGradientPaint(center, (int) (EARTH_RADIUS * zoomSize), dist, colors);
        g2.setPaint(rgp);
        g2.fill(new Ellipse2D.Double((int) -(EARTH_RADIUS * zoomSize) / 2.0,(int) -(EARTH_RADIUS * zoomSize) / 2.0, (int) (EARTH_RADIUS * zoomSize), (int) (EARTH_RADIUS * zoomSize)));

        //Render Earth
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        boolean changeCursor = false;
        boolean mouseInCountry = false;
        int lastIDi = 0;
        int count = 0;
        for (int i = 0; i < countries.polygons.size() + (mouseInCountry ? 1 : 0); i++){
            if (i >= countries.polygons.size()) break;

            if (mouseInCountry && count == i + 1){
                i = lastIDi;
                mouseOnCountry = countries.polygons.get(i).name;
                mouseOnCountryID = countries.polygons.get(i).id;
                countries.setPolygonsColor(switch (colorState){
                    case 1 -> correctColor;
                    case -1 -> wrongColor;
                    case 0 -> countries.polygons.get(i).geoShapes.color;
                    default -> throw new IllegalStateException("Unexpected value: " + colorState);
                }, mouseOnCountryID);
                colorState = 0;
                changeCursor = true;
                zBuffer = zBufferBackup;
            }
            else if (countries.polygons.get(i).id != countries.polygons.get(lastIDi).id) {
                mouseInCountry = false;
            }

            if (countries.polygons.get(i).id != countries.polygons.get(lastIDi).id) lastIDi = i;

            //Threads
            /*RenderThread renderThread = new RenderThread(countries.polygons.get(i).geoShapes.triangles, transform, zoomSize, getWidth(), getHeight(), mouseX, mouseY, mouseInCountry, zBuffer);
            renderThread.start();
            System.out.println(i + ". Thread finished");

            mouseInCountry = renderThread.mouseInCountry;
            zBuffer = renderThread.zBuffer;
            addPixels(renderThread.pixels);*/

            for (Triangle t : countries.polygons.get(i).geoShapes.triangles){
                //Vertex normal = Vertex.normalVector(t.v1, t.v2, t.v3);

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
                            if (zBuffer[x][y] < depth && depth > 0){
                                if ((mouseX == x && mouseY == y) || mouseInCountry) {
                                    mouseInCountry = true;
                                    img.setRGB(x, y, Triangle.getShadow(t.color, normal.z).brighter().brighter().getRGB());
                                }
                                else img.setRGB(x, y, Triangle.getShadow(t.color, normal.z).getRGB());
                                zBuffer[x][y] = depth;
                            }
                        }
                    }
                }
            }

            count++;
            if (mouseInCountry) count++;
        }
        g2.drawImage(img, -getWidth() / 2,-getHeight() / 2, getWidth(), getHeight(), this);
        /*for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (pixels[x][y] != null){
                    g2.setColor(pixels[x][y]);
                    g2.drawRect(x - (getWidth() / 2), y - (getHeight() / 2), 1, 1);
                }
            }
        }*/

        //Render Dots
        /*for (Dot dot : dots){
            Vertex v = transform.transform(dot.v);
            v = new Vertex(v.x * zoomSize, v.y * zoomSize, v.z * zoomSize);
            if (v.z >= -1){
                int size = (int) (Math.max(4, dot.size * zoomSize));

                g2.setColor(dot.color);
                g2.fillOval((int) (v.x - size/2), (int) (v.y - size/2), size, size);
            }
        }*/

        //Render Trajectories
        for (Trajectory trajectory : trajectories){
            for (Vertex v : trajectory.segments){
                v = transform.transform(v);
                v = new Vertex(v.x * zoomSize, v.y * zoomSize, v.z * zoomSize);

                int size = (int) (Math.max(4, trajectory.start.size * zoomSize));
                g2.setColor(trajectory.start.color);
                g2.fillOval((int) (v.x - size/2), (int) (v.y - size/2), size, size);
            }
        }

        if (changeCursor) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            //Outline
            for (CountryPolygon cp : countries.polygons){
                if (cp.id == mouseOnCountryID){
                    Path2D path = new Path2D.Double();
                    Vertex v = GeoPoly.gpsToSphere(new Vertex(cp.geoShapes.vertices.get(0).x, cp.geoShapes.vertices.get(0).y, cp.geoShapes.vertices.get(0).z));
                    v = transform.transform(v);
                    path.moveTo(v.x * zoomSize, v.y * zoomSize);
                    for (int i = 1; i < cp.geoShapes.vertices.size(); i++){
                        v = GeoPoly.gpsToSphere(new Vertex(cp.geoShapes.vertices.get(i).x, cp.geoShapes.vertices.get(i).y, cp.geoShapes.vertices.get(i).z));
                        v = transform.transform(v);
                        path.lineTo(v.x * zoomSize, v.y * zoomSize);
                    }
                    path.closePath();
                    g2.setColor(cp.geoShapes.getColor().brighter().brighter().brighter());
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(path);
                }
            }

            //Name Textbox
            if (view){
                int fontSize = 20;
                g2.setColor(new Color(0,0,0,100));
                g2.fillRect((mouseX - getWidth() / 2) - (fontSize * 2 / 5), (mouseY - getHeight() / 2) - fontSize, mouseOnCountry.length() * fontSize*3/5 + (fontSize * 4 / 5), fontSize*7/5);

                g2.setColor(new Color(255,255,255));
                g2.setFont(new Font("Courier Prime", Font.BOLD, fontSize));
                g2.drawString(mouseOnCountry, mouseX - getWidth() / 2,mouseY - getHeight() / 2);
            }
        }
        else if (!mouseHold) this.setCursor(Cursor.getDefaultCursor());

        //UI
        g2.setColor(new Color(255,255,255));
        g2.setFont(new Font("Ariel", Font.BOLD, 20));
        g2.drawString("ZOOM: " + zoomSize + "x", -getWidth() / 3,getHeight() / 3);

        g2.setColor(new Color(255,255,255));
        g2.setFont(new Font("Ariel", Font.BOLD, 25));
        g2.drawString("[R:" + rightCount + "] Click on " + randomCountry.toUpperCase() + " [W:" + wrongAmount + "]", -getWidth() / 3, (getHeight() / 2) - (getHeight() / 10));

        g2.setColor(new Color(255,255,255));
        g2.setFont(new Font("Ariel", Font.BOLD, 20));
        g2.drawString(printFrames + " fps", -getWidth() / 2, -getHeight() / 2 + 20);

        frames++;
    }

    public void addPixels(Color[][] others){
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                if (!Objects.equals(others[i][j], new Color(5, 200, 120))){
                    pixels[i][j] = others[i][j];
                }
            }
        }
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

    public void recolorCountries(Color c){
        for (int i = 0; i < 256; i++) {
            int plusID = 0;
            while (countries.polygons.get(i + plusID).id != i){
                plusID++;
            }

            int randomOffset = new Random().nextInt(25);
            int heightColor = (int) Math.round(Math.abs(Math.cos(Math.toRadians(countries.polygons.get(i + plusID).geoPoint[0])) * 200));

            int red = Math.max(0, Math.min(255, c.getRed() - heightColor + randomOffset));
            int green = Math.max(0, Math.min(255, c.getGreen() - heightColor + randomOffset));
            int blue = Math.max(0, Math.min(255, c.getBlue() - heightColor + randomOffset));

            countries.setPolygonsColor(new Color(red, green, blue), i);
        }
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
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        /*trajectories.get(0).end.v = new Vertex((e.getX() - (double) getWidth() / 2) / zoomSize, (e.getY() - (double) getHeight() / 2) / zoomSize, 0);
        System.out.println(trajectories.get(0).end.v.x + ", " + trajectories.get(0).end.v.y);
        trajectories.get(0).setSegments();*/

        if (mouseOnCountry.equalsIgnoreCase(randomCountry) || wrongAmount == wrongMax){
            rightCount++;
            wrongAmount = 0;
            colorState = 1;

            nextRandomName(randomCountry);

            System.out.println("Correct: " + mouseOnCountry.toUpperCase());
        }
        else {
            wrongAmount++;
            colorState = -1;
            System.out.println("Wrong: " + mouseOnCountry.toUpperCase());
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
        if (zoomSize < 0.25) zoomSize = 0.25;
        if (zoomSize > 500000) zoomSize = 500000;
        repaint();
    }

}