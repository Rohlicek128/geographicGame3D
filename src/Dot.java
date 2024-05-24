import java.awt.*;

public class Dot {

    Vertex v;
    int size;
    int minSize;
    Color color;

    public Dot(Vertex v, int size, int minSize, Color color) {
        this.v = v;
        this.size = size;
        this.minSize = minSize;
        this.color = color;
    }

    public void drawDot(Graphics2D g, Matrix3 transform, double zoomSize){
        Vertex tv = transform.transform(this.v);
        tv = new Vertex(tv.x * zoomSize, tv.y * zoomSize, tv.z * zoomSize);

        double difC = RenderPanel.EARTH_DIAMETER / 2.0 * zoomSize - Math.sqrt(tv.x*tv.x + tv.y*tv.y);
        if ((difC < 0 || tv.z > 0) && this.color.getRGB() != 0){
            int tsize = (int) Math.max(this.minSize, this.size * zoomSize);

            g.setColor(color);
            g.fillOval((int) (tv.x - tsize/2), (int) (tv.y - tsize/2), tsize, tsize);
        }
    }

}
