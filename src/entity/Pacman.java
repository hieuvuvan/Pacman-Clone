package entity;

import java.awt.Graphics2D;
import java.awt.geom.Arc2D;

import main.MainGame;
import main.Map;
import util.Key;
import util.Mode;
import util.SoundEffect;
import util.State;
import util.Theme;

public class Pacman extends Entity {
	// animate
	private int alpha = 0;
	private int frame = 0;

	private int nextdx;
	private int nextdy;

	// status
	private int numberFoodEatean;
	private int lifes;

	public Pacman(MainGame game) {
		super(game);
	}

	public void init() {
		setNextDirection(LEFT);
		setDirection(LEFT);
		setCenterCell(17, 17);
		alpha = 0;
	}

	public void setLifes(int lifes) {
		this.lifes = Math.max(0, lifes);
	}

	public int getLifes() {
		return this.lifes;
	}

	@Override
	public void update() {
		int row = map.getRow(y);
		int col = map.getCol(x);
		if (map.isMiddleCell(x, y)) {
			if (map.isFood(row, col)) {
				SoundEffect.EATFOOD.play();
				map.removeFoodEnergy(row, col);
				game.increaseScore(10);
				numberFoodEatean++;
				if (game.isMode(Mode.FRIGHTENEND))
					setSpeedByMaxSpeed(game.getCurrentLevel().dotEatingFrightSpeed);
				else
					setSpeedByMaxSpeed(game.getCurrentLevel().dotEatingSpeed);
			} else if (map.isEnergy(row, col)) {
				SoundEffect.EATEENERGY.play();
				map.removeFoodEnergy(row, col);
				game.increaseScore(20);
				numberFoodEatean++;
				game.changeFrightendMode();
				setSpeedByMaxSpeed(game.getCurrentLevel().dotEatingFrightSpeed);
			} else {
				setSpeedByMaxSpeed(game.getCurrentLevel().pacmanSpeed);
			}
			if (game.isFruit(row, col)) {
				if (!game.isEatenFruit()) {
					SoundEffect.EATFRUIT.play();
					final int r = row, c = col;
					game.addSchedule(new Schedule(game.getTimeCount(), 2) {
						@Override
						public void update() {
							game.drawEatenFruitScore(r, c);
						}
					});
					game.increaseScore(game.getCurrentLevel().fruitScore);
					game.eatFruit();
				}
			}
		}
		updateDirection();
		updatePosition();
		updateFrame();
		if (checkCollisionWithWall()) {
			setCenterCell(row, col);
			alpha = 0;
		}
		boundedByMap();
	}
	
	public void resetFoodEaten() {
		this.numberFoodEatean = 0;
	}

	private void updateDirection() {
		int row = map.getRow(y);
		int col = map.getCol(x);

		// kiem tra ghost house
		if (row == 11 && (col == 13 || col == 14) && getNextDirection() == DOWN)
			return;
		if (dx != nextdx || dy != nextdy) { // 2 vector khac nhau
			if (dx + nextdx == 0 && dy + nextdy == 0) { // 2 vector nguoc chieu
				setDirection(getNextDirection());
			} else {
				if (map.isMiddleCell(x, y) && !map.isWall(row + nextdy, col + nextdx)) {
					setDirection(getNextDirection());
				}
			}
		}
	}

	public void updateFrame() {
		if (!game.isState(State.DYING)) {
			if (game.getTick() % 2 == 0)
				alpha = (int) (120 * Math.sin(frame++));
		} else {
			if (game.getTick() % 2 == 0)
				alpha = Math.min(360, alpha + 8);
		}
	}
	
	public boolean isDying() {
		return game.isState(State.DYING) && alpha < 360;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	private void setNextDirection(int dx, int dy) {
		this.nextdx = dx;
		this.nextdy = dy;
	}

	public void setNextDirection(int dir) {
		switch (dir) {
		case UP:
			setNextDirection(0, -1);
			break;
		case DOWN:
			setNextDirection(0, 1);
			break;
		case LEFT:
			setNextDirection(-1, 0);
			break;
		case RIGHT:
			setNextDirection(1, 0);
			break;
		}
	}

	public byte getNextDirection() {
		if (nextdy == -1)
			return UP;
		else if (nextdy == 1)
			return DOWN;
		else if (nextdx == 1)
			return RIGHT;
		else
			return LEFT;
	}

	public int getNumberFoodEaten() {
		return numberFoodEatean;
	}

	@Override
	public void draw(Graphics2D g) {
		int size = (int) (Map.SIZE * 1.4);
		g.setColor(Theme.PACMAN_COLOR);
		g.fill(new Arc2D.Double(x - size / 2, y - size / 2, size, size, getAlphaStart(), 360 - alpha, Arc2D.PIE));
	}

	private int getAlphaStart() {
		switch (getDirection()) {
		case UP:
			return 90 + alpha / 2;
		case DOWN:
			return -(90 - alpha / 2);
		case RIGHT:
			return alpha / 2;
		default:
			return -(180 - alpha / 2);
		}
	}

	public void keyPress(Key key) {
		switch (key) {
		case UP:
			setNextDirection(UP);
			break;
		case DOWN:
			setNextDirection(DOWN);
			break;
		case LEFT:
			setNextDirection(LEFT);
			break;
		case RIGHT:
			setNextDirection(RIGHT);
			break;
		default:
			break;
		}
	}

	public void setNumberFoodEatean(int i) {
		numberFoodEatean = i;
	}

}
