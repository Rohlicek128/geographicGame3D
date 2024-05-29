import java.io.Serializable;

public class Score implements Comparable, Serializable {

    String name;
    int score;

    public Score(String name, int score) {
        this.name = name;
        this.score = score;
    }

    @Override
    public int compareTo(Object o) {
        Score s = (Score) o;
        return Integer.compare(s.score, this.score);
    }
}
