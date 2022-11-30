import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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


    public Handler(Socket socket, Game game, int playerNumber) {

        this.playerNumber = playerNumber;
        this.socket = socket;
        this.game = game;
        this.numberOfRounds=game.getNumberOfRounds();

        try {
            ObjOut = new ObjectOutputStream(socket.getOutputStream());
            ObjIn = new ObjectInputStream(socket.getInputStream());

            this.playerName = (String) ObjIn.readObject();

            if (playerNumber == 1) {
                game.setFullGameList();
            }
            this.numberOfRounds = game.getNumberOfRounds();         // Vi måste sätta antalet rounds för båda spelare
            this.questionsPerRound = game.getQuestionsPerRound();

            ObjOut.writeObject((String) "Welcome " + playerName);
            ObjOut.writeObject((String) "Waiting for an opponent to connect...");

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
            ObjOut.writeObject((String) "Opponent connected!");
            ObjOut.writeObject((String) "You are playing against: " + opponent.playerName);
            ObjOut.flush();

            String[] opponentPlayerName = new String[]{"1", opponent.playerName};
            ObjOut.writeObject((String[]) opponentPlayerName);
            ObjOut.flush();

            ObjOut.writeObject((int) 3);
            ObjOut.flush();

            currentQuestionList = game.getOneRoundList(roundsPlayed);

            while (true) {

                if (getOpponent() != null) {                // Denna if-sats villkorar chat och spel till när det finns en opponent


                    Object objectIn = ObjIn.readObject();

                    if (objectIn instanceof Integer) {
                        int command = (int) objectIn;

                        if (command == -1){                   //surrender
                            opponent.ObjOut.writeObject((int) -1);
                            ObjOut.flush();
                        }
                        if (command == 1) {                 // Första frågan har index 0 eftersom command 1 är nytt spel
                            ObjOut.writeObject(currentQuestionList.get(questionsAsked).questionsAndAnswersList);
                            ObjOut.flush();
                        }
                        if (command == 2){                    // Next-roundknappen
                            roundsPlayed++;
                            questionsAsked = 0;
                            roundScore = 0;
                            currentQuestionList = game.getOneRoundList(roundsPlayed);
                            ObjOut.writeObject(currentQuestionList.get(questionsAsked).questionsAndAnswersList);//ger indexOutOfBounds
                            ObjOut.flush();
                        }

                        if (command >= 10) {   // Varje gång clienten svarat på en fråga hoppar vi in här

                            if (command==10) {  //rätt svar
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
}