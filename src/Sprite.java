import java.io.Serializable;

public class Sprite implements Serializable
{
   private String name;
   
   public Sprite(String name)
   {
      this.name = name;
   }
   
   public String getName() 
   {
      return name;
   }
}