public class CountryPolygon {

    String name;
    Polygon geoShape;
    double[] geoPoint;

    public CountryPolygon(String name, Polygon geoShape, double[] geoPoint) {
        this.name = name;
        this.geoShape = geoShape;
        this.geoPoint = geoPoint;
    }

}
