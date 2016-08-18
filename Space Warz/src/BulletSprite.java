/** Program by Soeren Walls **/

//Import all necessary files
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class BulletSprite {
	private InputStream sprite = this.getClass().getClassLoader().getResourceAsStream("media/bullet.png");
	private InputStream sprite2 = this.getClass().getClassLoader().getResourceAsStream("media/bullet2.png");
	private int xPos, yPos, offset = 0;
	private double bulletDistX = 0, bulletDistY = 0, distRatio = 1, xIncrease = 10, yIncrease = 10;
	private BufferedImage spriteImg;
	boolean bulletVisible = true;
	
	private int GAMESCREEN_HEIGHT = 600;
	private int GAMESCREEN_WIDTH = 800;
	private final int BULLET_SPEED = 5;
	private final int OFFSET_AMOUNT = 150;
	
	public BulletSprite(GamePanel panel, int x, int y, double disx, double disy, int whichspr, int off)
	{
		offset = off;
		GAMESCREEN_WIDTH = panel.getWidth();
		GAMESCREEN_HEIGHT = panel.getHeight();
		try {
			if(whichspr==1) spriteImg = ImageIO.read(sprite);
			else spriteImg = ImageIO.read(sprite2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xPos = x - spriteImg.getWidth()/2;
		yPos = y - spriteImg.getHeight()/2;
		bulletDistX = disx;
		bulletDistY = disy;
		//Offset bullet to the left
		if(offset==-1){
			if(OFFSET_AMOUNT>Math.abs(bulletDistX))
				bulletDistX = -bulletDistX;
			else {
				if(bulletDistX<=0) bulletDistX+=OFFSET_AMOUNT;
				else bulletDistX-=OFFSET_AMOUNT;
			}
			if(OFFSET_AMOUNT>Math.abs(bulletDistY))
				bulletDistY = -bulletDistY;
			else {
				if(bulletDistY<=0) bulletDistY+=OFFSET_AMOUNT;
				else bulletDistY-=OFFSET_AMOUNT;
			}
		//Offset bullet to the right
		} else if(offset==1){
			if(OFFSET_AMOUNT>Math.abs(bulletDistX))
				bulletDistX = -bulletDistX;
			else {
				if(bulletDistX<=0) bulletDistX-=OFFSET_AMOUNT;
				else bulletDistX+=OFFSET_AMOUNT;
			}
			if(OFFSET_AMOUNT>Math.abs(bulletDistY))
				bulletDistY = -bulletDistY;
			else {
				if(bulletDistY<=0) bulletDistY-=OFFSET_AMOUNT;
				else bulletDistY+=OFFSET_AMOUNT;
			}
		}
		//Calculate where this bullet needs to go (based on distance between mouse and PlayerSprite)
		if(bulletDistY != 0 && bulletDistX != 0){
			if(Math.abs(bulletDistX) > Math.abs(bulletDistY))
			{
				distRatio = bulletDistY / bulletDistX;
				yIncrease = BULLET_SPEED * (distRatio);
				xIncrease = yIncrease * 1/(distRatio);
				if(bulletDistY < 0) yIncrease = 0 - Math.abs(yIncrease);
				else yIncrease = Math.abs(yIncrease);
				if(bulletDistX < 0) xIncrease = 0 - Math.abs(xIncrease);
				else xIncrease = Math.abs(xIncrease);
			} else if(Math.abs(bulletDistX) < Math.abs(bulletDistY)) {
				distRatio = bulletDistX / bulletDistY;
				xIncrease = BULLET_SPEED * (distRatio);
				yIncrease = xIncrease * 1/(distRatio);
				if(bulletDistX < 0) xIncrease = 0 - Math.abs(xIncrease);
				else xIncrease = Math.abs(xIncrease);
				if(bulletDistY < 0) yIncrease = 0 - Math.abs(yIncrease);
				else yIncrease = Math.abs(yIncrease);
			}
		} else if(bulletDistX==0) {
			xIncrease = 0;
			if(bulletDistY > 0) yIncrease = BULLET_SPEED;
			else yIncrease = -BULLET_SPEED;
		} else if(bulletDistY==0) {
			yIncrease = 0;
			if(bulletDistX > 0) xIncrease = BULLET_SPEED;
			else xIncrease = -BULLET_SPEED;
		}
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
	
	public Rectangle getSpriteSize()
	{
		return new Rectangle(xPos,yPos,spriteImg.getWidth(),spriteImg.getHeight());
	}
	
	public boolean isInScreen()
	{
		return bulletVisible;
	}
	
	public void setVisible(boolean v)
	{
		bulletVisible = v;
	}
	
	public void moveSprite()
	{
		xPos = xPos - (int)xIncrease;
		yPos = yPos - (int)yIncrease;
		if(yPos < 0 || yPos > GAMESCREEN_HEIGHT || xPos < 0 || xPos > GAMESCREEN_WIDTH) bulletVisible = false;
	}
}
