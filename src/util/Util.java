package util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Util {

	// load file from resource path
	public static InputStream loadFile(String path) {
		return Util.class.getResourceAsStream("/" + path);
	}

	// load image from filename
	public static BufferedImage loadImage(String filename) {
		try {
			BufferedImage image = ImageIO.read(loadFile("image/" + filename));
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// load font
	public static Font loadFont(String fontfile) {
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Font font = Font.createFont(Font.TRUETYPE_FONT, loadFile("data/" + fontfile));
			ge.registerFont(font);
			return font;
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// read json file
	public static JSONArray loadLevel(String filepath) {
		try {
			JSONParser parser = new JSONParser();
			JSONArray jsonArray = (JSONArray) parser.parse(new InputStreamReader(Util.loadFile(filepath)));
			return jsonArray;
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int random(int max) {
		return random(0, max);
	}

	public static int random(int min, int max) {
		return (int) (Math.floor(Math.random() * (max - min + 1) + min));
	}

	public static int randomChoice(int[] arr) {
		return arr[random(arr.length - 1)];
	}

	public static byte randomChoice(byte[] arr) {
		return arr[random(arr.length - 1)];
	}

	public static double randomChoice(double[] arr) {
		return arr[random(arr.length - 1)];
	}

	public static float randomChoice(float[] arr) {
		return arr[random(arr.length - 1)];
	}

	public static boolean isInsideRect(int x, int y, int x1, int y1, int width, int height) {
		int x2 = x1 + width, y2 = y1 + height;
		if (x >= x1 && x <= x2 && y >= y1 && y <= y2)
			return true;
		return false;
	}

}
