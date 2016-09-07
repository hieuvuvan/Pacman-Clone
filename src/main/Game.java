package main;

import java.awt.BorderLayout;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Game extends JFrame {
	private MainGame mainGame = new MainGame();
	public static final boolean DEBUG = false;

	public Game() {
		setTitle("Pacman");
		setLayout(new BorderLayout(0, 0));
		add(mainGame);
		setResizable(false);
		pack();
		
		mainGame.start();

		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public static void main(String[] args) {
		new Game();
	}
}
