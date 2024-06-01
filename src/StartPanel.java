import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class StartPanel extends JPanel implements KeyListener {

    Font courierFontBold = loadFont("fonts/CourierPrime-Bold.ttf");
    Font courierFontRegular = loadFont("fonts/CourierPrime-Regular.ttf");
    Font courierFontItalic = loadFont("fonts/CourierPrime-Italic.ttf");

    char[] percText = new char[3];
    int currentIndexPerc = 0;
    char[] timeText = new char[3];
    int currentIndexTime = -percText.length;

    ArrayList<Difficulty> difficulties = new ArrayList<>();
    int row = 1;

    boolean tempSpin = false;

    public StartPanel() {
        this.addKeyListener(this);
        this.setFocusable(true);

        Dot rusStartDot = new Dot(new Vertex(45.755, 51.813,0), 1, 3, new Color(255, 255, 255, 192), "Russia"); //Russia Silo
        Dot norStartDot = new Dot(new Vertex(129.165, 41.132,0), 1, 3, new Color(255, 255, 255, 192), "North Korea"); //North Korean Silo

        Dot nycEndDot = new Dot(new Vertex(-73.941, 40.740, 0), 1, 6, new Color(246, 8, 66), "New York");
        Dot laEndDot = new Dot(new Vertex(-118.252, 34.056, 0), 1, 6, new Color(246, 8, 66), "Los Angeles");
        Dot lndEndDot = new Dot(new Vertex(-0.125, 51.501, 0), 1, 6, new Color(246, 8, 66), "London");
        Dot tyoEndDot = new Dot(new Vertex(139.785, 35.675, 0), 1, 6, new Color(246, 8, 66), "Tokyo");
        Dot capEndDot = new Dot(new Vertex(18.519, -33.935, 0), 1, 6, new Color(246, 8, 66), "Cape Town");
        Dot aucEndDot = new Dot(new Vertex(174.764, -36.848, 0), 1, 6, new Color(246, 8, 66), "Auckland");
        Dot prgEndDot = new Dot(new Vertex(14.510, 50.051, 0), 1, 6, new Color(246, 8, 66), "Prague");
        Dot setEndDot = new Dot(new Vertex(-122.235, 47.608, 0), 1, 6, new Color(246, 8, 66), "Seattle");
        Dot dubEndDot = new Dot(new Vertex(55.290, 25.205, 0), 1, 6, new Color(246, 8, 66), "Dubai");
        Dot delEndDot = new Dot(new Vertex(77.212, 28.618, 0), 1, 6, new Color(246, 8, 66), "New Delhi");
        Dot helEndDot = new Dot(new Vertex(24.938, 60.167, 0), 1, 6, new Color(246, 8, 66), "Helsinki");

        difficulties.add(new Difficulty("Peaceful", 100));
        difficulties.add(new Difficulty("Easy", 10, 180, rusStartDot, lndEndDot));
        difficulties.add(new Difficulty("Medium", 20, 220, rusStartDot, nycEndDot));
        difficulties.add(new Difficulty("Hard", 50, 300, norStartDot, setEndDot));
        difficulties.add(new Difficulty("Insane", 100, 310, norStartDot, aucEndDot));

        difficulties.add(new Difficulty("Custom", 0, 0, rusStartDot, laEndDot));

        Timer update = new Timer(25, e -> repaint());
        update.setRepeats(true);
        update.start();
    }

    @Override
    public void paintComponent(Graphics g1) {
        super.paintComponent(g1);
        this.requestFocus();
        Graphics2D g = (Graphics2D) g1;

        //BG
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());

        //Difficulties
        int x = getWidth() / 2;
        int y = 125;
        int fontSize = 30;
        g.setColor(new Color(255,255,255));
        g.setFont(courierFontRegular.deriveFont(Font.PLAIN, (float) fontSize));
        String diffText = "DIFFICULTY";
        g.drawString(diffText, (int) (-(diffText.length() / 2.0) * fontSize/1.66) + x, y - fontSize);

        for (int i = 0; i < difficulties.size(); i++) {
            boolean isSelected = i == row;
            difficulties.get(i).drawDifficultyBox(g, courierFontBold, x, y, 50, isSelected);
            y += 60 + (i == difficulties.size() - 2 ? 30 : 0);

            if (isSelected && i != difficulties.size() - 1) difficulties.get(i).drawDifficultyInfo(g, courierFontBold, getWidth() / 2, 530, 40);
        }

        //Custom difficulty
        if (row == difficulties.size() - 1){
            //Percentage
            x = getWidth() / 4;
            y = 525;
            UIText.drawTextBox(g, percText, new char[]{'0', '5', '8'}, currentIndexPerc, courierFontBold, x, y, 175);

            fontSize = 20;
            g.setColor(new Color(255,255,255));
            g.setFont(courierFontBold.deriveFont(Font.BOLD, (float) fontSize));
            g.drawString("% OF COUNTRIES", (int) (x - ((175 / percText.length) * 1.5 + 10)), y - 6);

            //Time
            x = getWidth() * 3/4;
            UIText.drawTextBox(g, timeText, new char[]{'3', '1', '4'}, currentIndexTime, courierFontBold, x, y, 175);

            g.setColor(new Color(255,255,255));
            g.setFont(courierFontBold.deriveFont(Font.BOLD, (float) fontSize));
            g.drawString("SECONDS", (int) (x - ((175 / percText.length) * 1.5 + 10)), y - 6);
        }

        //Press F1 for help
        fontSize = 22;
        g.setColor(new Color(255,255,255, 125));
        g.setFont(courierFontBold.deriveFont(Font.ITALIC, (float) fontSize));
        g.drawString("Press F1 in-game for help...", fontSize / 2, fontSize);

        fontSize = 25;
        g.setColor(new Color(255,255,255, 255));
        g.setFont(courierFontItalic.deriveFont(Font.ITALIC, (float) fontSize));
        String nextText = "Press ENTER to start...";
        g.drawString(nextText, (int) (-(nextText.length() / 2.0) * fontSize/1.66) + getWidth() / 2, fontSize * 12 + getHeight() / 2);
    }

    public void startGame(){
        this.removeKeyListener(this);
        this.setVisible(false);
        this.removeAll();

        RenderWindow rw = (RenderWindow) SwingUtilities.getWindowAncestor(this);
        //rw.renderPanel.difficulty = difficulties.get(row);
        //rw.renderPanel.startGame();
        rw.setNewRenderPanel(difficulties.get(row), tempSpin);
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
        if (row == difficulties.size() - 1 && currentIndexTime < 0) {
            if (percText[0] == '0' && percText[1] == '0' && e.getKeyChar() == '0') return;
            if (currentIndexPerc == 0 && !String.valueOf(e.getKeyChar()).matches("[0-1]+")) return;
            if (percText[0] == '1' && !String.valueOf(e.getKeyChar()).matches("0")) return;
            if (currentIndexPerc >= percText.length || !String.valueOf(e.getKeyChar()).matches("[0-9]+")) return;

            percText[currentIndexPerc] = Character.toUpperCase(e.getKeyChar());
            currentIndexPerc++;
            currentIndexTime++;
        }
        else if (String.valueOf(e.getKeyChar()).matches("[0-9]+")) {
            if (timeText[0] == '0' && timeText[1] == '0' && e.getKeyChar() == '0') return;
            if (currentIndexTime >= timeText.length || !String.valueOf(e.getKeyChar()).matches("[0-9]+")) return;

            timeText[currentIndexTime] = Character.toUpperCase(e.getKeyChar());
            currentIndexPerc++;
            currentIndexTime++;
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            if (row >= difficulties.size() - 1) {
                try {
                    difficulties.get(difficulties.size() - 1).correctPercentage = Integer.parseInt(EndPanel.charsToString(percText));
                    difficulties.get(difficulties.size() - 1).flightDuration = Integer.parseInt(EndPanel.charsToString(timeText));
                }
                catch (Exception ignored){
                    return;
                }
            }
            startGame();
            System.out.println("GAME STARTED");
        }
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
            System.exit(0);
        }
        else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
            currentIndexPerc = Math.max(0, currentIndexPerc - 1);
            currentIndexTime = Math.max(-percText.length, currentIndexTime - 1);
            if (currentIndexPerc < percText.length) percText[currentIndexPerc] = ' ';
            if (currentIndexTime >= 0) timeText[currentIndexTime] = ' ';
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
            row--;
            if (row > difficulties.size() - 1) row = 0;
            if (row < 0) row = difficulties.size() - 1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
            row++;
            if (row > difficulties.size() - 1) row = 0;
            if (row < 0) row = difficulties.size() - 1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_END){
            tempSpin = !tempSpin;
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
