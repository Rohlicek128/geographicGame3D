import org.locationtech.jts.triangulate.ConformingDelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Countries {

    ArrayList<CountryPolygon> polygons = new ArrayList<>();
    //ConformingDelaunayTriangulationBuilder

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

                //"{""coordinates"": [
                String p = split[1].replace("\"{\"\"coordinates\"\": [", "");
                p = p.replace("[", "");
                p = p.replace("]", "");
                String[] pSplit = p.split(",");
                double[][] gps;
                gps = new double[pSplit.length][2];
                for (int i = 0; i < pSplit.length / 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        gps[i][j] = Double.parseDouble(pSplit[i + j]);
                    }
                }
                Polygon polygon = new Polygon(gps);

                String name = split[5];

                polygons.add(new CountryPolygon(name, polygon, geoPoint));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
