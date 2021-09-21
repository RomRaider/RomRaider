package com.romraider.maps;

public class UserLevelException extends Exception {
	private int level = -1;
	
	public UserLevelException(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
}
