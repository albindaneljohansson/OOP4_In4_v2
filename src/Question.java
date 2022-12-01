import java.util.ArrayList;
import java.util.List;

/** questionsAndAnswersList:
 * Alternativ index 0-3
 * Fråga index 4
 * Svar index 5
 * questionsPerRound index 6
 * rundsPerGame index 7
 */

public class Question {
    String category;
    List<String> questionsAndAnswersList;

    public Question(String category, int questionsPerRound, int roundsPerGame) {
        this.category=category;
        DataReader dr = new DataReader(category);
        List<String> tempList = new ArrayList<>(dr.getAlternatives());
        this.questionsAndAnswersList = tempList;
        questionsAndAnswersList.add(dr.getQuestion());
        questionsAndAnswersList.add(dr.getAnswer());
        questionsAndAnswersList.add(String.valueOf(questionsPerRound));     //Lägger antal frågor per round
        questionsAndAnswersList.add(String.valueOf(roundsPerGame));          //Lägger antal ronder
    }

    public List<String> getQuestionsAndAnswersList() {
        return questionsAndAnswersList;
    }
}