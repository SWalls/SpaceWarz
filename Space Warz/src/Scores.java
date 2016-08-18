/** Program by Soeren Walls **/

import java.io.*;
import java.util.ArrayList;

public class Scores {
	public final static String myFile = "highscores.txt";
	public static void saveScore(String name, int score, ArrayList<String> prevScores, int replaceScore)
	{
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(myFile));
			String newScore = name+":"+score;
			if(replaceScore<0){
				prevScores.add(newScore);
			} else {
				if(prevScores.size()<5) prevScores.add("Player 1:0");
				for(int i=prevScores.size()-1; i>replaceScore; i--){
					prevScores.set(i, prevScores.get(i-1));
				}
				prevScores.set(replaceScore, newScore);
			}
			for(int i=0; i<prevScores.size(); i++){
				String prevName = formatScore(prevScores.get(i))[0];
				String prevScore = formatScore(prevScores.get(i))[1];
				bw.write(prevName + ":" + prevScore);
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch(Exception e) {
			//couldn't save
		}
	}
	
	public static ArrayList<String> getScores()
	{
		try {
			ArrayList<String> str = new ArrayList<String>();
			BufferedReader bread = new BufferedReader(new FileReader(myFile));
			String lineOfText = bread.readLine();
			while(lineOfText != null && !lineOfText.equals("")){
				str.add(lineOfText);
				lineOfText = bread.readLine();
			}
			bread.close();
			return str;
		} catch(Exception e) {
			return new ArrayList<String>();
		}
	}
	
	public static String[] formatScore(String score)
	{
		String[] formattedScore = score.split(":");
		return formattedScore;
	}
}
