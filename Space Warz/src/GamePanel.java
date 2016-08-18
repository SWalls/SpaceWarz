/** Program by Soeren Walls **/

//Import all necessary files
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@SuppressWarnings("serial")
public class GamePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
	//Timer used to repaint JPanel every 15ms
	private Timer timerBuff;
	//1 and Only PlayerSprite used in game (doesn't currently support multi-player)
	private PlayerSprite spr;
	
	//Angle to rotate PlayerSprite's sprite Image (updated when mouse is moved)
	private double angleToRotate = 0;
	//various counters for keeping certain Game-States for a set amount of time
	private int countOnReset = 0, countDisplayLvl = 0, countOnDeath = 0, countFire = 0;
	//Integers used to keep track of current Game-State
	private int level = 0, lives = 5, score = 0, wave = 1;
	//Booleans used to tell JPanel how to paint & what to do in ActionPerformed
	private boolean alive = true, won = false, paused = false, levelAdvance = true, playerDying = false, keepFiring = false, miniBossKilled = true, miniBossAdded = false, minibDying = false, hasEnteredScore = false;

	//Heart image used for displaying number of Lives remaining
	private InputStream heartSprite = this.getClass().getClassLoader().getResourceAsStream("media/heart.png");
	private BufferedImage heartImg;
	
	//New cursor inside window (replaces default cursor)
	private CursorTarget windowCursor;
	//Array of PlayerSprite's bullets
	private ArrayList<BulletSprite> bullets;
	//Array of Arrays of each Enemy's bullets
	private ArrayList<ArrayList<BulletSprite>> enemyBullets = new ArrayList<ArrayList<BulletSprite>>();
	//Array of Arrays of each Mini Boss' bullets (in case future levels have multiple minibosses at once)
	private ArrayList<ArrayList<BulletSprite>> minibBullets = new ArrayList<ArrayList<BulletSprite>>();
	//Array of enemies currently alive in-game
	private ArrayList<Enemy1Sprite> enemies = new ArrayList<Enemy1Sprite>();
	//Array of enemies whose "dying" boolean = true (enemies in this ArrayList will be removed from game in 10ms)
	private ArrayList<Enemy1Sprite> dyingEnemies = new ArrayList<Enemy1Sprite>();
	//Array of stars in background (they move down the screen to create illusion of flying forward)
	private ArrayList<StarSprite> stars = new ArrayList<StarSprite>();
	//Array of Mini Bosses currently alive in-game
	private ArrayList<MiniBossSprite> miniBosses = new ArrayList<MiniBossSprite>();
	//Array of coordinates at which enemies are created (values for this array are pseudo-randomly generated)
	private int[][] enemyCoord = new int[4][2];
	//Array of coordinates at which stars are created (values for this array are pseudo-randomly generated)
	//Here, number of stars to create at beginning of game equals 200
	private int[][] starsCoord = new int[200][2];
	
	//Input dialog used to ask User for name when High Score is achieved
	private JOptionPane highScore;
	private JDialog hsDialog;
	
	//Sounds used in game
	private Sound soundShoot, soundExplode, soundBGMusic, soundWarp;
	
	//Constants (some of which aren't final because they are changed in Constructor method)
	private int PANEL_WIDTH = 800;
	private int PANEL_HEIGHT = 600;
	private final int NUM_OF_LEVELS = 6;
	private final int LEVEL_GET_SHIELDS = 3;
	private int NUM_OF_WAVES = 1;
	
	//The JFrame in which this GamePanel (JPanel) was created
	GUI windowFrame;
	
	//Constructor method
	public GamePanel(GUI wf, int gwid, int ghei)
	{
		//Define constants to true values
		PANEL_WIDTH = gwid;
		PANEL_HEIGHT = ghei;
		windowFrame = wf;

		//Create BufferedImage of heart sprite
		try {
			heartImg = ImageIO.read(heartSprite);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Set up this JPanel
		this.setPreferredSize(new Dimension(PANEL_WIDTH,PANEL_HEIGHT));
		this.addKeyListener(new TimeAdapter());
		this.setFocusable(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.setBackground(Color.BLACK);
		this.setDoubleBuffered(true);
		this.setVisible(true);
		
		//Set up JOptionPane for high score
		highScore = new JOptionPane("Congratulations! You got a new HIGH SCORE!\nPlease enter your name:\n", JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, null, "New High Score");
		highScore.setWantsInput(true);
		hsDialog = highScore.createDialog(null, "New High Score");
		hsDialog.setBounds((PANEL_WIDTH/2)-150,(PANEL_HEIGHT/2)-300,300,150);
		hsDialog.setVisible(false);
		
		//Instantiate Sounds used in game and start background music
		soundShoot = new Sound("media/laser.wav");
		soundExplode = new Sound("media/explode.wav");
		soundBGMusic = new Sound("media/bgmusic.wav");
		soundWarp = new Sound("media/warp.wav");
		soundBGMusic.playSound(true);
		
		//Instantiate 1 and only PlayerSprite
		spr = new PlayerSprite(this);
		//Add moving stars to the background
		addStars();
		//Replace default cursor with cross-hair
		removeCursor();
		
		//Begin timer (calls ActionPerformed every 15ms)
		timerBuff = new Timer(15,this);
		timerBuff.start();
		
		//Call paintComponent()
		this.repaint();
	}
	
	//Return JFrame width (should equal screen width)
	public int getWidth()
	{
		return PANEL_WIDTH;
	}
	
	//Return JFrame height (should equal screen height)
	public int getHeight()
	{
		return PANEL_HEIGHT;
	}
	
	//Add enemies to game (called at beginning of each new level and each new wave)
	public void addEnemies()
	{
		int minY;
		int maxY;
		int maxX = PANEL_WIDTH - 51;
		//For each enemy we need to create
		for(int i=0; i<enemyCoord.length; i++)
		{
			//If not Level 1, generate pseudo-random direction for enemy to travel (up or down)
			Random rand = new Random();
			int dir = rand.nextInt(2);
			if(level<=1) dir=0;
			if(dir<1){
				//Direction is up
				minY = 0-(PANEL_HEIGHT*1);
				maxY = 0;
			} else {
				//Direction is down
				minY = PANEL_HEIGHT;
				maxY = PANEL_HEIGHT*2;
			}
			//Generate random X-coordinate for enemy
			enemyCoord[i][0] = rand.nextInt(maxX);
			//Generate random Y-coordinate for enemy
			enemyCoord[i][1] = rand.nextInt(maxY-minY+1) + minY;
			//Finally, add this enemy to ArrayList of enemies
			enemies.add(new Enemy1Sprite(this, enemyCoord[i][0], enemyCoord[i][1], spr, -1));
		}
	}
	
	//Add stars to background (called once at the beginning of the game)
	public void addStars()
	{
		int maxY = PANEL_HEIGHT;
		int maxX = PANEL_WIDTH - 51;
		//For each star we need to create
		for(int i=0; i<starsCoord.length; i++)
		{
			//Generate random X and Y coordinates for star
			Random rand = new Random();
			starsCoord[i][0] = rand.nextInt(maxX);
			starsCoord[i][1] = rand.nextInt(maxY);
			//Add this star to ArrayList of stars
			stars.add(new StarSprite(this, starsCoord[i][0], starsCoord[i][1]));
		}
	}
	
	//Add mini boss to game (called after last wave of every even-numbered level)
	public void addMiniBoss(int whichOne, int he)
	{
		//Add this Mini Boss to ArrayList of mini bosses
		miniBosses.add(new MiniBossSprite(this, PANEL_WIDTH/2, -100, spr, whichOne, he));
		miniBossAdded = true;
	}
	
	//Remove default cursor (called once at beginning of game)
	public void removeCursor()
	{
		BufferedImage cursorBlankImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorBlankImage, new Point(0, 0), "blank");
		windowFrame.getContentPane().setCursor(blankCursor);
	}
	
	//Paint the JPanel (called by ActionPerformed every 15ms)
	public void paint(Graphics g)
	{
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setColor(Color.WHITE);
		g2.setFont(new Font("Sans Serif", Font.PLAIN, 12));
		
		//Draw stars
		for(StarSprite star : stars) g2.drawImage(star.getSpriteImage(), star.getXPos(), star.getYPos(), this);
		
		//Draw PlayerSprite's bullets
		bullets = spr.getBulletsFired();
		for(BulletSprite blt : bullets) g2.drawImage(blt.getSpriteImage(), blt.getXPos(), blt.getYPos(), this);
		
		//Draw enemies
		for(int z=0; z<enemies.size(); z++)
		{
			//Draw each enemy's bullets if an enemy is shooting
			if(enemyBullets.size() >= z+1)
			{
				ArrayList<BulletSprite> thisEnemyBullets = enemyBullets.get(z);
				for(BulletSprite blt : thisEnemyBullets) g2.drawImage(blt.getSpriteImage(), blt.getXPos(), blt.getYPos(), this);
			}
			double rotationDegrees;
			//If enemy sprite is not exploding, always rotate the enemy to face the PlayerSprite
			if(!enemies.get(z).isDying()) rotationDegrees = getEnemyRotAngle(enemies.get(z));
			//If it is exploding, don't rotate the image
			else rotationDegrees = 0;
			//Get the center X and Y values of the sprite image
			double centerX = (double)enemies.get(z).getWidth() / 2;
			double centerY = (double)enemies.get(z).getHeight() / 2;
			//Rotate the image
			AffineTransform atrans = AffineTransform.getRotateInstance(rotationDegrees, centerX, centerY);
			AffineTransformOp atop = new AffineTransformOp(atrans, AffineTransformOp.TYPE_BILINEAR);
			//If the enemy is visible, draw the enemy on the screen
			if(enemies.get(z).isVisible()) g2.drawImage(atop.filter(enemies.get(z).getSpriteImage(),null), enemies.get(z).getXPos(), enemies.get(z).getYPos(), this);
		}
		
		//Draw mini bosses
		for(int z=0; z<miniBosses.size(); z++)
		{
			//Draw each mini boss' bullets if a mini boss is shooting
			if(minibBullets.size() >= z+1)
			{
				ArrayList<BulletSprite> thisMinibBullets = minibBullets.get(z);
				for(BulletSprite blt : thisMinibBullets) g2.drawImage(blt.getSpriteImage(), blt.getXPos(), blt.getYPos(), this);
			}
			double rotationDegrees;
			//If mini boss sprite is not exploding, always rotate the mini boss to face the PlayerSprite
			if(!miniBosses.get(z).isDying()) rotationDegrees = getMinibRotAngle(miniBosses.get(z));
			//If it is exploding, don't rotate the image
			else rotationDegrees = 0;
			//Get the center X and Y values of the sprite image
			double centerX = (double)miniBosses.get(z).getWidth() / 2;
			double centerY = (double)miniBosses.get(z).getHeight() / 2;
			//Rotate the image
			AffineTransform atrans = AffineTransform.getRotateInstance(rotationDegrees, centerX, centerY);
			AffineTransformOp atop = new AffineTransformOp(atrans, AffineTransformOp.TYPE_BILINEAR);
			//If the mini boss is visible, draw the mini boss on the screen
			if(miniBosses.get(z).isVisible()){
				g2.drawImage(atop.filter(miniBosses.get(z).getSpriteImage(),null), miniBosses.get(z).getXPos(), miniBosses.get(z).getYPos(), this);
				//If the mini boss has been damaged (collided with PlayerSprite or Bullet), then draw "-10" next to the mini boss
				if(miniBosses.get(z).isHurting()){
					g2.setColor(Color.RED);
					g2.setFont(new Font("Sans Serif", Font.BOLD, 16));
					g2.drawString("-10", miniBosses.get(z).getXPos() + miniBosses.get(z).getWidth() + 5, miniBosses.get(z).getYPos());
					g2.setColor(Color.WHITE);
					g2.setFont(new Font("Sans Serif", Font.PLAIN, 12));
				}
			}
		}
		//If PlayerSprite is exploding, don't rotate the image
		if(playerDying) angleToRotate = 0;
		//If it's not exploding, always rotate PlayerSprite to face the direction of the Mouse position
		else if(alive && !won && !paused) angleToRotate = getRotationAngle();
		//Get center X and Y values of the sprite image
		double sprCenterX = (double)spr.getWidth() / 2;
		double sprCenterY = (double)spr.getHeight() / 2;
		//Rotate the image
		AffineTransform spr_atrans = AffineTransform.getRotateInstance(angleToRotate, sprCenterX, sprCenterY);
		AffineTransformOp spr_atop = new AffineTransformOp(spr_atrans, AffineTransformOp.TYPE_BILINEAR);
		//If PlayerSprite is visible, draw it on the screen
		if(spr.isVisible() && (countOnReset<20 || (countOnReset>40 && countOnReset<60) || (countOnReset>80 && countOnReset<100) || (countOnReset>120 && countOnReset<140) || (countOnReset>160 && countOnReset<180) || (countOnReset>200 && countOnReset<220))){
			g2.drawImage(spr_atop.filter(spr.getSpriteImage(),null), spr.getXPos(), spr.getYPos(), this);
			//If shields are activated and the player has been damaged, draw "-25" next to the PlayerSprite
			if(spr.hasShields() && spr.isHurting() && spr.getHealth()>0){
				g2.setColor(Color.RED);
				g2.setFont(new Font("Sans Serif", Font.BOLD, 16));
				g2.drawString("-25", spr.getXPos() + spr.getWidth() + 5, spr.getYPos());
				g2.setColor(Color.WHITE);
				g2.setFont(new Font("Sans Serif", Font.PLAIN, 12));
			}
		}

		//If windowCursor has been defined, draw the Cross-Hair cursor on the screen wherever the Mouse is
		if(windowCursor!=null) g2.drawImage(windowCursor.getSpriteImage(), windowCursor.getXPos(), windowCursor.getYPos(), this);
		
		//Draw information about the Game-State in the top left corner of the screen
		g2.drawString("LEVEL: " + level, 5, 15);
		g2.drawString("WAVE: " + wave + " / " + NUM_OF_WAVES, 5, 35);
		g2.drawString("ENEMIES: " + enemies.size(), 5, 55);
		
		//Draw information about the status of PlayerSprite in the bottom left corner of the screen
		int offset = (GUI.FULLSCREEN ? 20 : 0);
		g2.setColor(Color.WHITE);
		g2.drawString("LIVES: ", 5, PANEL_HEIGHT-35-offset);
		for(int k=0; k<lives; k++){
			g2.drawImage(heartImg, 45+(k*20), PANEL_HEIGHT-47-offset, this);
		}
		g2.setColor(Color.GREEN);
		g2.drawString("SCORE: " + score, 5, PANEL_HEIGHT-15-offset);
		g2.setColor(Color.WHITE);

		//If a mini boss is present, draw its health on the top of the screen
		if(miniBosses.size()>0) { g2.setFont(new Font("Sans Serif", Font.BOLD, 18)); g2.drawString("BOSS HEALTH: " + miniBosses.get(0).getHealth(), PANEL_WIDTH/2 - 70, 15); }
		
		//If PlayerSprite has shields activated, draw its health on the bottom of the screen
		if(spr.hasShields()) {
			//If PlayerSprite has no health, change the color to Red
			if(spr.getHealth()==0) g2.setColor(Color.RED);
			//Otherwise keep the color Green
			else g2.setColor(Color.GREEN);
			g2.setFont(new Font("Sans Serif", Font.BOLD, 18));
			g2.drawString("SHIELDS HEALTH: " + spr.getHealth(), PANEL_WIDTH/2 - 85, PANEL_HEIGHT - 15);
			g2.setColor(Color.WHITE);
		}
		
		//If it's time to advance to the next level, draw information about the level in the center of the screen
		if(levelAdvance){
			//If there are still more levels remaining, draw everything
			if(level+1<=NUM_OF_LEVELS){
				//Draw level number on the screen
				int newlvl = level+1;
				g2.setFont(new Font("Sans Serif", Font.ITALIC, 16));
				g2.drawString("LEVEL " + newlvl, (PANEL_WIDTH/2) - 33, PANEL_HEIGHT/2 + 25);
				//If it's the first level, draw the Game Title on the screen
				if(newlvl==1) {
					g2.setFont(new Font("Sans Serif", Font.ITALIC, 12));
					g2.drawString("SOEREN WALLS PRESENTS", (PANEL_WIDTH/2) - 82, PANEL_HEIGHT/2 - 25);
					g2.setFont(new Font("Sans Serif", Font.BOLD, 18));
					g2.drawString("SPACE WARZ", (PANEL_WIDTH/2) - 60, PANEL_HEIGHT/2);
				//If it's the level at which PlayerSprite obtains shields, draw the message on the screen
				} else if(newlvl==LEVEL_GET_SHIELDS) {
					g2.setFont(new Font("Sans Serif", Font.ITALIC, 12));
					g2.drawString("YOU'VE BEEN AWARDED", (PANEL_WIDTH/2) - 70, PANEL_HEIGHT/2 - 25);
					g2.setFont(new Font("Sans Serif", Font.BOLD, 18));
					g2.setColor(Color.GREEN);
					g2.drawString("SHIELDS", (PANEL_WIDTH/2) - 40, PANEL_HEIGHT/2);
				}
			//If there are no more levels remaining, end the game
			} else {
				levelAdvance = false;
				won = true;
				//soundBGMusic.stopSound();
				spr.kill();
			}
		}
		
		//If the player died prematurely (and is out of lives) display Game Over message
		if(!alive && !won && !paused) {
			g2.setColor(Color.RED);
			g2.setFont(new Font("Sans Serif", Font.BOLD, 16));
			g2.drawString("GAME OVER", (PANEL_WIDTH/2) - 50, PANEL_HEIGHT/2);
			g2.setColor(Color.WHITE);
			g2.setFont(new Font("Sans Serif", Font.ITALIC, 12));
			g2.drawString("Press ENTER to Play Again", (PANEL_WIDTH/2) - 75, PANEL_HEIGHT/2 + 35);
			g2.drawString("Press ESC to Quit", (PANEL_WIDTH/2) - 50, PANEL_HEIGHT/2 + 55);
			drawScores(g2);
		//If the player finished all the levels and is still alive, display You Win message
		} else if(alive && won && !paused) {
			g2.setColor(Color.GREEN);
			g2.setFont(new Font("Sans Serif", Font.BOLD, 16));
			g2.drawString("YOU WIN!", (PANEL_WIDTH/2) - 40, PANEL_HEIGHT/2);
			g2.setColor(Color.WHITE);
			g2.setFont(new Font("Sans Serif", Font.ITALIC, 12));
			g2.drawString("Press ENTER to Play Again", (PANEL_WIDTH/2) - 75, PANEL_HEIGHT/2 + 35);
			g2.drawString("Press ESC to Quit", (PANEL_WIDTH/2) - 50, PANEL_HEIGHT/2 + 55);
			drawScores(g2);
		//If the game is paused, display Paused message
		} else if(alive && !won && paused) {
			g2.setColor(Color.WHITE);
			g2.setFont(new Font("Sans Serif", Font.BOLD, 16));
			g2.drawString("PAUSED", (PANEL_WIDTH/2) - 35, PANEL_HEIGHT/2 - 95);
			g2.setFont(new Font("Sans Serif", Font.ITALIC, 12));
			g2.drawString("Press Q to Quit", (PANEL_WIDTH/2) - 43, PANEL_HEIGHT/2 - 75);
			g2.drawString("Press ESC to Resume", (PANEL_WIDTH/2) - 62, PANEL_HEIGHT/2 - 55);
		}
		
		//Finish painting
		g.dispose();
	}
	
	//Override actionPerformed method (called by Timer every 15ms)
	public void actionPerformed(ActionEvent e)
	{
		//Instantiate the new cursor (cross-hair) if it hasn't already been set
		if(windowCursor==null) windowCursor = new CursorTarget(getMouseCoords()[0], getMouseCoords()[1]);
		
		//Do all of the following as long as: (1) the game is not over, (2) the player has not yet won, and (3) the game is not paused
		if(alive && !won && !paused){
			//If ESC was pressed, pause game and pause all sounds
			if(spr.isPaused()){
				paused = true;
				soundShoot.pauseSound();
				soundExplode.pauseSound();
				soundWarp.pauseSound();
				soundBGMusic.pauseSound();
			}
			//If the player is exploding, start counter, and in 750ms reset (kill) the player
			if(playerDying)
			{
				countOnDeath++;
				if(countOnDeath>50){ //750 milliseconds (3/4 second)
					spr.resetSprite();
					countOnDeath=0;
					playerDying=false;
				}
			}
			//If the player has been reset & is not currently collidable, start counter, and in 3000ms make the player collidable
			if(!spr.isCollidable() && countOnDeath==0)
			{
				countOnReset++;
				if(countOnReset>200){ //3000ms (3 seconds)
					spr.setCollidable(true);
					countOnReset=0;
				}
			}
			
			//Do the following if it's time to advance to the next level
			if(levelAdvance)
			{
				//If the counter hasn't been started yet, temporarily speed up the stars (creates the illusion of warping to the next level)
				if(countDisplayLvl==0){
					for(StarSprite star : stars) star.setStarSpeed(10);
					if(level+1<=NUM_OF_LEVELS) soundWarp.playSound();
					//Activate player shields at Level 3
					if(level+1==3) spr.activateShields(true);
				}
				//Increase the counter
				countDisplayLvl++;
				//After the counter>=120 (1.8 seconds), slow down the stars by 1 speed unit every 150ms
				switch(countDisplayLvl){
					case 120: for(StarSprite star : stars) star.setStarSpeed(9); break;
					case 130: for(StarSprite star : stars) star.setStarSpeed(8); break;
					case 140: for(StarSprite star : stars) star.setStarSpeed(7); break;
					case 150: for(StarSprite star : stars) star.setStarSpeed(6); break;
					case 160: for(StarSprite star : stars) star.setStarSpeed(5); break;
					case 170: for(StarSprite star : stars) star.setStarSpeed(4); break;
					case 180: for(StarSprite star : stars) star.setStarSpeed(3); break;
					case 190: for(StarSprite star : stars) star.setStarSpeed(2); break;
				}
				//Finally, after 3 seconds have passed, go to the next level
				if(countDisplayLvl>200){ //3000ms (3 seconds)
					nextLevel();
				}
			}
			
			//Do the following if all the enemies have been killed
			if(enemies.size()==0){
				//If this isn't the last wave, go to the next wave and add more enemies
				if(wave < NUM_OF_WAVES){
					wave++;
					enemyBullets.clear();
					enemyCoord = new int[level*4][2];
					addEnemies();
				//Otherwise, if it is the last wave, do the following:
				} else {
					//If the mini boss has been killed (automatically true on odd-numbered levels) it's time to advance to the next level
					if(miniBossKilled) levelAdvance = true;
					//If the mini boss hasn't been killed nor added to the game, add the mini boss and wait for it to be killed
					//This increases the health of the mini boss by 50 for every even-numbered level
					else { if(!miniBossAdded) addMiniBoss(0,200+((level/2)*50)); }
				}
			}
			
			//If the user is dragging the mouse (i.e. holding down the mouse button), fire a bullet every 225ms
			if(keepFiring)
			{
				countFire++;
				if(countFire % 15 == 0) fire();
			} else countFire = 0;
			
			//If the mini boss is exploding, kill it (and remove it from the game) in 750ms (3/4 second)
			if(minibDying){
				for(int q=0; q<miniBosses.size(); q++){
					miniBosses.get(q).incDeathCount();
					if(miniBosses.get(q).getDeathCount()>50) { miniBosses.get(q).setVisible(false); miniBosses.remove(q); minibDying = false; }
				}
			}
			
			//If any enemies are exploding, kill them (and remove them from the game) in 750ms (3/4 second)
			if(dyingEnemies.size()>0)
			{
				for(int q=0; q<dyingEnemies.size(); q++)
				{
					dyingEnemies.get(q).incDeathCount();
					if(dyingEnemies.get(q).getDeathCount()>50) { dyingEnemies.get(q).setVisible(false); dyingEnemies.remove(q); }
				}
			}
			
			//If the Player is shooting bullets, move them appropriately every 15ms
			//Also remove any bullets which have exited the visible bounds of the screen
			bullets = spr.getBulletsFired();
			if(bullets.size()>0)
			{
				for(int i=0; i<bullets.size(); i++)
				{
					if(bullets.get(i).isInScreen()) bullets.get(i).moveSprite();
					else { bullets.remove(i); }
				}
			}
			
			//Do the following if there are enemies currently alive in-game
			if(enemies.size()>0)
			{
				//Do the following for every enemy currently alive
				for(int i=0; i<enemies.size(); i++)
				{
					//Add all bullets fired by every enemy into enemyBullets ArrayList
					if(enemyBullets.size() < i+1) enemyBullets.add(enemies.get(i).getBulletsFired());
					else { enemyBullets.set(i, enemies.get(i).getBulletsFired()); }
					//If any bullets have been fired, move them appropriately
					//Also remove any bullets which have exited the visible bounds of the screen
					if(enemyBullets.get(i).size()>0)
					{
						for(int j=0; j<enemyBullets.get(i).size(); j++)
						{
							if(enemyBullets.get(i).get(j).isInScreen()) enemyBullets.get(i).get(j).moveSprite();
							else { enemyBullets.get(i).get(j).setVisible(false); enemyBullets.get(i).remove(j); }
						}
					}
					
					//Do the following if this enemy has not exploded
					if(enemies.get(i).isVisible())
					{
						//If the enemy is not currently exploding, move the enemy appropriately
						//And pseudo-randomly fire bullets (1/100 chance every 15ms)
						if(!enemies.get(i).isDying()){
							enemies.get(i).moveSprite();
							if(enemies.get(i).getYPos() > 0 && enemies.get(i).getYPos() < PANEL_HEIGHT && alive && !won){
								Random randShoot = new Random();
								int randShootNum = randShoot.nextInt(100);
								if(randShootNum > 20 && randShootNum < 22) { enemies.get(i).fireBullet(); soundShoot.playSound(); }
							}
						}
					//Otherwise, if the enemy has exploded & none of its bullets remain on screen,
					//then remove the enemy (and its bullets) from the game.
					} else {
						if(enemyBullets.get(i).size()==0 && enemies.get(i).getBulletsFired().size()==0)
						{
							enemies.remove(i);
							enemyBullets.remove(i);
						}
					}
				}
			}
			
			//Do the following if a mini boss is currently alive in-game
			if(miniBosses.size()>0)
			{
				//Do the following for every mini boss currently alive
				for(int i=0; i<miniBosses.size(); i++)
				{
					//Add all bullets fired by every mini boss into minibBullets ArrayList
					if(minibBullets.size() < i+1) minibBullets.add(miniBosses.get(i).getBulletsFired());
					else { minibBullets.set(i, miniBosses.get(i).getBulletsFired()); }
					//If any bullets have been fired, move them appropriately
					//Also remove any bullets which have exited the visible bounds of the screen
					if(minibBullets.get(i).size()>0)
					{
						for(int j=0; j<minibBullets.get(i).size(); j++)
						{
							if(minibBullets.get(i).get(j).isInScreen()) minibBullets.get(i).get(j).moveSprite();
							else { minibBullets.get(i).get(j).setVisible(false); minibBullets.get(i).remove(j); }
						}
					}

					//Do the following if this mini boss has not exploded
					if(miniBosses.get(i).isVisible())
					{
						//If the mini boss is not currently exploding, move the mini boss appropriately
						//And pseudo-randomly fire bullets (1/10 chance every 15ms)
						if(!miniBosses.get(i).isDying()){
							miniBosses.get(i).moveSprite();
							if(miniBosses.get(i).getYPos() > 0 && miniBosses.get(i).getYPos() < PANEL_HEIGHT && alive && !won){
								Random randShoot = new Random();
								int randShootNum = randShoot.nextInt(100);
								if(randShootNum > 19 && randShootNum < 30) { miniBosses.get(i).fireBullet(); soundShoot.playSound(); }
							}
						}
					//Otherwise, if the mini boss has exploded & none of its bullets remain on screen,
					//then remove the mini boss (and its bullets) from the game.
					} else {
						if(minibBullets.get(i).size()==0 && miniBosses.get(i).getBulletsFired().size()==0)
						{
							miniBosses.remove(i);
							minibBullets.remove(i);
							miniBossKilled = true;
						}
					}
				}
			}
			
			//Move all stars appropriately every 15ms
			for(int i=0; i<stars.size(); i++)
			{
				if(stars.get(i).isInScreen()) stars.get(i).moveSprite();
				else stars.remove(i);
			}
			
			//Move the player sprite appropriately every 15ms
			spr.moveSprite();
			//Check for any collision between any two objects, every 15ms
			checkForCollision();
		
		//Otherwise, do the following if the game is currently over, won, or paused
		} else {
			//Enter high score if game over
			if(score>0 && !hasEnteredScore && (!alive || won)){
				ArrayList<String> highscores = Scores.getScores();
				//If there are any high scores, check to see if we can enter new one
				if(highscores.size()>0){
					int prevMaxScoreIndex = -1;
					//For each high score already in file, check to see if this score > previous high score
					for(int j=highscores.size()-1; j>-1; j--){
						int scoreNum = Integer.parseInt(Scores.formatScore(highscores.get(j))[1]);
						if(score>scoreNum){
							prevMaxScoreIndex = j;
						}
					}
					//If there aren't 5 high scores yet OR a lower score was found in previous high scores, add this score
					if(highscores.size()<5 || prevMaxScoreIndex>-1){
						hsDialog.setVisible(true);
						String userName = highScore.getInputValue().toString();
						//If user doesn't input name, set it to "Player 1"
						if(highScore.getInputValue() == JOptionPane.UNINITIALIZED_VALUE || userName.equals("")) userName = "Player 1";
						hsDialog.setVisible(false);
						Scores.saveScore(userName, score, highscores, prevMaxScoreIndex);
					}
				//If there are no high scores, automatically add this score
				} else {
					hsDialog.setVisible(true);
					String userName = highScore.getInputValue().toString();
					//If user doesn't input name, set it to "Player 1"
					if(highScore.getInputValue() == JOptionPane.UNINITIALIZED_VALUE || userName.equals("")) userName = "Player 1";
					hsDialog.setVisible(false);
					Scores.saveScore(userName, score, highscores, -1);
				}
				//Don't enter the score again
				hasEnteredScore = true;
			}
			//If the game is paused, wait for user to press ESC again
			//When the user does this, resume the game and resume all sounds that were playing before
			if(!spr.isPaused() && alive && !won){
				paused = false;
				soundShoot.resumeSound();
				soundExplode.resumeSound();
				soundWarp.resumeSound();
				soundBGMusic.resumeSound(true);
			}
			//If the user pressed ENTER on the 'Game Over' or 'You Win' screen, restart the game
			if(spr.playAgain()){
				soundBGMusic.stopSound();
				spr.resetSprite();
				spr.setRestart(false);
				this.setVisible(false);
				//Clear all arrays
				bullets.clear();
				enemyBullets.clear();
				minibBullets.clear();
				enemies.clear();
				dyingEnemies.clear();
				stars.clear();
				miniBosses.clear();
				//Call garbage collector
				System.gc();
				//Reset the JFrame
				windowFrame.remove(this);
				@SuppressWarnings("unused")
				GUI newGame = new GUI();
				windowFrame.dispose();
			//Otherwise, if the user pressed ESC on the 'Game Over' or 'You Win' screen (or Q on the 'Paused' screen), exit the game
			} else if(spr.quitGame()){
				this.setVisible(false);
				windowFrame.remove(this);
				windowFrame.dispose();
				System.exit(0);
			}
		}
		
		//Call the paintComponent method every 15ms
		this.repaint();
	}
	
	//Draw high scores on Game Over or You Win screen (called once by ActionPerformed when game is ended)
	private void drawScores(Graphics2D g2D)
	{
		//Set color to white and draw title
		g2D.setColor(Color.WHITE);
		g2D.drawString("HIGH SCORES", (PANEL_WIDTH/2) - 50, PANEL_HEIGHT/2 + 95);
		ArrayList<String> scoresToDraw = Scores.getScores();
		//For each current high score, draw the user's name and score on the screen
		for(int k=0; k<scoresToDraw.size(); k++){
			String sdName = Scores.formatScore(scoresToDraw.get(k))[0];
			String sdScore = Scores.formatScore(scoresToDraw.get(k))[1];
			g2D.drawString(sdName, (PANEL_WIDTH/2) - 80, PANEL_HEIGHT/2 + 115 + (20*k));
			g2D.drawString(sdScore, (PANEL_WIDTH/2) + 50, PANEL_HEIGHT/2 + 115 + (20*k));
		}
	}
	
	//Advance to the next level (called by ActionPerformed when the counter for AdvanceLevel reaches 200, or 3 seconds)
	private void nextLevel()
	{
		//Increment level
		level++;
		//Reset wave
		wave = 1;
		
		//Every 3rd level, add one more wave
		if(level>2 && level % 3 == 0){ NUM_OF_WAVES++; }
		//Reset mini boss booleans
		miniBossAdded = false;
		minibDying = false;
		//If this is an even-numbered level, add a mini boss at the end
		if(level>1 && level % 2 == 0){ miniBossKilled = false; }
		//Otherwise, don't add a mini boss
		else { miniBossKilled = true; }
		//Reset all the stars' speed to 1
		for(StarSprite star : stars) star.setStarSpeed(1);
		
		//Remove all enemy bullets and add new enemies
		enemyBullets.clear();
		enemyCoord = new int[level*4][2];
		addEnemies();
		
		//Reset levelAdvance and counter
		levelAdvance = false;
		countDisplayLvl=0;
	}
	
	//Fire a bullet (called every time the user clicks the mouse, OR every 225ms if the mouse is being dragged)
	public void fire()
	{
		//If player is not dying & the game is not over & not paused, fire a bullet
		if(!playerDying && !won && alive && !paused){
			spr.fireBullet();
			soundShoot.playSound();
		}
	}
	
	//Check for collisions between any objects (called by ActionPerformed every 15ms)
	public void checkForCollision()
	{
		//Do all the following if the PlayerSprite is collidable
		Rectangle shipSize = spr.getSpriteSize();
		if(spr.isCollidable())
		{
			//Do the following for every enemy that is currently alive
			for(Enemy1Sprite enm : enemies)
			{
				//Do the following if this enemy is collidable & collides with PlayerSprite & PlayerSprite is not exploding
				Rectangle enmSize = enm.getSpriteSize();
				if(enm.isCollidable() && shipSize.intersects(enmSize) && !playerDying)
				{
					//Kill this enemy
					killEnemy(enm);
					//If PlayerSprite isn't currently hurting (i.e. it's been damaged while shields are activated), kill the player
					if(!spr.isHurting()) killPlayer();
					//If PlayerSprite has shields and has health (i.e. didn't die), play explosion sound for the enemy's death
					if(spr.hasShields() && spr.getHealth()>0) soundExplode.playSound();
				}
			}
			//Do the following for every enemy that has fired a bullet
			for(int w=0; w<enemyBullets.size(); w++)
			{
				//Do the following for every bullet fired by this enemy
				for(BulletSprite blt : enemyBullets.get(w))
				{
					//Do the following if the bullet collides with PlayerSprite
					Rectangle bltSize = blt.getSpriteSize();
					if(bltSize.intersects(shipSize) && !playerDying)
					{
						//Remove the bullet from the game
						blt.setVisible(false);
						//If PlayerSprite isn't currently hurting, kill the player
						if(!spr.isHurting()) killPlayer();
					}
				}
			}
			//Do the following for every mini boss that is currently alive
			for(MiniBossSprite minib : miniBosses)
			{
				//Do the following if the mini boss is collidable & collides with PlayerSprite & PlayerSprite is not exploding
				Rectangle minibSize = minib.getSpriteSize();
				if(minib.isCollidable() && shipSize.intersects(minibSize) && !playerDying)
				{
					//If PlayerSprite isn't currently hurting, kill the player
					if(!spr.isHurting()) killPlayer();
					//If shields are not activated and the mini boss has health, subtract 10 from the mini boss' health
					if(!spr.hasShields() && minib.getHealth()>19) minib.takeHealth(10);
					//If shields are not activated and the mini boss doesn't have health, kill the mini boss
					else if(!spr.hasShields() && minib.getHealth()<20) {
						minib.takeHealth(10);
						killMiniBoss(minib);
						score+=200;
						miniBossKilled = true;
						//If PlayerSprite has shields and health (i.e. didn't die), play explosion sound for mini boss' death
						if(spr.hasShields() && spr.getHealth()>0) soundExplode.playSound();
					}
				}
			}
			//Do the following for every mini boss that has fired a bullet
			for(int w=0; w<minibBullets.size(); w++)
			{
				//Do the following for every bullet fired by this mini boss
				for(BulletSprite blt : minibBullets.get(w))
				{
					//Do the following if this bullet collides with PlayerSprite
					Rectangle bltSize = blt.getSpriteSize();
					if(bltSize.intersects(shipSize) && !playerDying && !miniBossKilled)
					{
						//Remove bullet from the game
						blt.setVisible(false);
						//If PlayerSprite is not currently hurting, kill the player
						if(!spr.isHurting()) killPlayer();
					}
				}
			}
		}
		//Do the following for every enemy currently alive
		for(Enemy1Sprite enm : enemies)
		{
			//Change the cross-hair to Red if it collides with an enemy
			Rectangle cursorSize = windowCursor.getSpriteSize();
			Rectangle enmSize = enm.getSpriteSize();
			if(enmSize.intersects(cursorSize))
			{
				if(!windowCursor.isRed()) windowCursor.detectEnemy();
			} else {
				if(windowCursor.isRed()) windowCursor.stopDetect();
			}
		}
		//Do the following for every bullet fired by PlayerSprite
		for(BulletSprite blt : bullets)
		{
			//Do the following for every enemy currently alive
			Rectangle bltSize = blt.getSpriteSize();
			for(Enemy1Sprite enm : enemies)
			{
				//If this enemy is collidable & collides with this bullet, remove the bullet and the enemy from the game
				Rectangle enmSize = enm.getSpriteSize();
				if(enm.isCollidable() && bltSize.intersects(enmSize))
				{
					soundExplode.playSound();
					blt.setVisible(false);
					killEnemy(enm);
					score+=15;
				}
			}
			//Do the following for every mini boss currently alive
			for(MiniBossSprite minib : miniBosses)
			{
				//Do the following if this mini boss is collidable & collides with this bullet
				Rectangle minibSize = minib.getSpriteSize();
				if(minib.isCollidable() && bltSize.intersects(minibSize))
				{
					//Remove the bullet from the game
					blt.setVisible(false);
					//If the mini boss has health, subtract 10 from its health
					if(minib.getHealth()>19) minib.takeHealth(10);
					//Otherwise, kill the mini boss
					else {
						minib.takeHealth(10);
						soundExplode.playSound();
						killMiniBoss(minib);
						score+=200;
						miniBossKilled = true;
					}
				}
			}
		}
	}
	
	//Kill the player (called by checkForCollision (in ActionPerformed) when there is a collision detected between PlayerSprite & another object)
	public void killPlayer()
	{
		//Only do damage to the ship if it has shields and health
		if(spr.hasShields() && spr.getHealth()>0 && !spr.isHurting()){
			spr.doDamage();
		//Otherwise, kill the player
		} else {
			if(score>=50) score-=50;
			else score = 0;
			playerDying = true;
			soundExplode.playSound();
			spr.explode();
			spr.setCollidable(false);
			//If lives remain, subtract 1
			if(lives>1) lives--;
			//Otherwise, end the game
			else { lives--; alive = false; spr.kill(); }
		}
	}
	
	//Kill an enemy (called by checkForCollision when a collision is detected)
	public void killEnemy(Enemy1Sprite enm)
	{
		enm.explode();
		dyingEnemies.add(enm);
	}
	
	//Kill a mini boss (called by checkForCollision when a collision is detected)
	public void killMiniBoss(MiniBossSprite minib)
	{
		minib.explode();
		minibDying = true;
	}
	
	//Get mouse coordinates (called by getRotationAngle, ActionPerformed, mouseDragged, and mouseMoved)
	public int[] getMouseCoords()
	{
		PointerInfo mi = MouseInfo.getPointerInfo();
		Point mouseCoord = mi.getLocation();
		Point frameCoord = windowFrame.getLocationOnScreen();
		int mouseX = (int)mouseCoord.getX() - (int)frameCoord.getX();
		int mouseY = (int)mouseCoord.getY() - (int)frameCoord.getY();
		int[] mouseCoords = { mouseX,mouseY };
		return mouseCoords;
	}
	
	//Calculate angle to rotate PlayerSprite image (called every 15ms by ActionPerformed)
	public double getRotationAngle()
	{
		int mouseX = getMouseCoords()[0];
		int mouseY = getMouseCoords()[1];
		int centerPosX = (int)(spr.getXPos() + (spr.getWidth()/2));
		int centerPosY = (int)(spr.getYPos() + (spr.getHeight()/2));		
		int distX = centerPosX - mouseX;
		int distY = centerPosY - mouseY;
		//Calculate the angle in radians
		double angleToRotate = Math.toRadians((Math.toDegrees(Math.atan2(distY, distX))) - 90);
		spr.setBulletTrajectory(distX, distY);
		return angleToRotate;
	}
	
	//Calculate angle to rotate Enemy sprite image (called every 15ms by ActionPerformed)
	public double getEnemyRotAngle(Enemy1Sprite enmy)
	{
		int enmyX = (int)enmy.getXPos() + (enmy.getWidth()/2);
		int enmyY = (int)enmy.getYPos() + (enmy.getHeight()/2);
		int centerPosX = (int)(spr.getXPos() + (spr.getWidth()/2));
		int centerPosY = (int)(spr.getYPos() + (spr.getHeight()/2));		
		int distX = enmyX - centerPosX;
		int distY = enmyY - centerPosY;
		int distRotX = centerPosX - enmyX;
		int distRotY = centerPosY - enmyY;
		//Calculate the angle in radians
		double angleToRotate = Math.toRadians((Math.toDegrees(Math.atan2(distRotY, distRotX))) - 90);
		enmy.setBulletTrajectory(distX, distY);
		return angleToRotate;
	}
	
	//Calculate angle to rotate Mini Boss sprite image (called every 15ms by ActionPerformed)
	public double getMinibRotAngle(MiniBossSprite minib)
	{
		int enmyX = (int)minib.getXPos() + (minib.getWidth()/2);
		int enmyY = (int)minib.getYPos() + (minib.getHeight()/2);
		int centerPosX = (int)(spr.getXPos() + (spr.getWidth()/2));
		int centerPosY = (int)(spr.getYPos() + (spr.getHeight()/2));		
		int distX = enmyX - centerPosX;
		int distY = enmyY - centerPosY;
		int distRotX = centerPosX - enmyX;
		int distRotY = centerPosY - enmyY;
		//Calculate the angle in radians
		double angleToRotate = Math.toRadians((Math.toDegrees(Math.atan2(distRotY, distRotX))) - 90);
		minib.setBulletTrajectory(distX, distY);
		return angleToRotate;
	}
	
	//Override mousePressed to fire bullet
	public void mousePressed(MouseEvent e)
	{
		fire();
	}
	
	//Override mouseReleased to stop firing more bullets (after being dragged)
	public void mouseReleased(MouseEvent e)
	{
		keepFiring = false;
	}
	
	public void mouseClicked(MouseEvent e)
	{
		//Required override method
	}
	
	public void mouseEntered(MouseEvent e)
	{
		//Required override method
	}
	
	public void mouseExited(MouseEvent e)
	{
		//Required override method
	}

	//Override mouseDragged to update cursor position & fire more bullets
	public void mouseDragged(MouseEvent e)
	{
		if(windowCursor!=null) windowCursor.moveSprite(getMouseCoords()[0], getMouseCoords()[1]);
		keepFiring = true;
	}

	//Override mouseMoved to update cursor position
	public void mouseMoved(MouseEvent e)
	{
		if(windowCursor!=null) windowCursor.moveSprite(getMouseCoords()[0], getMouseCoords()[1]);
	}
	
	//Class to extend KeyAdapter (used for keyboard input)
	private class TimeAdapter extends KeyAdapter
	{
		//Call every time a key is released
		public void keyReleased(KeyEvent e)
		{
			spr.keyReleased(e);
		}
		//Call every time a key is pressed
		public void keyPressed(KeyEvent e)
		{
			spr.keyPressed(e);
		}
	}
}