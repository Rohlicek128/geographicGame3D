import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class LoadingPanel extends JPanel implements ActionListener {

    StringBuilder loadingText = new StringBuilder("LOADING");
    int dots = 0;
    String loadingBar = "[          ]";
    int timeExpected = 16;
    int seconds = 0;

    Color primary;
    Color secondary;
    Font courierFontBold = loadFont("fonts/CourierPrime-Bold.ttf");

    public LoadingPanel(Color p, Color s) {
        this.primary = p;
        this.secondary = s;

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        this.setVisible(true);

        Timer timer = new Timer(1000, this);
        timer.setRepeats(this.isVisible());
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g1){
        Graphics2D g = (Graphics2D) g1;

        //BG
        g.setColor(primary.darker().darker());
        g.fillRect(0, 0, getWidth(), getHeight());

        int borderWidth = 15;
        g.setColor(primary.darker().darker().darker().darker());
        g.fillRect(borderWidth, borderWidth, getWidth() - borderWidth*2, getHeight() - borderWidth*2);

        //Atm
        /*int atmSize = getWidth() * 5/4;
        Point2D center = new Point2D.Float((float) (getWidth()/2.0), (float) (getHeight() /6 + getWidth()/2.0));
        float[]  dist = {0.0f, 0.5f, 0.75f, 1.0f};
        Color[] colors = {new Color(255, 255, 255, 255), new Color(255, 255, 255, 0), new Color(255, 255, 255, 0), new Color(255, 255, 255, 0)};

        RadialGradientPaint rgp = new RadialGradientPaint(center, atmSize, dist, colors);
        g.setPaint(rgp);
        g.fill(new Ellipse2D.Double(-atmSize / 2.0 + getWidth()/2.0,-atmSize / 2.0 + getWidth()/2.0, atmSize, atmSize));*/

        //Circle
        int ovalSize = getWidth();
        g.setColor(primary.brighter());
        g.fillOval(0, (int) Math.pow(seconds * 7, 0.7), ovalSize, ovalSize);

        //Text
        int size = 25;
        g.setFont(courierFontBold.deriveFont((float) size));
        g.setColor(secondary);
        g.drawString(loadingText.toString(), getWidth() / 2 - size*6, getHeight()/3 + size/2 + 7);

        //Bar
        size = 50;
        g.setFont(courierFontBold.deriveFont((float) size));
        g.setColor(secondary);
        g.drawString(loadingBar, getWidth() / 2 - 7 * size/2, getHeight()/2 + size/2 - 7);

        //Seconds
        size = 30;
        g.setFont(courierFontBold.deriveFont((float) size));
        g.setColor(secondary);
        g.drawString(seconds + " s", getWidth()/2 - size, getHeight() - size);
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
    public void actionPerformed(ActionEvent e) {
        dots++;
        if (dots >= 4){
            dots = 0;
            loadingText.replace(0, loadingText.length(), "LOADING");
        }
        else loadingText.append(".");

        seconds++;
        StringBuilder temp = new StringBuilder("[");
        int progress = (int) Math.floor(((double) (seconds) / (double) (timeExpected)) * 10.0);
        if (progress > 10) progress = 10;

        temp.append("#".repeat(progress));
        temp.append(" ".repeat(10 - progress));
        temp.append("]");

        loadingBar = temp.toString();

        repaint();
    }
}
