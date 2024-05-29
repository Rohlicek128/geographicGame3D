public class Continent {

    String name;
    LocationType type;
    double[] centralCoordinates;
    int numOfCountries;

    public Continent(String name, LocationType type, double centralX, double centralY) {
        this.name = name;
        this.type = type;
        this.centralCoordinates = new double[]{centralX, centralY};
    }

    public void loadNumOfCountries(Countries countries){
        int lastID = 0;
        for (CountryPolygon cp : countries.polygons){
            if (type == LocationType.CONTINENT){
                if (cp.continent.equalsIgnoreCase(name) && cp.id > lastID){
                    numOfCountries++;
                    lastID = cp.id;
                }
            }
            if (name.equalsIgnoreCase("Northern America")){
                if ((cp.region.equalsIgnoreCase("Northern America") ||
                        cp.region.equalsIgnoreCase("Central America") ||
                        cp.region.equalsIgnoreCase("Caribbean")) && cp.id > lastID){
                    numOfCountries++;
                    lastID = cp.id;
                }
            }
            else if (type == LocationType.REGION){
                if (cp.region.equalsIgnoreCase(name) && cp.id > lastID){
                    numOfCountries++;
                    lastID = cp.id;
                }
            }
        }
    }

}
