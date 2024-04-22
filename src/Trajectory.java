import java.awt.*;
import java.util.ArrayList;

public class Trajectory {

    Dot start;
    Dot end;
    int numOfSegments;
    ArrayList<Vertex> segments;

    public Trajectory(Dot start, Dot end, int numOfSegments) {
        this.start = start;
        this.end = end;
        this.numOfSegments = numOfSegments;
        setSegments();
    }

    //Segments from start dot to end dot
    public void setSegments(){
        segments = new ArrayList<>();

        //distance from start to end
        double difX = end.v.x - start.v.x;
        double difY = end.v.y - start.v.y;
        double difZ = end.v.z - start.v.z;
        for (int s = 0; s <= numOfSegments; s++) {
            double x = difX * ((double) s / numOfSegments);
            double y = difY * ((double) s / numOfSegments);
            double z = difZ * ((double) s / numOfSegments);
            Vertex v = new Vertex(start.v.x + x, (start.v.y + y) * (Math.sin(Math.toRadians(s * ((double) 180 /numOfSegments))) * 0.5 + 1), start.v.z + z);
            segments.add(v);
        }
    }

    public Vertex offsetVertexToSphere(Vertex v){
        double c = Math.sqrt(v.x*v.x + v.y*v.y);
        double difC = RenderPanel.EARTH_RADIUS / 2.0 - c;

        return new Vertex(v.x, v.y - difC, v.z);

        /*double angXY = Math.atan2(v.y, v.x);
        double angXZ = Math.atan2(v.x, v.y);

        double x = RenderPanel.EARTH_RADIUS / 2.0 * Math.cos(angXY) * Math.sin(angXZ);
        double y = RenderPanel.EARTH_RADIUS / 2.0 * Math.sin(angXY);
        double z = RenderPanel.EARTH_RADIUS / 2.0 * Math.cos(angXY) * Math.cos(angXZ);
        return new Vertex(x, y, z);*/
    }

}
