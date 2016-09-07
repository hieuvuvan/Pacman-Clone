package entity;

import main.Map;

public class Cell {
	public int row;
	public int col;
	public int prior;

	public Cell(int row, int col, int prior) {
		this.row = row;
		this.col = col;
		this.prior = prior;
	}
	
	public Cell(int row, int col) {
		this(row, col, 0);
	}
	
	public Cell() {
		this(0, 0);
	}

	public Cell(Cell c) {
		this(c.row, c.col, 0);
	}

	public int getID() {
		return col + Map.COLS * row;
	}

	public void set(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public static Cell getCellFromID(int id) {
		return new Cell(id / Map.COLS, id % Map.COLS);
	}

	public void set(Cell other) {
		set(other.row, other.col);
	}
	
	public int getCenterX() {
		return col * Map.SIZE + Map.SIZE / 2;
	}
	
	public int getCenterY() {
		return row * Map.SIZE + Map.SIZE / 2;
	}

	public int distance(int row, int col) {
		int deltaRow = this.row - row;
		int deltaCol = this.col - col;
		return deltaCol * deltaCol + deltaRow *deltaRow;
	}

	public int distance(Cell other) {
		return distance(other.row, other.col);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Cell) {
			Cell c = (Cell) obj;
			return row == c.row && col == c.col;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return row * 7 + col * 13;
	}

	@Override
	public String toString() {
		return "[" + row + ", " + col + "]"; 
	}
}
