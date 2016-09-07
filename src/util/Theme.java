package util;

import java.awt.Color;
import java.awt.Font;

public interface Theme {

	// general
	public Font FONT = new Font(Util.loadFont("BD_Cartoon.ttf").getFontName(), Font.PLAIN, 20);
	public Color BG_COLOR = Color.BLACK;
	public Color TEXT_COLOR = Color.YELLOW;

	// entity
	public Color PACMAN_COLOR = Color.YELLOW;

	// Map
	public Color FOOD_COLOR = Color.WHITE;
	public Color INTERSECTPOINT_COLOR = new Color(0, 127, 14);

}
