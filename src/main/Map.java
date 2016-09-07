package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;

import entity.Cell;
import entity.Entity;
import util.Theme;
import util.Util;

public class Map {
	// constants
	public static final byte EMPTY = 0x00;
	public static final byte FOOD = 0x01;
	public static final byte ENERGY = 0x02;
	public static final byte WALL = 0x04;
	public static final byte INTERSECT = 0x08;

	public static final int ROWS = 31;
	public static final int COLS = 28;
	public static final int SIZE = 15;

	public static final BufferedImage img = Util.loadImage("map.png");

	private byte[][] grid;

	private boolean isShowGrid = false;
	private boolean isBlinkMap = false;
	private boolean toggleColor = false;
	private boolean showEnergy = false;
	private boolean isDrawFood = false;

	private int maxFood = 0;

	private MainGame game;

	public Map(MainGame game) {
		grid = new byte[ROWS][COLS];
		this.game = game;
	}

	public void init() {
		maxFood = 0;
		toggleColor = false;
		isBlinkMap = false;
		loadMapFromImage("mapdata.png");
	}

	public void update() {
		if (game.getTick() % 10 == 0) {
			if (isBlinkMap)
				toggleColor = !toggleColor;
			showEnergy = !showEnergy;
		}
	}

	// get column from x
	public int getCol(double x) {
		return (int) (x / SIZE);
	}

	// get row from y
	public int getRow(double y) {
		return (int) (y / SIZE);
	}

	public int getWidth() {
		return COLS * SIZE;
	}

	public int getHeight() {
		return ROWS * SIZE;
	}

	public int getMaxFood() {
		return maxFood;
	}

	public List<Cell> neighbors(int row, int col) {
		List<Cell> neighborsList = new ArrayList<>();
		if (row - 1 >= 0 && !isWall(row - 1, col)) {
			neighborsList.add(new Cell(row - 1, col));
		}
		if (row + 1 < ROWS && !isWall(row + 1, col)) {
			neighborsList.add(new Cell(row + 1, col));
		}
		if (col - 1 >= 0 && !isWall(row, col - 1)) {
			neighborsList.add(new Cell(row, col - 1));
		}
		if (col + 1 < COLS && !isWall(row, col + 1)) {
			neighborsList.add(new Cell(row, col + 1));
		}
		return neighborsList;
	}

	public List<Cell> neighbors(Cell cell) {
		return neighbors(cell.row, cell.col);
	}

	public void setShowGrid(boolean value) {
		isShowGrid = value;
	}

	public void setBlink(boolean value) {
		this.isBlinkMap = value;
	}
	
	public void setDrawFood(boolean value) {
		isDrawFood = value;
	}
	
	public boolean isWall(int row, int col) {
		try {
			return grid[row][col] == WALL;
		} catch (ArrayIndexOutOfBoundsException e) {
			if(row == 14 && (col < 4 || col >= Map.COLS - 4))
				return false;
			else 
				return true;
		}
	}

	public boolean isFood(int row, int col) {
		try {
			return (grid[row][col] & FOOD) == FOOD;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isEnergy(int row, int col) {
		try {
			return (grid[row][col] & ENERGY) == ENERGY;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isIntersectPoint(int row, int col) {
		try {
			return (grid[row][col] & INTERSECT) == INTERSECT;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public boolean isEmpty(int row, int col) {
		try {
			return grid[row][col] == EMPTY || grid[row][col] == INTERSECT;
		} catch (ArrayIndexOutOfBoundsException e) {
			return row == 14 && (col < 4 || col >= Map.COLS - 4);
		}
	}

	public void removeFoodEnergy(int row, int col) {
		try {
			byte type = grid[row][col];
			if ((type & FOOD) == FOOD || (type & ENERGY) == ENERGY) {
				if ((type & INTERSECT) == INTERSECT)
					grid[row][col] = INTERSECT;
				else
					grid[row][col] = EMPTY;
			}
		} catch (ArrayIndexOutOfBoundsException e) {

		}
	}

	public boolean isShowGrid() {
		return isShowGrid;
	}

	public boolean isInGhostHouse(int row, int col) {
		if (row >= 13 && row <= 15 && col >= 11 && col <= 16)
			return true;
		else if (row == 12 && (col == 13 || col == 14))
			return true;
		else
			return false;
	}

	public boolean isMiddleCell(double x, double y) {
		double radiusAccept = 0.51 * Entity.MAXSPEED;
		double dx = x - getCol(x) * Map.SIZE - Map.SIZE / 2;
		double dy = y - getRow(y) * Map.SIZE - Map.SIZE / 2;
		return dx * dx + dy * dy < radiusAccept * radiusAccept;
	}

	public void loadMapFromImage(String fileImage) {
		BufferedImage image = Util.loadImage(fileImage);
		BufferedImage buffer = new BufferedImage(COLS, ROWS, BufferedImage.TYPE_INT_RGB);
		buffer.createGraphics().drawImage(image, 0, 0, null);
		int[] pixels = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();

		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				int color = pixels[col + COLS * row] & 0xffffff;

				if (color == 0xb6b6b6) { // GRAY
					grid[row][col] = FOOD;
					maxFood++;
				} else if (color == 0x000000) { // BLACK
					grid[row][col] = EMPTY;
				} else if (color == 0x00ff00) { // GREEN
					grid[row][col] = ENERGY;
					maxFood++;
				} else if (color == 0x0033ff) { // BLUE
					grid[row][col] = WALL;
				} else if (color == 0xff0000) { // RED
					grid[row][col] = FOOD | INTERSECT;
					maxFood++;
				} else if (color == 0xffff00) { // YELLOW
					grid[row][col] = INTERSECT;
				}
			}
		}
	}

	public void draw(Graphics2D g) {
		// draw image background
		drawImageBackGround(g);
		
		if (isDrawFood)
			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLS; col++) {
					if (isFood(row, col))
						drawFood(g, row, col);
					else if (isEnergy(row, col))
						drawEnergy(g, row, col);
					if (isShowGrid && isIntersectPoint(row, col))
						drawIntersectPoint(g, row, col);
				}
			}

		// draw grid
		if (isShowGrid)
			drawGrid(g);
	}

	private void drawImageBackGround(Graphics2D g) {
		if (!toggleColor) {
			g.drawImage(img, 0, 0, COLS * SIZE / 2, ROWS * SIZE, 0, 0, 125, 278, null);
			g.drawImage(img, COLS * SIZE, 0, COLS * SIZE - COLS * SIZE / 2, ROWS * SIZE, 0, 0, 125, 278, null);
		} else {
			g.drawImage(img, COLS * SIZE - COLS * SIZE / 2, 0, COLS * SIZE, ROWS * SIZE, 126, 0, 251, 278, null);
			g.drawImage(img, COLS * SIZE / 2, 0, 0, ROWS * SIZE, 126, 0, 251, 278, null);
		}
	}

	private void drawGrid(Graphics2D g) {
		int rows = game.getHeight() / SIZE;
		int cols = game.getWidth() / SIZE;
		g.setColor(Color.GRAY);
		for (int row = 0; row <= rows; row++) {
			g.drawLine(0, -MainGame.OFFSETY + row * SIZE, cols * SIZE, -MainGame.OFFSETY + row * SIZE);
		}

		for (int col = 0; col <= cols; col++) {
			g.drawLine(col * SIZE, -MainGame.OFFSETY, col * SIZE, -MainGame.OFFSETY + rows * SIZE);
		}
	}

	private void drawFood(Graphics2D g, int row, int col) {
		g.setColor(Theme.FOOD_COLOR);
		g.fillRect(col * Map.SIZE + Map.SIZE / 2 - 2, row * Map.SIZE + Map.SIZE / 2 - 2, 4, 4);
	}

	private void drawEnergy(Graphics2D g, int row, int col) {
		if (showEnergy) {
			g.setColor(Theme.FOOD_COLOR);
			g.fillOval(col * Map.SIZE + Map.SIZE / 2 - 6, row * Map.SIZE + Map.SIZE / 2 - 6, 12, 12);
		}
	}

	private void drawIntersectPoint(Graphics2D g, int row, int col) {
		g.setColor(Theme.INTERSECTPOINT_COLOR);
		g.fillRect(col * SIZE, row * SIZE, SIZE, SIZE);
	}
}
