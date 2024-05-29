import java.io.*;
import java.util.TreeSet;

public class ScoreboardManager {

    TreeSet<Score> scoreboard = new TreeSet<>();

    public ScoreboardManager() {
    }

    public void removeScore(String name){
        scoreboard.removeIf(s -> s.name.equalsIgnoreCase(name));
    }

    public void loadScoreboard(){
        try {
            FileInputStream file = new FileInputStream("scores.txt");
            ObjectInputStream obj = new ObjectInputStream(file);

            this.scoreboard = (TreeSet<Score>) obj.readObject();
            obj.close();
            System.out.println("READ FROM SCOREBOARD.");
        }
        catch (IOException e){
            System.out.println("NO SCOREBOARD FOUND");
        }
        catch (Exception ignored){
        }
    }

    public void addToScoreboard(Score score){
        try {
            scoreboard.add(score);

            FileOutputStream file = new FileOutputStream("scores.txt");
            ObjectOutputStream obj = new ObjectOutputStream(file);

            obj.writeObject(this.scoreboard);
            obj.flush();
            obj.close();
            System.out.println("SAVED TO SCOREBOARD.");
        }
        catch (Exception ignored){
        }
    }

}
