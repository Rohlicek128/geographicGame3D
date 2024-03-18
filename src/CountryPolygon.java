public class CountryPolygon {

    String name;
    geoPoly geoShape;
    double[] geoPoint;
    String type;

    public CountryPolygon(String name, geoPoly geoShape, double[] geoPoint, String type) {
        this.name = name;
        this.geoShape = geoShape;
        this.geoPoint = geoPoint;
        this.type = type;
    }

}
