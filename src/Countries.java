import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Objects;
import java.util.Random;

public class Countries implements Serializable {

    ArrayList<CountryPolygon> polygons = new ArrayList<>();
    int count = 0;
    Color secondary;

    String buildPath;

    public Countries(String file, boolean build, Color s) {
        this.secondary = s;
        long startTimeLoading = System.currentTimeMillis();
        buildPath = file;

        if (build) loadFromFile(buildPath);
        else readFromCache(Objects.requireNonNull(this.getClass().getResource("world/cache.txt")).getPath());

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

    /**
     * Sets every triangle to random color.
     */
    public void setRandomTriangleColor(){
        for (CountryPolygon p : polygons){
            p.geoShapes.setRandomColor();
        }
    }

    /**
     * If cache isn't found, it rebuilds them from scratch.
     * @param file - path to .csv
     */
    public void loadFromFile(String file){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("world/world-administrative-boundaries.csv")));

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

                String name = split[5];
                String continent = split[6];
                String region = split[7];

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

                    System.out.println((count + 1) + "-" + multiCount + ": " + name + " (" + currentTime + "ms)");
                    multiCount++;

                    polygons.add(new CountryPolygon(name, count, geoPoly, geoPoint, continent, region));
                }
                count++;
            }
        }
        catch (Exception ignored){
        }

        writeToCache("resources/world/cache.txt");
    }

    /**
     * Converts String of coordinates to polygons.
     * @param data - Unconverted polygons
     * @return MultiPolygon.
     */
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

    /**
     * Recolors every country by color and their distance from the equator.
     * @param c - color
     * @param darkenCoef - how darker should it be.
     */
    public void recolorCountries(Color c, int darkenCoef){
        for (int i = 0; i <= polygons.get(polygons.size() - 1).id; i++) {
            int plusID = 0;
            try {
                while (polygons.get(i + plusID).id != i){
                    plusID++;
                }
            }
            catch (Exception e){
                plusID--;
            }

            int randomOffset = new Random().nextInt(30);
            int heightColor = (int) Math.round(Math.abs(Math.cos(Math.toRadians(polygons.get(i + plusID).geoPoint[0])) * darkenCoef));

            int red = Math.max(0, Math.min(255, c.getRed() - heightColor + randomOffset));
            int green = Math.max(0, Math.min(255, c.getGreen() - heightColor + randomOffset));
            int blue = Math.max(0, Math.min(255, c.getBlue() - heightColor + randomOffset));

            setPolygonsColor(new Color(red, green, blue), i);
        }
    }

    /**
     * Writes countries to cache for faster startup.
     * @param path - path to cache
     */
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
            System.out.println("SAVING TO CACHE FAILED.");
        }
    }

    /**
     * Reads countries from cache for faster startup.
     * @param path - path to cache
     */
    public void readFromCache(String path){
        try {
            //FileInputStream file = new FileInputStream(Objects.requireNonNull(this.getClass().getResource(path)).getFile());
            ObjectInputStream obj = new ObjectInputStream(this.getClass().getResourceAsStream("world/cache.txt"));

            this.polygons = (ArrayList<CountryPolygon>) obj.readObject();
            obj.close();
            System.out.println("READ FROM CACHE.");
        }
        catch (Exception e){
            System.out.println("CACHE ERROR");
            loadFromFile(buildPath);
        }
    }

    public CountryPolygon findByName(String name){
        for (CountryPolygon cp : polygons){
            if (cp.name.equalsIgnoreCase(name)){
                return cp;
            }
        }
        throw new InputMismatchException();
    }

    public int vertexCountForCountry(CountryPolygon country){
        int vCount = 0;
        for (CountryPolygon cp : polygons){
            if (cp.id == country.id) vCount += cp.geoShapes.vertices.size();
        }
        return vCount;
    }

}
