import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
//          DEN HÄR SKA TAS BORT SEDAN, DET ÄR DEN JAG HADE JOBBAT MED INANN JAG PUSSLADE IHOP.
//          DET ÄR CLIENT SOM GÄLLER!! VÅGAR BARA INTE DELETA ÄN INNAN JAG SER VAD SOM FÖLJER MED UPP
public class ClientGUI extends JFrame implements ActionListener {
    JPanel commandPanel = new JPanel();
    JButton newGameButton = new JButton("Starta nytt spel");
    JButton nextRoundButton = new JButton("Nästa rond");
    JButton surrenderButton = new JButton("Ge upp");    //knapp för att avsluta spelet i förtid

    JPanel gamePanel = new JPanel();
    JPanel alternativesPanel = new JPanel();
    JButton alternativeButton_1 = new JButton(" ");
    JButton alternativeButton_2 = new JButton(" ");
    JButton alternativeButton_3 = new JButton(" ");
    JButton alternativeButton_4 = new JButton(" ");

    JPanel questionPanel = new JPanel();
    JLabel questionLabel = new JLabel(" ");

    JPanel chatPanel = new JPanel();
    JTextField textField = new JTextField(33);
    JTextArea chatArea = new JTextArea(6, 33);
    JScrollPane chatScroll = new JScrollPane(chatArea); //scrollPane för att förhindra att chatten tar upp hela spelytan

    String playerName;

    List<String> questionList;
    List<String> scoreList = new ArrayList<>();

    boolean win;
    boolean correctAnswer;

    //states för spelet
    static int Command_newGame = 1;
    static int Command_newRound = 2;
    static int Command_surrender = -1;

    ObjectInputStream ObjIn;
    ObjectOutputStream ObjOut;


    public ClientGUI() throws IOException {

        try {
            Socket socket = new Socket("127.0.0.1", 8902);
            ObjOut = new ObjectOutputStream(socket.getOutputStream());
            ObjIn = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        playerName = JOptionPane.showInputDialog(null, "Ange ditt namn").toUpperCase().trim();

        ObjOut.writeObject(playerName); //skicka namnet genom socketen

        buildGUI(); //bygger ihop grafiken

    }

    public void setUpQuestion(List<String> list) { // Titlar sätts från fråge-listan

        //  surrenderButton.setEnabled(true);
        surrenderButton.setVisible(true);
        alternativeButton_1.setBackground(Color.lightGray);
        alternativeButton_2.setBackground(Color.lightGray);
        alternativeButton_3.setBackground(Color.lightGray);
        alternativeButton_4.setBackground(Color.lightGray);


        alternativesPanel.setVisible(true);
        alternativeButton_1.setText(list.get(0));
        alternativeButton_2.setText(list.get(1));
        alternativeButton_3.setText(list.get(2));
        alternativeButton_4.setText(list.get(3));
        questionLabel.setText(list.get(4));
        repaint();
        revalidate();
    }

    public void play() {

        try {
            Object fromServer;

            while ((fromServer = ObjIn.readObject()) != null) {

                if (fromServer instanceof String) {
                    chatArea.append((String) fromServer + "\n");

                }

                if (fromServer instanceof List<?>) {    // Om en lista inkommer uppdateras knappar med nya frågor
                    questionList = (List<String>) fromServer;

                    setUpQuestion(questionList); //uppdaterar GUI med frågor och alternativ

                }

                if (fromServer instanceof String[]) {
                    String[] resultArray = (String[]) fromServer;
                    if (resultArray[0].equalsIgnoreCase("0")) {
                        String message = "";
                        // newGame();
                        if (Integer.parseInt(resultArray[1]) == 0) {
                            message = "Vinst! ";
                        } else if (Integer.parseInt(resultArray[1]) == 1) {
                            message = "Förlust! ";
                        } else if (Integer.parseInt(resultArray[1]) == 2) {
                            message = "Oavgjort! ";
                        }
                        questionLabel.setText(message + "Du fick " + resultArray[3] + " poäng. " +
                                resultArray[4] + " fick " + resultArray[5] + " poäng!");

                        scoreList.add(resultArray[3] + "-" + resultArray[5]); //lägger till rondens resultat till en lista som kan visas i slutet av spelet

                        System.out.println(scoreList.get(0));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildGUI() {
        setTitle("Quizzkampen - " + playerName);
        setLayout(new BorderLayout());
        add(buildCommandPanel(), BorderLayout.NORTH);
        add(buildGamePanel(), BorderLayout.CENTER);
        add(buildChatPanel(), BorderLayout.SOUTH);


        newGameButton.addActionListener(this);
        alternativeButton_1.addActionListener(this);
        alternativeButton_2.addActionListener(this);
        alternativeButton_3.addActionListener(this);
        alternativeButton_4.addActionListener(this);
        textField.addActionListener(this);
        nextRoundButton.addActionListener(this);
        surrenderButton.addActionListener(this);    //actionlyssnare för surrender

        //pack();
        setSize(400, 400);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public JPanel buildCommandPanel() {
        commandPanel.setLayout(new FlowLayout());
        commandPanel.add(newGameButton);
        commandPanel.add(nextRoundButton);
        commandPanel.add(surrenderButton);
        //  newGameButton.setVisible(true);
        //nextRoundButton.setVisible(false);// knappen syns inte
        //surrenderButton.setVisible(false);
        newGameButton.setEnabled(true); //knappen är aktiv och kan tryckas ner
        nextRoundButton.setEnabled(false); //knappen syns, men kan inte tryckas ner
        surrenderButton.setEnabled(false);
        return commandPanel;
    }

    public JPanel buildGamePanel() {
        gamePanel.setLayout(new BorderLayout());
        alternativesPanel = buildAlternativesPanel();
        gamePanel.add(alternativesPanel, BorderLayout.NORTH);
        alternativesPanel.setVisible(false);
        questionPanel.add(questionLabel);
        gamePanel.add(questionPanel, BorderLayout.CENTER);
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
        chatScroll.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS); //alt VERTICAL_SCROLLBAR_AS_NEEDED
        //chatPanel.add(chatArea, BorderLayout.CENTER);
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        chatPanel.add(textField, BorderLayout.SOUTH);
        return chatPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {

            if (e.getSource() == textField) {
                ObjOut.writeObject((String) playerName + ": " + textField.getText().trim());
                ObjOut.flush();
                textField.setText("");
            }
            if (e.getSource() == newGameButton) {  // triggar handler att skicka lista med frågor
                ObjOut.writeObject((int) Command_newGame);
                ObjOut.flush();

            }
            if (e.getSource() == nextRoundButton) {//trigga handler att skicka nästa lista med frågor
                ObjOut.writeObject((int) Command_newRound);
                ObjOut.flush();

                System.out.println("next round button");
            }
            if (e.getSource() == surrenderButton) {
                ObjOut.writeObject((int) Command_surrender);
                ObjOut.flush();

                System.exit(0); //rätt sätt att ge upp?
            }

            if ((e.getSource() == alternativeButton_1) || (e.getSource() == alternativeButton_2)
                    || (e.getSource() == (alternativeButton_3)) || (e.getSource() == (alternativeButton_4))) {

                JButton button = (JButton) e.getSource();
                Color bColor; //byta färg på knappen beroende på om det var rätt eller fel
                String answer = button.getText();

                if (answer.equalsIgnoreCase(questionList.get(5))) {

                    bColor=Color.GREEN;

                    correctAnswer = true; //Anna: behövs den här variabeln? varför inte ObjOut.writeObject(true);
                    // och motsvarande i else-satsen
                    ObjOut.writeObject((Boolean) correctAnswer);
                    ObjOut.flush();
                } else {

                    bColor=Color.RED;

                    correctAnswer = false;

                    ObjOut.writeObject((Boolean) correctAnswer);
                    ObjOut.flush();
                }
                button.setBackground(bColor);//färgen sätts efter om svaret var rätt/fel

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        ClientGUI c = new ClientGUI();
        c.play();
    }
}


