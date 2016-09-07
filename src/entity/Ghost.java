package entity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

import algorithm.PathFinding;
import main.MainGame;
import main.Map;
import util.Mode;
import util.SoundEffect;
import util.Util;

public class Ghost extends Entity {

	public static final int BLINKY = 0;
	public static final int PINKY = 1;
	public static final int INKY = 2;
	public static final int CLYDE = 3;

	// animate
	private BufferedImage sprite;
	private int currentFrame;
	private int numberCurrentFrame;
	private int offsetColImage;
	private int offsetRowImage;

	private Cell scatterTarget = new Cell();
	private Cell target = new Cell();

	private int type;
	private Pacman pacman;

	// status
	private int blinkCount = 0;
	private int numberFrameBlink = 0;
	private boolean doorOpen;
	private boolean eaten;
	private boolean eatable;

	private Cell[] pathToHome;
	private int nextCellIndexToHome = 0;

	private Cell homeCell = new Cell();

	public Ghost(MainGame game, BufferedImage sprite, int type) {
		super(game);
		this.type = type;
		this.sprite = sprite;
		pacman = game.getPacman();
		switch (type) {
		case BLINKY:
			homeCell.set(14, 15);
			scatterTarget.set(-3, Map.COLS - 2);
			break;
		case PINKY:
			homeCell.set(14, 12);
			scatterTarget.set(-3, 1);
			break;
		case INKY:
			homeCell.set(14, 11);
			scatterTarget.set(Map.ROWS + 1, Map.COLS - 1);
			break;
		default:
			homeCell.set(14, 16);
			scatterTarget.set(Map.ROWS + 1, 0);
			break;
		}
	}

	public void init() {
		blinkCount = 0;
		eaten = false;
		doorOpen = false;
		eatable = false;
		pathToHome = null;
		setTarget(scatterTarget);
		nextCellIndexToHome = 0;
		resetOffsetImageRow();
		setCenterCell(homeCell.row, homeCell.col);
		setDirection(UP);
	}

	private void resetOffsetImageRow() {
		if (type == BLINKY) {
			offsetRowImage = 0;
		} else if (type == PINKY) {
			offsetRowImage = 1;
		} else if (type == INKY) {
			offsetRowImage = 2;
		} else {
			offsetRowImage = 3;
		}
	}

	@Override
	public void update() {
		if (isHidden())
			goHome();
		else if (insideGhostHouse()) {
			updateWhenInsideHouse();
		} else {
			updateWhenOutsideHouse();
			boundedByMap();
		}
		updatePosition();
		updateFrame();
	}

	private void updateWhenOutsideHouse() {
		int row = map.getRow(y);
		int col = map.getCol(x);

		if (game.isMode(Mode.FRIGHTENEND)) {
			if (!isHidden())
				eatable = true;
		} else {
			eatable = false;
		}
		if (eatable && checkCollisionWithPacman()) {
			SoundEffect.EATGHOST.play();
			eaten = true;
			eatable = false;
			pathToHome = null;
			nextCellIndexToHome = 0;
			final int factor = game.getFactorEatenGhostScore();
			game.increaseScore(factor * 200);
			game.addSchedule(new Schedule(game.getTimeCount(), 2) {
				@Override
				public void update() {
					game.drawEatenGhostScore(factor * 200, row, col);
				}
			});
			game.setFactorEatenGhostScore(Math.min(8, factor * 2));
			return;
		}

		if (map.isIntersectPoint(row, col) && map.isMiddleCell(x, y)) {
			if (isElroy())
				setTarget(pacman.getCurrentCell());
			else
				updateTarget();
			if(game.isMode(Mode.FRIGHTENEND))
				setRandomDirection();
			else 
				chooseDirection();
		}

		if (checkCollisionWithWall() && !map.isIntersectPoint(row, col)) {
			if (!isFrightend()) {
				if (!map.isWall(row + dx, col - dy))
					setDirection(-dy, dx);
				else if (!map.isWall(row - dx, col + dy))
					setDirection(dy, -dx);
			} else
				setRandomDirection();
		}

		if (insideTunnel())
			setSpeedByMaxSpeed(game.getCurrentLevel().ghostTunnelSpeed);
		else if (isFrightend())
			setSpeedByMaxSpeed(game.getCurrentLevel().ghostFrightSpeed);
		else if (isElroy())
			setElroySpeed();
		else
			setSpeedByMaxSpeed(game.getCurrentLevel().ghostSpeed);
		boundedByMap();
	}
	
	public boolean eatable() {
		return eatable;
	}

	private void updateWhenInsideHouse() {
		int row = map.getRow(y);
		int col = map.getCol(x);
		if (doorOpen) {
			setSpeedByMaxSpeed(game.getCurrentLevel().ghostSpeed);
			if (col == homeCell.col) {
				if (row == 14 && map.isMiddleCell(x, y)) {
					setCenterRow(row);
					if (col < 14)
						setDirection(RIGHT);
					else
						setDirection(LEFT);
				}
			} else if ((col == 13 || col == 14) && map.isMiddleCell(x, y)) {
				setCenterCol(col);
				setDirection(UP);
			}
		} else {
			setSpeedByMaxSpeed(1);
		}
		if (checkCollisionWithWall())
			reverseDirection();
	}

	public void goHome() {
		setSpeedByMaxSpeed(3);
		if (pathToHome == null) {
			pathToHome = PathFinding.getPath(getCurrentCell(), homeCell, map);
		}
		Cell nextCell = pathToHome[nextCellIndexToHome];
		Cell currentCell = getCurrentCell();

		if (nextCell.equals(currentCell)) {
			if (dy == -1 && y + dy * currentSpeed < nextCell.getCenterY()) {
				y = nextCell.getCenterY();
				++nextCellIndexToHome;
			} else if (dy == 1 && y + dy * currentSpeed > nextCell.getCenterY()) {
				y = nextCell.getCenterY();
				++nextCellIndexToHome;
			} else if (dx == 1 && x + dx * currentSpeed > nextCell.getCenterX()) {
				x = nextCell.getCenterX();
				++nextCellIndexToHome;
			} else if (dx == -1 && x + dx * currentSpeed < nextCell.getCenterX()) {
				x = nextCell.getCenterX();
				++nextCellIndexToHome;
			}
		} else
			setDirection(nextCell.col - currentCell.col, nextCell.row - currentCell.row);

		if (nextCellIndexToHome >= pathToHome.length) {
			nextCellIndexToHome = pathToHome.length - 1;
			eaten = false;
			pathToHome = null;
			setDirection(UP);
			setSpeedByMaxSpeed(1);
		}
	}

	private void updateFrame() {
		if (!isFrightend() && !isHidden()) {
			resetOffsetImageRow();
			switch (getDirection()) {
			case UP:
				offsetColImage = 0;
				break;
			case DOWN:
				offsetColImage = 2;
				break;
			case LEFT:
				offsetColImage = 4;
				break;
			case RIGHT:
				offsetColImage = 6;
				break;
			}
			numberCurrentFrame = 2;
		} else if (isHidden()) {
			offsetRowImage = 4;
			numberCurrentFrame = 1;
			switch (getDirection()) {
			case UP:
				offsetColImage = 4;
				break;
			case DOWN:
				offsetColImage = 5;
				break;
			case LEFT:
				offsetColImage = 6;
				break;
			case RIGHT:
				offsetColImage = 7;
				break;
			}
		} else {
			offsetRowImage = 4;
			offsetColImage = 0;
			numberCurrentFrame = isBlink() ? 4 : 2;
		}
		currentFrame = Math.min(currentFrame, offsetColImage + numberCurrentFrame - 1);
		currentFrame = Math.max(currentFrame, offsetColImage);
		if (game.getTick() % 10 == 0) {
			currentFrame = offsetColImage + (currentFrame + 1) % numberCurrentFrame;
			numberFrameBlink = (numberFrameBlink + 1) % 4;
			if (isBlink() && numberFrameBlink == 3)
				blinkCount = Math.max(blinkCount - 1, 0);
		}

	}

	public boolean checkCollisionWithPacman() {
		if (map.getCol(x) == map.getCol(pacman.x) && Math.abs(y - pacman.y) < Map.SIZE / 2)
			return true;
		else if (map.getRow(y) == map.getRow(pacman.y) && Math.abs(x - pacman.x) < Map.SIZE / 2)
			return true;
		else
			return false;
	}

	public boolean insideTunnel() {
		int row = map.getRow(y);
		int col = map.getCol(x);
		return row == 14 && (col < 4 || col >= Map.COLS - 4);
	}

	public boolean insideGhostHouse() {
		int row = map.getRow(y);
		int col = map.getCol(x);
		return row <= 15 && row >= 13 && col <= 16 && col >= 11;
	}

	public boolean isElroy() {
		return type == BLINKY && game.getRemainFood() <= game.getCurrentLevel().elroyDotsLeftPart1;
	}

	public boolean isHidden() {
		return eaten;
	}

	public boolean isFrightend() {
		return eatable;
	}

	public void setElroySpeed() {
		if (game.getRemainFood() <= game.getCurrentLevel().elroyDotsLeftPart2)
			setSpeedByMaxSpeed(game.getCurrentLevel().elroySpeedPart2);
		else
			setSpeedByMaxSpeed(game.getCurrentLevel().elroySpeedPart1);
	}

	public void openDoor() {
		doorOpen = true;
	}

	public boolean isDoorOpened() {
		return doorOpen;
	}

	private void setRandomDirection() {
		int row = map.getRow(y);
		int col = map.getCol(x);
		ArrayList<Point> listValidDir = new ArrayList<>();
		if (!map.isWall(row + dx, col - dy))
			listValidDir.add(new Point(-dy, dx));
		if (!map.isWall(row - dx, col + dy))
			listValidDir.add(new Point(dy, -dx));
		if (!map.isWall(row + dy, col + dx))
			listValidDir.add(new Point(dx, dy));
		int randomIndex = Util.random(listValidDir.size() - 1);
		setDirection(listValidDir.get(randomIndex).x, listValidDir.get(randomIndex).y);
	}

	public void reverseDirection() {
		dx *= -1;
		dy *= -1;
		updateTarget();
	}

	public void blink(int blinkCount) {
		this.blinkCount = blinkCount;
	}

	public boolean isBlink() {
		return blinkCount > 0;
	}

	private void updateTarget() {
		if (game.isMode(Mode.CHASE)) {
			int pacmanRow = map.getRow(pacman.y);
			int pacmanCol = map.getCol(pacman.x);
			if (type == BLINKY) {
				setTarget(pacmanRow, pacmanCol);
			} else if (type == PINKY) {
				setTarget(pacmanRow + 3 * pacman.dy, pacmanCol + 3 * pacman.dx);
			} else if (type == INKY) {
				Ghost blinky = game.getGhost(BLINKY);
				setTarget(2 * (pacmanRow + 2 * pacman.dy) - map.getRow(blinky.y),
						2 * (pacmanCol + 2 * pacman.dx) - map.getCol(blinky.x));
			} else {
				int deltax = map.getCol(x) - pacmanCol;
				int deltay = map.getRow(y) - pacmanRow;
				if (deltax * deltax + deltay * deltay >= 64) {
					setTarget(pacmanRow, pacmanCol);
				} else {
					setTarget(scatterTarget);
				}

			}
		} else if (game.isMode(Mode.SCATTER)) {
			setTarget(scatterTarget);
		}
	}

	private void setTarget(int row, int col) {
		target.row = row;
		target.col = col;
	}

	private void setTarget(Cell target) {
		setTarget(target.row, target.col);
	}

	// algorithm
	private void chooseDirection() {
		byte dir = -1;
		Cell currentCell = getCurrentCell();
		if ((target.row >= 0 && target.row <= 5 && currentCell.row < Map.ROWS && currentCell.row >= Map.ROWS - 5)
				|| (currentCell.row >= 0 && currentCell.row <= 5 && target.row < Map.ROWS
						&& target.row >= Map.ROWS - 5))
			dir = PathFinding.greedyBestFirstSearch(currentCell, target, getDirection(), map);
		else
			dir = PathFinding.aStar(currentCell, target, getDirection(), map);
		if (dir != -1)
			setDirection(dir);
		else
			normalChooseDirection();
	}

	private void normalChooseDirection() {
		int row = map.getRow(y);
		int col = map.getCol(x);
		ArrayList<Cell> list = new ArrayList<>();
		if (!map.isWall(row + dy, col + dx)) {
			list.add(new Cell(row + dy, col + dx));
		}
		if (!map.isWall(row - dx, col + dy)) {
			list.add(new Cell(row - dx, col + dy));
		}
		if (!map.isWall(row + dx, col - dy)) {
			list.add(new Cell(row + dx, col - dy));
		}
		// get minimum value
		Cell min = Collections.min(list, (c1, c2) -> c1.distance(target) - c2.distance(target));
		setDirection(min.col - col, min.row - row);
	}

	@Override
	public void draw(Graphics2D g) {
		if (pathToHome != null && map.isShowGrid())
			drawPath(g);

		drawGhost(g);

		if (map.isShowGrid() && !isFrightend() && !isHidden() && !insideGhostHouse())
			drawTarget(g);
	}

	private void drawPath(Graphics2D g) {
		Color color;
		switch (type) {
		case BLINKY:
			color = Color.RED;
			break;
		case PINKY:
			color = Color.PINK;
			break;
		case INKY:
			color = Color.CYAN;
			break;
		default:
			color = Color.ORANGE;
			break;
		}
		g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 255 / 2));
		for (Cell c : pathToHome)
			g.fillRect(c.col * Map.SIZE, c.row * Map.SIZE, Map.SIZE, Map.SIZE);
	}

	private void drawGhost(Graphics2D g) {
		int size = 30;
		g.drawImage(sprite, (int) (x - size / 2), (int) (y - size / 2), (int) (x + size / 2), (int) (y + size / 2),
				currentFrame * 20, offsetRowImage * 20, (currentFrame + 1) * 20, offsetRowImage * 20 + 20, null);
	}

	private void drawTarget(Graphics2D g) {
		switch (type) {
		case BLINKY:
			g.setColor(Color.RED);
			break;
		case PINKY:
			g.setColor(Color.PINK);
			break;
		case INKY:
			g.setColor(Color.CYAN);
			break;
		case CLYDE:
			g.setColor(Color.ORANGE);
			break;
		}
		Stroke old = g.getStroke();
		g.setStroke(new BasicStroke(2));
		g.drawRect(target.col * Map.SIZE, target.row * Map.SIZE, Map.SIZE, Map.SIZE);
		g.setStroke(old);
	}
}