package Shared;

import javax.swing.*;
import java.awt.*;

//JComponent will draw the karts and the racetrack and keep re-painting it
public class DrawingComponent extends JComponent {

    private RaceTrack raceTrack;
    private Kart kart1;
    private Kart kart2;

    public DrawingComponent(RaceTrack raceTrack,Kart kart1,Kart kart2){
        this.raceTrack = raceTrack;
        this.kart1 = kart1;
        this.kart2 = kart2;
    }
    public void paintComponent(Graphics g) {
        raceTrack.drawRaceTrack(g);
        kart1.kartPositioned(g);
        kart2.kartPositioned(g);
        repaint();
    }
}