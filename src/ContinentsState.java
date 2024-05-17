import java.util.ArrayList;

public class ContinentsState {

    ArrayList<String> orderedContinents = new ArrayList<>();
    int currentIndex = 0;
    int minCountrySize;

    int correctMax;
    int correctCount;

    public ContinentsState(int minCountrySize, int correctMax) {
        this.minCountrySize = minCountrySize;
        this.correctMax = correctMax;
        loadContinentsOrder();
    }

    public void loadContinentsOrder(){
        orderedContinents.add("Europe");
        orderedContinents.add("Northern America");
        orderedContinents.add("South America");
        orderedContinents.add("Asia");
        orderedContinents.add("Africa");
        orderedContinents.add("Oceania");
        orderedContinents.add("Antarctica");
    }

    public boolean equalsCurrentContinent(CountryPolygon country){
        if (correctCount == correctMax) {
            correctCount = 0;
            currentIndex++;
        }

        //Divide Americas into North and South
        if (orderedContinents.get(currentIndex).equalsIgnoreCase("Northern America")){
            return country.region.equalsIgnoreCase("Northern America") || country.region.equalsIgnoreCase("Central America");
        }
        if (orderedContinents.get(currentIndex).equalsIgnoreCase("South America")){
            return country.region.equalsIgnoreCase("South America");
        }

        //Other continents
        return country.continent.equalsIgnoreCase(orderedContinents.get(currentIndex));
    }

}
