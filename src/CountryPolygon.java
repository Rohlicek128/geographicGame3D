import java.awt.*;
import java.util.ArrayList;

public class CountryPolygon {

    String name;
    ArrayList<GeoPoly> geoShapes = new ArrayList<>();
    double[] geoPoint;
    String type;

    public CountryPolygon(String name, GeoPoly geoShape, double[] geoPoint, String type) {
        this.name = name;
        this.geoShapes.add(geoShape);
        this.geoPoint = geoPoint;
        this.type = type;
    }

}
