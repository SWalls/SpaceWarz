/** Program by Soeren Walls **/

//Import all necessary files
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class StarSprite {
	private InputStream sprite = this.getClass().getClassLoader().getResourceAsStream("media/star.png");
	private int xPos, yPos;
	private BufferedImage spriteImg;
	boolean starVisible = true;
	
	@SuppressWarnings("unused")
	private int GAMESCREEN_WIDTH = 800;
	private int GAMESCREEN_HEIGHT = 600;
	private int STAR_SPEED = 1;
	
	private GamePanel panel;
	
	public StarSprite(GamePanel p, int x, int y)
	{
		panel = p;
		GAMESCREEN_WIDTH = panel.getWidth();
		GAMESCREEN_HEIGHT = panel.getHeight();
		try {
			spriteImg = ImageIO.read(sprite);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xPos = x - spriteImg.getWidth()/2;
		yPos = y - spriteImg.getHeight()/2;
	}
	
	public BufferedImage getSpriteImage()
	{
		return spriteImg;
	}
	
	public int getXPos()
	{
		return xPos;
	}
	
	public int getYPos()
	{
		return yPos;
	}
	
	public boolean isInScreen()
	{
		return starVisible;
	}
	
	public void setStarSpeed(int sp)
	{
		STAR_SPEED = sp;
	}
	
	public void setVisible(boolean v)
	{
		starVisible = v;
	}
	
	public void moveSprite()
	{
		if(yPos > GAMESCREEN_HEIGHT + spriteImg.getHeight()) yPos = 0 - 3;
		yPos = yPos + STAR_SPEED;
	}
}
