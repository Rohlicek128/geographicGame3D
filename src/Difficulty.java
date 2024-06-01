import java.awt.*;
import java.io.Serializable;

public class Difficulty implements Serializable {

    String name;
    int correctPercentage;
    int flightDuration;
    Dot startDot;
    Dot endDot;

    public Difficulty(String name, int correctPercentage, int flightDuration, Dot startDot, Dot endDot) {
        this.name = name;
        this.correctPercentage = correctPercentage;
        this.flightDuration = flightDuration;
        this.startDot = startDot;
        this.endDot = endDot;
    }

    public Difficulty(String name, int correctPercentage) {
        this.name = name;
        this.correctPercentage = correctPercentage;
    }

    public void drawDifficultyBox(Graphics2D g, Font font, int x, int y, double size, boolean isSelected){
        int h = (int) size;
        int w = h * 6;
        //Box
        if (isSelected) {
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(x - w/2, y - h/2, w, h, h, h);

            g.setColor(new Color(0, 0, 0));
        }
        else {
            g.setColor(new Color(255, 255, 255));
            g.setStroke(new BasicStroke((float) (size / 30.0), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawRoundRect(x - w/2, y - h/2, w, h, h, h);
        }

        //Text
        g.setFont(font.deriveFont(Font.BOLD, (float) h));
        g.drawString(name.toUpperCase(), (int) (-(name.length() / 2.0) * h /1.66) + x, y + h /3);
    }

    public void drawDifficultyInfo(Graphics2D g, Font font, int x, int y, double size){
        //From To
        g.setColor(new Color(255,255,255));
        if (startDot != null) {
            g.setFont(font.deriveFont(Font.BOLD, (float) size));
            String text = startDot.name + " -> " + endDot.name;
            g.drawString(text.toUpperCase(), (int) (-(text.length() / 2.0) * size/1.66) + x, y);
        }

        //Percentage & Speed
        size *= 1.75;
        g.setFont(font.deriveFont(Font.PLAIN, (float) size));
        String text = correctPercentage + "%";
        //String text = (int) Math.ceil(211.0 * (correctPercentage / 100.0)) + " c.";
        g.drawString(text, (int) (-(text.length() / 2.0) * size/1.66 + x - size * 2), (int) (y + size/1.25));

        if (startDot != null) text = flightDuration + "s";
        else text = "∞s";
        g.drawString(text, (int) (-(text.length() / 2.0) * size/1.66 + x + size * 2), (int) (y + size/1.25));
    }

}
