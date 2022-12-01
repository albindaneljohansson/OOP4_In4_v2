import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

/**
 * Object input/outputstream:
 * String för chat
 * Listor (in) för frågor
 * boolean för rätt/fel svar
 * int för spellogik, command:
 * <p>
 * 1 - New Game
 * 2 - next Round
 * 3 - Oppenent ansluten
 * -1 - Surrender
 * <p>
 * resultat i String array:
 * [0] -  TYP AV INFORMATION SOM SKA SKICKAS?? (0 = RESULTAT för rond, 1=resultat för spel? AVATAR, ?? )
 * [1] - 0 förlust, 1 vinst, 2 oavgjort
 * [2] - Client name
 * [3] - Client resultat
 * [4] - opponent name
 * [5] - oppenent resultat
 */

public class Client extends JFrame implements ActionListener {

    JPanel commandPanel = new JPanel();
    JButton newGameButton = new JButton("Starta spel");
    JButton nextRoundButton = new JButton("Nästa rond");
    JButton showFinalResultButton = new JButton("Slutresultat");
    JButton surrenderButton = new JButton("Ge upp");
    JButton finishButton = new JButton("Avsluta spel");
    JLabel topLabel = new JLabel("Gnugga geniknölarna");
    JLabel blankLabel = new JLabel(" ");//hjälpkomponent för att få till rätt layout på commandPanel

    JPanel gamePanel = new JPanel();
    JPanel alternativesPanel = new JPanel();
    JButton alternativeButton_1 = new JButton(" ");
    JButton alternativeButton_2 = new JButton(" ");
    JButton alternativeButton_3 = new JButton(" ");
    JButton alternativeButton_4 = new JButton(" ");
    String answerFeedback;
    JPanel questionResultPanel = new JPanel();

    JPanel questionPanel = new JPanel();
    JLabel questionLabel = new JLabel(" ");


    JPanel chatPanel = new JPanel();
    JTextArea chatArea = new JTextArea(8, 33);
    JScrollPane sp = new JScrollPane(chatArea,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    JTextField textField = new JTextField(33);
    Color[] colorArray = new Color[4];

    ObjectInputStream ObjIn;
    ObjectOutputStream ObjOut;

    final String playerName;
    static String opponentPlayerName;           //slippa hämta namnet om och om igen?

    List<String> questionList;
    List<String> scoreList = new ArrayList<>();

    int roundsPerGame = 0;
    int roundsPlayed = 0;

    int questionsPerRound = 0; // För att hålla reda på när sista frågan är ställd så vi kan
    //plocka bort frågeknappar medan vi väntar
    int questionsAnswered = 0;

    int totalGameScore = 0;
    int opponentTotalGameScore = 0;


    //boolean win;
    // boolean correctAnswer;

    final static int COMMAND_SURRENDER = -1;
    final static int COMMAND_CLOSE = 0;
    final static int COMMAND_NEW_GAME = 1;      //Commands att skicka till handlern (alltid int när det inte är till chatten)
    final static int COMMAND_NEXT_ROUND = 2;
    final static int COMMAND_FINAL_RESULT = 3;
    final static int COMMAND_CORRECT = 10;
    final static int COMMAND_INCORRECT = 11;


    public Client(String playerName) throws IOException {

        try {
            Socket socket = new Socket("127.0.0.1", 8902);
            ObjOut = new ObjectOutputStream(socket.getOutputStream());
            ObjIn = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.playerName = playerName;

        ObjOut.writeObject(playerName);

        buildGUI();

    }

    public void buildGUI() {
        setTitle("Quizzkampen - " + playerName);
        setLayout(new BorderLayout());

        add(buildCommandPanel(), BorderLayout.NORTH);
        add(buildGamePanel(), BorderLayout.CENTER);
        add(buildChatPanel(), BorderLayout.SOUTH);


        newGameButton.addActionListener(this);
        nextRoundButton.addActionListener(this);
        surrenderButton.addActionListener(this);
        showFinalResultButton.addActionListener(this);
        finishButton.addActionListener(this);
        alternativeButton_1.addActionListener(this);
        alternativeButton_2.addActionListener(this);
        alternativeButton_3.addActionListener(this);
        alternativeButton_4.addActionListener(this);
        textField.addActionListener(this);


        //pack();
        setSize(400, 400);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }


    public JPanel buildCommandPanel() {
        commandPanel.setLayout(new GridLayout(1, 3, 5, 5));

        commandPanel.add(newGameButton);
        newGameButton.setVisible(false);  // Nytt spel dolt tills oppenent ansluter
        commandPanel.add(topLabel);
        commandPanel.add(blankLabel);

        return commandPanel;
    }

    public JPanel buildGamePanel() {
        gamePanel.setLayout(new BorderLayout());
        alternativesPanel = buildAlternativesPanel();
        gamePanel.add(alternativesPanel, BorderLayout.NORTH);
        alternativesPanel.setVisible(false);
        questionPanel.add(questionLabel);
        gamePanel.add(questionPanel, BorderLayout.CENTER);
        questionResultPanel.setLayout(new FlowLayout());
        gamePanel.add(questionResultPanel, BorderLayout.SOUTH);
        return gamePanel;
    }

    public JPanel buildAlternativesPanel() {
        alternativesPanel.setLayout(new GridLayout(2, 2, 5, 5));
        alternativesPanel.add(alternativeButton_1);
        alternativesPanel.add(alternativeButton_2);
        alternativesPanel.add(alternativeButton_3);
        alternativesPanel.add(alternativeButton_4);
        return alternativesPanel;
    }

    public JPanel buildChatPanel() {
        chatPanel.setLayout(new BorderLayout());
        chatArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) chatArea.getCaret();  // Dessa två rader sätter uppdateringspolicy för
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);      // Scrollpane så nedersta raden visas

        sp.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS); //alt VERTICAL_SCROLLBAR_AS_NEEDED

        chatPanel.add(sp, BorderLayout.CENTER);
        chatPanel.add(textField, BorderLayout.SOUTH);
        return chatPanel;
    }

    public void addColors(Color[] colorArray) {
        //purple, blue, green, yellow, orange, red, gray
        Color darkest = colorArray[0];
        Color dark = colorArray[1];
        Color light = colorArray[2];
        Color lightest = colorArray[3];


        newGameButton.setBackground(darkest);
        surrenderButton.setBackground(darkest);
        nextRoundButton.setBackground(darkest);
        showFinalResultButton.setBackground(darkest);
        finishButton.setBackground(darkest);
        newGameButton.setForeground(light);
        surrenderButton.setForeground(light);
        nextRoundButton.setForeground(light);
        showFinalResultButton.setForeground(light);
        finishButton.setForeground(light);

        alternativeButton_1.setBackground(dark);
        alternativeButton_2.setBackground(dark);
        alternativeButton_3.setBackground(dark);
        alternativeButton_4.setBackground(dark);
        alternativeButton_1.setForeground(lightest);
        alternativeButton_2.setForeground(lightest);
        alternativeButton_3.setForeground(lightest);
        alternativeButton_4.setForeground(lightest);
        gamePanel.setBackground(light);
        commandPanel.setBackground(light);
        topLabel.setForeground(darkest);
        alternativesPanel.setBackground(light);
        questionResultPanel.setBackground(light);
        questionPanel.setBackground(light);
        questionLabel.setForeground(darkest);
        chatArea.setBackground(lightest);
        chatArea.setForeground(darkest);
    }

    public void updateCommandComponents(int command_int) {

        commandPanel.remove(0);             //tömmer panelen så att den kan ersättas med nya komponenter
        commandPanel.remove(0);
        commandPanel.remove(0);

        if (command_int == -1) {                            //startläge samt efter att slutresultat visats/surrender
            commandPanel.add(newGameButton, 0, 0);
            commandPanel.add(topLabel, 0, 1);
            commandPanel.add(blankLabel, 0, 2);
        }
        if (command_int == 0) {                    //Efter att man visat slutresultatet, eller när motståndaren gett upp
            commandPanel.add(blankLabel, 0, 0);
            commandPanel.add(topLabel, 0, 1);
            commandPanel.add(finishButton, 0, 2);
        }
        if (command_int == 1) {                            //medan en rond spelas
            commandPanel.add(blankLabel, 0, 0);
            commandPanel.add(topLabel, 0, 1);
            commandPanel.add(surrenderButton, 0, 2);
        }
        if (command_int == 2) {                            //när en rond är klar
            commandPanel.add(nextRoundButton, 0, 0);
            commandPanel.add(topLabel, 0, 1);
            commandPanel.add(surrenderButton, 0, 2);
        }
        if (command_int == 3) {                            //när hela spelet är klart
            commandPanel.add(blankLabel, 0, 0);
            commandPanel.add(topLabel, 0, 1);
            commandPanel.add(showFinalResultButton, 0, 2);
        }
    }

    public void setUpQuestion(List<String> list) throws InterruptedException { // Titlar sätts från fråge-listan
        updateCommandComponents(COMMAND_NEW_GAME);
        if (answerFeedback != null) {
            returnButtonColor();
        }
        alternativesPanel.setVisible(true);
        alternativeButton_1.setText(list.get(0));
        alternativeButton_2.setText(list.get(1));
        alternativeButton_3.setText(list.get(2));
        alternativeButton_4.setText(list.get(3));
        questionLabel.setText(list.get(4));
        repaint();
        revalidate();
    }

    public void returnButtonColor() throws InterruptedException {//om vi ska försöka gå tillbaka till att ändra färg på svarsknapparna
        Thread.sleep(1000);
        alternativeButton_1.setBackground(null);
        alternativeButton_2.setBackground(null);
        alternativeButton_3.setBackground(null);
        alternativeButton_4.setBackground(null);
        repaint();
        revalidate();
        answerFeedback = null;
    }

    public void newRound(String roundResult) {
        if (roundsPlayed < roundsPerGame) {             // Om inte spelet är klart
            updateCommandComponents(COMMAND_NEXT_ROUND);
            questionLabel.setText(roundResult);
            repaint();
            revalidate();
        }
        if (roundsPlayed == roundsPerGame) {            // om spelet är klart
            updateCommandComponents(COMMAND_FINAL_RESULT);
            questionLabel.setText(roundResult);         // visa rondresultat och ny knapp för hela spelets resultat
            repaint();
            revalidate();
        }
    }

    public void waitForOpponent() {          // Båda spelare hoppar hit efter en klar ronda
        // men spleare 2 hinner inte se detta innan GUI uppdatera igen
        // då resultatlistan kommer in


        alternativesPanel.setVisible(false);                //döljer panelen med knapparna
        questionLabel.setText("Väntar på motspelare");
        repaint();
        revalidate();
    }

    public void opponentSurrender() {
        alternativesPanel.setVisible(false);
        updateCommandComponents(COMMAND_CLOSE);
        questionResultPanel.setVisible(false);
        questionLabel.setText(opponentPlayerName + " gav upp spelet. Du vann!");
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
                    setUpQuestion(questionList);
                }
                if (fromServer instanceof Color[]) {     //färgerna för temat tas emot
                    colorArray = (Color[]) fromServer;
                    addColors(colorArray);
                }

                if (fromServer instanceof String[]) {                            // tar emot resultat-arrayer
                    String[] resultArray = (String[]) fromServer;

                    if (resultArray[0].equalsIgnoreCase("0")) {
                        questionsAnswered = 0;
                        roundsPlayed++;

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
                        totalGameScore = totalGameScore + Integer.parseInt(resultArray[3]);
                        opponentTotalGameScore = opponentTotalGameScore + Integer.parseInt(resultArray[5]);
                        newRound(roundResult);                                 // som kan visas i slutet av spelet
                    }

                    if (resultArray[0].equalsIgnoreCase("1")) {
                        opponentPlayerName = resultArray[1];
                    }
                }
                if (fromServer instanceof Integer) {
                    int inCommand = (int) fromServer;
                    if (inCommand == COMMAND_SURRENDER) {
                        opponentSurrender();
                    }
                    if (inCommand == COMMAND_CLOSE) {

                        return;
                    }
                    if (inCommand == 3) {
                        newGameButton.setVisible(true);

                    }
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
                ObjOut.writeObject((String) playerName + ": " + textField.getText().trim());
                ObjOut.flush();
                textField.setText("");
            }
            if (e.getSource() == newGameButton) {                   // triggar handler att skicka lista med frågor
                ObjOut.writeObject((int) COMMAND_NEW_GAME);
                ObjOut.flush();
            }
            if (e.getSource() == nextRoundButton) {                   //trigga handler att skicka nästa lista med frågor

                for (int i = 0; i < questionsPerRound; i++) {
                    questionResultPanel.remove(0);
                }
                repaint();
                revalidate();

                ObjOut.writeObject((int) COMMAND_NEXT_ROUND);
                ObjOut.flush();
            }
            if (e.getSource() == showFinalResultButton) {       //Plocka bort alla knappar och skriva ut scorelist

                updateCommandComponents(COMMAND_CLOSE);
                for (int i = 0; i < questionsPerRound; i++) {
                    questionResultPanel.remove(0);
                }

                String winner = "";
                if (totalGameScore > opponentTotalGameScore) {
                    winner = "Du vann!";
                }
                if (totalGameScore < opponentTotalGameScore) {
                    winner = opponentPlayerName + " vann!";
                }
                if (totalGameScore == opponentTotalGameScore) {
                    winner = "Oavgjort!";
                }
                // enda sättet att få ny rad i JLabel är tydligen HTML
                String finalResult = "<html>" + "<center>" + "Resultat: " + winner + "</center>" +
                        playerName + ": &emsp; &emsp; " + opponentPlayerName + ":";
                for (String s : scoreList) {                    // Hämtar resultat från ScoreList
                    finalResult = finalResult + "<center>" + s + "</center>";
                }
                finalResult = finalResult + "Totalt:   " + totalGameScore + "-" + opponentTotalGameScore + "</html>";
                questionLabel.setText(finalResult);

                repaint();
                revalidate();
            }
            if (e.getSource() == surrenderButton) {

                ObjOut.writeObject((int) COMMAND_SURRENDER);
                ObjOut.flush();

                System.exit(0); //rätt sätt att ge upp? kanske bättre med att kunna välja newGame?
            }
            if (e.getSource() == finishButton) {
                System.out.println("if finishButton");
                ObjOut.writeObject((int) COMMAND_CLOSE);
                ObjOut.flush();

            }

            if ((e.getSource() == alternativeButton_1) || (e.getSource() == alternativeButton_2)
                    || (e.getSource() == (alternativeButton_3)) || (e.getSource() == (alternativeButton_4))) {
                JButton button = (JButton) e.getSource();
                String answer = button.getText();

                questionsAnswered++;

                if (answer.equalsIgnoreCase(questionList.get(5))) {
                    JButton correct = new JButton(String.valueOf(questionsAnswered));
                    correct.setBackground(Color.GREEN);
                    questionResultPanel.add(correct);

                    repaint();
                    revalidate();

                    //  correctAnswer = true; //Anna: behövs den här variabeln? varför inte ObjOut.writeObject(true);
                    // och motsvarande i else-satsen

                    ObjOut.writeObject((int) COMMAND_CORRECT);
                    ObjOut.flush();

                }
                if (!answer.equalsIgnoreCase(questionList.get(5))) {
                    JButton inCorrect = new JButton(String.valueOf(questionsAnswered));
                    inCorrect.setBackground(Color.RED);
                    questionResultPanel.add(inCorrect);
//                    button.setBackground(Color.RED);
                    //                  answerFeedback="Fel";
                    repaint();
                    revalidate();
                    //correctAnswer = false;
                    ObjOut.writeObject((int) COMMAND_INCORRECT);
                    ObjOut.flush();
                }
                if (questionsAnswered == questionsPerRound) {  // Om man svarat antal frågor per runda
                    waitForOpponent();                      // innan motståndare får man vänta på listan
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {

        String playerName = JOptionPane.showInputDialog(null, "Ange ditt namn").toUpperCase().trim();

        Client client = new Client(playerName);
        client.play();

        while (true) {

            int playAgain = JOptionPane.showConfirmDialog(null, "Vill du starta ett nytt spel?", "Quizzkampen",
                    JOptionPane.YES_NO_OPTION);

            if (playAgain == 0) {
                client.dispose();
                client = new Client(playerName);
                client.play();
            }
            if (playAgain != 0) {
                System.exit(0);
            }
        }
    }

}
