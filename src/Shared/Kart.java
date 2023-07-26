package Shared;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.Serializable;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


public class Kart  implements Serializable {
    private String kartColour;
    private int kartSpeed;
    private int kartDirection;
    private int positionX;
    private int positionY;
    private ImageIcon kartImages[];
    private int kartWidth;
    private int kartHeigth;

    public Kart(String kartColour, int kartSpeed,int kartDirection,int positionX,int positionY )
    {
        this.kartColour = kartColour;
        this.kartSpeed = kartSpeed;
        this.kartDirection = kartDirection;
        this.positionX = positionX;
        this.positionY = positionY;
        this.kartImages = getKartImages();
    }
    public String getKartColour(){return kartColour;}
    public int getKartSpeed(){return kartSpeed;}
    public void setKartSpeed(int speed){this.kartSpeed = speed;}
    public int getKartDirection(){return kartDirection;}
    public void setKartDirection(int kartDirection){this.kartDirection = kartDirection;}
    public int getPositionX(){return positionX;}
    public void setPositionX(int x){this.positionX = x;}
    public int getPositionY(){return positionY;}
    public void setPositionY(int y){this.positionY = y;}

    public void updatePositionX(double x) {positionX += x;}
    public void updatePositionY(double y) {
        positionY += y;
    }

    //this method is used to add soundtracks when kart crashes or starts accelerating
    public Clip loadKartSound(String soundFilePath,String type) {
        try {
            File soundFile = new File(soundFilePath);

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource(soundFilePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            if (type == "crash") {
                clip.start(); // Play the sound
                Thread.sleep(10000); // Wait for 1 second
                clip.stop();
                clip.close();
                audioInputStream.close();
            }
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //get that kart images and add them to their respective arrays
    public ImageIcon[] getKartImages() {
        kartImages = new ImageIcon[16];
        // load the car images into their arrays
        for (int count = 0; count < 16; count++) {
            kartImages[count] = new ImageIcon(getClass().getResource("/Images/" + this.getKartColour() + count + ".png"));
        }
        return kartImages;
    }

    public int getKartWidth(){
        this.kartWidth = this.kartImages[this.kartDirection].getIconWidth();
        return kartWidth;
    }

    public int getKartHeight(){
        this.kartHeigth = this.kartImages[this.kartDirection].getIconWidth();
        return kartHeigth;
    }
    public void kartPositioned(Graphics g) {
        kartImages[this.kartDirection].paintIcon(null, g, this.positionX, this.positionY);
    }

    //method used to make the karts move around the racetrack
    public void moveForward() {
        double angle = Math.toRadians(this.getKartDirection() * 22.5);
        updatePositionX(getKartSpeed() * Math.cos(angle));
        updatePositionY(getKartSpeed() * Math.sin(angle));
        if (getKartSpeed() > 0) {
            Clip clip = this.loadKartSound("/Audio/car-moving.wav","none"); // Load the sound file
            clip.start(); // Play the sound
        }
    }

    //used to increase speed of kart
    public void increaseSpeed(){
        //if speed is more than 0 and less than 100
        //increase speed by 5
        if (this.getKartSpeed() >= 0 && this.getKartSpeed() <= 100) {
            this.setKartSpeed(this.getKartSpeed() + 4);
        }
    }

    //used to decrease speed of kart
    public void decreaseSpeed(){
        //if speed is more than 0 and less than 100
        //decrease speed by 5
        if (this.getKartSpeed() > 0 && this.getKartSpeed() <= 100) {
            this.setKartSpeed(this.getKartSpeed() - 4);
        }
    }

    //used to decrease turn kart left
    public void turnLeft(){
        this.setKartDirection(this.getKartDirection() - 1);
        if (this.getKartDirection() < 0) { this.setKartDirection(15);}
    }

    //used to decrease turn kart right
    public void turnRight(){
        this.setKartDirection(this.getKartDirection() + 1);
        if (this.getKartDirection() > 15) {this.setKartDirection(0);}
    }

    //drawing a rectangle around the kart images and returning the rectangle
    public Rectangle getKartRectangle(){
        Rectangle kartRectangle = new Rectangle(this.getPositionX() +10 , this.getPositionY() +10, this.getKartWidth() -20, this.getKartHeight() -20);
        return kartRectangle;
    }

    //check if the kart rectangles interact with the outer racetrack boundaries using the intersects
    public void kartHitsOuterRaceTrack(Line2D bottom,Line2D right,Line2D top,Line2D left){
        if (right.intersects(this.getKartRectangle()) || top.intersects(this.getKartRectangle()) || left.intersects(this.getKartRectangle())){
            if(this.getKartSpeed() != 0) {
                // Collision occurred
                // Set speed to 0
                this.kartSpeed = 0;
            }
        }
    }

    //check if the kart rectangles interact with the racetrack boundaries using the intersects
    public void kartHitsRaceTrack(int x,int y,int width,int height){
        Rectangle area =  new Rectangle(x,y,width,height);

        if (area.intersects(this.getKartRectangle())) {
            if (this.getKartSpeed() != 0) {
                // Collision occurred
                // Set speed to 0
                this.setKartSpeed(0);
            }
        }
    }

    //keep track of the laps using counter. Each time a kart interacts with the rectangles placed of the edges of the racetrack, it is updated
    //the code does not account for cheating yet.

    public int lapCounter(Rectangle rectangle1,Rectangle rectangle2,Rectangle rectangle3,Rectangle rectangle4){
        int counter = 0;
        if (rectangle1.intersects(this.getKartRectangle()))
        {
            counter = 1;
        }
        else if (rectangle2.intersects(this.getKartRectangle()))
        {
            counter = 2;
        }
        else if (rectangle3.intersects(this.getKartRectangle()))
        {
            counter = 3;
        }
        else if (rectangle4.intersects(this.getKartRectangle()))
        {
            counter = 4;
        }
        return counter;
    }
}
