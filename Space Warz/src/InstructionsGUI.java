/** Program by Soeren Walls **/

//Import files for JPanel, JFrame
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

@SuppressWarnings({ "serial", "unused" })
public class InstructionsGUI extends JFrame implements ActionListener
{
	//Declare variables for panel
	private JPanel myPanel;
	private JTextPane myTextArea;
	private JScrollPane scrollPane;
	private JLabel spacerLbl;
	private JButton continueBtn, exitBtn;

	//Begin constructor method
	public InstructionsGUI()
	{
		//Instantiate panel
		myPanel = new JPanel();
		myPanel.setPreferredSize(new Dimension(400,300));
		myPanel.setLayout(new FlowLayout());
		myPanel.setVisible(true);
		myPanel.setBackground(Color.BLACK);
		
		//Instantiate spacerLbl
		spacerLbl = new JLabel("");
		spacerLbl.setPreferredSize(new Dimension(100,20));
		
		//Set text and decorations for TextPane
		myTextArea = new JTextPane();
		String textAreaSubTitle = "Soeren Walls Presents\n";
		String textAreaTitle = "SPACE WARZ\n";
		String textAreaText = "\n\nUse WASD to control the spaceship.\n"
				 + "Use your Mouse to aim.\n"
				 + "Click to shoot enemy ships.\n"
				 + "Destroy all enemy ships to win.\n\n"
				 + "Press ESC at any time to Pause the Game.";
		myTextArea.setSelectedTextColor(Color.WHITE);
		myTextArea.setEditable(false);
		myTextArea.setBackground(Color.BLACK);
		myTextArea.setBorder(null);
		StyledDocument doc = myTextArea.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		Style style = myTextArea.addStyle("style", null);
		Style bigStyle = myTextArea.addStyle("big style", null);
		StyleConstants.setForeground(style, Color.WHITE);
		StyleConstants.setForeground(bigStyle, Color.RED);
		StyleConstants.setFontSize(bigStyle, 18);
		try {
			doc.insertString(doc.getLength(), textAreaSubTitle, style);
			doc.insertString(doc.getLength(), textAreaTitle, bigStyle);
			doc.insertString(doc.getLength(), textAreaText, style);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Instantiate scrollPane
		scrollPane = new JScrollPane(myTextArea);
		scrollPane.setPreferredSize(new Dimension(350,200));
		scrollPane.setBackground(null);
		scrollPane.setBorder(null);
		
		//Instantiate continueBtn
		continueBtn = new JButton("Play Game!");
		continueBtn.setPreferredSize(new Dimension(100,35));
		continueBtn.addActionListener(this);
		//Instantiate exitBtn
		exitBtn = new JButton("Exit");
		exitBtn.setPreferredSize(new Dimension(100,35));
		exitBtn.addActionListener(this);
		
		//Add components
		myPanel.add(spacerLbl);
		myPanel.add(scrollPane);
		myPanel.add(continueBtn);
		myPanel.add(exitBtn);
		
		//Set up window
		this.add(myPanel);
		this.setSize(400,300);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setUndecorated(true);
		this.setResizable(false);
		this.setTitle("Instructions");
		this.pack();
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter() {
			   public void windowClosing(WindowEvent evt) {
				     openRealGUI();
				   }
				  });
	}//end GUI constructor
	
	//Override actionPerformed method
	public void actionPerformed(ActionEvent e)
	{
		//If Continue button is pressed, close this and open Game GUI
		if(e.getSource() == continueBtn)
		{
			this.dispose();
			openRealGUI();
		//If Exit button is pressed, exit the program
		} else if(e.getSource() == exitBtn) {
			this.dispose();
			System.exit(0);
		}
	}
	
	//Open Game GUI
	public void openRealGUI()
	{
		GUI game = new GUI();
	}
}