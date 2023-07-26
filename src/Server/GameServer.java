package Server;

import Shared.Game;
import Shared.Kart;
import Shared.RaceTrack;
import Shared.DrawingComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
/*
The Shared.Game Server is responsible for updating the co-ordinates for both the players
input streams used to receive messages from the clients after it receives the co-ordinates, it updates them
 */

public class GameServer {
    // Declare a server socket and a client socket for the server
    private int numPlayers;
    private int maxPlayers;

    //Server JFrame
    private GameServerGui gameGui;
    private Container contentPane;
    private DrawingComponent dc;
    private Timer animationTimer;

    //kart objects
    private Kart kartBlue = null;
    private Kart kartWhite = null;
    private RaceTrack raceTrack;
    private Game game;

    //Runnables to read and write to the players
    private ReadFromClient ReadRunnable1;
    private ReadFromClient ReadRunnable2;
    private WriteToClient WriteRunnable1;
    private WriteToClient WriteRunnable2;

    private int kart1Direction, Kart2Direction; //store kart direction for each player to send out to other player
    private int kart1PostionX, kart1PositionY, kart2PositionX, kart2PositionY; //store kart co-ordinates for each player to send out to other player

    //server socket and sockets for each client
    private ServerSocket serverSocket;
    private Socket socket1;
    private Socket socket2;


    public GameServer() {
        System.out.println("===== GAME SERVER =====");
        //initialise number of players and maximum number of players
        numPlayers = 0;
        maxPlayers = 2;

        //initialise kart-coordinates for each kart
        kart1PostionX = 380;
        kart1PositionY = 550;
        kart2PositionX = 380;
        kart2PositionY = 500;

        //initialise direction of each kart
        kart1Direction = 0;
        Kart2Direction = 0;
        raceTrack = new RaceTrack();
        game = new Game();
        // Try to open a server socket on port 45371
        try {
            serverSocket = new ServerSocket(45371);
        } catch (IOException e) {
            System.out.println("IOException from Server.GameServer constructor");
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("Waiting for connections...");

            //if number of players joined is 2, then create input,output,object input,object output streams to
            // send out information to the players
            while (numPlayers < maxPlayers) {
                Socket s = serverSocket.accept();
                DataInputStream in = new DataInputStream(
                        s.getInputStream()
                );
                DataOutputStream out = new DataOutputStream(
                        s.getOutputStream()
                );

                numPlayers++;
                //numPlayers will update to 1 after first player joins,
                // it send out player number to the other player
                out.writeInt(numPlayers);

                //server updates which player has joined
                System.out.println("Player #" + numPlayers + "has connected.");

                //after accepting connections,we would like to create the read and write from client objects
                //numplayers is the playerID for the current player the runnable is being created for
                ReadFromClient rfc = new ReadFromClient(numPlayers, in);
                WriteToClient wtc = new WriteToClient(numPlayers, out);

                //client handler object

                //if you are the player ID is 1, create kart objects,assign runnables and socket
                if (numPlayers == 1) {
                    kartBlue = new Kart("BlueKart", 0, 0, 380, 550);
                    socket1 = s;
                    ReadRunnable1 = rfc;
                    WriteRunnable1 = wtc;
                } else {
                    kartWhite = new Kart("WhiteKart", 0, 0, 380, 500);
                    socket2 = s;
                    ReadRunnable2 = rfc;
                    WriteRunnable2 = wtc;

                    //send start message to the players "We now have 2 players. Go!"
                    //we want to send the start message after the second client has connected
                    //The player waits for the start message, then starts the player threads
                    WriteRunnable1.sendStartMsg();
                    WriteRunnable2.sendStartMsg();

                    //after both players have connected we can start the threads for reading and writing to the both players
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

    public void setUpGUI(){
        gameGui = new GameServerGui(this);
        gameGui.setPreferredSize(new Dimension(850, 750));
        contentPane = gameGui.getContentPane();
        contentPane.setPreferredSize(new Dimension(850, 650));
        dc = new DrawingComponent(raceTrack,kartBlue,kartWhite);
        contentPane.add(dc);
        gameGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameGui.pack();
        gameGui.setVisible(true);
        setUpAnimationTimer();
    }

    //the game visuals are updated here such as when the kart starts moving, collide with racetrack boundary or crash into other kart
    public void setUpAnimationTimer() {
        int interval = 10;
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                dc.repaint();
            }
        };
        animationTimer = new Timer(interval, al);
        animationTimer.start();
    }

    //send out the co-ordinates
    //while loop to ensure co-ordinates are constantly updated
    //use sleep to not overwhelm the thread **
    public class WriteToClient implements Runnable {
        private int playerID;
        private DataOutputStream dataOut;

        public WriteToClient(int pid, DataOutputStream out) {
            playerID = pid;
            dataOut = out;
            System.out.println("WTC" + playerID + " Runnable created");

        }

        //then go to player frame to receives info sent by server
        public void run() {
            try {
                while (true) {
                    //send and receive the karts
                    sendEnemyKart();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
            } catch (IOException ex) {
                System.out.println("IOException from RFC run ()");
            }
        }

        //send start message to client to start their threads
        public void sendStartMsg() {
            try {
                dataOut.writeUTF("We now have 2 players. Go!");
            } catch (IOException ex) {
                System.out.println("IOException from sendStartMsg()");
            }
        }

        //send co-ordinates of karts to each player, player 2 receives kart1 updates and player 1 receives kart 2
        public void sendEnemyKart() throws IOException {
            if (playerID == 1) {
                dataOut.writeInt(kart2PositionX);
                dataOut.writeInt(kart2PositionY);
                dataOut.writeInt(Kart2Direction);
                dataOut.flush();
            } else {
                dataOut.writeInt(kart1PostionX);
                dataOut.writeInt(kart1PositionY);
                dataOut.writeInt(kart1Direction);
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
    //receives the co-ordinates from client
    public class ReadFromClient implements Runnable {
        private int playerID;
        private DataInputStream dataIn;

        public ReadFromClient(int pid, DataInputStream in) {
            playerID = pid;
            dataIn = in;
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

        //updates the karts in its own game window
        public void updateKarts() throws IOException {
            //receive kart objects and set it to player 1
            if (playerID == 1) {
                kart1PostionX = dataIn.readInt();
                kart1PositionY = dataIn.readInt();
                kart1Direction = dataIn.readInt();

                kartBlue.setPositionX(dataIn.readInt());
                kartBlue.setPositionY(dataIn.readInt());
                kartBlue.setKartDirection(dataIn.readInt());
            } else {
                kart2PositionX = dataIn.readInt();
                kart2PositionY = dataIn.readInt();
                Kart2Direction = dataIn.readInt();

                kartWhite.setPositionX(dataIn.readInt());
                kartWhite.setPositionY(dataIn.readInt());
                kartWhite.setKartDirection(dataIn.readInt());
            }
        }

        private String receiveMessage() {
            try {
                return dataIn.readLine();
            } catch (Exception e) {
                System.out.println("Exception from Server.GameServer receiveMessage()");
                System.out.println(e.getMessage());
                return null;
            }
        }
    }

    public static void main(String[] args) {
        GameServer gs = new GameServer();
        gs.acceptConnections();
        gs.setUpGUI();
    }
}
