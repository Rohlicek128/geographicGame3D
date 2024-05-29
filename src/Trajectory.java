import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Trajectory {

    Dot start;
    Dot end;
    int numOfSegments;
    ArrayList<Dot> dotSegments;

    double straightDistance;
    boolean spaceoutSegments;
    double dotFrequency;

    public Trajectory(Dot start, Dot end, int numOfSegments) {
        this.start = start;
        this.end = end;
        this.spaceoutSegments = false;
        this.numOfSegments = numOfSegments;
        setSegments();

        Collections.reverse(dotSegments);
    }
    public Trajectory(Dot start, Dot end, boolean spaceoutSegments, int dotFrequencyKm) {
        this.start = start;
        this.end = end;
        this.spaceoutSegments = spaceoutSegments;
        this.dotFrequency = dotFrequencyKm;
        setSegments();

        Collections.reverse(dotSegments);
    }

    //Segments from start dot to end dot
    public void setSegments(){
        dotSegments = new ArrayList<>();
        Vertex sv = GeoPoly.gpsToSphere(end.v);
        Vertex ev = GeoPoly.gpsToSphere(start.v);

        //distance from start to end
        double difX = ev.x - sv.x;
        double difY = ev.y - sv.y;
        double difZ = ev.z - sv.z;

        double pyth = Math.sqrt(difX*difX + difY*difY + difZ*difZ);
        straightDistance = Math.round(pyth * 6371) / 100.0;
        if (spaceoutSegments) numOfSegments = (int) Math.ceil(straightDistance / dotFrequency);

        for (int s = 0; s <= numOfSegments; s++) {
            double x = difX * ((double) s / numOfSegments);
            double y = difY * ((double) s / numOfSegments);
            double z = difZ * ((double) s / numOfSegments);
            Vertex v = new Vertex(sv.x + x, sv.y + y, sv.z + z);
            dotSegments.add(new Dot(offsetVertexToSphere(v, s, pyth), start.size, start.minSize, start.color));
        }
    }

    public Vertex offsetVertexToSphere(Vertex v, int s, double pyth){
        double heightDiv = Math.pow(200 / pyth, Math.min(1.5, 220 / pyth));
        //System.out.println(pyth + ", " + heightDiv);
        double offset = (Math.sin(Math.toRadians(s * (180.0 / numOfSegments))) / heightDiv + 1);
        double x = v.x * offset;
        double y = v.y * offset;
        double z = v.z * offset;

        return new Vertex(x, y, z);
    }

    public void recolorFromPercentage(Color color, double startPerc, double endPerc){
        int startIndex = (int) Math.floor(numOfSegments * startPerc);
        int endIndex = (int) Math.floor(numOfSegments * endPerc);
        for (int i = startIndex; i < endIndex; i++) {
            dotSegments.get(i).color = color;
        }
    }

}
