import java.awt.*;
import java.util.Collections;

public class RocketTrajectory extends Trajectory{

    int currentSegment;
    int flightDuration;

    boolean explode = false;
    int explosionFrame = 0;
    int explosionDurationMillis;
    int explosionMaxSize;

    public RocketTrajectory(Dot start, Dot end, int flightDuration) {
        super(start, end, true, 10);
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
            if (show && !explode){
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
        if (explode) return;
        currentSegment += numOfSegments / flightDuration;
        currentSegment = Math.min(numOfSegments, currentSegment);
    }

    public void explodeRocket(int durationMillis, int maxSize){
        if (explode) return;

        explode = true;
        explosionDurationMillis = durationMillis;
        explosionMaxSize = maxSize;

        currentSegment++;
        updateRocket(false);
    }

    public void animateExplosion(int curFps){
        explosionFrame += (int) ((1.0 / curFps) * 1000.0);

        int alpha = (int) (Math.max(0, Math.min(255, 255.0 - (((double) explosionFrame / (double) explosionDurationMillis) * 255.0))));
        int other = (int) (Math.max(0, Math.min(255, ((double) explosionFrame / (double) explosionDurationMillis) * 255.0)));

        dotSegments.get(currentSegment).color = new Color(255, other, other, alpha);
        dotSegments.get(currentSegment).size = (explosionFrame * explosionMaxSize) / explosionDurationMillis;
        dotSegments.get(currentSegment).minSize = end.minSize;

        explosionFrame++;
    }

}
