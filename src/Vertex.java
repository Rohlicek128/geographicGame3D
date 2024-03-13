public class Vertex {

    double x;
    double y;
    double z;

    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static boolean crossProduct(Vertex A, Vertex B, Vertex C, Vertex p){
        Vertex V1V2 = new Vertex(B.x - A.x,B.y - A.y,B.z - A.z);
        Vertex V1V3 = new Vertex(C.x - A.x,C.y - A.y,C.z - A.z);
        Vertex V1P = new Vertex(p.x - A.x,p.y - A.y,p.z - A.z);

        double V1V2CrossV1V3 = V1V2.x * V1V3.y - V1V3.x * V1V2.y;
        double V1V2CrossV1P = V1V2.x * V1P.y - V1P.x * V1V2.y;

        return V1V2CrossV1V3 * V1V2CrossV1P >= 0;
    }

}
