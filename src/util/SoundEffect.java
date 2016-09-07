package util;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public enum SoundEffect {
	DIE("die.wav"), EATGHOST("eatghost.wav"), EATFRUIT("fruit.wav"), EATFOOD("eatfood.wav"), EATEENERGY("eatenergy.wav"), STARTGAME("start.wav"), A1("ambientone.wav"), A2("ambienttwo.wav"), A3(
					"ambientthree.wav"), A4("ambientfour.wav"), AMBIENTFRIGHT("ambientfright.wav"), CUTSCENE(
							"cutscene.wav");

	private static boolean muted = false;
	public static final SoundEffect[] AMBIENT = { A1, A2, A3, A4 };
	private static SoundEffect[] listAudio = { DIE, EATGHOST, EATEENERGY, EATFOOD, STARTGAME, A1, A2, A3,
			A4, AMBIENTFRIGHT, CUTSCENE };

	private Clip clip;

	SoundEffect(String soundFileName) {
		try {
			AudioInputStream audioInputStream = AudioSystem
					.getAudioInputStream(new BufferedInputStream(Util.loadFile("audio/" + soundFileName)));
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void play() {
		if (!muted) {
			if (clip.isRunning()) {
				clip.stop();
			}
			clip.setFramePosition(0);
			clip.start();
		}
	}

	public void setMute(boolean muted) {
		BooleanControl volume = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
		volume.setValue(muted);
	}

	public boolean isRunning() {
		return clip.isRunning();
	}

	public static void setMuteAll(boolean muted) {
		for (SoundEffect s : listAudio)
			s.setMute(muted);
		SoundEffect.muted = muted;
	}

	public static boolean isMuted() {
		return SoundEffect.muted;
	}

	public void stop() {
		if (clip.isRunning()) {
			clip.stop();
		}
	}

	public void loop() {
		if (clip.isRunning()) {
			clip.stop();
		}
		clip.setFramePosition(0);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
		clip.start();
	}

}
