import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;

public class ContinentsState {

    ArrayList<Continent> orderedContinents = new ArrayList<>();
    int currentIndex = 0;
    int minCountrySize;

    Difficulty difficulty;
    int correctCount;

    int overallWrongCount;
    int wrongMax;

    int overallCorrectCount;
    int overallMaxCorrect;

    int points;

    public ContinentsState(int minCountrySize, Difficulty difficulty, int wrongMax) {
        this.minCountrySize = minCountrySize;
        this.difficulty = difficulty;
        this.wrongMax = wrongMax;

        loadContinentsOrder();
    }

    /**
     * Loads to all continents the number of countries in that continent.
     * @param countries - List of all countries
     */
    public void loadNumOfCountries(Countries countries){
        for (int i = 0; i < orderedContinents.size(); i++){
            orderedContinents.get(i).loadNumOfCountries(countries);
            overallMaxCorrect += getCurrentCorrectMax(i);
        }
    }

    /**
     * Gets the max number of countries in the current continent to advance to the next.
     * @param index - continents order in the list.
     * @return the max number of countries.
     */
    public int getCurrentCorrectMax(int index){
        return (int) Math.ceil(orderedContinents.get(index).numOfCountries * (difficulty.correctPercentage / 100.0));
    }

    public void loadContinentsOrder(){
        orderedContinents.add(new Continent("Europe", LocationType.CONTINENT, 14.5, 50.0));
        orderedContinents.add(new Continent("Northern America", LocationType.REGION, -94.9, 41.2));
        orderedContinents.add(new Continent("South America", LocationType.REGION, -60.4, -18.5));
        orderedContinents.add(new Continent("Asia", LocationType.CONTINENT, 86.0, 34.1));
        orderedContinents.add(new Continent("Africa", LocationType.CONTINENT, 14.9, 3.6));
        orderedContinents.add(new Continent("Oceania", LocationType.CONTINENT, 148.5, -28.8));
        orderedContinents.add(new Continent("Antarctica", LocationType.CONTINENT, 83.0, -85.0));
    }

    /**
     * Calculates points.
     * @param time - time in milliseconds from which it took to guess all the countries.
     * @return points.
     */
    public int calculateScore(long time){
        int maxTimePoints = 2000 - difficulty.flightDuration;
        int timePoints = (int) Math.max(0, Math.min(maxTimePoints, (maxTimePoints - ((time / 1000.0) / difficulty.flightDuration) * maxTimePoints)));
        return points + timePoints;
    }

    public void randomizeOrder(){
        Collections.shuffle(orderedContinents);
    }

    /**
     * Checks if the randomly generated country is eligible to be next.
     * @param country - randomly generated country;
     * @param vertexCount - size of the country.
     * @return if its eligible.
     */
    public boolean equalsCurrentContinent(CountryPolygon country, int vertexCount){
        if (correctCount >= getCurrentCorrectMax(currentIndex)) {
            correctCount = 0;
            currentIndex++;
        }

        Continent currentContinent = orderedContinents.get(currentIndex);
        if (vertexCount < minCountrySize) return false;

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
