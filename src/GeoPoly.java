import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GeoPoly {

    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Triangle> triangles = new ArrayList<>();
    Color color;

    public GeoPoly(ArrayList<Vertex> vertices) {
        this.vertices = vertices;
    }

    public GeoPoly(double[][] gps, Color color) {
        this.color = color;
        loadGPStoCoordinates(gps);
        this.triangles = triangulation(verticesToGeometry(vertices), 0);
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

    public int vertexAboveLine(Vertex v, Vertex v1, Vertex v2){
        if (v1.x > v2.x){
            Vertex temp = v1;
            v1 = v2;
            v2 = temp;
        }

        Vertex V1 = new Vertex(v2.x - v1.x, v2.y - v1.y, 0);
        Vertex V2 = new Vertex(v2.x - v.x, v2.y - v.y, 0);
        double V1V2 = V1.x * V2.y - V1.y * V2.x;

        if (V1V2 > 0) return 1;
        else if (V1V2 < 0) return -1;
        else return 0;
    }

    public Geometry verticesToGeometry(ArrayList<Vertex> v){
        Coordinate[] coordinates = new Coordinate[v.size() + 1];
        for (int i = 0; i < v.size(); i++){
            coordinates[i] = new Coordinate();
            coordinates[i].setX(v.get(i).x);
            coordinates[i].setY(v.get(i).y);
            coordinates[i].setZ(v.get(i).z);
        }
        coordinates[v.size()] = coordinates[0];

        return new GeometryFactory().createPolygon(coordinates);
    }

    public ArrayList<Triangle> geometryToTriangles(Geometry g){
        ArrayList<Triangle> temp = new ArrayList<>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Coordinate[] coordinates = g.getGeometryN(i).getCoordinates();
            Vertex v1 = gpsToSphere(new Vertex(coordinates[0].x, coordinates[0].y, coordinates[0].z));
            Vertex v2 = gpsToSphere(new Vertex(coordinates[1].x, coordinates[1].y, coordinates[1].z));
            Vertex v3 = gpsToSphere(new Vertex(coordinates[2].x, coordinates[2].y, coordinates[2].z));

            //int randomNum = new Random().nextInt(200) + 256 - 200;
            temp.add(new Triangle(v1, v2, v3, color));
        }
        return temp;
    }

    public ArrayList<Triangle> triangulation(Geometry g, double tolerance){
        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(g);
        builder.setTolerance(tolerance);

        Geometry triangulation = builder.getTriangles(new GeometryFactory());
        //triangulation.buffer(0.0001 * 0.0001 * 0.0001);

        try {
            triangulation = triangulation.intersection(g.buffer(0.0001));
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

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
        int resize = 1;
        for (double[] g : gps) {
            double x = g[0] * resize;
            double y = g[1] * resize;
            double z = g[1] * resize;
            vertices.add(new Vertex(x, y, 0));
        }
    }

    public Vertex gpsToSphere(Vertex gps){
        double resize = 100.0;
        double x = resize * Math.cos(Math.toRadians(gps.y)) * Math.cos(Math.toRadians(gps.x));
        double y = resize * Math.cos(Math.toRadians(gps.y)) * Math.sin(Math.toRadians(gps.x));
        double z = resize * Math.sin(Math.toRadians(gps.y));
        return new Vertex(y, -z, x);
    }

}
