import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Countries implements Serializable {

    ArrayList<CountryPolygon> polygons = new ArrayList<>();
    int count = 1;
    Color secondary;

    public Countries(String file, boolean build, Color s) {
        this.secondary = s;
        long startTimeLoading = System.currentTimeMillis();

        if (build) loadFromFile(file);
        else readFromCache("cache.txt");

        long currentTimeLoading = System.currentTimeMillis() - startTimeLoading;
        currentTimeLoading = Math.round(currentTimeLoading / 100.0) / 10;
        int minutes = (int) (currentTimeLoading / 60);
        double seconds = currentTimeLoading - (minutes * 60L);
        System.out.println("LOADING TIME: " + minutes + "m " + seconds + "s");
    }

    public void setPolygonsColor(Color color, int id){
        for (CountryPolygon p : polygons){
            if (p.id == id){
                p.geoShapes.setColor(color);
            }
        }
    }

    //"{""coordinates"": [[[77.88883000000004, 35.44156000000004], [77.91205000000008, 35.43726000000004], ...
    public ArrayList<double[][]> stringToPolygons(String data){
        data = data.replace("\"{\"\"coordinates\"\": ", "");
        data = data.replace(", \"\"type\"\": \"\"Polygon\"\"}\"", "");
        data = data.replace(", \"\"type\"\": \"\"MultiPolygon\"\"}\"", "");

        char[] chars = data.toCharArray();

        ArrayList<StringBuilder> polyStrings = new ArrayList<>();

        int polyCount = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '[' && chars[i + 1] == '[' && chars[i + 2] == '['){
                polyStrings.add(new StringBuilder());
                for (int j = i + 1; j < chars.length; j++) {
                    if (chars[j] == ']' && chars[j + 1] == ']' && chars[j + 2] == ']'){
                        i = j + 3;
                        break;
                    }
                    polyStrings.get(polyCount).append(chars[j]);
                }
                polyCount++;
            }
        }

        ArrayList<double[][]> result = new ArrayList<>();
        for (int i = 0; i < polyStrings.size(); i++) {
            String temp = polyStrings.get(i).toString();
            temp = temp.replace("[", "");
            temp = temp.replace("]", "");
            String[] tempSplit = temp.split(",");

            double[][] tempGPS;
            tempGPS = new double[tempSplit.length / 2][2];
            for (int x = 0; x < tempSplit.length / 2; x++) {
                for (int y = 0; y < 2; y++) {
                    double num = Double.parseDouble(tempSplit[x * 2 + y]);
                    if (num != 0.0){
                        tempGPS[x][y] = num;
                    }
                }
            }
            result.add(tempGPS);
        }
        return result;
    }

    public void loadFromFile(String file){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            br.readLine();
            String s = "";
            while ((s = br.readLine()) != null){
                String[] split = s.split(";");

                String[] geoPointSplit = split[0].split(",");
                double[] geoPoint = new double[]{
                        Double.parseDouble(geoPointSplit[0]),
                        Double.parseDouble(geoPointSplit[1])
                };

                ArrayList<double[][]> GPSs = stringToPolygons(split[1]);
                String type = "";
                if (GPSs.size() == 1) type = "Polygon";
                else type = "MultiPolygon";


                int randomOffset = new Random().nextInt(55);
                int randomColor = (int) Math.round(Math.abs(Math.sin(Math.toRadians(geoPoint[0])) * 100));

                int red = Math.max(0, Math.min(255, secondary.getRed() + randomColor - randomOffset));
                int green = Math.max(0, Math.min(255, secondary.getGreen() + randomColor - randomOffset));
                int blue = Math.max(0, Math.min(255, secondary.getBlue() + randomColor - randomOffset));
                int alpha = 255;

                int multiCount = 1;
                for (double[][] gps : GPSs){
                    long startTime = System.currentTimeMillis();
                    GeoPoly geoPoly = new GeoPoly(gps, new Color(red, green, blue, alpha));
                    long currentTime = System.currentTimeMillis() - startTime;

                    String name = split[5];
                    System.out.println(count + "-" + multiCount + ": " + name + " (" + currentTime + "ms)");
                    multiCount++;

                    polygons.add(new CountryPolygon(name, count - 1, geoPoly, geoPoint, type));
                }
                count++;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //writeToCache("cache.txt");
    }

    public void writeToCache(String path){
        try {
            FileOutputStream file = new FileOutputStream(path);
            ObjectOutputStream obj = new ObjectOutputStream(file);

            obj.writeObject(this.polygons);
            obj.flush();
            obj.close();
            System.out.println("SAVED TO CACHE.");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void readFromCache(String path){
        try {
            FileInputStream file = new FileInputStream(path);
            ObjectInputStream obj = new ObjectInputStream(file);

            this.polygons = (ArrayList<CountryPolygon>) obj.readObject();
            obj.close();
            System.out.println("READ FROM CACHE.");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
