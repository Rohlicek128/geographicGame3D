import java.awt.*;

public class CountryPolygon {

    String name;
    GeoPoly geoShape;
    double[] geoPoint;
    String type;

    public CountryPolygon(String name, GeoPoly geoShape, double[] geoPoint, String type) {
        this.name = name;
        this.geoShape = geoShape;
        this.geoPoint = geoPoint;
        this.type = type;
    }

}
