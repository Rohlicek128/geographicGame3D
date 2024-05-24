import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener {

    Countries countries;
    ArrayList<Integer> guessedCountries = new ArrayList<>();
    ContinentsState continentsState;
    long startTime;
    long endTime;
    boolean gameEnd = false;

    ArrayList<Dot> dots = new ArrayList<>();
    ArrayList<Trajectory> trajectories = new ArrayList<>();

    static final int EARTH_DIAMETER = 200;

    double startX = -14;
    double startY = -50;
    boolean mouseHold;
    double xAng = startX;
    double yAng = startY;
    double xLastAng = startX;
    double yLastAng = startY;
    double xCurrent = startX;
    double yCurrent = startY;

    int mouseX;
    int mouseY;
    double zoomSize = 2;

    Random random = new Random();
    String randomCountry;
    String mouseOnCountry;
    int mouseOnCountryID;
    int wrongAmount = 0;
    int wrongMax = 3;
    boolean view = false;
    boolean viewTooltip = false;

    Color primary;
    Color secondary;
    Color correctColor;
    Color wrongColor;
    Font courierFontBold = loadFont("resources/fonts/CourierPrime-Bold.ttf");
    Font courierFontRegular = loadFont("resources/fonts/CourierPrime-Regular.ttf");

    int frames = 0;
    int printFrames = 0;

    GameAudio gameAudio;
    boolean rocketShow = true;
    int coreCount = 2;
    int trianglesRendered = 0;

    public RenderPanel(Color p, Color s, Countries preloaded) {
        if (preloaded != null) this.countries = preloaded;
        else this.countries = new Countries("resources/world/world-administrative-boundaries.csv", false, s);
        this.continentsState = new ContinentsState(450, 1, wrongMax, countries);
        //this.dots.add(new Dot(new Vertex(1500, 0, 0), (int) (EARTH_DIAMETER / 4.0), 1, new Color(144, 144, 144))); //Moon

        Dot startDot = new Dot(new Vertex(45.755, 51.813,0), 1, 3, new Color(255, 255, 255, 192)); //Russia Silo
        //Dot startDot = new Dot(new Vertex(129.165, 41.132,0), 1, 3, new Color(255, 255, 255, 192)); //North Korean Silo

        Dot endDot = new Dot(new Vertex(-73.941, 40.740, 0), 1, 6, new Color(246, 8, 66)); //New York
        //Dot endDot = new Dot(new Vertex(-118.252, 34.056, 0), 1, 6, new Color(246, 8, 66)); //Los Angeles
        //Dot endDot = new Dot(new Vertex(-0.125, 51.501, 0), 1, 6, new Color(246, 8, 66)); //London
        //Dot endDot = new Dot(new Vertex(139.785, 35.675, 0), 1, 6, new Color(246, 8, 66)); //Tokyo
        //Dot endDot = new Dot(new Vertex(18.519, -33.935, 0), 1, 6, new Color(246, 8, 66)); //Cape Town
        //Dot endDot = new Dot(new Vertex(174.764, -36.848, 0), 1, 6, new Color(246, 8, 66)); //Auckland
        this.trajectories.add(new Trajectory(startDot, endDot, true, 300));
        this.trajectories.add(new RocketTrajectory(startDot, endDot, 35));
        System.out.println("Rocket distance: " + trajectories.get(1).straightDistance + " km");

        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
        this.setFocusable(true);

        this.primary = p;
        this.secondary = s;
        recolorCountries(s, 210);

        this.mouseOnCountry = "";
        this.randomCountry = "";
        nextRandomName("");

        gameAudio = new GameAudio();
        gameAudio.start();

        AtomicBoolean nextSec = new AtomicBoolean(true);
        Timer rocketTicks = new Timer(1000, e -> {
            printFrames = frames;
            frames = 0;

            rocketShow = !rocketShow;
            RocketTrajectory rt = (RocketTrajectory) trajectories.get(1);

            rt.updateRocket(rocketShow);
            if (rt.currentSegment >= rt.numOfSegments && rocketShow) {
                if (nextSec.get()) nextSec.set(false);
                else endGame(EndType.LOST);
            }
            repaint();
        });
        rocketTicks.restart();
        rocketTicks.start();

        Timer updating = new Timer(1, e -> {
            repaint();
            frames++;
        });
        updating.restart();
        updating.start();

        startTime = System.currentTimeMillis();
    }

    public void paintComponent(Graphics g1){
        Graphics2D g = (Graphics2D) g1;

        //Fill Background
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());

        //Transform
        Matrix3 transform = getMatrixTransformation();

        //Translate to center of the screen
        g.translate(getWidth() / 2, getHeight() / 2);

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
                if (rt.futureMouseOnCountryID != -1) mouseOnCountryID = rt.futureMouseOnCountryID;
                trianglesRendered += rt.trianglesRendered;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        g.drawImage(img, -getWidth() / 2,-getHeight() / 2, getWidth(), getHeight(), this);

        //Atm transition
        dist = new float[]{0.47f, 0.57f, 0.9f, 1.0f};
        colors = new Color[]{new Color(255, 255, 255, 0), new Color(255, 255, 255, 150), new Color(255, 255, 255, 255), new Color(255, 255, 255, 255)};

        rgp = new RadialGradientPaint(center, (int) (EARTH_DIAMETER * zoomSize), dist, colors);
        g.setPaint(rgp);
        g.fill(new Ellipse2D.Double((int) -(EARTH_DIAMETER * zoomSize) / 2.0,(int) -(EARTH_DIAMETER * zoomSize) / 2.0, (int) (EARTH_DIAMETER * zoomSize), (int) (EARTH_DIAMETER * zoomSize)));

        //Rocket Explosion
        RocketTrajectory rt = (RocketTrajectory) trajectories.get(1);
        if (rt.explode){
            if (rt.explosionFrame <= rt.explosionDurationMillis && endTime + 750 < System.currentTimeMillis() - startTime) rt.animateExplosion(printFrames);
            else if (endTime + rt.explosionDurationMillis + 3250 < System.currentTimeMillis() - startTime) endGame(EndType.WON);
        }

        //Render Dots
        for (Dot dot : dots){
            dot.drawDot(g, transform, zoomSize);
        }

        //Render Trajectories
        for (Trajectory trajectory : trajectories){
            for (Dot dot : trajectory.dotSegments){
                dot.drawDot(g, transform, zoomSize);
            }
        }

        //Outline for guess
        if (wrongAmount >= wrongMax) {
            for (CountryPolygon cpo : countries.polygons){
                if (cpo.name.equalsIgnoreCase(randomCountry)){
                    cpo.drawCountryOutline(g, transform, zoomSize, 3.0f);
                }
            }
        }

        //If mouse is on a country
        if (changeCursor) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            //Outline
            for (CountryPolygon cp : countries.polygons){
                if (cp.id == mouseOnCountryID){
                    cp.drawCountryOutline(g, transform, zoomSize, 2.0f);
                }
            }

            //Name Textbox
            if (view && viewTooltip){
                CountryPolygon country = countries.findByName(mouseOnCountry);

                int fontSize = 20;
                g.setColor(new Color(0,0,0,100));
                g.fillRect((mouseX - getWidth() / 2) - (fontSize * 2 / 5), (mouseY - getHeight() / 2) - fontSize, country.name.length() * fontSize*3/5 + (fontSize * 4 / 5), fontSize*7/5);

                g.setFont(courierFontBold.deriveFont((float) fontSize));
                g.setColor(new Color(255,255,255));
                g.drawString(country.name, mouseX - getWidth() / 2,mouseY - getHeight() / 2);

                g.setFont(courierFontBold.deriveFont((float) (fontSize / 1.5)));
                g.drawString(country.continent, mouseX - getWidth() / 2, (int) (mouseY - getHeight() / 2 - fontSize * 1.75));
                g.drawString(country.region, mouseX - getWidth() / 2, (int) (mouseY - getHeight() / 2 - fontSize * 1.1));
                g.drawString(country.geoShapes.vertices.size() + " vertices", mouseX - getWidth() / 2, (int) (mouseY - getHeight() / 2 - fontSize * 2.4));
            }
        }
        else if (!mouseHold) this.setCursor(Cursor.getDefaultCursor());

        //UI
        g.setColor(new Color(255,255,255, (int) Math.min(150, Math.max(0, 150 - (zoomSize*2 - 50)))));
        String correctAmountText = continentsState.correctCount + "/" + continentsState.getCurrentCorrectMax(continentsState.currentIndex);
        int fontSize = 35;
        g.setFont(courierFontBold.deriveFont((float) fontSize));
        g.drawString(correctAmountText, (int) (-(correctAmountText.length() / 2.0) * fontSize/1.66), (int) ((getHeight()/2 - 360) + 360 * 7.2/10));

        g.setColor(new Color(255,255,255, (int) Math.min(175, Math.max(0, 175 - (zoomSize*1.5 - 50)))));
        String clickText = "Click on " + randomCountry.toUpperCase();
        fontSize = 25;
        g.setFont(courierFontRegular.deriveFont(Font.PLAIN, (float) fontSize));
        g.drawString(clickText, (int) (-(clickText.length() / 2.0) * fontSize/1.66), (getHeight()/2 - 360) + 360 * 9/10);

        String continent = continentsState.orderedContinents.get(continentsState.currentIndex).name.toUpperCase();
        if (randomCountry.equalsIgnoreCase("Russia")) continent = "\"EUROPE\"";
        fontSize = 50;
        g.setFont(courierFontBold.deriveFont((float) fontSize));
        g.drawString(continent, (int) (-(continent.length() / 2.0) * fontSize/1.66), (int) ((getHeight()/2 - 360) + 360 * 8.35/10));

        //Tooltip
        if (viewTooltip){
            fontSize = 20;
            g.setColor(new Color(255,255,255));
            g.setFont(new Font("Ariel", Font.BOLD, fontSize));

            g.drawString(printFrames + " fps", -getWidth() / 2, -getHeight() / 2 + fontSize);

            g.drawString("Zoom:  " + zoomSize + "x", -getWidth() / 2, -getHeight() / 2 + fontSize * 3);
            g.drawString("Guessed:  " + continentsState.overallCorrectCount + "/" + continentsState.overallMaxCorrect, -getWidth() / 2, -getHeight() / 2 + fontSize * 4);
            RocketTrajectory rocket = (RocketTrajectory) trajectories.get(1);
            g.drawString("Time:  " + Math.round((System.currentTimeMillis() - startTime) / 100.0) / 10.0 + " s / " + rocket.flightDuration + " s", -getWidth() / 2, -getHeight() / 2 + fontSize * 5);
            g.drawString("Volume:  " + gameAudio.audioVolume + " db", -getWidth() / 2, -getHeight() / 2 + fontSize * 6);

            g.drawString("Render Cores:  " + coreCount, -getWidth() / 2, -getHeight() / 2 + fontSize * 8);
            g.drawString("Triangles rendered:  " + trianglesRendered, -getWidth() / 2, -getHeight() / 2 + fontSize * 9);

            g.drawString("Transform angle:  X: " + Math.round(xAng * 10.0) / 10.0 + ", Y: " + Math.round(yAng * 10.0) / 10.0, -getWidth() / 2, -getHeight() / 2 + fontSize * 11);
        }

        trianglesRendered = 0;
        frames++;
    }

    public Matrix3 getMatrixTransformation() {
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
        return headingTransform.multiply(pitchTransform);
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

    public Color getColorByWrongAmount(){
        return switch (Math.min(wrongMax, wrongAmount)){
            case 0 -> correctColor;
            case 1 -> new Color(255, 203, 21);
            case 2 -> new Color(255, 97, 18);
            case 3 -> wrongColor;
            default -> new Color(219, 39, 255);
        };
    }

    public void nextRandomName(String lastName){
        if (gameEnd) return;

        //Recolor by wrong
        if (!lastName.equalsIgnoreCase("") && lastName.equalsIgnoreCase(mouseOnCountry)) countries.setPolygonsColor(getColorByWrongAmount(), countries.findByName(randomCountry).id);

        int id;
        int checkedCount = 0;
        int lastMin = continentsState.minCountrySize;
        try {
            do{
                boolean skip = false;
                if (checkedCount >= countries.polygons.size() / 2) {
                    continentsState.minCountrySize -= 50;
                    checkedCount = 0;
                }

                id = random.nextInt(countries.polygons.get(countries.polygons.size() - 1).id + 1);
                for (int i : guessedCountries){
                    if (id == i){
                        skip = true;
                        break;
                    }
                }

                if (!skip){
                    for (CountryPolygon c : countries.polygons){
                        if (c.id == id && continentsState.equalsCurrentContinent(c)){
                            randomCountry = c.name;
                            break;
                        }
                    }
                    checkedCount++;
                }
            }
            while (randomCountry.equalsIgnoreCase(lastName));
            continentsState.minCountrySize = lastMin;
        }
        catch (Exception e){
            //Check if Won (disgusting, I know)
            RocketTrajectory rt = (RocketTrajectory) trajectories.get(1);
            rt.explodeRocket(1500, 45);
            gameEnd = true;
            continentsState.currentIndex = continentsState.orderedContinents.size() - 1;
            interruptAudio();

            endTime = System.currentTimeMillis() - startTime;
        }
    }

    public void recolorCountries(Color c, int darkenCoef){
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

            int randomOffset = new Random().nextInt(30);
            int heightColor = (int) Math.round(Math.abs(Math.cos(Math.toRadians(countries.polygons.get(i + plusID).geoPoint[0])) * darkenCoef));

            int red = Math.max(0, Math.min(255, c.getRed() - heightColor + randomOffset));
            int green = Math.max(0, Math.min(255, c.getGreen() - heightColor + randomOffset));
            int blue = Math.max(0, Math.min(255, c.getBlue() - heightColor + randomOffset));

            countries.setPolygonsColor(new Color(red, green, blue), i);
        }
    }

    public static Font loadFont(String path){
        Font temp = null;
        try {
            temp = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(path)).deriveFont(Font.BOLD, 25);
        } catch (Exception e) {
            System.out.println("Font failed to load.");
        }
        return temp;
    }

    public void endGame(EndType endType){
        interruptAudio();

        this.getParent().add(new EndPanel(endType, continentsState, endTime));
        this.setVisible(false);
        this.removeAll();
    }

    public void interruptAudio(){
        do {
            gameAudio.interrupt();
        }
        while (gameAudio.isAlive());
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

        if (mouseOnCountry.equalsIgnoreCase(randomCountry)){
            continentsState.correctCount++;
            continentsState.overallCorrectCount++;

            guessedCountries.add(countries.findByName(mouseOnCountry).id);

            System.out.println("Correct: " + mouseOnCountry.toUpperCase());

            nextRandomName(randomCountry);
            wrongAmount = 0;
        }
        else {
            wrongAmount = Math.min(wrongMax, wrongAmount + 1);
            continentsState.overallWrongCount++;
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