import java.awt.*;

public class UIText {

    String text;
    boolean isSpaceAbove;

    public UIText(String text, boolean isSpaceAbove) {
        this.text = text;
        this.isSpaceAbove = isSpaceAbove;
    }

    public static void drawTextBox(Graphics2D g, char[] spaces, int currentIndex, Font font, int cX, int cY, double size){
        int cells = spaces.length;
        int w = (int) (size / cells);
        int h = (int) (w * 1.1);
        int hOffset = 10;
        for (int i = 0; i < cells; i++) {
            int x = (w * (i - (cells-1) / 2)) - w/2 + (hOffset * (i - (cells-1) / 2)) + cX;
            g.setColor(new Color(255,255,255));
            g.drawRect(x, cY, w, h);

            //Name
            g.setFont(font.deriveFont(Font.BOLD, (float) (w * 1.28)));
            if (currentIndex > i) g.drawString(String.valueOf(spaces[i]), x + hOffset, cY + h - hOffset);

            //Underscore
            if (currentIndex == i) {
                int alpha = Math.sin(System.currentTimeMillis() / 300.0) < 0 ? 0 : 255;
                g.setColor(new Color(255,255,255, alpha));
                g.fillRect(x + hOffset, cY + h - hOffset, w - hOffset*2, -hOffset / 2);
            }
        }
    }

}
