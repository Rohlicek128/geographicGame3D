import javax.sound.sampled.*;
import java.io.File;
import java.util.Objects;

public class GameAudio extends Thread{

    int wholeDuration;
    boolean timeSet;
    double audioVolume = -30;

    public GameAudio(int wholeDuration) {
        this.wholeDuration = wholeDuration;
        this.timeSet = true;
    }

    public GameAudio() {
        this.timeSet = false;
    }

    public void run(){
        if (timeSet){
            for (int i = 0; i < wholeDuration; i++) {
                playBeep(1.0);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("AUDIO INTERRUPTED");
                    break;
                }
            }
        }
        else {
            while (true) {
                playBeep(1.0);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("AUDIO INTERRUPTED");
                    break;
                }
            }
        }
    }

    public void playBeep(double duration){
        int sampleRate = 44100;
        double freqOfTone = 375;

        byte[] buf = new byte[1];;
        AudioFormat af = new AudioFormat((float) sampleRate, 8, 1, true, false );
        SourceDataLine sdl;

        try {
            sdl = AudioSystem.getSourceDataLine(af);
            sdl.open();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        FloatControl volume = (FloatControl) sdl.getControl(FloatControl.Type.MASTER_GAIN);
        //System.out.println("MAX: " + volume.getMaximum() + ", MIN: " + volume.getMinimum());
        volume.setValue((float) Math.min(volume.getMaximum(), Math.max(volume.getMinimum(), audioVolume)));

        sdl.start();
        for (int i = 0; i < (int) (duration * sampleRate); i++) {
            double angle = i / ((float) sampleRate / freqOfTone) * 2.0 * Math.PI;
            buf[0] = (byte) (Math.sin(angle) * 100);
            sdl.write(buf, 0, 1);
        }
        sdl.drain();
        sdl.flush();
        sdl.stop();
    }

    public synchronized void playSound(String path, double volumeLevel) {
        new Thread(() -> {
            try {
                sleep(1200);
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(this.getClass().getResource("sounds/Explosion-Sound-Effect.wav"));
                clip.open(inputStream);

                FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                //System.out.println("MAX: " + volume.getMaximum() + ", MIN: " + volume.getMinimum());
                volume.setValue((float) Math.min(volume.getMaximum() - 15, Math.max(volume.getMinimum(), volumeLevel + 10)));

                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
