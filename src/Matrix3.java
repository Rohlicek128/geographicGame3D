public class Matrix3 {

    double[][] values;

    public Matrix3(double[][] values) {
        this.values = values;
    }

    /**
     * Multiplies 3x3 matrix with this 3x3 matrix.
     * @param other - the other matrix
     * @return multiplied matrix.
     */
    public Matrix3 multiply(Matrix3 other){
        double[][] result = new double[3][3];
        for (int row = 0; row < 3; row++){
            for (int col = 0; col < 3 ; col++){
                for (int i = 0; i < 3; i++){
                    result[row][col] += this.values[row][i] * other.values[i][col];
                }
            }
        }
        return new Matrix3(result);
    }

    /**
     * Transforms vertex.
     * @param in - input vertex.
     * @return transformed vertex.
     */
    public Vertex transform(Vertex in){
        return new Vertex(
                in.x * values[0][0] + in.y * values[1][0] + in.z * values[2][0],
                in.x * values[0][1] + in.y * values[1][1] + in.z * values[2][1],
                in.x * values[0][2] + in.y * values[1][2] + in.z * values[2][2]
        );
    }
}
