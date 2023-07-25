import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GamePanel extends JPanel implements ActionListener {
    private RaceTrack raceTrack;
    private Kart kartBlue;
    private Kart kartWhite;

    //timer is created and started
    public GamePanel(RaceTrack raceTrack,Kart kartBlue,Kart kartWhite) {
        this.raceTrack = raceTrack;
        this.kartBlue = kartBlue;
        this.kartWhite = kartWhite;
        Timer gameTimer = new Timer(10, this); //to update game
        gameTimer.start();
    }

    //The racetrack and the karts are painted on the window
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        raceTrack.drawRaceTrack(g);
        kartBlue.kartPositioned(g);
        kartWhite.kartPositioned(g);
        repaint();
    }

    //actionPerformed repaints the window
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}

