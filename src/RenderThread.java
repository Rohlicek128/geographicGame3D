import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

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
    boolean mouseInCountry;
    double[][] zBuffer;
    double[][] zBufferBackup;
    boolean changeCursor;
    String mouseOnCountry;
    int mouseOnCountryID;

    Graphics2D g2;
    ImageObserver observer;

    public RenderThread(int startIndex, int endIndex, Countries countries, Matrix3 transform, double zoomSize, int width, int height, int mouseX, int mouseY, double[][] zBuffer, Graphics2D g2, ImageObserver observer) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.countries = countries;
        this.transform = transform;
        this.zoomSize = zoomSize;
        this.width = width;
        this.height = height;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.zBuffer = zBuffer;

        this.g2 = g2;
        this.observer = observer;

        zBufferBackup = new double[width][height];
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                zBufferBackup[x][y] = Double.NEGATIVE_INFINITY;
            }
        }
    }

    @Override
    public void run(){
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        changeCursor = false;
        mouseInCountry = false;
        int lastIDi = 0;
        int count = 0;
        for (int i = startIndex; i < endIndex + (mouseInCountry ? 1 : 0); i++){
            //if (i >= countries.polygons.size()) break;

            if (mouseInCountry && count == i + 1){
                i = lastIDi;
                mouseOnCountry = countries.polygons.get(i).name;
                mouseOnCountryID = countries.polygons.get(i).id;
                countries.setPolygonsColor(countries.polygons.get(i).geoShapes.color, mouseOnCountryID);
                /*countries.setPolygonsColor(switch (colorState){
                    case 1 -> correctColor;
                    case -1 -> wrongColor;
                    case 0 -> countries.polygons.get(i).geoShapes.color;
                    default -> throw new IllegalStateException("Unexpected value: " + colorState);
                }, mouseOnCountryID);*/
                //colorState = 0;
                changeCursor = true;
                zBuffer = zBufferBackup;
            }
            else if (countries.polygons.get(i).id != countries.polygons.get(lastIDi).id) {
                mouseInCountry = false;
            }

            if (countries.polygons.get(i).id != countries.polygons.get(lastIDi).id) lastIDi = i;

            for (Triangle t : countries.polygons.get(i).geoShapes.triangles){
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
                                        img.setRGB(x, y, Triangle.getShadow(t.color, Math.abs(normal.z)).brighter().brighter().getRGB());
                                    }
                                    else img.setRGB(x, y,  Triangle.getShadow(t.color, Math.abs(normal.z)).getRGB());
                                }
                                zBuffer[x][y] = depth;
                            }
                        }
                    }
                }
            }

            count++;
            if (mouseInCountry) count++;
        }

        /*for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, new Color(0, 255, 0).getRGB());
            }
        }*/
        g2.drawImage(img, -width / 2,-height / 2, width, height, observer);

    }

}
