package main;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import util.Key;
import util.Theme;
import util.Util;

@SuppressWarnings("serial")
public abstract class AbstractGamePanel extends Canvas implements Runnable, KeyListener, MouseListener {
	public static final int WIDTH = Map.SIZE * Map.COLS;
	public static final int OFFSETY = Map.SIZE * 3;
	public static final int HEIGHT = Map.SIZE * Map.ROWS + OFFSETY + Map.SIZE * 2;

	protected BufferedImage doubleBuffer;

	private Thread thread;
	public static final int FPS = 60;
	private long tick = 0;

	protected boolean running = false;
	protected Graphics2D g;

	public AbstractGamePanel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Theme.BG_COLOR);
		addKeyListener(this);
		addMouseListener(this);
		setFocusable(true);
		requestFocus();

		doubleBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) doubleBuffer.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.translate(0, Map.SIZE * 3);
	}

	public synchronized void start() {
		if (running)
			return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void stop() {
		if (running) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			running = false;
		}
	}

	protected abstract void keyPress(Key key);

	protected abstract void mouseClick(int x, int y);

	protected abstract void update();

	
	protected void draw() {
		g.setColor(Theme.BG_COLOR);
		g.fillRect(0, -OFFSETY, getWidth(), getHeight());
	}

	protected void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}

		draw();

		Graphics g = bs.getDrawGraphics();
		g.drawImage(doubleBuffer, 0, 0, null);
		g.dispose();
		bs.show();
	}

	public long getTick() {
		if (tick > 0)
			return tick;
		else
			return -1 * tick;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch (keyCode) {
		case KeyEvent.VK_S:
		case KeyEvent.VK_DOWN:
			keyPress(Key.DOWN);
			break;
		case KeyEvent.VK_W:
		case KeyEvent.VK_UP:
			keyPress(Key.UP);
			break;
		case KeyEvent.VK_A:
		case KeyEvent.VK_LEFT:
			keyPress(Key.LEFT);
			break;
		case KeyEvent.VK_D:
		case KeyEvent.VK_RIGHT:
			keyPress(Key.RIGHT);
			break;
		case KeyEvent.VK_ENTER:
			keyPress(Key.ENTER);
			break;
		default:
			break;
		}

	}

	@Override
	public void run() {
		long lastTime = System.nanoTime();
		double nsPerTick = 1000000000D / FPS;

		double delta = 0;

		// game loop
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / nsPerTick;
			lastTime = now;

			while (delta >= 1) {
				tick++;
				update();
				delta -= 1;
			}

			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			render();
		}
	}
	
	public boolean isClickedCell(int x, int y, int row, int col) {
		return Util.isInsideRect(x, y, col * Map.SIZE, MainGame.OFFSETY + row * Map.SIZE, Map.SIZE, Map.SIZE);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		mouseClick(e.getX(), e.getY());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

}
