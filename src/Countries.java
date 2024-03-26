import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Countries {

    ArrayList<CountryPolygon> polygons = new ArrayList<>();
    int count = 1;

    public Countries(String file) {
        loadFromFile(file);
    }

    public List<String[]> readData(String path){
        List<String[]> temp = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String s = "";
            while ((s = br.readLine()) != null){
                temp.add(s.split(";"));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return temp;
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
        long startTimeLoadning = System.currentTimeMillis();
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
                int randomColor = (int) Math.round(Math.abs(Math.sin(Math.toRadians(geoPoint[0])) * 200));

                int multiCount = 1;
                for (double[][] gps : GPSs){
                    long startTime = System.currentTimeMillis();
                    GeoPoly geoPoly = new GeoPoly(gps, new Color(randomColor + randomOffset, randomColor + randomOffset, randomColor + randomOffset));
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

        //tf :skull:
        long currentTimeLoading = System.currentTimeMillis() - startTimeLoadning;
        currentTimeLoading = Math.round(currentTimeLoading / 100.0) / 10;
        int minutes = (int) (currentTimeLoading / 60);
        double seconds = currentTimeLoading - (minutes * 60L);

        System.out.println("LOADING TIME: " + minutes + "m " + seconds + "s");
    }

}
