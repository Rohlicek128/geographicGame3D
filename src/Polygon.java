import java.awt.*;
import java.util.ArrayList;

public class Polygon {

    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Triangle> triangles = new ArrayList<>();

    public Polygon(ArrayList<Vertex> vertices) {
        this.vertices = vertices;
    }

    public Polygon(double[][] gps) {
        loadGPStoCoordinates(gps);
        verteciesToTriangles();
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
