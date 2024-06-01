import java.io.Serializable;

public class Score implements Comparable, Serializable {

    String name;
    int score;
    Difficulty difficulty;

    public Score(String name, int score, Difficulty difficulty) {
        this.name = name;
        this.score = score;
        this.difficulty = difficulty;
    }

    @Override
    public int compareTo(Object o) {
        Score s = (Score) o;
        return Integer.compare(s.score, this.score);
    }
}
