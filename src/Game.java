import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Game {

    int player1_gameScore = 0;
    int player2_gameScore = 0;
    final int numberOfRounds;
    final int QuestionsPerRound;
    List<List<Questions>> fullGameList = new ArrayList<>();
    List<String> categories = new ArrayList<>();


    public Game (){
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream("src/Game.properties"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String roundsString = properties.getProperty("numRounds");
        this.numberOfRounds= Integer.parseInt(roundsString);
        String questionsString = properties.getProperty("questionsPerRound");
        this.QuestionsPerRound = Integer.parseInt(questionsString);

        categories=List.of("Geografi", "Film", "Mat och Dryck", "Naturvetenskap","TV och Dator-spel", "Musik");
    }

    //returnerar en lista med samtliga questions för den specifika ronden
    public List<Questions> getOneRoundList(int roundNumber) {
        return fullGameList.get(roundNumber);
    }

    // setter för antalet frågor + kategori i ett spel
    private List<Questions> setOneRoundList(String category) {
        List<Questions> tempOneRoundList = new ArrayList<>();

        while (tempOneRoundList.size() <= numberOfRounds) {
            Questions q = new Questions(category, QuestionsPerRound, numberOfRounds); // Skickar med antal frågor och ronder
            int counter=0;
            if (tempOneRoundList.size() > 0) {
                   for (int j=0; j<tempOneRoundList.size(); j++) {                  // Iterera över listan - Om ej dublettfråga - Lägg till
                        if (!q.getQuestionsAndAnswersList().get(4).equalsIgnoreCase(tempOneRoundList.get(j).getQuestionsAndAnswersList().get(4))) {
                            counter++;
                            if (counter == tempOneRoundList.size()) {
                                tempOneRoundList.add(q);
                            }
                        }
                   }
                }
                if (tempOneRoundList.size() == 0){
                    tempOneRoundList.add(q);
                }
            }
        return tempOneRoundList;
    }

    // Anropar setOneRoundList och skapar så många ronder samt frågor per rond som anges i properties
    // Skickar med Shufflade categories för varje runda
    // Varje rond har alltså samma kategori
    public void setFullGameList() {
        List<String> copyCat = new ArrayList<>(categories);
        Collections.shuffle(copyCat);                       //Shuffla innan gör att vi aldrig får samma kategori 2ggr/spel
        for (int i = 0; i < numberOfRounds; i++) {
            List<Questions> temp = new ArrayList<>();
            temp = setOneRoundList(copyCat.get(i));
            fullGameList.add(temp);
        }

    }
    public int getQuestionsPerRound() {
        return QuestionsPerRound;
    }

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public void setGameScore (int playerNumber, int roundScore) {
        if (playerNumber == 1) {
            this.player1_gameScore = player1_gameScore + roundScore;
        }
        if (playerNumber == 2) {
            this.player2_gameScore = player2_gameScore + roundScore;
        }
    }
}
