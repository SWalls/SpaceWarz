/** Program by Soeren Walls **/

//Import all necessary files
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;

public class PlayerSprite {
	//Instantiate InputStreams for sprite images
	private InputStream sprite = this.getClass().getClassLoader().getResourceAsStream("media/ship.png");
	private InputStream shieldsSprite = this.getClass().getClassLoader().getResourceAsStream("media/ship-shields.png");
	private InputStream shieldsDamageSprite = this.getClass().getClassLoader().getResourceAsStream("media/ship-shields-damage.png");
	private InputStream explosionSprite = this.getClass().getClassLoader().getResourceAsStream("media/explosion.gif");

	//Sprite images used in this class
	private BufferedImage spriteImg, explosionSpriteImg, shieldsSpriteImg, shieldsDamageSpriteImg;
	
	//X and Y position, changes to position, counters used in moveSprite(), and player health
	private int changex = 0, changey = 0, x, y, ax=0, ay=0, countacc = 0, hurtCount = 0, health = 100;
	//Booleans to control Game-State
	private boolean visible = true, collidable = true, dying = false, dead = false, restartGame = false, endGame = false, pauseGame = false, accel = false, decel = false, shields = false, hurting = false;
	//Array of bullets fired by player
	private ArrayList<BulletSprite> bulletsFired = new ArrayList<BulletSprite>();
	//Distance between mouse and player (passed on to BulletSprite class; tells bullet which way to go)
	private double bulletDistX = 0, bulletDistY = 0;

	//Constants
	private final int PLAYER_SPEED = 4;
	private int GAME_WIDTH = 800;
	private int GAME_HEIGHT = 600;
	private int PLAYERSPRITE_HEIGHT, PLAYERSPRITE_WIDTH;
	
	//The JPanel in which this is contained
	private GamePanel panel;
	
	public PlayerSprite(GamePanel p)
	{
		//Instantiate panel
		panel = p;
		//Redefine constants to true value
		GAME_WIDTH = panel.getWidth();
		GAME_HEIGHT = panel.getHeight();
		
		//Instantiate BufferedImages
		try {
			spriteImg = ImageIO.read(sprite);
			shieldsSpriteImg = ImageIO.read(shieldsSprite);
			shieldsDamageSpriteImg = ImageIO.read(shieldsDamageSprite);
			explosionSpriteImg = ImageIO.read(explosionSprite);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Redefine constants to true value
		PLAYERSPRITE_WIDTH = spriteImg.getWidth();
		PLAYERSPRITE_HEIGHT = spriteImg.getHeight();
		
		//Set player's initial x and y position
		x = GAME_WIDTH/2 - PLAYERSPRITE_WIDTH/2;
		y = GAME_HEIGHT/2 + 150 - PLAYERSPRITE_HEIGHT/2;
	}
	
	//Move the player (called every 15ms by GamePanel.paint() method)
	public void moveSprite()
	{
		//Do the following if it's accelerating from a full-stop
		if(accel){
			//Reset counter
			if(decel){
				countacc = 0;
				decel = false;
			}
			//If not paused, keep counting
			if(!pauseGame) countacc++;
			//Every 75ms, increment or decrement the player speed as necessary to accelerate
			if(countacc > 0 && countacc % 5 == 0){
				if(changex>0 && ax<changex) ax++;
				else if(changex<0 && ax>changex) ax--;
				if(changey>0 && ay<changey) ay++;
				else if(changey<0 && ay>changey) ay--;
				if(Math.abs(ax)>=Math.abs(changex) && Math.abs(ay)>=Math.abs(changey)){
					stopAccel(changex, changey);
				}
			}
		//Otherwise, do the following if it's decelerating from full-speed
		} else if(decel){
			//Reset counter
			if(accel){
				countacc = 0;
				accel = false;
			}
			//If not paused, keep counting
			if(!pauseGame) countacc++;
			//Every 75ms, increment or decrement the player speed as necessary to decelerate
			if(countacc > 0 && countacc % 5 == 0){
				if(ax>0) ax--;
				else if(ax<0) ax++;
				if(ay>0) ay--;
				else if(ay<0) ay++;
				if(ax==0 && ay==0){
					stopAccel(changex, changey);
				}
			}
		}
		
		//If game is not paused, update the player position based on speed
		if(!pauseGame){
			x = x + ax;
			y = y + ay;
		}
		
		//If player is hurting, start counter, and after 300ms (0.3 second) stop hurting
		if(hurting){
			hurtCount++;
			if(hurtCount>20) { hurting=false; hurtCount=0; }
		}
		
		//If shields are activated and player has health, redefine width and height (because shields image is bigger)
		if(shields && health>0 && PLAYERSPRITE_WIDTH == spriteImg.getWidth()){
			PLAYERSPRITE_WIDTH = shieldsSpriteImg.getWidth();
			PLAYERSPRITE_HEIGHT = shieldsSpriteImg.getHeight();
		//Otherwise, revert width and height back to normal values
		} else if(health==0 && !hurting && PLAYERSPRITE_WIDTH == shieldsSpriteImg.getWidth()){
			PLAYERSPRITE_WIDTH = spriteImg.getWidth();
			PLAYERSPRITE_HEIGHT = spriteImg.getHeight();
		}
		
		//If player exits visible bounds of the screen, put the player back inside the screen
		if(x<1) x=1;
		else if(x+(PLAYERSPRITE_WIDTH)>GAME_WIDTH) x=GAME_WIDTH-(PLAYERSPRITE_WIDTH);
		if(y<1) y=1;
		else if(y+(PLAYERSPRITE_HEIGHT)>GAME_HEIGHT) y=GAME_HEIGHT-(PLAYERSPRITE_HEIGHT);
	}
	
	//Stop accelerating or decelerating: reset everything related to player's speed (called by moveSprite())
	public void stopAccel(int newx, int newy)
	{
		ax = newx;
		ay = newy;
		countacc = 0;
		accel = false;
		decel = false;
	}
	
	//Take away health from the player any time shields are activated and it's not already hurting
	public void doDamage()
	{
		if(shields && !hurting && health>24){
			health = health - 25;
			hurting = true;
		}
	}
	
	//Check if player is hurting
	public boolean isHurting()
	{
		return hurting;
	}
	
	//Return player's health
	public int getHealth()
	{
		return health;
	}
	
	//Return sprite x position
	public int getXPos()
	{
		return x;
	}
	
	//Return sprite y position
	public int getYPos()
	{
		return y;
	}
	
	//Return sprite image width
	public int getWidth()
	{
		return PLAYERSPRITE_WIDTH;
	}
	
	//Return sprite image height
	public int getHeight()
	{
		return PLAYERSPRITE_HEIGHT;
	}
	
	//Return current image being used for PlayerSprite
	public BufferedImage getSpriteImage()
	{
		if(dying) return explosionSpriteImg;
		else if(shields && health>0 && !hurting) return shieldsSpriteImg;
		else if(hurting) return shieldsDamageSpriteImg;
		else return spriteImg;
	}
	
	//Return bullets fired by PlayerSprite
	public ArrayList<BulletSprite> getBulletsFired()
	{
		return bulletsFired;
	}
	
	//Explode PlayerSprite and set dying=true
	public void explode()
	{
		stopAccel(0,0);
		dying = true;
		changex = 0;
		changey = 0;
	}
	
	//Reset sprite completely: health, dying, hurting, x position, y position all set back to default
	public void resetSprite()
	{
		setVisible(false);
		dying = false;
		hurting = false;
		x = GAME_WIDTH/2 - PLAYERSPRITE_WIDTH/2;
		y = GAME_HEIGHT/2 + 150 - PLAYERSPRITE_HEIGHT/2;
		setVisible(true);
		if(health<100) health=100;
	}
	
	//Pause the sprite's movement
	public void pause()
	{
		if(pauseGame) pauseGame = false;
		else pauseGame = true;
	}
	
	//Kill the player
	public void kill()
	{
		dead = true;
	}
	
	//Check if player has shields
	public boolean hasShields()
	{
		return shields;
	}
	
	//Activate or deactivate player's shields
	public void activateShields(boolean sh)
	{
		shields = sh;
		hurting = false;
		health = 100;
	}
	
	//Check if sprite movement is paused
	public boolean isPaused()
	{
		return pauseGame;
	}
	
	//Check if we should restart the game
	public boolean playAgain()
	{
		return restartGame;
	}
	
	//Check if we should exit the game
	public boolean quitGame()
	{
		return endGame;
	}
	
	//We should/shouldn't restart the game
	public void setRestart(boolean rs)
	{
		restartGame = rs;
	}
	
	//Check if player is collidable (i.e. whether has been recently damaged or not)
	public boolean isCollidable()
	{
		return collidable;
	}
	
	//Make player collidable or not
	public void setCollidable(boolean c)
	{
		collidable = c;
	}
	
	//Make player visible or invisible
	public void setVisible(boolean v)
	{
		visible = v;
	}
	
	//Check if player is visible
	public boolean isVisible()
	{
		return visible;
	}
	
	//Return box around sprite image
	public Rectangle getSpriteSize()
	{
		return new Rectangle(x, y, PLAYERSPRITE_WIDTH, PLAYERSPRITE_HEIGHT);
	}
	
	//Instantiate bullet distance variables to pass on to BulletSprite (tells bullet how to travel)
	public void setBulletTrajectory(double disx, double disy)
	{
		bulletDistX = disx;
		bulletDistY = disy;
	}
	
	//Fire a bullet
	public void fireBullet()
	{
		bulletsFired.add(new BulletSprite(panel, x + PLAYERSPRITE_WIDTH/2, y + PLAYERSPRITE_HEIGHT/2, bulletDistX, bulletDistY, 1, 0));
	}
	
	//Remove a bullet from the game
	public void removeBullet(int bnum)
	{
		bulletsFired.remove(bnum);
	}
	
	//Wait for keyboard input (called from GamePanel)
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		//Restart game if user pressed ENTER on Game Over screen
		if(dead && keyCode==KeyEvent.VK_ENTER) restartGame = true;
		//Exit game if user pressed Q on Paused screen
		else if(pauseGame && keyCode==KeyEvent.VK_Q) endGame = true;
		//Exit game if user pressed ESC on Game Over screen (or pause/unpause Game if pressed during gameplay)
		if(keyCode==KeyEvent.VK_ESCAPE) { if(dead) endGame = true; else this.pause(); }
		if(!dying){
			//Move right
			if((keyCode==KeyEvent.VK_RIGHT || keyCode==KeyEvent.VK_D)) {
				if(changex==0) accel = true;
				changex = PLAYER_SPEED;
			//Move left
			} else if((keyCode==KeyEvent.VK_LEFT || keyCode==KeyEvent.VK_A)) {
				if(changex==0) accel = true;
				changex = -PLAYER_SPEED;
			//Move up
			} else if((keyCode==KeyEvent.VK_UP || keyCode==KeyEvent.VK_W)) {
				if(changey==0) accel = true;
				changey = -PLAYER_SPEED;
			//Move down
			} else if((keyCode==KeyEvent.VK_DOWN || keyCode==KeyEvent.VK_S)) {
				if(changey==0) accel = true;
				changey = PLAYER_SPEED;
			}
		}
	}
	
	//Wait for keyboard button release (called from GamePanel)
	public void keyReleased(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		if(!dying){
			//Stop moving right
			if(keyCode==KeyEvent.VK_RIGHT || keyCode==KeyEvent.VK_D) {
				changex = 0;
			//Stop moving left
			} else if(keyCode==KeyEvent.VK_LEFT || keyCode==KeyEvent.VK_A) {
				changex = 0;
			//Stop moving up
			} else if(keyCode==KeyEvent.VK_UP || keyCode==KeyEvent.VK_W) {
				changey = 0;
			//Stop moving down
			} else if(keyCode==KeyEvent.VK_DOWN || keyCode==KeyEvent.VK_S) {
				changey = 0;
			}
			//Decelerate if no buttons are being pressed
			if(changex==0 && changey==0 && !accel){
				decel = true;
			}
		}
	}
}
