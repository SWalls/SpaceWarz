/** Program by Soeren Walls **/

//Import all necessary files
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.*;

public class MiniBossSprite {
	private InputStream[] sprite = { this.getClass().getClassLoader().getResourceAsStream("media/miniboss0.png"), this.getClass().getClassLoader().getResourceAsStream("media/miniboss0.png") };
	private InputStream[] spriteHurt = { this.getClass().getClassLoader().getResourceAsStream("media/miniboss0-hurt.png"), this.getClass().getClassLoader().getResourceAsStream("media/miniboss0-hurt.png") };
	private InputStream explosionSprite = this.getClass().getClassLoader().getResourceAsStream("media/explosion.gif");
	private PlayerSprite sprPlayer;
	private int sprDistX, sprDistY, sprXPos, sprYPos, gravPullX=0, gravPullY=0;
	private int xPos, yPos, width, height;
	private double bulletDistX = 0, bulletDistY = 0;
	private boolean visible = true, collidable = true, dying = false, hurting = false;
	private BufferedImage spriteImg, spriteHurtImg, spriteExplosionImg;
	private int randRotAmt, deathCount = 0, hurtCount = 0, bossHealth = 100;
	private ArrayList<BulletSprite> bulletsFired = new ArrayList<BulletSprite>();

	private int GAME_WIDTH = 800;
	private int GAME_HEIGHT = 600;
	private final int ENEMY_SPEED = 2;
	private int DIST_TO_GRAVITATE = 400;
	private final int GRAV_ATTRACTION = 1;
	
	private String direction = "down";
	
	private GamePanel panel;
	
	public MiniBossSprite(GamePanel p, int x, int y, PlayerSprite spr, int chooseSprite, int bh)
	{
		panel = p;
		bossHealth = bh;
		GAME_WIDTH = panel.getWidth();
		GAME_HEIGHT = panel.getHeight();
		DIST_TO_GRAVITATE = GAME_WIDTH - 200;
		sprPlayer = spr;
		try {
			spriteImg = ImageIO.read(sprite[chooseSprite]);
			spriteHurtImg = ImageIO.read(spriteHurt[chooseSprite]);
			spriteExplosionImg = ImageIO.read(explosionSprite);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		width = spriteImg.getWidth();
		height = spriteImg.getHeight();
		xPos = x;
		yPos = y;
		if(yPos > GAME_HEIGHT) direction = "up";
		//randRotAmt = randRot.nextInt(360);
		randRotAmt = 0;
	}
	
	public void moveSprite()
	{
		if(direction.equals("down")){
			if(yPos > GAME_HEIGHT) yPos = 0 - height;
			yPos = yPos + ENEMY_SPEED;
		} else {
			if(yPos < 0 - height) yPos = GAME_HEIGHT;
			yPos = yPos - ENEMY_SPEED;
		}
		
		if(hurting){
			hurtCount++;
			if(hurtCount>10) { hurting=false; hurtCount=0; }
		}
		
		if(yPos > 0 && yPos < GAME_HEIGHT){
			sprXPos = sprPlayer.getXPos();
			sprYPos = sprPlayer.getYPos();
			sprDistX = sprXPos - xPos;
			sprDistY = sprYPos - yPos;
			if( ((sprDistX < 0 && sprDistX > -DIST_TO_GRAVITATE) || (sprDistX > 0 && sprDistX < DIST_TO_GRAVITATE)) && ((sprDistY < 0 && sprDistY > -DIST_TO_GRAVITATE) || (sprDistY > 0 && sprDistY < DIST_TO_GRAVITATE)) )
			{
				if(sprXPos > xPos) gravPullX = GRAV_ATTRACTION*(1-(sprDistX/GAME_WIDTH));
				else gravPullX = GRAV_ATTRACTION*(1+(sprDistX/GAME_WIDTH));
				if(sprYPos > yPos) gravPullY = GRAV_ATTRACTION*(1-(sprDistY/GAME_HEIGHT));
				else gravPullY = GRAV_ATTRACTION*(1+(sprDistY/GAME_HEIGHT));
				if(sprDistX < 0) gravPullX = -gravPullX;
				if(sprDistY < 0) gravPullY = -gravPullY;
				
				if(direction.equals("down")){
					if(gravPullY>0) yPos--;
					yPos += gravPullY;
					xPos += gravPullX;
				} else {
					if(gravPullY>0) yPos++;
					yPos -= gravPullY;
					xPos += gravPullX;
				}
			}
		}
	}
	
	public void fireBullet()
	{
		bulletsFired.add(new BulletSprite(panel, xPos + width/2, yPos + height/2, bulletDistX, bulletDistY, 2, 0));
		bulletsFired.add(new BulletSprite(panel, xPos + width/2, yPos + height/2, bulletDistX, bulletDistY, 2, -1));
		bulletsFired.add(new BulletSprite(panel, xPos + width/2, yPos + height/2, bulletDistX, bulletDistY, 2, 1));
	}
	
	public void removeBullet(int bnum)
	{
		bulletsFired.remove(bnum);
	}
	
	public ArrayList<BulletSprite> getBulletsFired()
	{
		return bulletsFired;
	}
	
	public void explode()
	{
		dying = true;
		collidable = false;
	}
	
	public boolean isHurting()
	{
		return hurting;
	}
	
	public int getHealth()
	{
		return bossHealth;
	}
	
	public void takeHealth(int th)
	{
		bossHealth-=th;
		hurting = true;
	}
	
	public void setCollidable(boolean cl)
	{
		collidable = cl;
	}
	
	public int getDeathCount()
	{
		return deathCount;
	}
	
	public void incDeathCount()
	{
		deathCount++;
	}
	
	public boolean isCollidable()
	{
		return collidable;
	}
	
	public boolean isDying()
	{
		return dying;
	}
	
	public int getXPos()
	{
		return xPos;
	}
	
	public int getYPos()
	{
		return yPos;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getRandRot()
	{
		return randRotAmt;
	}
	
	public boolean isVisible()
	{
		return visible;
	}
	
	public void setVisible(boolean v)
	{
		visible = v;
	}
	
	public BufferedImage getSpriteImage()
	{
		if(dying) return spriteExplosionImg;
		else if(hurting) return spriteHurtImg;
		else return spriteImg;
	}
	
	public Rectangle getSpriteSize()
	{
		return new Rectangle(xPos,yPos,width,height);
	}
	
	public void setBulletTrajectory(int xchange, int ychange)
	{
		bulletDistX = xchange;
		bulletDistY = ychange;
	}
}
