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

    List<Questions> oneRoundList = new ArrayList<>();

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
        String roundsString = properties.getProperty("numRounds", String.valueOf(6));//=2 just nu
        this.numberOfRounds= Integer.parseInt(roundsString);
        String questionsString = properties.getProperty("questionsPerRound", String.valueOf(3));//=2 just nu
        this.QuestionsPerRound = Integer.parseInt(questionsString);

        categories=List.of("cities", "movies");
    }

    // Returnerar ett questionsobject med så många frågor man efterfrågar i setQuestions

    //returnerar en lista med samtliga questions för den specifika ronden
    public List<Questions> getOneRoundList(int roundNumber) {
        return fullGameList.get(roundNumber);
    }

    // setter för antalet frågor + kategori i ett spel
    private List<Questions> setOneRoundList(String category) {
        List<Questions> oneRoundList = new ArrayList<>();
        for (int i = 0; i< QuestionsPerRound; i++) {
            Questions q = new Questions(category, QuestionsPerRound, numberOfRounds); // Skickar med frågot per round så clienten vet
            oneRoundList.add(q);
        }
        return oneRoundList;
    }

    public List<List<Questions>> getFullGameList() {
        return fullGameList;
    }

    public void setFullGameList() {
        List<String> copyCat = new ArrayList<>(categories);
        Collections.shuffle(copyCat);
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
