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

    ArrayList<Difficulty> difficultiesSpeed = new ArrayList<>();
    ArrayList<Difficulty> difficultiesKnowledge = new ArrayList<>();
    int row = 0;
    int col = 0;

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

        difficultiesSpeed.add(new Difficulty("Easy", 1, 17, rusStartDot, prgEndDot));
        difficultiesSpeed.add(new Difficulty("Medium", 5, 25, rusStartDot, lndEndDot));
        difficultiesSpeed.add(new Difficulty("Hard", 10, 32, rusStartDot, nycEndDot));
        difficultiesSpeed.add(new Difficulty("Undoable", 5, 12, rusStartDot, helEndDot));

        difficultiesKnowledge.add(new Difficulty("Easy", 25, 180, norStartDot, aucEndDot));
        difficultiesKnowledge.add(new Difficulty("Medium", 50, 320, norStartDot, setEndDot));
        difficultiesKnowledge.add(new Difficulty("Hard", 75, 400, norStartDot, dubEndDot));
        difficultiesKnowledge.add(new Difficulty("Hopeless", 100, 390, norStartDot, delEndDot));

        Timer update = new Timer(25, e -> repaint());
        update.setRepeats(true);
        update.start();
    }

    public void paintComponent(Graphics g1) {
        this.requestFocus();
        Graphics2D g = (Graphics2D) g1;

        //BG
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());

        //Diffs
        //Speed
        int x = getWidth() / 4;
        int y = 250;
        int fontSize = 30;
        g.setColor(new Color(255,255,255));
        g.setFont(courierFontRegular.deriveFont(Font.PLAIN, (float) fontSize));
        String diffText = "SPEED";
        g.drawString(diffText, (int) (-(diffText.length() / 2.0) * fontSize/1.66) + x, y - fontSize);

        for (int i = 0; i < difficultiesSpeed.size(); i++) {
            boolean isSelected = i == row && col == 0;
            difficultiesSpeed.get(i).drawDifficultyBox(g, courierFontBold, x, y, 50, isSelected);
            y += 60;

            if (isSelected) difficultiesSpeed.get(i).drawDifficultyInfo(g, courierFontBold, getWidth() / 2, 510, 40);
        }
        //Knowledge
        x = getWidth() * 3/4;
        y = 250;
        g.setColor(new Color(255,255,255));
        g.setFont(courierFontRegular.deriveFont(Font.PLAIN, (float) fontSize));
        diffText = "KNOWLEDGE";
        g.drawString(diffText, (int) (-(diffText.length() / 2.0) * fontSize/1.66) + x, y - fontSize);

        for (int i = 0; i < difficultiesKnowledge.size(); i++) {
            boolean isSelected = i == row && col == 1;
            difficultiesKnowledge.get(i).drawDifficultyBox(g, courierFontBold, x, y, 50, isSelected);
            y += 60;

            if (isSelected) difficultiesKnowledge.get(i).drawDifficultyInfo(g, courierFontBold, getWidth() / 2, 510, 40);
        }

        //Translate
        g.translate(getWidth() / 2, getHeight() / 2);

        //Difficulty temp
        /*int x = -getWidth() / 25;
        UIText.drawTextBox(g, percText, currentIndexPerc, courierFontBold, x, -100, 200);
        int fontSize = (int) ((200 / percText.length) * 1.28);
        g.setColor(new Color(255,255,255));
        g.setFont(courierFontBold.deriveFont(Font.BOLD, (float) fontSize));
        g.drawString("%", x + fontSize * percText.length / 2, -35);*/

        //Text
        fontSize = 100;
        g.setColor(new Color(255,255,255));
        g.setFont(courierFontBold.deriveFont(Font.BOLD, (float) fontSize));
        String nameText = "GAME NAME";
        g.drawString(nameText, (int) (-(nameText.length() / 2.0) * fontSize/1.66), -getHeight() / 2 + 100);

        fontSize = 35;
        g.setFont(courierFontRegular.deriveFont(Font.PLAIN, (float) fontSize));
        String signatureText = "by Adam Švec";
        g.drawString(signatureText, (int) (-(signatureText.length() / 2.0) * fontSize/1.66), (int) (-getHeight() / 2 + 100 + fontSize * 1.25));

        fontSize = 25;
        g.setFont(courierFontItalic.deriveFont(Font.ITALIC, (float) fontSize));
        String nextText = "Press ENTER to start...";
        g.drawString(nextText, (int) (-(nextText.length() / 2.0) * fontSize/1.66), fontSize * 12);
    }

    public void startGame(){
        this.removeKeyListener(this);
        this.setVisible(false);
        this.removeAll();


        RenderWindow rw = (RenderWindow) SwingUtilities.getWindowAncestor(this);
        rw.setNewRenderPanel(switch (col){
            case 0 -> difficultiesSpeed.get(row);
            case 1 -> difficultiesKnowledge.get(row);
            default -> throw new IllegalStateException("Unexpected value: " + col);
        }, tempSpin);
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
        if (percText[0] == '0' && percText[1] == '0' && e.getKeyChar() == '0') return;

        if (currentIndexPerc == 0 && !String.valueOf(e.getKeyChar()).matches("[0-1]+")) return;
        if (percText[0] == '1' && !String.valueOf(e.getKeyChar()).matches("0")) return;
        if (currentIndexPerc >= percText.length || !String.valueOf(e.getKeyChar()).matches("[0-9]+")) return;
        percText[currentIndexPerc] = Character.toUpperCase(e.getKeyChar());
        currentIndexPerc++;

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            startGame();
            System.out.println("GAME STARTED");
        }
        else if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
            System.exit(0);
        }
        else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
            currentIndexPerc = Math.max(0, currentIndexPerc - 1);
            percText[currentIndexPerc] = ' ';
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
            if (col == 0) row = Math.max(0, Math.min(difficultiesSpeed.size() - 1, row - 1));
            else row = Math.max(0, Math.min(difficultiesKnowledge.size() - 1, row - 1));
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
            if (col == 0) row = Math.max(0, Math.min(difficultiesSpeed.size() - 1, row + 1));
            else row = Math.max(0, Math.min(difficultiesKnowledge.size() - 1, row + 1));
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A){
            col = Math.max(0, Math.min(1, col - 1));

            if (col == 0) row = Math.max(0, Math.min(difficultiesSpeed.size() - 1, row));
            else row = Math.max(0, Math.min(difficultiesKnowledge.size() - 1, row));
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
            col = Math.max(0, Math.min(1, col + 1));

            if (col == 0) row = Math.max(0, Math.min(difficultiesSpeed.size() - 1, row));
            else row = Math.max(0, Math.min(difficultiesKnowledge.size() - 1, row));
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
