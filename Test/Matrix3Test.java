import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Matrix3Test {

    @Test
    void multiply() {
        Matrix3 matrix1 = getTransformation(-14.5, -50);
        Matrix3 matrix2 = new Matrix3(new double[][]{
                {0.9681476403781077, 0.19180221077401205, 0.16094116431946046},
                {0.0, 0.6427876096865394, -0.766044443118978},
                {-0.25038000405444144, 0.7416441200304001, 0.6223133075823072}});

        assertArrayEquals(matrix1.values, matrix2.values);
    }

    @Test
    void transform() {
        Matrix3 transform = getTransformation(-14.5, -50);
        Vertex test = transform.transform(new Vertex(50, 50, 50));

        assertEquals(test.x, 35.88838181618331);
        assertEquals(test.y, 78.81169702454758);
        assertEquals(test.z, 0.8605014391394796);
    }

    private Matrix3 getTransformation(double xAng, double yAng) {
        double heading = Math.toRadians(xAng);
        Matrix3 headingTransform = new Matrix3(new double[][]{
                {Math.cos(heading), 0, -Math.sin(heading)},
                {0, 1, 0},
                {Math.sin(heading), 0, Math.cos(heading)}
        });
        double pitch = Math.toRadians(yAng);
        Matrix3 pitchTransform = new Matrix3(new double[][]{
                {1, 0, 0},
                {0, Math.cos(pitch), Math.sin(pitch)},
                {0, -Math.sin(pitch), Math.cos(pitch)}
        });
        return headingTransform.multiply(pitchTransform);
    }
}