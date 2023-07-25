import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.geom.Line2D;


public class Game {
    private String winStatus = "";
    private Boolean crash = false;

    RaceTrack raceTrack = new RaceTrack();
    Line2D outer_bottom = new Line2D.Double(50,600,800,600);
    Line2D outer_right = new Line2D.Double(800,100,800,600);
    Line2D outer_top = new Line2D.Double(50,100,800,100);
    Line2D outer_left = new Line2D.Double(50,100,50,600);


    //kart's interaction with the racetrack boundaries and grass
    public void kartsCollidesWithRaceTrack(Kart kart)
    {
        kart.kartHitsOuterRaceTrack(outer_bottom,outer_right,outer_top,outer_left); // blue kart collides with outer edge
        kart.kartHitsRaceTrack(50, 600, 750, 20); //hits bottom of outer track
        //karts hit the grass area
        kart.kartHitsRaceTrack(raceTrack.GRASS_X, raceTrack.GRASS_Y, raceTrack.GRASS_WIDTH, raceTrack.GRASS_HEIGHT);
    }

    //when karts crash, set speed to 0 and play kart crash sound
    public Boolean kartsCrashed(Kart kart1,Kart kart2){
        if (kart1.getKartRectangle().intersects(kart2.getKartRectangle())){
            kart1.setKartSpeed(0);
            kart2.setKartSpeed(0);
            Clip clip = kart1.loadKartSound("/Audio/car-explosion.wav","crash"); // Load the sound file
            crash = true;
        }
        return crash;
    }

    //when one of the kart wins,returns message stating which kart wins
    public String hasWon(Kart kart1,Kart kart2){
        //count laps
        int blueCounter = 0;
        int whiteCounter = 0;

        Rectangle rectangle1 = new Rectangle(700,500,100,100);
        Rectangle rectangle2 = new Rectangle(700,100,100,100);
        Rectangle rectangle3 = new Rectangle(50,100,100,100);
        Rectangle rectangle4 = new Rectangle(50,500,100,100);

        blueCounter = kart1.lapCounter(rectangle1,rectangle2,rectangle3,rectangle4);
        whiteCounter = kart2.lapCounter(rectangle1,rectangle2,rectangle3,rectangle4);

        if (blueCounter > whiteCounter && blueCounter == 4) {
            winStatus = "Blue kart won the race!!!";
            System.out.println("Blue kart won the race!!!!");
        }
        else if (whiteCounter > blueCounter && whiteCounter == 4) {
            winStatus = "White Kart won the race!!!";
            System.out.println("White car won the race!!!!");
        }
        return winStatus;
    }
}