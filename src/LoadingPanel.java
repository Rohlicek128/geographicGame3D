import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadingPanel extends JPanel implements ActionListener {

    StringBuilder loadingText = new StringBuilder("LOADING");
    int dots = 0;
    String loadingBar = "[          ]";
    int timeExpected = 16;
    int seconds = 0;

    Color primary;
    Color secondary;

    public LoadingPanel(Color p, Color s) {
        this.primary = p;
        this.secondary = s;

        Timer timer = new Timer(1000, this);
        timer.setRepeats(true);
        timer.start();
    }

    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;

        //BG
        g2.setColor(primary.darker());
        g2.fillRect(0, 0, getWidth(), getHeight());

        int borderWidth = 15;
        g2.setColor(primary);
        g2.fillRect(borderWidth, borderWidth, getWidth() - borderWidth*2, getHeight() - borderWidth*2);

        //Text
        int size = 25;
        g2.setColor(secondary);
        g2.setFont(new Font("Courier Prime", Font.BOLD, size));
        g2.drawString(loadingText.toString(), getWidth() / 2 - size*6, getHeight()/3 + size/2 + 7);

        //Bar
        size = 50;
        g2.setColor(secondary);
        g2.setFont(new Font("Courier Prime", Font.BOLD, size));
        g2.drawString(loadingBar, getWidth() / 2 - 7 * size/2, getHeight()/2 + size/2 - 7);

        //Seconds
        size = 30;
        g2.setColor(secondary);
        g2.setFont(new Font("Courier Prime", Font.BOLD, size));
        g2.drawString(seconds + " s", getWidth() - 100, (int) (getHeight() - size));
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
