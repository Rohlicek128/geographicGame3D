import java.io.Serializable;

public class Vertex implements Serializable {

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

        return V1V2CrossV1V3 * V1V2CrossV1P > 0;
    }

    public static Vertex normalVector(Vertex v1, Vertex v2, Vertex v3){
        Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
        Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
        Vertex normal = new Vertex(
                ab.y * ac.z - ab.z * ac.y,
                ab.z * ac.x - ab.x * ac.z,
                ab.x * ac.y - ab.y * ac.x
        );

        double normalLength = Math.sqrt(Math.pow(normal.x, 2) + Math.pow(normal.y, 2) + Math.pow(normal.z, 2));
        normal.x /= normalLength;
        normal.y /= normalLength;
        normal.z /= normalLength;
        return normal;
    }

}
