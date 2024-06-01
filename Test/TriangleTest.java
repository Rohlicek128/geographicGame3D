import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class TriangleTest {

    @Test
    void getShadow() {
        assertEquals(Triangle.getShadow(new Color(255,255,255), 0.5), new Color(160,160,160));
    }
}