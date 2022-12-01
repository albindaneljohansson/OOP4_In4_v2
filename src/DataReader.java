import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DataReader {
    String category;
    List<String> oneQuestionList = new ArrayList<>();
    String pathToQuestionFile = "src/questionfile.txt";

    public DataReader(String category){
        this.category=category;
        fileReaderToList();
    }

    public String getAnswer() {
        return oneQuestionList.get(2);
    }

    public String getQuestion (){
        return oneQuestionList.get(1);
    }
    public List<String> getAlternatives () {
        List<String> listOf4 = new ArrayList<>();
        for (int i = 2; i<=5; i++){                     // Shufflar alternativen i oneQuestionList
            listOf4.add(oneQuestionList.get(i));
        }
        Collections.shuffle(listOf4);
        return listOf4;
    }

    public void fileReaderToList () {

        List<List> questionsInSameCategoryList = new ArrayList<>(); // lista för alla frågor i samma kategori
        String line;
        Path inFile = Paths.get(pathToQuestionFile);

        try(Scanner fileReader = new Scanner(inFile)){
            while (fileReader.hasNext()) {
                line = fileReader.nextLine();

                if (line.toUpperCase().startsWith(category.toUpperCase())) {
                    String[] questionData4partsLine;
                    questionData4partsLine = line.split(";");
                    List<String> oneQuestionList = new ArrayList<>();

                    oneQuestionList.add(questionData4partsLine[0]); // Category
                    oneQuestionList.add(questionData4partsLine[1]); // Question
                    oneQuestionList.add(questionData4partsLine[2]); // Answer
                    for (int i=3; i<questionData4partsLine.length; i++){ // Sparar så många alternativa svar det finns i oneQuestionList
                        oneQuestionList.add(questionData4partsLine[i]);
                    }
                    questionsInSameCategoryList.add(oneQuestionList); // Sparar alla frågor i en kategori i en lista
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
       Collections.shuffle(questionsInSameCategoryList);            // Shufflar listan med alla frågor i samma kategori
        this.oneQuestionList = questionsInSameCategoryList.get(0); // Sätter listan som övriga metoder hämtar ifrån till första frågan
    }
}


