public class QuestionBuilder {

    protected String category;
    protected String subCategory;
    protected String question;
    protected String answer;

    public QuestionBuilder (String category, String subCategory, String question, String answer){
        this.category = category;
        this.subCategory = subCategory;
        this.question = question;
        this.answer = answer;
    }
    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}

