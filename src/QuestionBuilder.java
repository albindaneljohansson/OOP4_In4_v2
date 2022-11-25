
public class QuestionBuilder {

    protected String category;
    protected String question;
    protected String answer;

    protected String altAnswer1;

    protected String altAnswer2;

    protected String altAnswer3;

    public QuestionBuilder(String category, String question, String answer,
                           String altAnswer1, String altAnswer2, String altAnswer3){
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.altAnswer1 = altAnswer1;
        this.altAnswer2 = altAnswer2;
        this.altAnswer3 = altAnswer3;
    }
    public String getCategory() {
        return category;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}

