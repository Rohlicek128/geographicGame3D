import java.awt.*;
import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.ArrayList;

public class CountryPolygon implements Serializable {

    String name;
    int id;
    GeoPoly geoShapes;
    double[] geoPoint;
    String continent;
    String region;

    public CountryPolygon(String name, int id, GeoPoly geoShape, double[] geoPoint, String continent, String region) {
        this.name = name;
        this.id = id;
        this.geoShapes = geoShape;
        this.geoPoint = geoPoint;
        this.continent = continent;
        this.region = region;
    }

    public void drawCountryOutline(Graphics2D g, Matrix3 transform, double zoomSize, float thickness, Color color){
        Path2D path = new Path2D.Double();
        Vertex v = GeoPoly.gpsToSphere(new Vertex(geoShapes.vertices.get(0).x, geoShapes.vertices.get(0).y, geoShapes.vertices.get(0).z));
        v = transform.transform(v);
        path.moveTo(v.x * zoomSize, v.y * zoomSize);
        for (int i = 1; i < geoShapes.vertices.size(); i++){
            v = GeoPoly.gpsToSphere(new Vertex(geoShapes.vertices.get(i).x, geoShapes.vertices.get(i).y, geoShapes.vertices.get(i).z));
            v = transform.transform(v);
            path.lineTo(v.x * zoomSize, v.y * zoomSize);
        }
        path.closePath();
        g.setColor(color);
        g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(path);
    }

}
