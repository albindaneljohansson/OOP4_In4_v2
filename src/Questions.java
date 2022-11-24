import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** questionsAndAnswersList:
 * Alternativ index 0-3
 * Fråga index 4
 * Svar index 5
 * questionsPerRound index 6
 * rundsPerGame index 7
 */

public class Questions {
    String category;
    List<String> questionsAndAnswersList;

    public Questions(String category, int questionsPerRound, int roundsPerGame) {
        this.category=category;
        Random rn = new Random();               // ger ett random for vilken fråga inom en kategori
        int questionNumber = rn.nextInt(2);
        DataBase db = new DataBase(category, questionNumber);
        List<String> tempList = new ArrayList<>(db.getAlternatives());
        this.questionsAndAnswersList = tempList;
        questionsAndAnswersList.add(db.getQuestion());
        questionsAndAnswersList.add(db.getAnswer());
        questionsAndAnswersList.add(String.valueOf(questionsPerRound)); //Lägger antal frågor per round sist i varje frågelista
        questionsAndAnswersList.add(String.valueOf(roundsPerGame));
    }

    public String getCategory() {
        return category;
    }

    public List<String> getQuestionsAndAnswersList() {
        return questionsAndAnswersList;
    }
}