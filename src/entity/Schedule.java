package entity;

public abstract class Schedule {
	public final long start;
	public final int time;
	public Schedule(long start, int time) {
		this.start = start;
		this.time = time;
	}
	public abstract void update();
	public void end() {
	}
}