import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Countries {

    ArrayList<CountryPolygon> polygons = new ArrayList<>();

    public Countries(String file) {
        loadFromFile(file);
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
                    System.out.println(type);
                    geoPoly geoPoly = new geoPoly(gps);

                    String name = split[5];

                    polygons.add(new CountryPolygon(name, geoPoly, geoPoint, type));
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
