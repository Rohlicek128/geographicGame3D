import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VertexTest {

    @Test
    void crossProduct() {
        Vertex A = new Vertex(0, 100, 0);
        Vertex B = new Vertex(100, 0, 0);
        Vertex C = new Vertex(-100, 0, 0);

        Vertex p = new Vertex(0, 50, 0);
        assertTrue(Vertex.crossProduct(A, B, C, p));

        p = new Vertex(0, 150, 0);
        assertFalse(Vertex.crossProduct(A, B, C, p));
    }

    @Test
    void normalVector() {
        Vertex A = new Vertex(0, 100, 0);
        Vertex B = new Vertex(100, 0, 0);
        Vertex C = new Vertex(-100, 0, 0);

        assertEquals(Vertex.normalVector(A, B, C).z, -1.0);
    }
}