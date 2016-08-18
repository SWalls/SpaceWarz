/** Program by Soeren Walls **/

//Import all necessary files
import java.net.URL;
import javax.sound.sampled.*;

public class Sound {
	private int soundFrame = 0;
	private Clip clip;
	//private File soundFile;
	
	public Sound(String soundFileName)
	{
		try {
			URL soundFile = this.getClass().getClassLoader().getResource(soundFileName);
			AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
			clip = AudioSystem.getClip();
			clip.open(ais);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void playSound()
	{
		if(clip.isRunning()) clip.stop();
		clip.setFramePosition(0);
		clip.start();
	}
	
	public void playSound(boolean l)
	{
		if(clip.isRunning()) clip.stop();
		clip.setFramePosition(0);
		if(l) clip.loop(Clip.LOOP_CONTINUOUSLY);
		else clip.start();
	}
	
	public void stopSound()
	{
		clip.stop();
	}
	
	public void pauseSound()
	{
		if(clip.isRunning()){
			soundFrame = clip.getFramePosition();
			clip.stop();
		}
	}
	
	public void resumeSound()
	{
		if(soundFrame != 0){
			clip.setFramePosition(soundFrame);
			clip.start();
			soundFrame = 0;
		}
	}
	
	public void resumeSound(boolean l)
	{
		if(soundFrame != 0){
			clip.setFramePosition(soundFrame);
			if(l) clip.loop(Clip.LOOP_CONTINUOUSLY);
			else clip.start();
			soundFrame = 0;
		}
	}
}
