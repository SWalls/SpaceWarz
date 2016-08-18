/** Program by Soeren Walls **/

//Import all necessary files
import java.awt.Rectangle;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class CursorTarget {
	private InputStream sprite = this.getClass().getClassLoader().getResourceAsStream("media/cursor.png");
	private InputStream cursorEnemySprite = this.getClass().getClassLoader().getResourceAsStream("media/cursor-red.png");
	private int xPos, yPos;
	private BufferedImage spriteImg, enemySpriteImg;
	private boolean visible = true, red = false;
	
	public CursorTarget(int x, int y)
	{
		try {
			spriteImg = ImageIO.read(sprite);
			enemySpriteImg = ImageIO.read(cursorEnemySprite);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xPos = x;
		yPos = y;
	}
	
	public void detectEnemy()
	{
		red = true;
	}
	
	public void stopDetect()
	{
		red = false;
	}
	
	public boolean isRed()
	{
		return red;
	}
	
	public BufferedImage getSpriteImage()
	{
		if(red) return enemySpriteImg;
		else return spriteImg;
	}
	
	public int getXPos()
	{
		return xPos;
	}
	
	public int getYPos()
	{
		return yPos;
	}
	
	public boolean isVisible()
	{
		return visible;
	}
	
	public void setVisible(boolean v)
	{
		visible = v;
	}
	
	public Rectangle getSpriteSize()
	{
		return new Rectangle(xPos,yPos,spriteImg.getWidth(),spriteImg.getHeight());
	}
	
	public void moveSprite(int newx, int newy)
	{
		xPos = newx;
		yPos = newy;
	}
}
