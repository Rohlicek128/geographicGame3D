import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener {

    Countries countries;
    ArrayList<Integer> guessedCountries = new ArrayList<>();
    ContinentsState continentsState;
    Difficulty difficulty;

    Timer rocketTicks;
    Timer update;
    Timer fps;
    long startTime;
    long endTime;
    boolean gameStart = false;
    boolean gameEnd = false;
    boolean toFixBug = false;

    ArrayList<Dot> dots = new ArrayList<>();
    ArrayList<Trajectory> trajectories = new ArrayList<>();
    ArrayList<MouseText> mouseTexts = new ArrayList<>();

    static final int EARTH_DIAMETER = 200;

    boolean mouseHold;
    double xAng;
    double yAng;
    double xLastAng;
    double yLastAng;
    double xCurrent;
    double yCurrent;

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
    boolean viewKeyHelp = false;

    Color primary;
    Color secondary;
    Color correctColor;
    Color wrongColor;
    Font courierFontBold = loadFont("fonts/CourierPrime-Bold.ttf");
    Font courierFontRegular = loadFont("fonts/CourierPrime-Regular.ttf");

    int frames = 0;
    int printFrames = 1;

    GameAudio gameAudio;
    boolean rocketShow = true;
    int coreCount = 2;
    int trianglesRendered = 0;

    public RenderPanel(Color p, Color s, Countries preloaded, Difficulty difficulty) {
        if (preloaded != null) this.countries = preloaded;
        else this.countries = new Countries(Objects.requireNonNull(this.getClass().getResource("world/world-administrative-boundaries.csv")).getPath(), false, s);
        this.difficulty = difficulty;

        this.primary = p;
        this.secondary = s;
        countries.recolorCountries(s, 210);

        update = new Timer(1, e -> {
            repaint();
            frames++;
        });
        update.setRepeats(true);
        update.start();

        fps = new Timer(1000, e -> {
            printFrames = frames;
            frames = 0;
        });
        fps.setRepeats(true);
        fps.start();

        gameAudio = new GameAudio();
        //startGame();
    }

    @Override
    public void paintComponent(Graphics g1){
        super.paintComponent(g1);
        Graphics2D g = (Graphics2D) g1;

        //Fill Background
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());

        //Transform
        Matrix3 transform = getMatrixTransformation();

        //Start idle spin
        if (!gameStart){
            zoomSize = Math.max(0.1, getHeight() / 300.0);
            spinEarth();
        }

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
        if (trajectories.size() >= 2){
            RocketTrajectory rt = (RocketTrajectory) trajectories.get(1);
            if (rt.explode){
                try {
                    if (rt.explosionFrame <= rt.explosionDurationMillis && endTime + 1250 < System.currentTimeMillis() - startTime) rt.animateExplosion(printFrames);
                    else if (endTime + rt.explosionDurationMillis + 3750 < System.currentTimeMillis() - startTime && !toFixBug) endGame(EndType.WON);
                }
                catch (Exception ignored){
                }
            }
        }
        if (difficulty.startDot == null){
            try {
                continentsState.getCurrentCorrectMax(continentsState.currentIndex);
            }
            catch (Exception e){
                endGame(EndType.WON);
            }
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

        //Help when wrong
        if (wrongAmount >= wrongMax) {
            int count = 0;
            for (CountryPolygon cpo : countries.polygons){
                if (cpo.name.equalsIgnoreCase(randomCountry)){
                    //Outline
                    cpo.drawCountryOutline(g, transform, zoomSize, 3.0f, wrongColor.brighter());
                    count++;
                    if (count > 1) continue;

                    //Pulsating circle
                    Vertex p = GeoPoly.gpsToSphere(new Vertex(cpo.geoPoint[1], cpo.geoPoint[0], 0));
                    p = transform.transform(p);
                    p = new Vertex(p.x * zoomSize, p.y * zoomSize, p.z * zoomSize);

                    int pSize = (int) (Math.max(25, Math.min(EARTH_DIAMETER, countries.vertexCountForCountry(cpo) / 20.0)) * zoomSize);
                    for (int i = 1; i <= 25; i++) {
                        int size = pSize / i;
                        int alpha = (int) (Math.max(0, Math.min(255, (80.0 - (80.0 / i)) * (Math.sin(System.currentTimeMillis() / 400.0) + 1))));
                        g.setColor(new Color(wrongColor.getRed(),wrongColor.getGreen(), wrongColor.getBlue(), alpha));
                        g.fillOval((int) (p.x - size/2), (int) (p.y - size/2), size, size);
                    }
                }
            }
        }

        //If mouse is on a country
        if (changeCursor) {
            if (this.getCursor() != Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            //Outline
            for (CountryPolygon cp : countries.polygons){
                if (cp.id == mouseOnCountryID){
                    cp.drawCountryOutline(g, transform, zoomSize, 2.0f, cp.geoShapes.getColor().brighter().brighter().brighter());
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
                g.drawString(countries.vertexCountForCountry(country) + " vertices", mouseX - getWidth() / 2, (int) (mouseY - getHeight() / 2 - fontSize * 2.4));
            }
        }
        else if (!mouseHold) this.setCursor(Cursor.getDefaultCursor());

        //UI
        if (!gameEnd && gameStart) {
            g.setColor(new Color(255,255,255, (int) Math.min(150, Math.max(0, 150 - (zoomSize - 160)))));
            String correctAmountText = continentsState.correctCount + "/" + continentsState.getCurrentCorrectMax(continentsState.currentIndex);
            int fontSize = 35;
            g.setFont(courierFontBold.deriveFont((float) fontSize));
            g.drawString(correctAmountText, (int) (-(correctAmountText.length() / 2.0) * fontSize/1.66), (int) ((getHeight()/2 - 360) + 360 * 7.2/10));

            g.setColor(new Color(255,255,255, (int) Math.min(175, Math.max(0, 175 - (zoomSize * 0.75 - 160)))));
            String continent = continentsState.orderedContinents.get(continentsState.currentIndex).name.toUpperCase();
            if (randomCountry.equalsIgnoreCase("Russia")) continent = "\"EUROPE\"";
            fontSize = 25;
            g.setFont(courierFontRegular.deriveFont(Font.PLAIN, (float) fontSize));
            g.drawString(continent, (int) (-(continent.length() / 2.0) * fontSize/1.66), (getHeight()/2 - 360) + 360 * 9/10);

            String country = randomCountry.toUpperCase();
            fontSize = 50;
            g.setFont(courierFontBold.deriveFont((float) fontSize));
            g.drawString(country, (int) (-(country.length() / 2.0) * fontSize/1.66), (int) ((getHeight()/2 - 360) + 360 * 8.35/10));
        }

        //Start text
        if (!gameStart) {
            //Start text
            int fontSize = 35;
            g.setColor(new Color(255,255,255, (int) (Math.min(1, Math.ceil(Math.sin(System.currentTimeMillis() / (Math.PI * 100)) + 0.25)) * 165.0)));
            g.setFont(courierFontBold.deriveFont(Font.ITALIC, (float) fontSize));
            String text = "Press any key to start...";
            g.drawString(text, (int) (-(text.length() / 2.0) * fontSize/1.66), fontSize / 2);

            //Copyright
            fontSize = 20;
            g.setColor(new Color(255,255,255, 20));
            g.setFont(courierFontBold.deriveFont(Font.ITALIC, (float) fontSize));
            text = "© 2024 Adam Švec, No rights reserved";
            g.drawString(text.toUpperCase(), (int) (-(text.length() / 2.0) * fontSize/1.66), getHeight() / 2 - 30);
        }

        //Translate back
        g.translate(-getWidth() / 2, -getHeight() / 2);

        //Draw Mouse texts
        mouseTexts.removeIf(mt -> mt.drawMouseText(g, courierFontBold, 25, (int) ((1.0 / printFrames) * 1000)));

        //Tooltip
        if (viewTooltip){
            ArrayList<UIText> uiTooltipText = new ArrayList<>();
            uiTooltipText.add(new UIText(printFrames + " fps", false));

            uiTooltipText.add(new UIText("Difficulty: " + difficulty.name.toUpperCase(), true));
            if (difficulty.startDot != null) uiTooltipText.add(new UIText(difficulty.startDot.name + " -> " + difficulty.endDot.name, false));

            uiTooltipText.add(new UIText("Guessed:  " + continentsState.overallCorrectCount + "/" + continentsState.overallMaxCorrect + " [" + difficulty.correctPercentage + "%]", true));
            uiTooltipText.add(new UIText("Wrong:  " + continentsState.overallWrongCount + "/" + (continentsState.wrongMax * continentsState.overallMaxCorrect), false));
            uiTooltipText.add(new UIText("Points:  " + continentsState.points + " pt", false));
            if (difficulty.startDot != null) {
                RocketTrajectory rocket = (RocketTrajectory) trajectories.get(1);
                uiTooltipText.add(new UIText("Time:  " + Math.round((System.currentTimeMillis() - startTime) / 100.0) / 10.0 + " s / " + rocket.flightDuration + " s", false));
            }

            uiTooltipText.add(new UIText("Resolution:  " + getWidth() + "x" + getHeight(), true));
            uiTooltipText.add(new UIText("Render Cores:  " + coreCount, false));
            uiTooltipText.add(new UIText("Triangles rendered:  " + trianglesRendered, false));

            uiTooltipText.add(new UIText("Transform angle:  X: " + Math.round(xAng * 10.0) / 10.0 + ", Y: " + Math.round(yAng * 10.0) / 10.0, true));
            uiTooltipText.add(new UIText("Zoom:  " + zoomSize + "x", false));
            uiTooltipText.add(new UIText("Volume:  " + gameAudio.audioVolume + " db", false));

            int x = 0;
            int y = 0;
            int fontSize = 20;
            g.setColor(new Color(255,255,255));
            g.setFont(new Font("Ariel", Font.BOLD, fontSize));

            int spaceCount = 0;
            for (int i = 0; i < uiTooltipText.size(); i++) {
                spaceCount += uiTooltipText.get(i).isSpaceAbove ? 1 : 0;
                g.drawString(uiTooltipText.get(i).text, x, y + fontSize * (i + 1 + spaceCount));
            }
        }

        //Key Help
        if (viewKeyHelp){
            ArrayList<UIText> uiKeyHelpText = new ArrayList<>();
            uiKeyHelpText.add(new UIText("F1 = This menu", false));
            uiKeyHelpText.add(new UIText("F3 = Info menu", false));
            uiKeyHelpText.add(new UIText("F11 = Fullscreen (Not recommended)", false));
            uiKeyHelpText.add(new UIText("ESCAPE = Quit Game", true));

            uiKeyHelpText.add(new UIText("R = Return to menu", false));
            uiKeyHelpText.add(new UIText("C = Center camera to current continent", true));

            uiKeyHelpText.add(new UIText("PAUSE = Stops rockets beeping", false));
            uiKeyHelpText.add(new UIText("PAGE UP = Increases audio volume", false));
            uiKeyHelpText.add(new UIText("PAGE DOWN = Decreases audio volume", false));

            int x = 0;
            int y = getHeight();
            int fontSize = 20;
            g.setColor(new Color(255,255,255));
            g.setFont(new Font("Ariel", Font.BOLD, fontSize));

            int spaceCount = 0;
            Collections.reverse(uiKeyHelpText);
            for (int i = 0; i < uiKeyHelpText.size(); i++) {
                spaceCount += uiKeyHelpText.get(i).isSpaceAbove ? 1 : 0;
                g.drawString(uiKeyHelpText.get(i).text, x, y - fontSize * (i + 6 + spaceCount));
            }
        }

        //Scanlines
        int thickness = 2;
        for (int i = 0; i < getHeight() / (thickness * 2); i++) {
            g.setColor(new Color(0,0,0, 40));
            g.fillRect(0, i * (thickness * 2), getWidth(), thickness);
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
        int offset = random.nextInt(25) - 12;
        Color c = switch (Math.min(wrongMax, wrongAmount)){
            case 0 -> correctColor;
            case 1 -> new Color(255, 203, 21);
            case 2 -> new Color(255, 97, 18);
            case 3 -> wrongColor;
            default -> new Color(219, 39, 255);
        };
        int r = Math.max(0, Math.min(255, c.getRed() + offset));
        int g = Math.max(0, Math.min(255, c.getGreen() + offset));
        int b = Math.max(0, Math.min(255, c.getBlue() + offset));
        return new Color(r, g, b);
    }

    /**
     * Generates next random country until it meets all the requirements.
     * @param lastName - Last generated country.
     */
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
                if (checkedCount >= 50) {
                    continentsState.minCountrySize -= 40;
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
                        if (c.id == id && continentsState.equalsCurrentContinent(c, countries.vertexCountForCountry(c))){
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

            gameAudio.playSound(Objects.requireNonNull(this.getClass().getResource("sounds/Explosion-Sound-Effect.wav")).getPath(), gameAudio.audioVolume);

            endTime = System.currentTimeMillis() - startTime;
        }
    }

    public Font loadFont(String path){
        Font temp = null;
        try {
            temp = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream(path)).deriveFont(Font.BOLD, 25);
        } catch (Exception e) {
            System.out.println("Font failed to load.");
        }
        return temp;
    }

    public void spinEarth(){
        xAng -= 0.35;
        yAng = -35.0;
        //yAng -= 0.35;
    }

    /**
     * Starts the game.
     */
    public void startGame(){
        this.continentsState = new ContinentsState(600, difficulty, wrongMax);
        this.continentsState.loadNumOfCountries(countries);
        //continentsState.randomizeOrder();
        setCameraToCoordinates(continentsState.orderedContinents.get(continentsState.currentIndex).centralCoordinates);

        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
        this.setFocusable(true);

        this.mouseOnCountry = "";
        this.randomCountry = "";
        nextRandomName("");

        gameStart = true;

        if (difficulty.startDot != null){
            this.trajectories.add(new Trajectory(difficulty.startDot, difficulty.endDot, true, 300));
            this.trajectories.add(new RocketTrajectory(difficulty.startDot, difficulty.endDot, difficulty.flightDuration));
            System.out.println("Rocket distance: " + trajectories.get(1).straightDistance + " km");

            gameAudio.start();

            AtomicBoolean nextSec = new AtomicBoolean(true);
            rocketTicks = new Timer(1000, e -> {
                rocketShow = !rocketShow;
                RocketTrajectory rt = (RocketTrajectory) trajectories.get(1);

                rt.updateRocket(rocketShow);
                if (rocketShow) trajectories.get(0).recolorFromPercentage(new Color(0, true), 0, (double) rt.currentSegment / rt.numOfSegments);
                if (rt.currentSegment >= rt.numOfSegments && rocketShow && !toFixBug) {
                    if (nextSec.get()) nextSec.set(false);
                    else {
                        gameEnd = true;
                        endTime = System.currentTimeMillis() - startTime - 1000;
                        endGame(EndType.LOST);
                    }
                }
                repaint();
            });
            rocketTicks.setRepeats(true);
            rocketTicks.start();
        }

        startTime = System.currentTimeMillis();
    }

    /**
     * Ends the game.
     * @param endType
     */
    public void endGame(EndType endType){
        toFixBug = true;
        if (difficulty.startDot != null){
            this.rocketTicks.start();
            this.update.stop();
        }

        interruptAudio();
        this.setVisible(false);
        this.removeAll();

        RenderWindow rw = (RenderWindow) SwingUtilities.getWindowAncestor(this);
        rw.setEndPanel(new EndPanel(endType, continentsState, endTime));
    }

    public void interruptAudio(){
        do {
            gameAudio.interrupt();
        }
        while (gameAudio.isAlive());
    }

    /**
     * Moves camera to desired coordinates.
     * @param coordinates - coordinates in the Cartesian coordinate system.
     */
    public void setCameraToCoordinates(double[] coordinates){
        double x = -coordinates[0];
        double y = -coordinates[1];

        xAng = x;
        yAng = y;
        xLastAng = x;
        yLastAng = y;
        xCurrent = x;
        yCurrent = y;
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
        yAng = Math.max(-90.0, Math.min(90.0, (yCurrent - (e.getY())) * yi + yLastAng));
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
        if (mouseOnCountry.equalsIgnoreCase("") || gameEnd) {
            repaint();
            return;
        }

        if (mouseOnCountry.equalsIgnoreCase(randomCountry)){
            continentsState.correctCount++;
            continentsState.overallCorrectCount++;

            int points = (30 - (wrongAmount * 10)) / (difficulty.startDot == null ? 2 : 1);
            continentsState.points += points;
            mouseTexts.add(new MouseText("+" + points, mouseX, mouseY, 750));

            guessedCountries.add(countries.findByName(mouseOnCountry).id);

            System.out.println("Correct: " + mouseOnCountry.toUpperCase());

            nextRandomName(randomCountry);
            wrongAmount = 0;
        }
        else {
            if (wrongAmount + 1 <= wrongMax){
                wrongAmount++;
                continentsState.overallWrongCount++;
            }

            mouseTexts.add(new MouseText("X", mouseX, mouseY, 750));
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