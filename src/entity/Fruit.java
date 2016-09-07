package entity;

public class Fruit extends Cell {
	private boolean eatean;
	private boolean showing;

	public Fruit(int row, int col) {
		super(row, col);
	}
	
	public void reset() {
		row = 0;
		col = 0;
		eatean = false;
		showing = false;
	}

	public boolean isEatean() {
		return eatean;
	}

	public void setEatean(boolean eatean) {
		this.eatean = eatean;
	}

	public boolean isShowing() {
		return showing;
	}

	public void setShowing(boolean showing) {
		this.showing = showing;
	}

}
