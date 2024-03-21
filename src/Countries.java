import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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

    /*public ArrayList<CountryPolygon> distilCountryData(List<String[]> data){
        ArrayList<CountryPolygon> temp = new ArrayList<>();
        for (String[] d : data) {
            ArrayList<String> gpsArray = new ArrayList<>();
            d[1] = d[1].replace("\"{\"\"coordinates\"\": ", "");


            String name = d[5];
            System.out.println(count + ". " + name);
            count++;

            int randomNum = new Random().nextInt(230) + 256 - 230;
            GeoPoly geoPoly = new GeoPoly(gps, new Color(randomNum, randomNum, randomNum));

            String[] dgp = d[0].split(",");
            double[] geoPoint = new double[]{
                    Double.parseDouble(dgp[0]),
                    Double.parseDouble(dgp[1])
            };

            temp.add(new CountryPolygon(name, geoPoly, geoPoint, type));
        }
        return temp;
    }*/

    public void loadFromFile(String file){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            JSONParser parser = new JSONParser();

            br.readLine();
            String s = "";
            while ((s = br.readLine()) != null){
                String[] split = s.split(";");

                String[] geoPointSplit = split[0].split(",");
                double[] geoPoint = new double[]{
                        Double.parseDouble(geoPointSplit[0]),
                        Double.parseDouble(geoPointSplit[1])
                };

                /*Object obj = parser.parse(split[1]);
                JSONObject jsonObject = (JSONObject) obj;

                JSONArray jsonArray = (JSONArray) jsonObject.get("coordinates");*/

                String p = split[1].replace("\"{\"\"coordinates\"\": [", "");
                p = p.replace("[", "");
                p = p.replace("]", "");
                String[] pSplit = p.split(",");
                double[][] gps;
                gps = new double[pSplit.length / 2][2];
                for (int i = 0; i < pSplit.length / 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        double x = Double.parseDouble(pSplit[i * 2 + j]);
                        if (x != 0.0){
                            gps[i][j] = x;
                        }
                    }
                }
                String type = pSplit[pSplit.length - 1];
                if (type.equalsIgnoreCase(" \"\"type\"\": \"\"MultiPolygon\"\"}\"")){
                    //System.out.println("multi");
                }
                else {
                    String name = split[5];
                    System.out.println(count + ". " + name);
                    count++;

                    int randomNum = new Random().nextInt(55);
                    int red = (int) Math.round(Math.abs(Math.sin(Math.toRadians(geoPoint[0])) * 200));
                    int green = (int) Math.round(Math.abs(Math.sin(Math.toRadians(geoPoint[0])) * 200));
                    int blue = (int) Math.round(Math.abs(Math.sin(Math.toRadians(geoPoint[0])) * 200));
                    GeoPoly geoPoly = new GeoPoly(gps, new Color(red + randomNum, green + randomNum, blue + randomNum));

                    polygons.add(new CountryPolygon(name, geoPoly, geoPoint, type));
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
