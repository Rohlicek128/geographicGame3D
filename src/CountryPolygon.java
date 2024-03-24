import java.awt.*;
import java.util.ArrayList;

public class CountryPolygon {

    String name;
    int id;
    ArrayList<GeoPoly> geoShapes = new ArrayList<>();
    double[] geoPoint;
    String type;

    public CountryPolygon(String name, int id, GeoPoly geoShape, double[] geoPoint, String type) {
        this.name = name;
        this.id = id;
        this.geoShapes.add(geoShape);
        this.geoPoint = geoPoint;
        this.type = type;
    }

}
