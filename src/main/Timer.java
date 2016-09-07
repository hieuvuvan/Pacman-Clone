package main;

public class Timer {
	private long deltaTime;
	private long lastTime;
	private long timeCount;
	private long delta;
	private boolean paused;
	private boolean showInfo;
	private String name;

	public Timer() {
		this("", false);
	}

	public Timer(String name, boolean showInfo) {
		this.name = name;
		this.showInfo = showInfo;
		lastTime = System.currentTimeMillis();
	}

	public void reset() {
		timeCount = 0;
		lastTime = System.currentTimeMillis();
		deltaTime = 0;
		paused = false;
	}

	public void pause() {
		if (!paused) {
			delta = System.currentTimeMillis() - lastTime;
			paused = true;
		}
	}
	
	public boolean paused() {
		return paused;
	}

	public void resume() {
		paused = false;
		lastTime = System.currentTimeMillis() - delta;
		delta = 0;
	}

	public void update() {
		long now = System.currentTimeMillis();
		deltaTime += now - lastTime;
		lastTime = now;
		if (deltaTime > 1000) {
			timeCount += 1;
			deltaTime -= 1000;
			if (showInfo)
				System.out.println(name + ": " + timeCount);
		}
	}

	public long getTimeCount() {
		return timeCount;
	}
}
