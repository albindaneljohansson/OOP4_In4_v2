import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Handler extends Thread {

    int playerNumber;
    String playerName;
    Handler opponent;
    Socket socket;
    ObjectInputStream ObjIn;
    ObjectOutputStream ObjOut;
    Game game;

    int questionsPerRound;

    int questionsAsked = 0;
    int numberOfRounds;
    int roundsPlayed = 0;

    int roundScore = 0;     //sammanställning av poäng per rond

    List<Questions> currentQuestionList = new ArrayList<>();

    Color[] colors;
    String colorTheme;


    public Handler(Socket socket, Game game, int playerNumber) {

        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream("src/colorTheme.properties"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.playerNumber = playerNumber;
        this.socket = socket;
        this.game = game;
        this.numberOfRounds = game.getNumberOfRounds();
        this.colorTheme = properties.getProperty("color");

        setColors(colorTheme);

        try {
            ObjOut = new ObjectOutputStream(socket.getOutputStream());
            ObjIn = new ObjectInputStream(socket.getInputStream());

            this.playerName = (String) ObjIn.readObject();

            if (playerNumber == 1) {
                game.setFullGameList();
            }
            this.numberOfRounds = game.getNumberOfRounds();         // Vi måste sätta antalet rounds för båda spelare
            this.questionsPerRound = game.getQuestionsPerRound();

            ObjOut.writeObject((String) "Välkommen " + playerName);
            ObjOut.flush();

            ObjOut.writeObject(colors);
            ObjOut.flush();

            if (playerNumber == 1) {
                ObjOut.writeObject((String) "Väntar på en motspelare...");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void setOpponent(Handler opponent) {
        this.opponent = opponent;
    }

    public Handler getOpponent() {
        return opponent;
    }

    public void run() {
        try {
            if (playerNumber == 1) {
                ObjOut.writeObject((String) "Motspelare ansluten!");
            }
            ObjOut.writeObject((String) "Du spelar mot: " + opponent.playerName);
            ObjOut.flush();

            String[] opponentPlayerName = new String[]{"1", opponent.playerName};
            ObjOut.writeObject((String[]) opponentPlayerName);
            ObjOut.flush();

            ObjOut.writeObject((int) 1);
            ObjOut.flush();

            currentQuestionList = game.getOneRoundList(roundsPlayed);

            while (true) {

                if (getOpponent() != null) {                // Denna if-sats villkorar chat och spel till när det finns en opponent


                    Object objectIn = ObjIn.readObject();

                    if (objectIn instanceof Integer) {
                        int command = (int) objectIn;

                        if (command == -1) {                   //surrender
                            opponent.ObjOut.writeObject((int) -1);
                            ObjOut.flush();
                        }
                        if (command == 0) {
                            System.out.println("command==0");
                            ObjOut.writeObject((int) 0);
                            ObjOut.flush();
                        }
                        if (command == 1) {                 // Första frågan har index 0 eftersom command 1 är nytt spel
                            ObjOut.writeObject(currentQuestionList.get(questionsAsked).questionsAndAnswersList);
                            ObjOut.flush();
                        }
                        if (command == 2) {                    // Next-roundknappen
                            roundsPlayed++;
                            questionsAsked = 0;
                            roundScore = 0;
                            currentQuestionList = game.getOneRoundList(roundsPlayed);
                            ObjOut.writeObject(currentQuestionList.get(questionsAsked).questionsAndAnswersList);//ger indexOutOfBounds
                            ObjOut.flush();
                        }

                        if (command >= 10) {   // Varje gång clienten svarat på en fråga hoppar vi in här

                            if (command == 10) {  //rätt svar
                                roundScore++;
                            }
                            questionsAsked++;

                            if (questionsAsked < questionsPerRound) {     // Om antalet frågor är mindre än frågor per rond skickar vi nästa fråga
                                ObjOut.writeObject(currentQuestionList.get(questionsAsked).questionsAndAnswersList);
                                ObjOut.flush();
                            }

                            if (questionsAsked == questionsPerRound) {      // När alla frågor är besvarade i en round

                                while ((opponent.questionsAsked < questionsPerRound) || (opponent.roundsPlayed < roundsPlayed)) { //Väntar på oppenent
                                    Thread.sleep(100);                                                          // Både för i round och för varje round
                                    if (command == -1) {                   //surrender
                                        opponent.ObjOut.writeObject((int) -1);
                                        ObjOut.flush();
                                    }
                                }
                                String[] resultArray = new String[6];
                                String result = "";

                                if (roundScore > opponent.roundScore) {
                                    result = "0";
                                }
                                if (roundScore < opponent.roundScore) {
                                    result = "1";
                                }
                                if (roundScore == opponent.roundScore) {
                                    result = "2";
                                }
                                resultArray = new String[]{"0", result, playerName, String.valueOf(roundScore), opponent.playerName, String.valueOf(opponent.roundScore)};
                                ObjOut.writeObject((String[]) resultArray);
                                ObjOut.flush();

                                game.setGameScore(playerNumber, roundScore); // plussa på poäng per runda i Game

                                if (roundsPlayed == numberOfRounds) {   // när runderna är slut, skicka till client att visa gameresult
                                    ObjOut.writeObject((Integer) 99);        // command 99 = hos client: visa gameresult-lista
                                    ObjOut.flush();
                                }
                            }
                        }
                    }

                    if (objectIn instanceof String) {
                        String input = objectIn.toString().trim();
                        ObjOut.writeObject((String) input);
                        opponent.ObjOut.writeObject((String) input);
                        ObjOut.flush();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setColors(String colorTheme) {
        Color darkest;
        Color dark;
        Color light;
        Color lightest;

        if (colorTheme.equalsIgnoreCase("purple")) {
            darkest = new Color(68, 50, 102);
            dark = new Color(140, 72, 159);
            light = new Color(195, 195, 229);
            lightest = new Color(241, 240, 255);
        } else if (colorTheme.equalsIgnoreCase("blue")) {
            darkest = new Color(0, 51, 102);
            dark = new Color(51, 102, 153);
            light = new Color(124, 181, 239);
            lightest = new Color(218, 238, 255);
        } else if (colorTheme.equalsIgnoreCase("green")) {
            darkest = new Color(0, 85, 2);
            dark = new Color(57, 145, 59);
            light = new Color(179, 222, 164);
            lightest = new Color(229, 252, 223);
        } else if (colorTheme.equalsIgnoreCase("yellowOrange")) {
            darkest = new Color(255, 102, 0);
            dark = new Color(255, 153, 0);
            light = new Color(255, 255, 102);
            lightest = new Color(255, 255, 190);
        } else if (colorTheme.equalsIgnoreCase("redPink")) {
            darkest = new Color(153, 0, 51);
            dark = new Color(218, 50, 107);
            light = new Color(238, 113, 155);
            lightest = new Color(246, 193, 212);
        } else {
            darkest = Color.BLACK;
            dark = Color.GRAY;
            light = Color.LIGHT_GRAY;
            lightest = Color.WHITE;
        }
        colors = new Color[]{darkest, dark, light, lightest};
    }
}