import java.awt.*;
import java.util.ArrayList;

public class RenderThread extends Thread{

    ArrayList<Triangle> triangles;
    Matrix3 transform;
    double zoomSize;
    int width;
    int height;
    int mouseX;
    int mouseY;
    boolean mouseInCountry;
    double[][] zBuffer;
    Color[][] pixels;

    public RenderThread(ArrayList<Triangle> triangles, Matrix3 transform, double zoomSize, int width, int height, int mouseX, int mouseY, boolean mouseInCountry, double[][] zBuffer) {
        this.triangles = triangles;
        this.transform = transform;
        this.zoomSize = zoomSize;
        this.width = width;
        this.height = height;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mouseInCountry = mouseInCountry;
        this.zBuffer = zBuffer;

        this.pixels = new Color[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.pixels[i][j] = new Color(5,200,120);
            }
        }
    }

    @Override
    public void run(){
        for (Triangle t : triangles){
            Vertex v1 = transform.transform(t.v1);
            Vertex v2 = transform.transform(t.v2);
            Vertex v3 = transform.transform(t.v3);

            v1 = new Vertex(v1.x * zoomSize, v1.y * zoomSize, v1.z * zoomSize);
            v2 = new Vertex(v2.x * zoomSize, v2.y * zoomSize, v2.z * zoomSize);
            v3 = new Vertex(v3.x * zoomSize, v3.y * zoomSize, v3.z * zoomSize);

            Vertex normal = Vertex.normalVector(v1, v2, v3);

            v1.x += width / 2.0;
            v1.y += height / 2.0;
            v2.x += width / 2.0;
            v2.y += height / 2.0;
            v3.x += width / 2.0;
            v3.y += height / 2.0;

            int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
            int maxX = (int) Math.min(width - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
            int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
            int maxY = (int) Math.min(height - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    Vertex p = new Vertex(x, y, 0);

                    boolean V1 = Vertex.crossProduct(v1, v2, v3, p);
                    boolean V2 = Vertex.crossProduct(v2, v3, v1, p);
                    boolean V3 = Vertex.crossProduct(v3, v1, v2, p);
                    if (V1 && V2 && V3){
                        double depth = v1.z + v2.z + v3.z;
                        if (zBuffer[x][y] < depth){
                            if (depth > 0){
                                if ((mouseX == x && mouseY == y) || mouseInCountry) {
                                    mouseInCountry = true;
                                    pixels[x][y] = Triangle.getShadow(t.color, Math.abs(normal.z)).brighter().brighter();
                                }
                                else pixels[x][y] = Triangle.getShadow(t.color, Math.abs(normal.z));
                            }
                            zBuffer[x][y] = depth;
                        }
                    }
                }
            }
        }
    }

}
