package com.romraider.maps;

public class UserLevelException extends Exception {
	private static final long serialVersionUID = 1L;
	private int level = -1;
	
	public UserLevelException(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
}
