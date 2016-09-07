package main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import util.Util;

public class Level {
	private final int[] ghostModeSwitchTimes;
	private int timeIndex;
	
	public final int level;
	public final double ghostSpeed;
	public final double ghostTunnelSpeed;
	public final double pacmanSpeed;
	public final double dotEatingSpeed;
	public final double ghostFrightSpeed;
	public final double pacmanFrightSpeed;
	public final double dotEatingFrightSpeed;
	public final int elroyDotsLeftPart1;
	public final double elroySpeedPart1;
	public final int elroyDotsLeftPart2;
	public final double elroySpeedPart2;
	public final int frightTime;
	public final int frightBlinkCount;
	public final int fruit;
	public final int fruitScore;

	private Level(JSONObject jsObj) {
		level = Integer.parseInt(jsObj.get("level").toString());
		ghostSpeed = Double.parseDouble(jsObj.get("ghostSpeed").toString());
		ghostTunnelSpeed = Double.parseDouble(jsObj.get("ghostTunnelSpeed").toString());
		pacmanSpeed = Double.parseDouble(jsObj.get("pacmanSpeed").toString());
		dotEatingSpeed = Double.parseDouble(jsObj.get("dotEatingSpeed").toString());
		ghostFrightSpeed = Double.parseDouble(jsObj.get("ghostFrightSpeed").toString());
		pacmanFrightSpeed = Double.parseDouble(jsObj.get("pacmanFrightSpeed").toString());
		dotEatingFrightSpeed = Double.parseDouble(jsObj.get("dotEatingFrightSpeed").toString());
		frightTime = Integer.parseInt(jsObj.get("frightTime").toString());
		frightBlinkCount = Integer.parseInt(jsObj.get("frightBlinkCount").toString());
		fruit = Integer.parseInt(jsObj.get("fruit").toString());
		fruitScore = Integer.parseInt(jsObj.get("fruitScore").toString());
		elroyDotsLeftPart1 = Integer.parseInt(jsObj.get("elroyDotsLeftPart1").toString());
		elroyDotsLeftPart2 = Integer.parseInt(jsObj.get("elroyDotsLeftPart2").toString());
		elroySpeedPart1 = Double.parseDouble(jsObj.get("elroySpeedPart1").toString());
		elroySpeedPart2 = Double.parseDouble(jsObj.get("elroySpeedPart2").toString());
		JSONArray tempArray = (JSONArray) jsObj.get("ghostModeSwitchTimes");
		ghostModeSwitchTimes = new int[tempArray.size()];
		for(int i = 0; i < ghostModeSwitchTimes.length; i++) {
			ghostModeSwitchTimes[i] = Integer.parseInt(tempArray.get(i).toString());
		}
	}
	
	private Level(Object obj) {
		this((JSONObject) obj);
	}
	
	public static Level[] getAllLevel() {
		JSONArray tempArray = Util.loadLevel("data/level.json");
		Level[] levels = new Level[tempArray.size()];
		for(int i = 0; i < levels.length; i++) {
			levels[i] = new Level(tempArray.get(i));
		}
		return levels;
	}

	public int getCurrentTimeModeSwitch() {
		return ghostModeSwitchTimes[timeIndex];
	}

	public void updateTimeModeSwitch() {
		timeIndex++;
		if (timeIndex >= ghostModeSwitchTimes.length) {
			timeIndex = ghostModeSwitchTimes.length - 1;
		}
	}
	
	public void reset() {
		timeIndex = 0;
	}
}
