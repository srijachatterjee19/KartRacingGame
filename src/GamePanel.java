import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GamePanel extends JPanel implements ActionListener{

    private String whichKartWonMessage = "";
    private String kartsCrashedMessage = "";

    public GamePanel()
    {
        Timer gameTimer = new Timer(100, this); //to update game
        gameTimer.start();

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
//        game.DrawRaceTrackAndKarts(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
    }
}