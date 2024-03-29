import java.awt.*;

public class Triangle {

    Vertex v1;
    Vertex v2;
    Vertex v3;
    Color color;

    public Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
    }

    public static Color getShadow(Color color, double shadow){
        double gamma = 1.5;

        double redLinear = Math.pow(color.getRed(), gamma) * shadow;
        double greenLinear = Math.pow(color.getGreen(), gamma) * shadow;
        double blueLinear = Math.pow(color.getBlue(), gamma) * shadow;

        int red = (int) Math.pow(redLinear, 1 / gamma);
        int green = (int) Math.pow(greenLinear, 1 / gamma);
        int blue = (int) Math.pow(blueLinear, 1 / gamma);
        return new Color(red, green, blue);
    }
}
