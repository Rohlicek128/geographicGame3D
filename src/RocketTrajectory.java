import java.awt.*;
import java.util.Collections;

public class RocketTrajectory extends Trajectory{

    int currentSegment;
    int flightDuration;

    public RocketTrajectory(Dot start, Dot end, int flightDuration) {
        super(start, end, 1500);
        this.flightDuration = flightDuration;

        Collections.reverse(dotSegments);
        for (Dot dot : dotSegments){
            dot.color = new Color(0, true);
        }
    }

    public void updateRocket(boolean show){
        int rocketSize = 3;
        try {
            //color current
            if (show){
                dotSegments.get(currentSegment).color = new Color(255,0, 0);
                dotSegments.get(currentSegment).size = rocketSize;
                dotSegments.get(currentSegment).minSize = end.minSize;
            }

            //recolor back previous
            for (int i = 0; i < currentSegment - (show ? 0 : numOfSegments / flightDuration - 1); i++) {
                //dotSegments.get(i).color = new Color(0, true);
                dotSegments.get(i).color = new Color(255,0, 0);
                dotSegments.get(i).size = 1;
                dotSegments.get(i).minSize = start.minSize;
            }
        }
        catch (Exception ignored){
        }
        currentSegment += numOfSegments / flightDuration;
        currentSegment = Math.min(numOfSegments, currentSegment);
    }

}
