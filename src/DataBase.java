import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class DataBase {
    String category;
    int questionsIndex;
    /**
     * Svar, frågor och alternativ måste ha samma index
     */
    //Skapa metod i den här klassen för att läsa in frågorna från textfil? Cruces.
    static String[] citiesAnswers = new String[]{"stockholm", "oslo"};
    static List<String> citiesAlternatives = List.of("stockholm", "oslo", "paris", "rom", "helsingfors", "budapest");
    static String[] citiesQuestions = new String[]{"Vad heter huvudstaden i Sverige?", "Vad heter huvustaden i Norge?"};


    //ytterligare en kategori frågor för att testa multipla ronder
    static String[] moviesAnswers = new String[]{"1964", "1984"};
    static List<String> moviesAlternatives = List.of("1964", "1984", "1968", "1997", "1971", "1974");
    static String[] moviesQuestions = new String[]{"Vilket år kom filmen Mary Poppins?", "Vad heter filmen som bygger på en roman av George Orwell?"};


    public DataBase(String category, int questionsIndex){
        this.category=category;
        this.questionsIndex=questionsIndex;
    }

    public String getAnswer() {
        if (category.equalsIgnoreCase("cities")) {
            return citiesAnswers[questionsIndex];
        }
        if (category.equalsIgnoreCase("movies")) {
            return moviesAnswers[questionsIndex];
        }
        return null;
    }

    public String getQuestion (){
        if (category.equalsIgnoreCase("cities")){
            return citiesQuestions[questionsIndex];
        }
        if (category.equalsIgnoreCase("movies")){
            return moviesQuestions[questionsIndex];
        }

        return null;
    }
    public List<String> getAlternatives () {
        if (category.equalsIgnoreCase("cities")) {
            return getAlternativesShuffleList(citiesAlternatives);
        }
        if (category.equalsIgnoreCase("movies")) {
            return getAlternativesShuffleList(moviesAlternatives);
        }
        return null;
    }

    public List <String> getAlternativesShuffleList (List<String> inList){
        List<String> copyList = new ArrayList<>(inList);
        copyList.remove(questionsIndex);
        Collections.shuffle(copyList);
        List<String> listOf4 = new ArrayList<>();
        for (int i = 0; i<3; i++){
            listOf4.add(copyList.get(i));
        }
        listOf4.add(getAnswer());
        Collections.shuffle(listOf4);

        return listOf4;
    }
    public static List<QuestionBuilder> fileReaderToList (String questionFile) {
        List<QuestionBuilder> questionList = new ArrayList<>();
        String line;
        String[] questionData4partsLine = new String[6];

        Path inFile = Paths.get(questionFile);

        try(Scanner fileReader = new Scanner(inFile)){
            while (fileReader.hasNext()) {
                line = fileReader.nextLine();
                questionData4partsLine = line.split(";");

                QuestionBuilder qb = new QuestionBuilder(questionData4partsLine[0].trim(),
                        questionData4partsLine[1].trim(),
                        questionData4partsLine[2].trim(),
                        questionData4partsLine[3].trim(),
                        questionData4partsLine[4].trim(),
                        questionData4partsLine[5].trim());

                questionList.add(qb);
            }
        }
        catch (NoSuchFileException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return questionList;
    }
    public List<QuestionBuilder> getCategoryList
            (String category, List<QuestionBuilder> questionList, int questionPerRound){
        List<QuestionBuilder> temp = new ArrayList<>();
        List<QuestionBuilder> categoryList = new ArrayList<>();

        for (QuestionBuilder qb : questionList){
            if(qb.category.equals(category)){
                categoryList.add(qb);
            }
        }
        Collections.shuffle(categoryList);
        for (int i = categoryList.size(); i > questionPerRound; i--) {
            categoryList.remove(categoryList.size()-1);
        }
        return categoryList;
    }
}
