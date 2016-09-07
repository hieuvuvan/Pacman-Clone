package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import entity.Cell;
import entity.Fruit;
import entity.Ghost;
import entity.Pacman;
import entity.Schedule;
import util.Key;
import util.Mode;
import util.SoundEffect;
import util.State;
import util.Theme;
import util.Util;

@SuppressWarnings("serial")
public class MainGame extends AbstractGamePanel {

	public final static int[] NUMBER_SHOW_FRUIT = { 70, 140, 190, 240 };
	public final static int TIME_SHOW_FRUIT = 15;

	private BufferedImage sprite;

	// score
	private long dynamicScore;
	private long score;
	private long highScore = 0;
	private boolean blinkHighScore = false;
	private boolean toggleHighScore = false;
	private int factorEatenGhostScore = 1;
	private int countBlinkScore = 0;
	private boolean blinkScore = false;
	private boolean toggleScore = false;

	private Map map;

	// pacman
	private Pacman pacman;
	private Ghost blinky, pinky, inky, clyde;
	private Ghost[] ghosts;

	private Level[] levels;
	private Level currentLevel;
	private int currentIndexLevel = 4;

	private Mode currentMode;
	private Mode previousMode;

	// timer
	private Timer mainTimer;
	private Timer measureMainTimer;
	private Timer switchModeTimer;
	private Timer frightendTimer;
	private long timeDelayCount = 0;
	private LinkedList<Schedule> schedules;

	private Fruit fruit;
	private int numFruitShown;
	private long fruitTimeCount;

	private State state;

	// sound
	private SoundEffect currentLoop;
	private boolean changedLoop = false;

	public MainGame() {
		setup();
		newGame();
	}

	public void newGame() {
		currentIndexLevel = 0;
		dynamicScore = score = 0;
		highScore = readHighScoreFromFile();
		initLevel();
		map.setDrawFood(false);
		mainTimer.reset();
		state = State.START;
		pacman.setLifes(3);
		timeDelayCount = mainTimer.getTimeCount() + 3;
		SoundEffect.STARTGAME.play();
	}

	public void reset() {
		currentMode = Mode.SCATTER;
		currentLevel.reset();
		switchModeTimer.reset();
		frightendTimer.reset();
		measureMainTimer.reset();
		toggleHighScore = false;
		toggleScore = false;
		blinkHighScore = false;
		blinkScore = false;
		schedules.clear();
		pacman.init();
		for (Ghost ghost : ghosts)
			ghost.init();
	}

	private void pauseTime() {
		measureMainTimer.pause();
		frightendTimer.pause();
		switchModeTimer.pause();
	}

	private void resumeTime() {
		measureMainTimer.resume();
		frightendTimer.resume();
		switchModeTimer.resume();
	}

	public void initLevel() {
		state = State.LOADING;
		pacman.resetFoodEaten();
		fruit.reset();
		numFruitShown = 0;
		currentLevel = levels[currentIndexLevel];
		map.init();
		reset();
		state = State.READY;
		pauseTime();
		currentLoop = SoundEffect.AMBIENT[0];
	}

	public void setup() {
		sprite = Util.loadImage("sprite.png");
		levels = Level.getAllLevel();
		map = new Map(this);
		pacman = new Pacman(this);
		blinky = new Ghost(this, sprite, Ghost.BLINKY);
		pinky = new Ghost(this, sprite, Ghost.PINKY);
		inky = new Ghost(this, sprite, Ghost.INKY);
		clyde = new Ghost(this, sprite, Ghost.CLYDE);

		ghosts = new Ghost[] { blinky, pinky, inky, clyde };

		schedules = new LinkedList<>();
		measureMainTimer = new Timer("MeasureMain", false);
		switchModeTimer = new Timer("Mode", false);
		frightendTimer = new Timer("Frightend", false);
		mainTimer = new Timer("Main", false);

		fruit = new Fruit(0, 0);
	}

	@Override
	public void update() {
		if (state == State.LOADING || state == State.WIN || state == State.GAMEOVER)
			return;

		// switch sound
		if (state == State.PLAY) {
			int foodEaten = pacman.getNumberFoodEaten(), maxFood = map.getMaxFood();
			if (!changedLoop && currentMode != Mode.FRIGHTENEND) {
				if (foodEaten == maxFood / 4) {
					currentLoop.stop();
					currentLoop = SoundEffect.AMBIENT[1];
					changedLoop = true;
				} else if (foodEaten == maxFood / 2) {
					currentLoop.stop();
					currentLoop = SoundEffect.AMBIENT[2];
					changedLoop = true;
				} else if (foodEaten == 3 * maxFood / 4) {
					currentLoop.stop();
					currentLoop = SoundEffect.AMBIENT[3];
					changedLoop = true;
				}
			} else if (foodEaten != maxFood / 4 && foodEaten != maxFood / 2 && foodEaten != 3 * maxFood / 4)
				changedLoop = false;

			if (!currentLoop.isRunning())
				currentLoop.loop();
		} else {
			currentLoop.stop();
		}
		mainTimer.update();
		if (numFruitShown < currentLevel.fruit && pacman.getNumberFoodEaten() >= NUMBER_SHOW_FRUIT[numFruitShown]
				&& !fruit.isShowing()) {
			fruit.set(randomFruit());
			fruit.setShowing(true);
			fruit.setEatean(false);
			fruitTimeCount = mainTimer.getTimeCount() + TIME_SHOW_FRUIT;
		}

		if (mainTimer.getTimeCount() >= fruitTimeCount && fruit.isShowing()) {
			numFruitShown = Math.min(numFruitShown + 1, NUMBER_SHOW_FRUIT.length);
			fruitTimeCount = 0;
			fruit.setShowing(false);
		}
		if (state == State.START) {
			if (mainTimer.getTimeCount() >= timeDelayCount - 2)
				map.setDrawFood(true);
			if (mainTimer.getTimeCount() >= timeDelayCount)
				state = State.READY;
			return;
		}

		map.update();
		updateScore();
		if (state == State.READY) {
			return;
		}
		if (state == State.PAUSE)
			return;
		if (state == State.DYING) {
			pacman.updateFrame();
			if (!pacman.isDying()) {
				if (pacman.getLifes() == 0)
					setState(State.GAMEOVER);
				else {
					setState(State.READY);
					reset();
				}
			}
			return;
		}

		if (state == State.COMPLETELEVEL) {
			if (mainTimer.getTimeCount() >= timeDelayCount) {
				if (currentIndexLevel + 1 == Level.getAllLevel().length) {
					currentIndexLevel = 0;
					state = State.WIN;
					return;
				} else
					currentIndexLevel++;
				SoundEffect.CUTSCENE.play();
				initLevel();
				state = State.READY;
				return;
			} else {
				map.setBlink(true);
				return;
			}
		}

		pacman.update();

		if (getRemainFood() == 0) {
			state = State.COMPLETELEVEL;
			timeDelayCount = mainTimer.getTimeCount() + 2;
			return;
		}

		for (Ghost ghost : ghosts) {
			ghost.update();
			if (!ghost.isHidden() && !ghost.eatable() && !ghost.insideGhostHouse()
					&& ghost.checkCollisionWithPacman()) {
				pacman.setAlpha(0);
				pacman.setLifes(pacman.getLifes() - 1);
				setState(State.DYING);
				SoundEffect.DIE.play();
				return;
			}
		}

		updateTime();
	}

	public void updateScore() {
		if (state == State.GAMEOVER || state == State.COMPLETELEVEL || state == State.WIN)
			dynamicScore = score;

		if (getTick() % 10 == 0) {
			if (blinkScore) {
				toggleScore = !toggleScore;
				if (toggleScore)
					countBlinkScore++;
				if (countBlinkScore == 5) {
					countBlinkScore = 0;
					blinkScore = false;
					toggleScore = false;
				}
			}
			if (blinkHighScore)
				toggleHighScore = !toggleHighScore;
		}

		// score
		if (!blinkScore) {
			if (score - dynamicScore > 1000)
				dynamicScore = dynamicScore + 1000;
			else if (score - dynamicScore > 100)
				dynamicScore = dynamicScore + 100;
			else
				dynamicScore = dynamicScore + 1;
			dynamicScore = Math.min(score, dynamicScore);
		}
		if (dynamicScore % 1000 == 0 && dynamicScore != 0)
			blinkScore = true;
		if (score > highScore) {
			highScore = score;
			writeHighScoreToFile(highScore);
			blinkHighScore = true;
		}
	}

	public void updateTime() {
		measureMainTimer.update();
		if (measureMainTimer.getTimeCount() == 0)
			blinky.openDoor();
		else if (measureMainTimer.getTimeCount() == 1)
			pinky.openDoor();
		else if (measureMainTimer.getTimeCount() == 2) {
			inky.openDoor();
			clyde.openDoor();
		}
		if (switchModeTimer.getTimeCount() == currentLevel.getCurrentTimeModeSwitch()) {
			switchMode();
			switchModeTimer.reset();
		}
		if (currentMode != Mode.FRIGHTENEND) {
			switchModeTimer.update();
		} else {
			frightendTimer.update();
			if (frightendTimer.getTimeCount() == currentLevel.frightTime) {
				for (Ghost ghost : ghosts) {
					ghost.blink(currentLevel.frightBlinkCount);
				}
			} else if (frightendTimer.getTimeCount() > currentLevel.frightTime) {
				boolean noneGhostBlink = true;
				for (Ghost ghost : ghosts) {
					if (ghost.isBlink() && !ghost.isHidden()) {
						noneGhostBlink = false;
						break;
					}
				}
				if (noneGhostBlink) {
					currentMode = previousMode;
					switchModeTimer.resume();
					int foodEaten = pacman.getNumberFoodEaten(), maxFood = map.getMaxFood();
					if (foodEaten >= 3 * maxFood / 4) {
						currentLoop.stop();
						currentLoop = SoundEffect.AMBIENT[3];
					} else if (foodEaten >= maxFood / 2) {
						currentLoop.stop();
						currentLoop = SoundEffect.AMBIENT[2];
					} else if (foodEaten >= maxFood / 4) {
						currentLoop.stop();
						currentLoop = SoundEffect.AMBIENT[3];
					} else {
						currentLoop.stop();
						currentLoop = SoundEffect.AMBIENT[0];
					}

					pacman.setSpeedByMaxSpeed(currentLevel.pacmanSpeed);
					for (Ghost ghost : ghosts)
						if (!ghost.insideGhostHouse() && !ghost.isHidden()) {
							if (!ghost.isElroy())
								ghost.setSpeedByMaxSpeed(currentLevel.ghostSpeed);
							else
								ghost.setElroySpeed();
						}
				}
			}
		}
	}

	public void setState(State state) {
		this.state = state;
	}

	public boolean isState(State state) {
		return this.state == state;
	}

	public Cell randomFruit() {
		Cell finalCell = new Cell(Map.ROWS - 1, Map.COLS - 1);
		Cell random = Cell.getCellFromID(Util.random(finalCell.getID()));
		while (map.isWall(random.row, random.col) || map.isInGhostHouse(random.row, random.col)
				|| random.equals(pacman.getCurrentCell())) {
			random = Cell.getCellFromID(Util.random(finalCell.getID()));
		}
		return random;
	}

	public boolean isFruit(int row, int col) {
		return fruit.isShowing() && fruit.row == row && fruit.col == col;
	}

	public boolean isEatenFruit() {
		return fruit.isShowing() && fruit.isEatean();
	}

	public void eatFruit() {
		numFruitShown = Math.min(numFruitShown + 1, NUMBER_SHOW_FRUIT.length);
		fruit.setEatean(true);
		fruit.setShowing(false);
	}

	public long getTimeCount() {
		return mainTimer.getTimeCount();
	}

	public int getFactorEatenGhostScore() {
		return factorEatenGhostScore;
	}

	public void setFactorEatenGhostScore(int factor) {
		this.factorEatenGhostScore = factor;
	}

	public void changeFrightendMode() {
		if (currentMode != Mode.FRIGHTENEND) {
			previousMode = currentMode;
			for (Ghost ghost : ghosts)
				if (!ghost.insideTunnel() && !ghost.insideGhostHouse())
					ghost.reverseDirection();
			switchModeTimer.pause();
			setFactorEatenGhostScore(1);
		}

		frightendTimer.reset();

		pacman.setSpeedByMaxSpeed(currentLevel.pacmanFrightSpeed);
		for (Ghost ghost : ghosts) {
			if (!ghost.insideGhostHouse() && !ghost.isHidden()) {
				ghost.blink(0);
				ghost.setSpeedByMaxSpeed(currentLevel.ghostFrightSpeed);
			}
		}
		currentLoop.stop();
		currentLoop = SoundEffect.AMBIENTFRIGHT;
		currentMode = Mode.FRIGHTENEND;
	}

	public void switchMode() {
		currentLevel.updateTimeModeSwitch();
		previousMode = currentMode;
		if (currentMode == Mode.CHASE) {
			currentMode = Mode.SCATTER;

		} else {
			currentMode = Mode.CHASE;
		}

		for (Ghost ghost : ghosts)
			if (!ghost.insideTunnel() && !ghost.insideGhostHouse() && !ghost.isElroy() && !ghost.isHidden())
				ghost.reverseDirection();
	}

	public Map getMap() {
		return this.map;
	}

	public Pacman getPacman() {
		return this.pacman;
	}

	public Ghost getGhost(int type) {
		switch (type) {
		case Ghost.BLINKY:
			return blinky;
		case Ghost.PINKY:
			return pinky;
		case Ghost.INKY:
			return inky;
		default:
			return clyde;
		}
	}

	public Ghost[] getGhosts() {
		return ghosts;
	}

	public Level getCurrentLevel() {
		return currentLevel;
	}

	public void addSchedule(Schedule s) {
		schedules.add(s);
	}

	public boolean isMode(Mode mode) {
		return this.currentMode == mode;
	}

	public void increaseScore(int delta) {
		this.score += delta;
	}

	public int getRemainFood() {
		return map.getMaxFood() - pacman.getNumberFoodEaten();
	}

	private void writeHighScoreToFile(long highScore) {
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("highscore"))) {
			dos.writeLong(highScore);
		} catch (IOException e) {
		}
	}

	private long readHighScoreFromFile() {
		try (DataInputStream dis = new DataInputStream(new FileInputStream("highscore"))) {
			long highScore = dis.readLong();
			return highScore;
		} catch (IOException e) {
			return 0;
		}
	}

	private void drawInfoDown() {
		// draw mute icon
		int size = 25;
		if (!SoundEffect.isMuted()) {
			g.drawImage(sprite, (Map.COLS - 2) * Map.SIZE + Map.SIZE / 2 - size / 2,
					Map.ROWS * Map.SIZE + Map.SIZE / 2 - size / 2, (Map.COLS - 2) * Map.SIZE + Map.SIZE / 2 + size / 2,
					Map.ROWS * Map.SIZE + Map.SIZE / 2 + size / 2, 20, 20 * 6, 20 * 2, 20 * 7, null);
		} else {
			g.drawImage(sprite, (Map.COLS - 2) * Map.SIZE + Map.SIZE / 2 - size / 2,
					Map.ROWS * Map.SIZE + Map.SIZE / 2 - size / 2, (Map.COLS - 2) * Map.SIZE + Map.SIZE / 2 + size / 2,
					Map.ROWS * Map.SIZE + Map.SIZE / 2 + size / 2, 20 * 2, 20 * 6, 20 * 3, 20 * 7, null);
		}

		// draw option show grid
		g.setColor(Color.WHITE);
		g.fillRect((Map.COLS - 1) * Map.SIZE, Map.ROWS * Map.SIZE, Map.SIZE, Map.SIZE);
		if (map.isShowGrid()) {
			g.drawImage(sprite, (Map.COLS - 1) * Map.SIZE, Map.ROWS * Map.SIZE, (Map.COLS - 1) * Map.SIZE + Map.SIZE,
					Map.ROWS * Map.SIZE + Map.SIZE, 0, 80, 20, 100, null);
		}

		// draw number fruit
		for (int i = 0; i < currentLevel.fruit - numFruitShown; i++)
			drawFruit((float) (Map.SIZE * (Map.COLS - 4 - i)), Map.SIZE * (Map.ROWS + 1));

		// draw pacman live
		g.setColor(Theme.PACMAN_COLOR);
		int x = Map.SIZE;
		int y = (Map.ROWS + 1) * Map.SIZE;
		int dx = 5;
		size = (int) (Map.SIZE * 1.5);
		for (int i = 0; i < pacman.getLifes(); i++) {
			g.fill(new Arc2D.Double(x + (size + dx) * i, y - size / 2, size, size, 45, 360 - 90, Arc2D.PIE));
		}

		// draw level
		drawString("Level " + currentLevel.level, map.getHeight() + 25, Theme.TEXT_COLOR, 20);
	}

	private void drawInfoUp() {

		// draw score
		if (!toggleScore)
			drawNumber(String.format("%05d", dynamicScore), 1, 8);

		// draw high score
		if (!toggleHighScore) {
			g.drawImage(sprite, Map.SIZE * 9, -OFFSETY, Map.SIZE * 18, -OFFSETY + Map.SIZE, 0, 20 * 7 + 9, 85,
					20 * 7 + 9 * 2, null);
			drawNumber(String.format("%05d", highScore), 1, 18);
		}

		// draw time
		g.drawImage(sprite, Map.SIZE * 22, -OFFSETY, Map.SIZE * 26, -OFFSETY + Map.SIZE, 85, 20 * 7 + 9, 121,
				20 * 7 + 9 * 2, null);
		drawNumber(mainTimer.getTimeCount() + "", 1, 26);
	}

	private void drawNumber(String strNumber, int row, int lenCol) {
		int offsetYImage = 20 * 7;
		int offsetY = row * Map.SIZE - OFFSETY;
		int size = Map.SIZE;
		int offsetX = (lenCol - strNumber.length()) * Map.SIZE;
		for (int i = 0; i < strNumber.length(); i++) {
			int n = strNumber.charAt(i) - '0';
			g.drawImage(sprite, offsetX + size * i, offsetY, offsetX + size + size * i, offsetY + size, n * 9,
					offsetYImage, n * 9 + 9, offsetYImage + 9, null);
		}

	}

	public void dialog(String message) {
		dialog(message, Theme.TEXT_COLOR);
	}

	public void dialog(String message, Color color) {
		g.setFont(Theme.FONT);
		g.setColor(color);
		FontMetrics fontMetric = g.getFontMetrics();
		int width = fontMetric.stringWidth(message);
		int height = fontMetric.getHeight();
		int x = WIDTH / 2 - width / 2;
		int y = Map.SIZE * 16 + OFFSETY - height / 2;
		g.drawString(message, x, y);
	}

	public void drawString(String s, int x, int y, Color color, int size) {
		g.setFont(new Font(Theme.FONT.getFontName(), Theme.FONT.getStyle(), size));
		g.setColor(color);
		g.drawString(s, x, y);
	}

	public void drawString(String s, int y, Color color, int size) {
		g.setFont(new Font(Theme.FONT.getFontName(), Theme.FONT.getStyle(), size));
		g.setColor(color);
		FontMetrics fontMetric = g.getFontMetrics();
		int width = fontMetric.stringWidth(s);
		int x = WIDTH / 2 - width / 2;
		g.drawString(s, x, y);
	}

	public void drawString(String s, int x, int y, int size) {
		g.setFont(new Font(Theme.FONT.getFontName(), Theme.FONT.getStyle(), size));
		g.setColor(Theme.TEXT_COLOR);
		g.drawString(s, x, y);
	}

	public void drawString(String s, int y, int size) {
		g.setFont(new Font(Theme.FONT.getFontName(), Theme.FONT.getStyle(), size));
		g.setColor(Theme.TEXT_COLOR);
		FontMetrics fontMetric = g.getFontMetrics();
		int width = fontMetric.stringWidth(s);
		int x = WIDTH / 2 - width / 2;
		g.drawString(s, x, y);
	}

	public void drawEatenGhostScore(int eatenGhostScore, int row, int col) {
		int size = 20;
		int x = col * Map.SIZE + Map.SIZE / 2 - size / 2;
		int y = row * Map.SIZE + Map.SIZE / 2 - size / 2;
		int offX = 0, offY = 20 * 5;
		if (eatenGhostScore == 400)
			offX = 20;
		else if (eatenGhostScore == 800)
			offX = 40;
		else if (eatenGhostScore == 1600)
			offX = 60;
		g.drawImage(sprite, x, y, x + size, y + size, offX, offY, offX + 20, offY + 20, null);
	}

	public void drawEatenFruitScore(int fruitScore, int row, int col) {
		int size = 20;
		int x = col * Map.SIZE + Map.SIZE / 2 - size / 2;
		int y = row * Map.SIZE + Map.SIZE / 2 - size / 2;
		int offX = 20 * 4, offY = 20 * 5;
		if (fruitScore == 300)
			offX = 20 * 5;
		else if (fruitScore == 500)
			offX = 20 * 6;
		else if (fruitScore == 700)
			offX = 20 * 7;
		g.drawImage(sprite, x, y, x + size, y + size, offX, offY, offX + 20, offY + 20, null);
	}

	public void drawEatenFruitScore(int row, int col) {
		int fruitScore = currentLevel.fruitScore;
		drawEatenFruitScore(fruitScore, row, col);
	}

	public void drawFruit(int row, int col) {
		if (map.isWall(row, col))
			return;
		int size = 24;
		int x = col * Map.SIZE + Map.SIZE / 2 - size / 2;
		int y = row * Map.SIZE + Map.SIZE / 2 - size / 2;
		int offX = 0, offY = 20 * 6;
		g.drawImage(sprite, x, y, x + size, y + size, offX, offY, offX + 20, offY + 20, null);
	}

	public void drawFruit(float x, float y) {
		int size = 24;
		int offX = 0, offY = 20 * 6;
		g.drawImage(sprite, (int) (x - size / 2.0), (int) (y - size / 2.0), (int) (x + size / 2.0),
				(int) (y + size / 2.0), offX, offY, offX + 20, offY + 20, null);
	}

	private void popUp() {
		int x = 100, y = 200, space = 200;
		if (state == State.GAMEOVER)
			drawString("Game Over", 140, 30);
		else if (state == State.WIN)
			drawString("Done!", 140, 30);
		drawString("Score", x, y, 15);
		drawString(score + "", x + space, y, 15);
		drawString("HighScore", x, y + 20, 15);
		drawString(highScore + "", x + space, y + 20, 15);
		drawString("Time", x, y + 40, 15);
		drawString(mainTimer.getTimeCount() + "", x + space, y + 40, 15);
		drawString("Press Enter", y + 80, 15);
	}

	@Override
	public void draw() {
		super.draw();

		drawInfoUp();

		if (state == State.WIN || state == State.GAMEOVER) {
			popUp();
			return;
		}
		map.draw(g);
		if (fruit.isShowing())
			drawFruit(fruit.row, fruit.col);

		for (Iterator<Schedule> iter = schedules.iterator(); iter.hasNext();) {
			Schedule s = iter.next();
			if (getTimeCount() <= s.start + s.time)
				s.update();
			else {
				s.end();
				iter.remove();
			}
		}

		if (!map.isShowGrid()) {
			switch (state) {
			case LOADING:
				dialog("Loading");
				break;
			case READY:
				dialog("Ready");
				break;
			case PAUSE:
				dialog("Pause");
				break;
			case DYING:
				if (pacman.getLifes() == 0)
					dialog("Game Over", Color.RED);
				break;
			default:
				break;
			}
		}

		drawInfoDown();

		if (state == State.START) {
			if (mainTimer.getTimeCount() < timeDelayCount - 1)
				return;
		}

		if (map.isShowGrid()) {
			pacman.draw(g);
			for (Ghost ghost : ghosts)
				ghost.draw(g);
		} else {
			if (state == State.PAUSE)
				return;
			pacman.draw(g);
			if (state == State.DYING)
				return;
			for (Ghost ghost : ghosts)
				ghost.draw(g);
		}
	}

	@Override
	protected void keyPress(Key key) {
		if (key == Key.ENTER) {
			switch (state) {
			case READY:
				if (mainTimer.paused())
					mainTimer.resume();
				state = State.PLAY;
				resumeTime();
				break;
			case PAUSE:
				state = State.PLAY;
				resumeTime();
				break;
			case PLAY:
				state = State.PAUSE;
				pauseTime();
				break;
			case GAMEOVER:
			case WIN:
				newGame();
				break;
			default:
				break;
			}
		}

		if (state == State.PLAY)
			pacman.keyPress(key);
	}

	@Override
	protected void mouseClick(int x, int y) {
		if (isClickedCell(x, y, Map.ROWS, Map.COLS - 1)) {
			map.setShowGrid(!map.isShowGrid());
		} else if (isClickedCell(x, y, Map.ROWS, Map.COLS - 2)) {
			SoundEffect.setMuteAll(!SoundEffect.isMuted());
		}

	}
}
