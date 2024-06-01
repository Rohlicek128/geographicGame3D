import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.atomic.AtomicInteger;

public class EndPanel extends JPanel implements KeyListener {

    int page = 1;

    EndType endType;
    ContinentsState continentsState;
    long finalTime;
    int score;
    double accuracy;

    boolean white = true;

    ScoreboardManager scoreboardManager;
    char[] yourName = new char[3];
    int currentIndexName = 0;
    boolean wasSet = false;

    Font courierFontBold = loadFont("fonts/CourierPrime-Bold.ttf");
    Font courierFontRegular = loadFont("fonts/CourierPrime-Regular.ttf");
    Font courierFontItalic = loadFont("fonts/CourierPrime-Italic.ttf");

    public EndPanel(EndType endType, ContinentsState continentsState, long finalTime) {
        this.endType = endType;
        this.continentsState = continentsState;
        this.finalTime = finalTime;
        this.score = continentsState.calculateScore(finalTime);
        this.accuracy = calculateAccuracy();

        this.scoreboardManager = new ScoreboardManager();
        this.scoreboardManager.loadScoreboard();

        this.addKeyListener(this);
        this.setFocusable(true);
        this.requestFocus();

        setupTimers();
    }

    @Override
    public void paintComponent(Graphics g1) {
        this.requestFocus();
        Graphics2D g = (Graphics2D) g1;

        //BG
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());

        //Translate
        g.translate(getWidth() / 2, getHeight() / 2);

        switch (page){
            case 1 -> drawPage1(g);
            case 2 -> drawPage2(g);
            case 3 -> drawPage3(g);
            default -> resetGame();
        }

        int fontSize = 25;
        g.setFont(courierFontItalic.deriveFont(Font.ITALIC, (float) fontSize));
        String nextText = "Press ENTER for next...";
        g.drawString(nextText, (int) (-(nextText.length() / 2.0) * fontSize/1.66), getHeight() / 2 - 100);

        if (page != 1){
            String backText = "Press ESCAPE to go back...";
            g.drawString(backText, (int) (-(backText.length() / 2.0) * fontSize/1.66), getHeight() / 2 - 75);
        }
    }

    /**
     * Draws your after game summary.
     * @param g - Graphics.
     */
    public void drawPage1(Graphics2D g){
        //Explosion screen
        if (white && endType == EndType.LOST) {
            g.setColor(new Color(255,255,255));
            g.fillRect(-getWidth() / 2, -getHeight() / 2, getWidth(), getHeight());
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        //Line
        if (endType == EndType.WON) g.setColor(new Color(0, 255, 0));
        else g.setColor(new Color(255, 0, 0));
        int thickness = 6;
        g.fillRect(-getWidth() / 4, -thickness / 2, getWidth() / 2, thickness);

        //Text
        int offset = 10;

        g.setColor(new Color(255, 255, 255));
        String gameText = switch (endType){
            case WON -> "You Won";
            case LOST -> "You Died";
        };
        int fontSize = 80;
        g.setFont(courierFontBold.deriveFont((float) fontSize));
        g.drawString(gameText.toUpperCase(), (int) (-(gameText.length() / 2.0) * fontSize/1.66), -offset * 2);


        String scoreText = "Score: " + score + " pt";
        fontSize = 30;
        g.setFont(courierFontRegular.deriveFont(Font.PLAIN, (float) fontSize));
        g.drawString(scoreText, (int) (-(scoreText.length() / 2.0) * fontSize/1.66), fontSize + offset);

        String timeText = "Time: " + (Math.round(finalTime / 100.0) / 10.0) + " s";
        g.drawString(timeText, (int) (-(timeText.length() / 2.0) * fontSize/1.66), fontSize * 2 + offset);

        String guessedCountText = "Guessed: " + continentsState.overallCorrectCount + "/" + continentsState.overallMaxCorrect;
        g.drawString(guessedCountText, (int) (-(guessedCountText.length() / 2.0) * fontSize/1.66), fontSize * 3 + offset);

        String accuracyText = "Accuracy: " + (Math.round(accuracy * 1000.0) / 10.0) + " %";
        g.drawString(accuracyText, (int) (-(accuracyText.length() / 2.0) * fontSize/1.66), fontSize * 4 + offset);
    }

    /**
     * Draws boxes for you to put your initials in.
     * @param g - Graphics.
     */
    public void drawPage2(Graphics2D g){
        //Set your name
        int y = -100;
        UIText.drawTextBox(g, yourName, null, currentIndexName, courierFontBold, 0, y, 400);

        //Text
        int fontSize = 40;
        g.setColor(new Color(255,255,255));
        g.setFont(courierFontBold.deriveFont((float) fontSize));
        String nameText = "Enter your initials:";
        g.drawString(nameText.toUpperCase(), (int) (-(nameText .length() / 2.0) * fontSize/1.66), y - fontSize / 2);

        fontSize = 25;
        g.setFont(courierFontItalic.deriveFont(Font.ITALIC, (float) fontSize));
        String escapeText = "Enter no name to skip...";
        g.drawString(escapeText, (int) (-(escapeText.length() / 2.0) * fontSize/1.66), y + 180);
    }

    /**
     * Draws scoreboard.
     * @param g - Graphics.
     */
    public void drawPage3(Graphics2D g){
        g.setColor(new Color(255, 255, 255));
        int fontSize = 85;
        g.setFont(courierFontBold.deriveFont((float) fontSize));
        String nameText = "SCOREBOARD";
        g.drawString(nameText, (int) (-(nameText.length() / 2.0) * fontSize/1.66), (int) ((fontSize * 1.25) - getHeight() / 2));

        int fromWalls = 125;
        if (scoreboardManager.scoreboard.isEmpty()){
            fontSize = 30;
            g.setFont(courierFontRegular.deriveFont(Font.PLAIN, (float) fontSize));
            String errorText = "No scores set.";
            g.drawString(errorText, (int) (-(errorText.length() / 2.0) * fontSize/1.66), 155 - getHeight() / 2);
        }

        //Translate back
        g.translate(-getWidth() / 2, -getHeight() / 2);

        int row = 3;
        g.setColor(new Color(255,255,255));
        for (Score s : scoreboardManager.scoreboard){
            if (row - 2 > 10) break;
            fontSize = 40;

            if (s.name.equalsIgnoreCase(charsToString(yourName)) && s.score == score){
                g.setColor(new Color(255, 255, 255, 37));
                g.fillRect(0, fontSize * row + fontSize/5, getWidth() , fontSize);
            }

            g.setColor(new Color(255, 255, 255));
            g.setFont(courierFontBold.deriveFont((float) fontSize));
            String space = row - 2 == 10 ? "" : " ";
            g.drawString((row - 2) + "." + space + s.name.toUpperCase(), fromWalls, fontSize * (row + 1));

            int smallFontSize = (int) (fontSize * 0.9);
            g.setFont(courierFontItalic.deriveFont(Font.ITALIC, (float) smallFontSize));
            String scoreText = s.score + "pt " + Character.toUpperCase(s.difficulty.name.toCharArray()[0]);
            g.drawString(scoreText, (int) (-(scoreText.length()) * smallFontSize/1.66) + getWidth() - fromWalls, fontSize * (row + 1));

            row++;
        }

        //Translate back back
        g.translate(getWidth() / 2, getHeight() / 2);
    }


    public static String charsToString(char[] chars){
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (!String.valueOf(c).matches("[a-zA-Z0-9]+")) sb.append(".");
            else sb.append(c);
        }
        return sb.toString();
    }

    public double calculateAccuracy(){
        return 1.0 - ((double) continentsState.overallWrongCount / (continentsState.wrongMax * continentsState.overallMaxCorrect));
    }

    public void setupTimers(){
        AtomicInteger whiteCount = new AtomicInteger();
        Timer update = new Timer(25, e -> {
            if (whiteCount.get() >= 50 && white) white = false;
            else whiteCount.getAndIncrement();
            repaint();
        });
        update.setRepeats(true);
        update.start();
    }

    public void resetGame(){
        this.removeKeyListener(this);
        this.setVisible(false);
        this.removeAll();

        RenderWindow rw = (RenderWindow) SwingUtilities.getWindowAncestor(this);
        rw.setStartPanel();
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

    @Override
    public void keyTyped(KeyEvent e) {
        if (currentIndexName >= yourName.length || !String.valueOf(e.getKeyChar()).matches("[a-zA-Z0-9]+") || wasSet) return;
        yourName[currentIndexName] = Character.toUpperCase(e.getKeyChar());
        currentIndexName++;

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            if (page == 2 && !wasSet && currentIndexName != 0) {
                scoreboardManager.addToScoreboard(new Score(charsToString(yourName), score, continentsState.difficulty));
                wasSet = true;
            }
            page++;
        }
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
            if (page == 1) return;
            page--;
        }
        else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
            if (wasSet) return;
            currentIndexName = Math.max(0, currentIndexName - 1);
            yourName[currentIndexName] = ' ';
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
