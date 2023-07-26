package Server;

import javax.swing.*;

public class GameServerGui extends JFrame {
    public GameServerGui(GameServer gameServer) {
        setTitle("Shared.Game Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 650);
        setVisible(true);
    }

    //call in server to update
    //this method will be called from the run() to update the labels when karts win/crash
    public synchronized void updateGame() {
//            System.out.println("update to jframe received");
    }
}