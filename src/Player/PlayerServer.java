package Player;

import Shared.Game;
import Shared.Kart;
import Shared.RaceTrack;
import Shared.DrawingComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/*
The Player Server is responsible for updating the co-ordinates of other player
The actionPerformed is used to update the game in its own window and the own player kart is moved using key's pressed by player
input streams used to receive messages from the server, after it receives the co-ordinates, it updates the co-ordinates and direction for the other player kart
 */

public class PlayerServer {
    private static String kartType;
    private RaceTrack raceTrack;
    private Game game;
    private Container contentPane;
    private DrawingComponent dc;
    private Timer animationTimer;


    private static Kart ownKart;
    private static Kart foreignKart;

    private static String responseLine = null;
    private ReadFromServer rfsRunnable;
    private WriteToServer wtsRunnable;
    private Socket clientSocket;
    private int playerID;
    private PlayerFrameGui gameGui;

    private static String serverHost = "localhost";


    public PlayerServer() {
        raceTrack = new RaceTrack();
        game = new Game();
    }

    public void connectToServer() {
        try {
            clientSocket = new Socket (serverHost, 45371);

            DataInputStream in = new DataInputStream(
                    clientSocket.getInputStream ()
            );
            DataOutputStream out = new DataOutputStream(
                    clientSocket.getOutputStream()
            );

            playerID = in.readInt(); //get playerID
            System.out.println("You are player#" + playerID);
            if(playerID == 1) {
                System.out.println("Waiting for Player #2 to connect...");
            }

            //get to see the feedback of the runnables getting created
            rfsRunnable = new ReadFromServer(in);
            wtsRunnable = new WriteToServer(out);
            rfsRunnable.waitForStartMsg();

        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + serverHost);
        } catch(IOException ex) {
            System.out.println("IOException from connectToServer()");
            System.out.println(ex.getMessage());
        }
    }

    public void createKarts() {
        // initialise our client's own kart object and other player kart
        if(playerID == 1) {
            ownKart = new Kart("BlueKart",0,0,380,550);
            foreignKart = new Kart("WhiteKart",0,0,380,500);
        } else {
            foreignKart = new Kart("BlueKart",0,0,380,550);
            ownKart = new Kart("WhiteKart",0,0,380,500);
        }
    }

    public void setUpGUI() {
        createKarts(); //create the kart objects
        gameGui = new PlayerFrameGui(this,playerID);
        contentPane = gameGui.getContentPane();
        gameGui.setTitle("Player #" + playerID);
        gameGui.setPreferredSize(new Dimension(850, 750));
        contentPane.setPreferredSize(new Dimension(850, 650));
        dc = new DrawingComponent(raceTrack,ownKart,foreignKart);
        contentPane.add(dc);
        gameGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameGui.pack();
        gameGui.setVisible(true);
        setUpKeyListener();
        setUpAnimationTimer();
    }

    //the game visuals are updated here such as when the kart starts moving, collide with racetrack boundary or crash into other kart
    public void setUpAnimationTimer() {
        int interval = 10;
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                ownKart.moveForward();
                game.kartsCollidesWithRaceTrack(ownKart);
                game.kartsCrashed(ownKart,foreignKart);
                game.kartsCrashed(ownKart,foreignKart);
                dc.repaint();
            }
        };
        animationTimer = new Timer(interval, al);
        animationTimer.start();
    }
    //the keys to control own kart is assigned here
    public void setUpKeyListener() {
        KeyListener kl = new KeyListener() {
            public void keyTyped(KeyEvent ke) {
            }
            public void keyPressed (KeyEvent ke) {
                int keyCode = ke.getKeyCode();

                if(playerID == 1) {
                    switch (keyCode) {
                        case KeyEvent.VK_UP:
                            ownKart.increaseSpeed();
                            break;
                        case KeyEvent.VK_DOWN:
                            ownKart.decreaseSpeed();
                            break;
                        case KeyEvent.VK_LEFT:
                            ownKart.turnLeft();
                            break;
                        case KeyEvent.VK_RIGHT:
                            ownKart.turnRight();
                            break;
                    }
                }else{
                    switch (keyCode) {
                        case KeyEvent.VK_W:
                            ownKart.increaseSpeed();
                            break;
                        case KeyEvent.VK_S:
                            ownKart.decreaseSpeed();
                            break;
                        case KeyEvent.VK_A:
                            ownKart.turnLeft();
                            break;
                        case KeyEvent.VK_D:
                            ownKart.turnRight();
                            break;
                    }
                }
            }
            public void keyReleased (KeyEvent ke) {
            }
        };
        contentPane.addKeyListener(kl);
        contentPane.setFocusable(true);
    }

    public class ReadFromServer implements Runnable {
        private DataInputStream dataIn;

        public ReadFromServer(DataInputStream in) {
            dataIn = in;
            System.out.println("RFS Runnable created");
        }

        //constantly receive the values
        public void run () {
            try {
                while(true) {
                    receiveEnemyKart();
                }
            } catch (IOException ex) {
                System.out.println("IOException from RFS run ()");
            }
        }
        //message from server that notifies clients we can start
        //we want second player to connect before we send that string
        public void waitForStartMsg() {
            try {
                String startMsg = dataIn.readUTF();
                System.out.println("Message from server: " + startMsg);
                Thread readThread = new Thread(rfsRunnable);
                Thread writeThread = new Thread (wtsRunnable);
                readThread.start();
                writeThread.start();
            } catch (IOException ex) {
                System.out.println("IOException from waitForStartMsg()");
            }
        }

        // reading information from the Shared.Game server to update
        // other player's kart co-ordinate and direction
        public void receiveEnemyKart() throws IOException {

            int foreignX = dataIn.readInt();
            int foreignY = dataIn.readInt();
            int foreignDirection = dataIn.readInt();

            if(foreignKart != null){
                foreignKart.setPositionX(foreignX);
                foreignKart.setPositionY(foreignY);
                foreignKart.setKartDirection(foreignDirection);
            }
        }
    }

    public class WriteToServer implements Runnable {
        private DataOutputStream dataOut;

        public WriteToServer(DataOutputStream out) {
            dataOut = out;
            System.out.println("WTS Runnable created");
        }

        public void run() {
            //while loop to ensure coordinates are constantly updated
            //use sleep to not overwhelm the thread
            try {
                while (true) {//
                    sendOwnKart();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }

                }
            } catch (IOException ex) {
                System.out.println("IOException from WTS run ()");
            }

        }

        //send own kart co-ordinates to the Shared.Game Server
        public void sendOwnKart() throws IOException {
            if (ownKart != null) {
                dataOut.writeInt(ownKart.getPositionX());
                dataOut.writeInt(ownKart.getPositionY());
                dataOut.writeInt(ownKart.getKartDirection());
                dataOut.flush();
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                System.out.println("InterruptedException from WTS run ()");
            }
        }
    }

    public static void main(String[] args) {
        PlayerServer playerServer = new PlayerServer();
        String errorMessage = "Shared.Kart designation need to be provided as either 'blue' or 'white'.";

        if (args.length == 1) {
            kartType = args[0];

            if (!kartType.equals("WhiteKart") && !kartType.equals("BlueKart")) {
                System.err.println(errorMessage);
                return;
            }
        } else {
            System.err.println(errorMessage);
            return;
        }
        playerServer.connectToServer();
        //set up gui after connection to server is successful
        playerServer.setUpGUI();
    }
}