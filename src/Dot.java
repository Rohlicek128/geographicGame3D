import java.awt.*;

public class Dot {

    Vertex v;
    double size;
    Color color;

    public Dot(Vertex v, double size, Color color) {
        this.v = GeoPoly.gpsToSphere(v);
        this.size = size;
        this.color = color;
    }

}
