import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Coordinates;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class Polygon {

    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Triangle> triangles = new ArrayList<>();

    public Polygon(ArrayList<Vertex> vertices) {
        this.vertices = vertices;
    }

    public Polygon(double[][] gps) {
        loadGPStoCoordinates(gps);
        this.triangles = triangulation(verticesToGeometry(vertices), 1.0);
    }

    public ArrayList<Triangle> geometryToVertices(Geometry g){
        ArrayList<Triangle> temp = new ArrayList<>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Coordinate[] c = g.getGeometryN(i).getCoordinates();
            Vertex[] triangle = new Vertex[3];
            for (int j = 0; j < 3; j++) {
                triangle[j] = new Vertex(c[j].getX(), c[j].getY(), c[j].getZ());
            }
            triangles.add(new Triangle(triangle[0], triangle[1], triangle[2], new Color(255,255,255)));
        }
        return temp;
    }

    public Geometry verticesToGeometry(ArrayList<Vertex> vertices){
        ArrayList<Vertex> temp = vertices;
        Coordinate[] coordinates = new Coordinate[vertices.size()];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = new Coordinate();
            double vertex = temp.get(i).x + temp.get(i).y + temp.get(i).z;
            double closest = Double.POSITIVE_INFINITY;
            for (int j = i; j < coordinates.length; j++) {
                double current = temp.get(j).x + temp.get(j).y + temp.get(j).z;

                //if (temp.get(j))
            }
        }
        for (int i = 0; i < vertices.size(); i++){
            coordinates[i].setX(vertices.get(i).x);
            coordinates[i].setY(vertices.get(i).y);
            coordinates[i].setZ(vertices.get(i).z);
        }
        return new GeometryFactory().createPolygon(coordinates);
    }

    public ArrayList<Triangle> geometryToTriangles(Geometry g){
        ArrayList<Triangle> temp = new ArrayList<>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Coordinate[] coordinates = g.getGeometryN(i).getCoordinates();
            Vertex v1 = new Vertex(coordinates[0].x, coordinates[0].y, coordinates[0].z);
            Vertex v2 = new Vertex(coordinates[1].x, coordinates[1].y, coordinates[1].z);
            Vertex v3 = new Vertex(coordinates[2].x, coordinates[2].y, coordinates[2].z);
            temp.add(new Triangle(v1, v2, v3, new Color(255,255,255)));
        }
        return temp;
    }

    public ArrayList<Triangle> triangulation(Geometry g, double tolerance){
        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(vertices);
        builder.setTolerance(tolerance);

        Geometry triangulation = builder.getTriangles(new GeometryFactory());
        triangulation = triangulation.intersection(g);
        return geometryToTriangles(triangulation);
    }

    public void verteciesToTriangles(){
        for (int i = 0; vertices.size() - i >= 3; i += 3) {
            Vertex[] triangle = new Vertex[3];
            for (int j = 0; j < 3; j++) {
                triangle[j] = new Vertex(vertices.get(i + j).x, vertices.get(i + j).y, vertices.get(i + j).z);
            }
            triangles.add(new Triangle(triangle[0], triangle[1], triangle[2], new Color(255,255,255)));
        }
    }

    public void loadGPStoCoordinates(double[][] gps){
        int resize = 100;
        for (double[] g : gps) {
            double x = Math.sin(g[1]);
            double y = Math.sin(g[0]);
            double z = Math.cos(g[0]);
            vertices.add(new Vertex(x * resize, y * resize, z * resize));
        }
    }

}
