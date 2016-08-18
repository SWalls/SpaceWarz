/** Program by Soeren Walls **/

//Import files for JPanel, JFrame
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

@SuppressWarnings({ "serial", "unused" })
public class GUI extends JFrame
{
	//Declare variables for panel
	private GamePanel myPanel;
	Toolkit tk = Toolkit.getDefaultToolkit();
	private int GAME_WIDTH = 800, GAME_HEIGHT = 600;
	public static final boolean FULLSCREEN = false;

	//Begin constructor method
	public GUI()
	{
		//Set up fullscreen
		if(FULLSCREEN){
			GAME_WIDTH = (int)tk.getScreenSize().getWidth();
			GAME_HEIGHT = (int)tk.getScreenSize().getHeight();
			this.setExtendedState(Frame.MAXIMIZED_BOTH);
			this.setUndecorated(true);
		}

		//Instantiate and add panel
		myPanel = new GamePanel(this, GAME_WIDTH, GAME_HEIGHT);
		this.add(myPanel);
		
		//Set up window
		this.setSize(GAME_WIDTH, GAME_HEIGHT);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setTitle("Soeren Presents: Space Warz");
		this.setBackground(Color.BLACK);
		this.pack();
		this.setVisible(true);
		
	}//end GUI constructor
}