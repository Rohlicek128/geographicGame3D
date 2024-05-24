import java.util.ArrayList;
import java.util.InputMismatchException;

public class ContinentsState {

    ArrayList<Continent> orderedContinents = new ArrayList<>();
    int currentIndex = 0;
    int minCountrySize;

    int correctPercentage;
    int correctCount;

    int overallWrongCount;
    int wrongMax;

    int overallCorrectCount;
    int overallMaxCorrect;

    public ContinentsState(int minCountrySize, int correctPercentage, int wrongMax, Countries countries) {
        this.minCountrySize = minCountrySize;
        this.correctPercentage = Math.max(1, Math.min(100, correctPercentage));
        this.wrongMax = wrongMax;

        loadContinentsOrder();
        for (int i = 0; i < orderedContinents.size(); i++){
            orderedContinents.get(i).loadNumOfCountries(countries);
            overallMaxCorrect += getCurrentCorrectMax(i);
        }
    }

    public int getCurrentCorrectMax(int index){
        return (int) Math.ceil(orderedContinents.get(index).numOfCountries * (correctPercentage / 100.0));
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
        if (correctCount >= getCurrentCorrectMax(currentIndex)) {
            correctCount = 0;
            currentIndex++;
        }

        Continent currentContinent = orderedContinents.get(currentIndex);
        if (country.geoShapes.vertices.size() < minCountrySize) return false;

        //Divide Americas into North and South
        if (currentContinent.name.equalsIgnoreCase("Northern America")){
            return country.region.equalsIgnoreCase("Northern America") || country.region.equalsIgnoreCase("Central America") || country.region.equalsIgnoreCase("Caribbean");
        }
        if (currentContinent.name.equalsIgnoreCase("South America")){
            return country.region.equalsIgnoreCase("South America");
        }

        //Other continents
        return country.continent.equalsIgnoreCase(currentContinent.name);
    }

}
