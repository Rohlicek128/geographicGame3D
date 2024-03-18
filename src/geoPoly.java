import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class geoPoly {

    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Triangle> triangles = new ArrayList<>();

    public geoPoly(ArrayList<Vertex> vertices) {
        this.vertices = vertices;
    }

    public geoPoly(double[][] gps) {
        loadGPStoCoordinates(gps);
        this.triangles = triangulation(verticesToGeometry(), 0);
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

    public Geometry verticesToGeometry(){
        /*Vertex leftmost = new Vertex(Double.POSITIVE_INFINITY, 0, 0);
        for (Vertex v : vertices) {
            if (v.x < leftmost.x) leftmost = v;
        }
        vertices.remove(leftmost);
        Vertex rightmost = new Vertex(Double.NEGATIVE_INFINITY, 0, 0);
        for (Vertex v : vertices) {
            if (v.x > rightmost.x) rightmost = v;
        }
        vertices.remove(rightmost);

        ArrayList<Vertex> A = new ArrayList<>();
        for (Vertex v : vertices){
            if (vertexAboveLine(v, leftmost, rightmost) == -1) A.add(v);
        }
        ArrayList<Vertex> B = new ArrayList<>();
        for (Vertex v : vertices){
            if (vertexAboveLine(v, leftmost, rightmost) == 1) B.add(v);
        }

        for (int i = 0; i < A.size(); i++) {
            for (int j = 0; j < A.size() - 1; j++) {
                if (A.get(j).x > A.get(j + 1).x){
                    Vertex temp = A.get(j);
                    A.set(j, A.get(j + 1));
                    A.set(j + 1, temp);
                }
            }
        }
        for (int i = 0; i < B.size(); i++) {
            for (int j = 0; j < B.size() - 1; j++) {
                if (B.get(j).x < B.get(j + 1).x){
                    Vertex temp = B.get(j);
                    B.set(j, B.get(j + 1));
                    B.set(j + 1, temp);
                }
            }
        }

        Coordinate[] coordinates = new Coordinate[A.size() + B.size() + 3];

        coordinates[0] = new Coordinate(leftmost.x, leftmost.y, leftmost.z);
        for (int i = 0; i < A.size(); i++){
            coordinates[i + 1] = new Coordinate(A.get(i).x, A.get(i).y, A.get(i).z);
        }
        coordinates[A.size() + 1] = new Coordinate(rightmost.x, rightmost.y, rightmost.z);
        for (int i = 0; i < B.size(); i++){
            coordinates[i + A.size() + 2] = new Coordinate(B.get(i).x, B.get(i).y, B.get(i).z);
        }
        coordinates[coordinates.length - 1] = coordinates[0];*/

        Coordinate[] coordinates = new Coordinate[vertices.size() + 1];
        for (int i = 0; i < vertices.size(); i++){
            coordinates[i] = new Coordinate();
            coordinates[i].setX(vertices.get(i).x);
            coordinates[i].setY(vertices.get(i).y);
            coordinates[i].setZ(vertices.get(i).z);
        }
        coordinates[vertices.size()] = coordinates[0];

        return new GeometryFactory().createPolygon(coordinates);
    }

    public ArrayList<Triangle> geometryToTriangles(Geometry g){
        ArrayList<Triangle> temp = new ArrayList<>();
        for (int i = 0; i < g.getNumGeometries(); i++) {
            Coordinate[] coordinates = g.getGeometryN(i).getCoordinates();
            Vertex v1 = gpsToSphere(new Vertex(coordinates[0].x, coordinates[0].y, coordinates[0].z));
            Vertex v2 = gpsToSphere(new Vertex(coordinates[1].x, coordinates[1].y, coordinates[1].z));
            Vertex v3 = gpsToSphere(new Vertex(coordinates[2].x, coordinates[2].y, coordinates[2].z));

            int randomNum = new Random().nextInt(200) + 256 - 200;
            temp.add(new Triangle(v1, v2, v3, new Color(randomNum,randomNum,randomNum)));
        }
        return temp;
    }

    public ArrayList<Triangle> triangulation(Geometry g, double tolerance){
        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(g);
        builder.setTolerance(tolerance);

        Geometry triangulation = builder.getTriangles(new GeometryFactory());
        triangulation.buffer(0.0001 * 0.0001 * 0.0001);

        System.out.println(triangulation.intersects(g));
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
        int resize = 1;
        for (double[] g : gps) {
            /*double x = Math.sin(g[0]);
            double y = Math.sin(g[1]);
            double z = Math.cos(g[1]);
            vertices.add(new Vertex(x * resize, y * resize, z * resize /10));*/

            double x = g[0] * resize;
            double y = g[1] * resize;
            double z = g[1] * resize;
            vertices.add(new Vertex(x, y, 0));
        }
    }

    public Vertex gpsToSphere(Vertex gps){
        double resize = 100.0;
        double x = Math.sin(gps.x);
        double y = Math.sin(gps.y);
        double z = Math.cos(gps.y);
        //return new Vertex(x * resize, y * resize, z * resize);
        return new Vertex(gps.x, -gps.y, gps.z);
    }

    /*ArrayList<Vertex> sorted = new ArrayList<>();

        int index = 0;
        while (vertices.size() != 0) {
            double vertex = vertices.get(index).x + vertices.get(index).y + vertices.get(index).z;
            sorted.add(vertices.get(index));
            vertices.remove(index);
            double closest = Double.POSITIVE_INFINITY;
            for (int i = 0; i < vertices.size(); i++) {
                double current = vertices.get(i).x + vertices.get(i).y + vertices.get(i).z;
                double result = Math.abs(current - vertex);
                if (index == i) continue;
                if (result < closest ) {
                    closest = result;
                    index = i;
                }
            }
        }
        Coordinate[] coordinates = new Coordinate[sorted.size() + 1];
        for (int i = 0; i < sorted.size(); i++){
            coordinates[i] = new Coordinate();
            coordinates[i].setX(sorted.get(i).x);
            coordinates[i].setY(sorted.get(i).y);
            coordinates[i].setZ(sorted.get(i).z);
        }
        coordinates[sorted.size()] = coordinates[0];
        vertices.addAll(sorted);

        Geometry g = new GeometryFactory().createPolygon(coordinates);

        return g;*/

}
