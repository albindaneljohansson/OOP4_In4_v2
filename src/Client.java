import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/** Object input/outputstream:
 * String för chat
 * Listor (in) för frågor
 * boolean för rätt/fel svar
 * int för spellogik, command:
 *
 * 1 - New Game
 * 2 - next Round
 *
 * resultat i String array:
 * [0] -  TYP AV INFORMATION SOM SKA SKICKAS?? (0 = RESULTAT för rond, 1=resultat för spel? AVATAR, ?? )
 * [1] - 0 förlust, 1 vinst, 2 oavgjort
 * [2] - Client name
 * [3] - Client resultat
 * [4] - opponent name
 * [5] - oppenent resultat
 */

public class Client extends JFrame implements ActionListener {
    String playerName;

    String opponentPlayerName;
    JPanel gamePanel = new JPanel();

    JPanel questionPanel = new JPanel();
    JPanel chatPanel = new JPanel();

    JTextArea chatArea = new JTextArea(8,33);
    JScrollPane sp   = new JScrollPane(chatArea,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    JTextField textField = new JTextField(33);
    JButton newGameButton = new JButton("Starta nytt spel");
    JButton nextRoundButton = new JButton("Nästa rond");
    JButton showFinalResultButton = new JButton("Visa spelresultat");
    JLabel questionLabel = new JLabel(" ");


    JButton questionButton_1 = new JButton(" ");
    JButton questionButton_2 = new JButton(" ");
    JButton questionButton_3 = new JButton(" ");
    JButton questionButton_4 = new JButton(" ");

    ObjectInputStream ObjIn;
    ObjectOutputStream ObjOut;

    List<String> questionList;
    List<String> scoreList=new ArrayList<>();

    int roundsPerGame =0;
    int roundsPlayed = 0;

    int questionsPerRound = 0; // För att hålla reda på när sista frågan är ställd så vi kan
    //plocka bort frågeknappar medan vi väntar
    int questionsAnswered = 0;

    boolean win;

    boolean correctAnswer;



    static int Command_newGame = 1;
    static int Command_newRound = 2;

    public  Client () throws IOException {

        try {
            Socket socket = new Socket("127.0.0.1", 8902);
            ObjOut = new ObjectOutputStream(socket.getOutputStream());
            ObjIn = new ObjectInputStream(socket.getInputStream());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        playerName = JOptionPane.showInputDialog(null, "Ange ditt namn").toUpperCase().trim();
        ObjOut.writeObject(playerName);
        setTitle("Quizzkampen - " + playerName);
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.NORTH);
        add(questionPanel, BorderLayout.CENTER);
        add(chatPanel, BorderLayout.SOUTH);

        DefaultCaret caret = (DefaultCaret)chatArea.getCaret();  // Dessa två rader sätter uppdateringspolicy för
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);      // Scrollpane så nedersta raden visas


        chatArea.setEditable(false);
        gamePanel.setLayout(new GridLayout(2,2,5,5));
        gamePanel.add(newGameButton);
        chatPanel.setLayout(new BorderLayout());

        chatPanel.add(sp, BorderLayout.CENTER);
        chatPanel.add(textField, BorderLayout.SOUTH);

        newGameButton.addActionListener(this);
        questionButton_1.addActionListener(this);
        questionButton_2.addActionListener(this);
        questionButton_3.addActionListener(this);
        questionButton_4.addActionListener(this);
        textField.addActionListener(this);
        nextRoundButton.addActionListener(this);
        showFinalResultButton.addActionListener(this);

        //pack();
        setSize(400, 400);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void addQuestionButtons(List<String> list) { // Titlar sätts från fråge-listan
        gamePanel.remove(newGameButton);
        gamePanel.add(questionButton_1);
        gamePanel.add(questionButton_2);
        gamePanel.add(questionButton_3);
        gamePanel.add(questionButton_4);
        questionPanel.add(questionLabel);
        questionButton_1.setText(list.get(0));
        questionButton_2.setText(list.get(1));
        questionButton_3.setText(list.get(2));
        questionButton_4.setText(list.get(3));
        questionLabel.setText(list.get(4));
        repaint();
        revalidate();
    }

    public void newGame (){
        gamePanel.remove(questionButton_1);
        gamePanel.remove(questionButton_2);
        gamePanel.remove(questionButton_3);
        gamePanel.remove(questionButton_4);
        questionLabel.setText("  ");
        gamePanel.add(newGameButton);
        repaint();
        revalidate();
    }

    public void newRound (String roundResult){
        if (roundsPlayed < roundsPerGame) {             // Om inte spelet är klart
            questionLabel.setText(roundResult);
            gamePanel.add(nextRoundButton);
            repaint();
            revalidate();
        }
        if (roundsPlayed == roundsPerGame) {            // om spelet är klart
            questionLabel.setText(roundResult);         // visa rondresultat och ny knapp för hela spelets resultat
            gamePanel.add(showFinalResultButton);
            repaint();
            revalidate();
        }
    }

    public void waitForOpponent(){                          // Båda spelare hoppar hit efter en klar ronda
        gamePanel.remove(questionButton_1);                 // men spleare 2 hinner inte se detta innan GUI uppdatera igen
        gamePanel.remove(questionButton_2);                 // då resultatlistan kommer in
        gamePanel.remove(questionButton_3);
        gamePanel.remove(questionButton_4);
        questionLabel.setText("Väntar på motspelare");
        repaint();
        revalidate();

    }

    public void play() {

        try {
            Object fromServer;

            while ((fromServer = ObjIn.readObject()) != null) {

                if (fromServer instanceof String) {                              //Om inobjekt är String -> lägg till i chat
                    chatArea.append((String) fromServer + "\n");
                }

                if (fromServer instanceof List<?>) {                            // Om en lista inkommer uppdateras knappar med nya frågor
                    questionList = (List<String>) fromServer;
                    questionsPerRound = Integer.parseInt(questionList.get(6));  //Hämtar questionsPerRound
                    roundsPerGame = Integer.parseInt(questionList.get(7));
                    addQuestionButtons(questionList);

                }

                if (fromServer instanceof String[]) {                            // tar emot resultat-arrayer
                    String[] resultArray = (String[]) fromServer;


                    if (resultArray[0].equalsIgnoreCase("0")) {
                        questionsAnswered = 0;
                        roundsPlayed++;
                        opponentPlayerName = resultArray[4];
                        String message = "";

                        if (Integer.parseInt(resultArray[1]) == 0) {
                            message = "Vinst! ";
                        } else if (Integer.parseInt(resultArray[1]) == 1) {
                            message = "Förlust! ";
                        } else if (Integer.parseInt(resultArray[1]) == 2) {
                            message = "Oavgjort! ";
                        }
                        String roundResult = (message + "Du fick " + resultArray[3] + " poäng. " +
                                resultArray[4] + " fick " + resultArray[5] + " poäng!");
                        scoreList.add(resultArray[3] + "-" + resultArray[5]); // lägger till rondens resultat till en lista
                        newRound(roundResult);                                 // som kan visas i slutet av spelet
                    }

                }
                if (fromServer instanceof Integer) {                    // visa resultat för hela spelet
                    int command99 = (int) fromServer;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        try {

            if (e.getSource() == textField) {
                ObjOut.writeObject((String) playerName + ": "+textField.getText().trim());
                ObjOut.flush();
                textField.setText("");
            }
            if (e.getSource() == newGameButton) {                   // triggar handler att skicka lista med frågor
                ObjOut.writeObject((int) Command_newGame);
                ObjOut.flush();

            }
            if (e.getSource()== nextRoundButton){                   //trigga handler att skicka nästa lista med frågor
                gamePanel.remove(nextRoundButton);
                repaint();
                revalidate();
                ObjOut.writeObject((int) Command_newRound);
                ObjOut.flush();
            }

            if (e.getSource() == showFinalResultButton) {       //Plocka bort alla knappar och skriva ut scorelist
                gamePanel.remove(showFinalResultButton);        // ända sättet att få ny rad i JLabel är tydligen HTML
                String finalResult = "<html>" + "<center>"+"Resultat:"+"</center>"+
                        "<br>"+playerName +": &emsp; &emsp;"+opponentPlayerName+":";
                for (String s : scoreList) {                    // Hämtar resultat från ScoreList
                    finalResult = finalResult + "<br>"+"<center>"+s+"</center>";
                }
                finalResult = finalResult +"</html>";
                questionLabel.setText(finalResult);
                repaint();
                revalidate();
            }

            if ((e.getSource() == questionButton_1) || (e.getSource() == questionButton_2)
                    || (e.getSource() == (questionButton_3)) || (e.getSource() == (questionButton_4))) {
                JButton button = (JButton)e.getSource();
                String answer = button.getText();

                questionsAnswered++;

                if (answer.equalsIgnoreCase(questionList.get(5))) {
                    correctAnswer = true; //Anna: behövs den här variabeln? varför inte ObjOut.writeObject(true);
                    // och motsvarande i else-satsen
                    ObjOut.writeObject((Boolean) correctAnswer);
                    ObjOut.flush();
                }
                if (!answer.equalsIgnoreCase(questionList.get(5))){
                    correctAnswer = false;
                    ObjOut.writeObject((Boolean) correctAnswer);
                    ObjOut.flush();
                }
                if (questionsAnswered == questionsPerRound){  // Om man svarat antal frågor per runda
                    waitForOpponent();                      // innan motståndare får man vänta på listan
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.play();
    }
}