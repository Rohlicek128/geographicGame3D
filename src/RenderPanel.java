import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener {

    Countries countries;
    ArrayList<Dot> dots = new ArrayList<>();
    ArrayList<Trajectory> trajectories = new ArrayList<>();

    static final int EARTH_DIAMETER = 200;
    //Font courierPrimeBold = registerFont("resources/fonts/CourierPrime-Bold.ttf", "CourierPrime");

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

    Color primary;
    Color secondary;
    Color correctColor;
    Color wrongColor;

    int frames = 0;
    int printFrames = 0;

    GameAudio gameAudio;
    boolean rocketShow = true;
    int coreCount = 2;

    public RenderPanel(Color p, Color s) {
        this.countries = new Countries("world-administrative-boundaries.csv", false, s);
        //this.dots.add(new Dot(new Vertex(1500, 0, 0), (int) (EARTH_DIAMETER / 4.0), 1, new Color(144, 144, 144))); //Moon

        Dot startDot = new Dot(new Vertex(45.755, 51.813,0), 1, 3, new Color(255, 255, 255, 192)); //Russia Silo
        Dot endDot = new Dot(new Vertex(-73.941, 40.740, 0), 1, 6, new Color(246, 8, 66)); //New York
        //Dot endDot = new Dot(new Vertex(-118.252, 34.056, 0), 1, 6, new Color(246, 8, 66)); //Los Angeles
        //Dot endDot = new Dot(new Vertex(-0.125, 51.501, 0), 1, 6, new Color(246, 8, 66)); //London
        //Dot endDot = new Dot(new Vertex(139.785, 35.675, 0), 1, 6, new Color(246, 8, 66)); //Tokyo
        //Dot endDot = new Dot(new Vertex(18.519, -33.935, 0), 1, 6, new Color(246, 8, 66)); //Cape Town
        this.trajectories.add(new Trajectory(startDot, endDot, 30));
        this.trajectories.add(new RocketTrajectory(startDot, endDot, 120));

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

        gameAudio = new GameAudio();
        gameAudio.start();

        AtomicBoolean nextSec = new AtomicBoolean(true);
        Timer fpsCounter = new Timer(1000, e -> {
            printFrames = frames;
            frames = 0;

            rocketShow = !rocketShow;
            RocketTrajectory rt = (RocketTrajectory) trajectories.get(1);

            rt.updateRocket(rocketShow);
            if (rt.currentSegment >= rt.numOfSegments && rocketShow) {
                if (nextSec.get()) nextSec.set(false);
                else endGame();
            }
            repaint();
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

    public void paintComponent(Graphics g1){
        Graphics2D g = (Graphics2D) g1;

        //Fill Background
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());

        //Transform
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

        //Translate to center of the screen
        g.translate(getWidth() / 2, getHeight() / 2);

        //FAX Machine Demo
        /*int paperWidth = (int) (EARTH_DIAMETER * 4 * 0.7);
        int paperHeight = (int) ((EARTH_DIAMETER * 2) * 0.25);
        g.setColor(new Color(210, 207, 200));
        g.fillRect(-paperWidth / 2, -getHeight() / 2, paperWidth, paperHeight);

        g.setColor(new Color(18, 18, 18));
        g.setFont(new Font("Courier Prime", Font.BOLD, 25));
        g.drawString("Uhhhh, so like click there... uh", -paperWidth / 2 + 20, -getHeight() / 2 + 20);
        int textHeight = getHeight() - 400;
        g.setColor(new Color(18, 18, 18));
        g.setFont(new Font("Courier Prime", Font.BOLD, textHeight));
        g.drawString("4/7", -textHeight + 200, textHeight / 4);*/

        //Atm
        int atmSize = (int) ((EARTH_DIAMETER * 4/3) * zoomSize);
        Point2D center = new Point2D.Float(0, 0);
        float[]  dist = {0.0f, 0.5f, 0.75f, 1.0f};
        Color[] colors = {new Color(255, 255, 255, 255), new Color(255, 255, 255, 0), new Color(255, 255, 255, 0), new Color(255, 255, 255, 0)};

        RadialGradientPaint rgp = new RadialGradientPaint(center, atmSize, dist, colors);
        g.setPaint(rgp);
        g.fill(new Ellipse2D.Double(-atmSize / 2.0,-atmSize / 2.0, atmSize, atmSize));

        //Oceans
        g.setColor(primary);
        g.fillOval((int) -(EARTH_DIAMETER * zoomSize) / 2,(int) -(EARTH_DIAMETER * zoomSize) / 2, (int) (EARTH_DIAMETER * zoomSize), (int) (EARTH_DIAMETER * zoomSize));

        //Oceans Shading
        dist = new float[]{0.0f, 0.5f, 0.9f,  1.0f};
        colors = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 175), new Color(0, 0, 0, 255), new Color(0, 0, 0, 255)};

        rgp = new RadialGradientPaint(center, (int) (EARTH_DIAMETER * zoomSize), dist, colors);
        g.setPaint(rgp);
        g.fill(new Ellipse2D.Double((int) -(EARTH_DIAMETER * zoomSize) / 2.0,(int) -(EARTH_DIAMETER * zoomSize) / 2.0, (int) (EARTH_DIAMETER * zoomSize), (int) (EARTH_DIAMETER * zoomSize)));

        //Thread Rendering
        ArrayList<RenderThread> renderThreads = new ArrayList<>();
        boolean changeCursor = false;
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < coreCount; i++) {
            int startIndex = countries.polygons.size() / coreCount * i;
            int endIndex = Math.min(countries.polygons.size(), countries.polygons.size() / coreCount * (i + 1) + 1);

            renderThreads.add(new RenderThread(startIndex, endIndex, countries, transform, zoomSize, getWidth(), getHeight(), mouseX, mouseY, mouseOnCountryID));
            renderThreads.get(i).start();
        }

        mouseOnCountryID = -1;
        mouseOnCountry = "";
        for (RenderThread rt : renderThreads){
            try {
                rt.join();
                img = addPixels(img, rt.img);

                if (rt.changeCursor) changeCursor = true;
                if (rt.mouseOnCountry != null) mouseOnCountry = rt.mouseOnCountry;
                if (rt.mouseOnCountryID != -1) mouseOnCountryID = rt.mouseOnCountryID;
                if (colorState != 0){
                    countries.setPolygonsColor(switch (colorState){
                        case 1 -> correctColor;
                        case -1 -> wrongColor;
                        default -> throw new IllegalStateException("Unexpected value: " + colorState);
                    }, mouseOnCountryID);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        colorState = 0;
        g.drawImage(img, -getWidth() / 2,-getHeight() / 2, getWidth(), getHeight(), this);

        //Render Dots
        for (Dot dot : dots){
            Vertex v = transform.transform(dot.v);
            v = new Vertex(v.x * zoomSize, v.y * zoomSize, v.z * zoomSize);

            double difC = RenderPanel.EARTH_DIAMETER / 2.0 * zoomSize - Math.sqrt(v.x*v.x + v.y*v.y);
            if (difC < 0 || v.z > 0){
                int size = (int) (Math.max(4, dot.size * zoomSize));

                g.setColor(dot.color);
                g.fillOval((int) (v.x - size/2), (int) (v.y - size/2), size, size);
            }
        }

        //Render Trajectories
        for (Trajectory trajectory : trajectories){
            for (Dot dot : trajectory.dotSegments){
                Vertex v = transform.transform(dot.v);
                v = new Vertex(v.x * zoomSize, v.y * zoomSize, v.z * zoomSize);

                double difC = RenderPanel.EARTH_DIAMETER / 2.0 * zoomSize - Math.sqrt(v.x*v.x + v.y*v.y);
                if ((difC < 0 || v.z > 0) && dot.color.getRGB() != 0){
                    int size = (int) Math.max(dot.minSize, dot.size * zoomSize);
                    g.setColor(dot.color);
                    g.fillOval((int) v.x - size/2, (int) v.y- size/2, size, size);
                }
            }
        }

        //If mouse is on a country
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
                    g.setColor(cp.geoShapes.getColor().brighter().brighter().brighter());
                    g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(path);
                }
            }

            //Name Textbox
            if (view){
                gameAudio.interrupt();
                CountryPolygon country = countries.findByName(mouseOnCountry);

                int fontSize = 20;
                g.setColor(new Color(0,0,0,100));
                g.fillRect((mouseX - getWidth() / 2) - (fontSize * 2 / 5), (mouseY - getHeight() / 2) - fontSize, country.name.length() * fontSize*3/5 + (fontSize * 4 / 5), fontSize*7/5);

                g.setColor(new Color(255,255,255));
                g.setFont(new Font("Courier Prime", Font.BOLD, fontSize));
                g.drawString(country.name, mouseX - getWidth() / 2,mouseY - getHeight() / 2);

                g.setFont(new Font("Courier Prime", Font.BOLD, (int) (fontSize / 1.5)));
                g.drawString(country.continent, mouseX - getWidth() / 2, (int) (mouseY - getHeight() / 2 - fontSize * 1.75));
                g.drawString(country.region, mouseX - getWidth() / 2, (int) (mouseY - getHeight() / 2 - fontSize * 1.1));
            }
        }
        else if (!mouseHold) this.setCursor(Cursor.getDefaultCursor());

        //UI
        g.setColor(new Color(255,255,255));
        g.setFont(new Font("Ariel", Font.BOLD, 20));
        g.drawString("ZOOM: " + zoomSize + "x", -getWidth() / 3,getHeight() / 3);

        g.setColor(new Color(255,255,255));
        g.setFont(new Font("Ariel", Font.BOLD, 25));
        g.drawString("[R:" + rightCount + "] Click on " + randomCountry.toUpperCase() + " [W:" + wrongAmount + "]", -getWidth() / 3, (getHeight() / 2) - (getHeight() / 10));

        g.setColor(new Color(255,255,255));
        g.setFont(new Font("Ariel", Font.BOLD, 20));
        g.drawString(printFrames + " fps", -getWidth() / 2, -getHeight() / 2 + 20);

        frames++;
    }

    public BufferedImage addPixels(BufferedImage img, BufferedImage other){
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (other.getRGB(x, y) != 0){
                    img.setRGB(x, y, other.getRGB(x, y));
                }
            }
        }
        return img;
    }

    public void nextRandomName(String lastName){
        int id;
        do{
            id = random.nextInt(256);
            for (CountryPolygon c : countries.polygons){
                if (c.id == id){
                    randomCountry = c.name;
                    break;
                    //if (c.geoPoint[0] >= 32.5 && c.geoPoint[0] <= 72.3 && c.geoPoint[1] >= -14.7 && c.geoPoint[1] <= 66.7){
                    //}
                }
            }
        }
        while (randomCountry.equalsIgnoreCase(lastName));
    }

    public void recolorCountries(Color c){
        for (int i = 0; i <= countries.polygons.get(countries.polygons.size() - 1).id; i++) {
            int plusID = 0;
            try {
                while (countries.polygons.get(i + plusID).id != i){
                    plusID++;
                }
            }
            catch (Exception e){
                plusID--;
            }

            int randomOffset = new Random().nextInt(25);
            int heightColor = (int) Math.round(Math.abs(Math.cos(Math.toRadians(countries.polygons.get(i + plusID).geoPoint[0])) * 200));

            int red = Math.max(0, Math.min(255, c.getRed() - heightColor + randomOffset));
            int green = Math.max(0, Math.min(255, c.getGreen() - heightColor + randomOffset));
            int blue = Math.max(0, Math.min(255, c.getBlue() - heightColor + randomOffset));

            countries.setPolygonsColor(new Color(red, green, blue), i);
        }
    }

    /*public static Font registerFont(String path, String name) {
        try {
            return Font.getFont(name);
        }
        catch (Exception ignored){
        }

        Font font = null;
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            font = Font.createFont(Font.TRUETYPE_FONT, new File(path));
            ge.registerFont(font);
        } catch (IOException | FontFormatException e) {
            System.out.println("Couldn't load font.");
        }
        return font;
    }*/

    public void endGame(){
        gameAudio.interrupt();
        this.setVisible(false);
        this.removeAll();
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
        if (mouseOnCountry.equalsIgnoreCase("")) {
            repaint();
            return;
        }

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
        zoomSize = Math.round(zoomSize * 1000) / 1000.0;
        if (zoomSize < 0.1) zoomSize = 0.1;
        if (zoomSize > 500000) zoomSize = 500000;
        repaint();
    }

}