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

    DataBase db;

    List<Questions> oneRoundList = new ArrayList<>();
    List<List<Questions>> fullGameList = new ArrayList<>();
    List<String> categories = new ArrayList<>();


    public Game (){
        Properties properties = new Properties();
       // DataBase.fileReaderToList(pathToQuestionFile);
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
        List<Questions> oneRoundList = new ArrayList<>();
        int j=0;
        while (oneRoundList.size() <= numberOfRounds) {
            Questions q = new Questions(category, QuestionsPerRound, numberOfRounds); // Skickar med antal frågor och ronder
                if (oneRoundList.size() > 0) {             // Lägger endast till en ny fråga om den inte finns i oneRoundList
                    if(!q.getQuestionsAndAnswersList().get(4).equalsIgnoreCase(oneRoundList.get(j).getQuestionsAndAnswersList().get(4))) {
                        oneRoundList.add(q);
                        j++;
                        }
                }
                if (oneRoundList.size() == 0){
                    oneRoundList.add(q);
                }
            }
        return oneRoundList;
    }

    public List<List<Questions>> getFullGameList() {
        return fullGameList;
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
