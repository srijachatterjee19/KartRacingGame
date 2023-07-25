import java.awt.*;
import java.awt.geom.Line2D;
import java.io.Serializable;

public class RaceTrack implements Serializable {

    public final int GRASS_X = 150;
    public final int GRASS_Y = 200;
    public final int GRASS_WIDTH = 550;
    public final int GRASS_HEIGHT = 300;
    public final int OUTER_EDGE_X = 50;
    public final int OUTER_EDGE_Y = 100;
    public final int OUTER_EDGE_WIDTH = 750;
    public final int OUTER_EDGE_HEIGHT = 500;
    public final int INNER_EDGE_X = 150;
    public final int INNER_EDGE_Y = 200;
    public final int INNER_EDGE_WIDTH = 550;
    public final int INNER_EDGE_HEIGHT = 300;

    //used to create rectangles around the racetrack
    public Rectangle rectangleObject(int x,int y, int width,int height) {
        Rectangle rectangle = new Rectangle(x,y,width,height);
        return rectangle;
    }

    public Line2D raceTrackLine(int x1,int y1,int x2,int y2){
        Line2D line = new Line2D.Double(x1, y1, x2, y2);
        return line;
    }

    //used to draw the racetrack
    public void drawRaceTrack(Graphics g) {
        Color c1 = Color.green;
        g.setColor( c1 );
        g.fillRect( GRASS_X, GRASS_Y, GRASS_WIDTH, GRASS_HEIGHT ); // grass
        Color c2 = Color.black;
        g.setColor( c2 );
        g.drawRect( OUTER_EDGE_X, OUTER_EDGE_Y, OUTER_EDGE_WIDTH, OUTER_EDGE_HEIGHT );// outer edge
        g.drawRect( INNER_EDGE_X, INNER_EDGE_Y, INNER_EDGE_WIDTH, INNER_EDGE_HEIGHT );// inner edge
        Color c3 = Color.yellow;
        g.setColor( c3 );
        g.drawRect( 100, 150, 650, 400 ); // mid-lane marker
        Color c4 = Color.white;
        g.setColor( c4 );
        g.drawLine( 425, 500, 425, 600 );
    }
}
