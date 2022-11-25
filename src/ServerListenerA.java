import java.net.ServerSocket;

public class ServerListenerA {

    public static void main(String[] args) throws Exception {

        ServerSocket listener = new ServerSocket(8902);

        try {
            while (true) {

                Game game = new Game();

                Handler player_1 = new Handler(listener.accept(), game, 1);
                Handler player_2 = new Handler(listener.accept(), game,2);



                player_1.setOpponent(player_2);
                player_2.setOpponent(player_1);
                player_1.start();
                player_2.start();
            }
        }
        finally {
            listener.close();
        }
    }
}
