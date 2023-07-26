package Player;

import javax.swing.*;

public class PlayerFrameGui extends JFrame {
    private int playerID;

    public PlayerFrameGui(PlayerServer playerFrame, int playerID) {
        this.playerID = playerID;
        setTitle("Player #" + playerID);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 650);
        setVisible(true);
    }
}