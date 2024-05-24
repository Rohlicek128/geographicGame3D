import java.awt.*;
import java.awt.image.BufferedImage;

public class RenderThread extends Thread{

    int startIndex;
    int endIndex;

    BufferedImage img;

    Countries countries;
    Matrix3 transform;
    double zoomSize;
    int width;
    int height;
    int mouseX;
    int mouseY;
    boolean changeCursor = false;
    String mouseOnCountry;
    int futureMouseOnCountryID = -1;
    int pastMouseOnCountryID;

    int trianglesRendered = 0;

    public RenderThread(int startIndex, int endIndex, Countries countries, Matrix3 transform, double zoomSize, int width, int height, int mouseX, int mouseY, int pastMouseOnCountryID) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.countries = countries;
        this.transform = transform;
        this.zoomSize = zoomSize;
        this.width = width;
        this.height = height;
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.pastMouseOnCountryID = pastMouseOnCountryID;
    }

    @Override
    public void run(){
        for (int i = startIndex; i < endIndex; i++){
            for (Triangle t : countries.polygons.get(i).geoShapes.triangles){
                Vertex v1 = transform.transform(t.v1);
                Vertex v2 = transform.transform(t.v2);
                Vertex v3 = transform.transform(t.v3);

                if (v1.z + v2.z + v3.z <= 0) continue;

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
                            if (t.color == null) t.color = new Color(255,0,255); //Fix later

                            if (mouseX == x && mouseY == y) {
                                changeCursor = true;
                                mouseOnCountry = countries.polygons.get(i).name;
                                futureMouseOnCountryID = countries.polygons.get(i).id;
                            }
                            if (pastMouseOnCountryID == countries.polygons.get(i).id) img.setRGB(x, y, Triangle.getShadow(t.color, Math.abs(normal.z)).brighter().brighter().getRGB());
                            else img.setRGB(x, y,  Triangle.getShadow(t.color, Math.abs(normal.z)).getRGB());
                        }
                    }
                }
                trianglesRendered++;
            }
        }
    }

}
