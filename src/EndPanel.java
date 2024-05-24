import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class EndPanel extends JPanel implements KeyListener {

    EndType endType;
    ContinentsState continentsState;
    long finalTime;
    int score;
    double wrongCoef;

    boolean white = true;

    Font courierFontBold = RenderPanel.loadFont("resources/fonts/CourierPrime-Bold.ttf");
    Font courierFontRegular = RenderPanel.loadFont("resources/fonts/CourierPrime-Regular.ttf");
    Font courierFontItalic = RenderPanel.loadFont("resources/fonts/CourierPrime-Italic.ttf");

    public EndPanel(EndType endType, ContinentsState continentsState, long finalTime) {
        this.endType = endType;
        this.continentsState = continentsState;
        this.finalTime = finalTime;
        this.score = calculateScore();

        this.addKeyListener(this);
        this.setFocusable(true);
        this.requestFocus();

        Timer whiteTimer = new Timer(1250, e -> {
            white = false;
            repaint();
        });
        whiteTimer.setRepeats(false);
        whiteTimer.start();
    }

    public void paintComponent(Graphics g1) {
        this.requestFocus();
        Graphics2D g = (Graphics2D) g1;

        //Explosion screen
        if (white && endType == EndType.LOST) {
            g.setColor(new Color(255,255,255));
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        //BG
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());

        //Translate
        g.translate(getWidth() / 2, getHeight() / 2);

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

        String accuracyText = "Accuracy: " + (Math.round(wrongCoef * 1000.0) / 10.0) + " %";
        g.drawString(accuracyText, (int) (-(accuracyText.length() / 2.0) * fontSize/1.66), fontSize * 4 + offset);


        fontSize = 25;
        g.setFont(courierFontItalic.deriveFont(Font.ITALIC, (float) fontSize));
        String nextText = "Press ENTER for next...";
        g.drawString(nextText, (int) (-(nextText.length() / 2.0) * fontSize/1.66), fontSize * 9 + offset);
    }

    public int calculateScore(){
        wrongCoef = 1.0 - ((double) continentsState.overallWrongCount / (continentsState.wrongMax * continentsState.overallMaxCorrect));

        double score;
        if (endType == EndType.LOST) score = -1000 + (((double) continentsState.overallCorrectCount / continentsState.overallMaxCorrect) * 1000.0) * wrongCoef;
        else score = ((Math.pow(continentsState.overallMaxCorrect, 2) / ((finalTime / 1000.0)) / 1.5) * 100.0) * wrongCoef;
        return (int) Math.floor(score);
    }

    public void resetGame(){
        RenderWindow rw = (RenderWindow) SwingUtilities.getWindowAncestor(this);
        rw.setRenderPanel();

        this.setVisible(false);
        this.removeAll();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            resetGame();
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
