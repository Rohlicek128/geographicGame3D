import java.util.ArrayList;
import java.util.InputMismatchException;

public class ContinentsState {

    ArrayList<Continent> orderedContinents = new ArrayList<>();
    int currentIndex = 0;
    int minCountrySize;

    int correctPercentage;
    int correctCount;

    public ContinentsState(int minCountrySize, int correctPercentage, Countries countries) {
        this.minCountrySize = minCountrySize;

        if (correctPercentage > 100 || correctPercentage <= 0) throw new InputMismatchException();
        else this.correctPercentage = correctPercentage;

        loadContinentsOrder();
        for (Continent c : orderedContinents) c.loadNumOfCountries(countries);
    }

    public int getCurrentCorrectMax(){
        return (int) Math.ceil(orderedContinents.get(currentIndex).numOfCountries * (correctPercentage / 100.0));
    }

    public void loadContinentsOrder(){
        orderedContinents.add(new Continent("Europe", LocationType.CONTINENT));
        orderedContinents.add(new Continent("Northern America", LocationType.REGION));
        orderedContinents.add(new Continent("South America", LocationType.REGION));
        orderedContinents.add(new Continent("Asia", LocationType.CONTINENT));
        orderedContinents.add(new Continent("Africa", LocationType.CONTINENT));
        orderedContinents.add(new Continent("Oceania", LocationType.CONTINENT));
        orderedContinents.add(new Continent("Antarctica", LocationType.CONTINENT));
    }

    public boolean equalsCurrentContinent(CountryPolygon country){
        Continent currentContinent = orderedContinents.get(currentIndex);
        if (country.geoShapes.vertices.size() < minCountrySize) return false;

        if (correctCount >= getCurrentCorrectMax()) {
            correctCount = 0;
            currentIndex++;
        }

        //Divide Americas into North and South
        if (currentContinent.name.equalsIgnoreCase("Northern America")){
            return country.region.equalsIgnoreCase("Northern America") || country.region.equalsIgnoreCase("Central America");
        }
        if (currentContinent.name.equalsIgnoreCase("South America")){
            return country.region.equalsIgnoreCase("South America");
        }

        //Other continents
        return country.continent.equalsIgnoreCase(currentContinent.name) && !country.name.equalsIgnoreCase("Bouvet Island");
    }

}
