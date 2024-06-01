import java.awt.*;

public class MouseText {

    String text;
    int x;
    int y;

    int currentTime = 0;
    int maxTime;

    public MouseText(String text, int x, int y, int maxTime) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.maxTime = maxTime;
    }

    public boolean drawMouseText(Graphics2D g, Font font, int size, int frames){
        currentTime = Math.min(maxTime, currentTime + frames);
        //Box
        /*g.setColor(new Color(0,0,0,Math.max(0, alpha - 100)));
        g.fillRect(x - (size * 2/5), y - size, text.length() * size*3/5 + (size * 4/5), size*7/5);*/

        //Text
        int alpha = (int) (255.0 - ((double) currentTime / maxTime) * 255.0);
        g.setFont(font.deriveFont((float) Math.pow(size * (currentTime + 1), 0.425)));
        g.setColor(new Color(255,255,255, alpha));
        g.drawString(text, x, y);

        return currentTime >= maxTime;
    }

}
