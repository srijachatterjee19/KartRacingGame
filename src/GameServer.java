import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/*
The Game Server is responsible for updating the co-ordinates for both the clients
input streams used to receive messages from the clients

1. after it receives the co-ordinates, it updates them
2. after it receives any messages like "karts crashed" or "kart won" it updates label on JFrame
3. the JFrame uses synchronised methods to update labels

update this for kart object and same for the other one
jpanel replaces the paint components and timer etc..
 */

public class GameServer {
    // Declare a server socket and a client socket for the server
    private int numPlayers;
    private int maxPlayers;

    private String kartType;

    private Kart kartBlue = null;
    private Kart kartWhite = null;

    private ReadFromClient ReadRunnable1;
    private ReadFromClient ReadRunnable2;
    private WriteToClient WriteRunnable1;
    private WriteToClient WriteRunnable2;

    private int direction1,direction2; //store co-ordinates to send out to other player
    private int kart1PostionX, kart1PositionY, kart2PositionX, kart2PositionY; //store co-ordinates to send out to other player

    private ServerSocket ss;
    private Socket socket1;
    private Socket socket2;
    private GameServerGui gameGui;
    private RaceTrack raceTrack;


    public GameServer() {
        System.out.println("===== GAME SERVER =====");
        numPlayers = 0;
        maxPlayers = 2;

        kart1PostionX = 380;
        kart1PositionY = 550;
        kart2PositionX = 380;
        kart2PositionY = 500;

        direction1 = 0;
        direction2 = 0;
        raceTrack = new RaceTrack();

        // Try to open a server socket on port 4000
        try {
            ss = new ServerSocket(45371);
        } catch (IOException e) {
            System.out.println("IOException from GameServer constructor");
        }
    }


    public void acceptConnections() {
        try {
            System.out.println("Waiting for connections...");

            while (numPlayers < maxPlayers) {
                Socket s = ss.accept();
                DataInputStream in = new DataInputStream(
                        s.getInputStream()
                );
                DataOutputStream out = new DataOutputStream(
                        s.getOutputStream()
                );

                ObjectOutput objectOutput = new ObjectOutputStream(
                        s.getOutputStream()
                );

                ObjectInput objectInput = new ObjectInputStream(
                        s.getInputStream()
                );

                BufferedReader inputStream = new BufferedReader(
                        new InputStreamReader(
                                s.getInputStream()
                        )
                );

                numPlayers++;
                out.writeInt(numPlayers);
                System.out.println("Player #" + numPlayers + "has connected.");

                //after accepting connections,we would like to create the read and write from client objects
                ReadFromClient rfc = new ReadFromClient(numPlayers, in, objectInput, inputStream);
                WriteToClient wtc = new WriteToClient(numPlayers, out, objectOutput);
                //if you are the first player to connect
                if (numPlayers == 1) {
                    kartType = "BlueKart";
                    kartBlue = new Kart("BlueKart", 0, 0, 380, 550);
                    socket1 = s;
                    ReadRunnable1 = rfc;
                    WriteRunnable1 = wtc;
                } else {
                    kartType = "WhiteKart";
                    kartWhite = new Kart("WhiteKart", 0, 0, 380, 500);
                    socket2 = s;
                    ReadRunnable2 = rfc;
                    WriteRunnable2 = wtc;
                    //send start message belongs to the write to client object
                    //we want to send the start message after the second client has connected
                    WriteRunnable1.sendStartMsg();
                    WriteRunnable2.sendStartMsg();

                    //after both players have connected we can start the threads for reading and writing to the client
                    Thread readThread1 = new Thread(ReadRunnable1);
                    Thread readThread2 = new Thread(ReadRunnable2);
                    readThread1.start();
                    readThread2.start();

                    Thread writeThread1 = new Thread(WriteRunnable1);
                    Thread writeThread2 = new Thread(WriteRunnable2);
                    writeThread1.start();
                    writeThread2.start();
                }
            }
            System.out.println("No longer accepting connections");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setUpGui() {
        //Create server game gui and pass the current instance and make JFrame visible
        gameGui = new GameServerGui(this);
        gameGui.setPreferredSize(new Dimension(850, 750));
        gameGui.setTitle("Game Server");
        GamePanel panel = new GamePanel();
        gameGui.add(panel);
        gameGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameGui.pack();
        gameGui.setVisible(true);
    }

    public class GamePanel extends JPanel implements ActionListener {

        private String whichKartWonMessage = "";
        private String kartsCrashedMessage = "";

        public GamePanel() {
            Timer gameTimer = new Timer(10, this); //to update game
            gameTimer.start();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            raceTrack.drawRaceTrack(g);
            kartBlue.kartPositioned(g);
            kartWhite.kartPositioned(g);
            repaint();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            repaint();
        }
    }

    public class GameServerGui extends JFrame {
        public GameServerGui(GameServer gameServer) {
            setTitle("Game Server");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(850, 650);
            setVisible(true);
        }

        //call in server to update
        public synchronized void updateGame() {
//            System.out.println("update to jframe received");
        }
    }


    //send out the co-ordinates
    //while loop to ensure co-ordinates are constantly updated
    //use sleep to not overwhelm the thread
    public class WriteToClient implements Runnable {
        private int playerID;
        private DataOutputStream dataOut;
        private ObjectOutput objOut;

        public WriteToClient(int pid, DataOutputStream out, ObjectOutput objectOutput) {
            playerID = pid;
            dataOut = out;
            objOut = objectOutput;
            System.out.println("WTC" + playerID + " Runnable created");

        }

        //then go to player frame to receives info sent by server
        public void run() {
            try {
                while (true) {
                    //send and receive the karts
                    sendEnemyKart();
//                    sendForeignKart();

                }
            } catch (IOException ex) {
                System.out.println("IOException from RFC run ()");
            }
        }

        //send start message
        public void sendStartMsg() {
            try {
                dataOut.writeUTF("We now have 2 players. Go!");
            } catch (IOException ex) {
                System.out.println("IOException from sendStartMsg()");
            }
        }

        public void sendEnemyKart() throws IOException {
            if (playerID == 1) {
                dataOut.writeInt(kart2PositionX);
                dataOut.writeInt(kart2PositionY);
                dataOut.writeInt(direction2);
                dataOut.flush();
            } else {
                dataOut.writeInt(kart1PostionX);
                dataOut.writeInt(kart1PositionY);
                dataOut.writeInt(direction1);
                dataOut.flush();
            }
        }

        private String sendMessage(String message) {

            try {
                dataOut.writeBytes(message + "\n");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return message;
        }
    }

    //gets the co-ordinates from client
    // receives the co-ordinates
    public class ReadFromClient implements Runnable {
        private int playerID;
        private String k;//kart type
        private DataInputStream dataIn;
        private ObjectInput objIn;
        private BufferedReader inp;

        public ReadFromClient(int pid, DataInputStream in, ObjectInput objectInput, BufferedReader inputStream) {
            playerID = pid;
            dataIn = in;
            objIn = objectInput;
            inp = inputStream;
            System.out.println("RFC" + playerID + " Runnable created");
        }

        public void run() {
            try {
                while (true) {
                    updateKarts();
                }
            } catch (IOException ex) {
                System.out.println("IOException from RFC run ()");
            }
        }

        public void updateKarts() throws IOException {
            //receive kart objects and set it to player 1
            if (playerID == 1) {
                kart1PostionX = dataIn.readInt();
                kart1PositionY = dataIn.readInt();

                direction1 = dataIn.readInt();
                kartBlue.setPositionX(dataIn.readInt());
                kartBlue.setPositionY(dataIn.readInt());
                kartBlue.setKartDirection(dataIn.readInt());
            } else {
                kart2PositionX = dataIn.readInt();
                kart2PositionY = dataIn.readInt();
                direction2 = dataIn.readInt();


                kartWhite.setPositionX(dataIn.readInt());
                kartWhite.setPositionY(dataIn.readInt());
                kartWhite.setKartDirection(dataIn.readInt());
            }
        }

        private String receiveMessage() {
            try {
                return inp.readLine();
            } catch (Exception e) {
                System.out.println("Exception from GameServer receiveMessage()");
                System.out.println(e.getMessage());
                return null;
            }
        }
    }


    public static void main(String[] args) {
        GameServer gs = new GameServer();
        gs.acceptConnections();
        gs.setUpGui();
    }
}

//for the coordinates to be sent to the client through server, need 2 runnables, with 2 threads
//each player ends up with 4
//after setting up the runnables ,ready to send the coordinates of the players