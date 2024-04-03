import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class CountryPolygon implements Serializable {

    String name;
    int id;
    GeoPoly geoShapes;
    double[] geoPoint;
    String type;

    public CountryPolygon(String name, int id, GeoPoly geoShape, double[] geoPoint, String type) {
        this.name = name;
        this.id = id;
        this.geoShapes = geoShape;
        this.geoPoint = geoPoint;
        this.type = type;
    }

}
