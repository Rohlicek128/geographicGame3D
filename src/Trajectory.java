import java.util.ArrayList;

public class Trajectory {

    Dot start;
    Dot end;
    int numOfSegments;
    ArrayList<Dot> dotSegments;

    public Trajectory(Dot start, Dot end, int numOfSegments) {
        this.start = start;
        this.end = end;
        this.numOfSegments = numOfSegments;
        setSegments();
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

    public Vertex generateDotOnGreatCircle(int s){
        double difX = end.v.x - start.v.x;
        double difY = end.v.y - start.v.y;
        double difZ = end.v.z - start.v.z;

        double radYX = Math.atan(difX / difY);
        return null;


        /*double difX = end.v.x - start.v.x;
        double difY = end.v.y - start.v.y;
        double difZ = end.v.z - start.v.z;

        double angXY = Math.atan(difY);
        double angXZ = Math.atan(difX);

        return  new Vertex(0,0,0);*/
    }

}
