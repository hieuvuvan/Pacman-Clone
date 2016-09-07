package entity;

import java.awt.Graphics2D;

import main.MainGame;
import main.Map;

public abstract class Entity {
	// constants
	public static final byte UP = 0;
	public static final byte DOWN = 1;
	public static final byte RIGHT = 2;
	public static final byte LEFT = 3;
	public static final double MAXSPEED = 2;

	// move and position
	protected double x, y;
	protected int dx;
	protected int dy;
	protected double currentSpeed;
	protected int radius = Map.SIZE / 2;

	protected MainGame game;
	protected Map map;

	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Entity(MainGame game) {
		this.game = game;
		this.map = game.getMap();
	}
	
	public void setSpeed(double speed) {
		this.currentSpeed = speed;
	}
	
	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public void setSpeedByMaxSpeed(double percent) {
		currentSpeed = percent * MAXSPEED;
	}

	public void setCenterCell(int row, int col) {
		x = col * Map.SIZE + Map.SIZE / 2;
		y = row * Map.SIZE + Map.SIZE / 2;
	}
	
	public void setCenterCell(Cell c) {
		setCenterCell(c.row, c.col);
	}

	protected void setCenterRow(int row) {
		y = Map.SIZE * row + Map.SIZE / 2;
	}

	protected void setCenterCol(int col) {
		x = Map.SIZE * col + Map.SIZE / 2;
	}
	
	protected void setDirection(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}

	public void setDirection(int dir) {
		switch (dir) {
		case UP:
			setDirection(0, -1);
			break;
		case DOWN:
			setDirection(0, 1);
			break;
		case LEFT:
			setDirection(-1, 0);
			break;
		case RIGHT:
			setDirection(1, 0);
			break;
		}
	}

	public byte getDirection() {
		if (dy == -1)
			return UP;
		else if (dy == 1)
			return DOWN;
		else if (dx == 1)
			return RIGHT;
		else
			return LEFT;
	}
	
	public Cell getCurrentCell() {
		return new Cell(map.getRow(y), map.getCol(x));
	}

	protected void boundedByMap() {
		if (x > map.getWidth())
			x = 0;
		else if (x < 0)
			x = map.getWidth();

		if (y > map.getHeight())
			y = 0;
		else if (y < 0)
			y = map.getHeight();
	}

	protected boolean checkCollisionWithWall() {
		int row = map.getRow(radius * dy + y + dy * currentSpeed);
		int col = map.getCol(radius * dx + x + dx * currentSpeed);
		if (map.isWall(row, col)) {
			return true;
		} else {
			return false;
		}
	}

	protected void updatePosition() {
		x += dx * currentSpeed;
		y += dy * currentSpeed;
		if (dx == 0)
			setCenterCol(map.getCol(x));
		else if (dy == 0)
			setCenterRow(map.getRow(y));
	}

	public abstract void draw(Graphics2D g);

	public abstract void update();
}
