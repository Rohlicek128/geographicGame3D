public class Continent {

    String name;
    LocationType type;
    int numOfCountries;
    int guessedCount;

    public Continent(String name, LocationType type) {
        this.name = name;
        this.type = type;
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
            if (type == LocationType.REGION){
                if (cp.region.equalsIgnoreCase(name) && cp.id > lastID){
                    numOfCountries++;
                    lastID = cp.id;
                }
            }
        }
    }

}
