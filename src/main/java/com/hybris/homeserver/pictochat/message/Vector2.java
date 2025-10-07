package com.hybris.homeserver.pictochat.message;

public class Vector2 {
	private int x;
	private int y;
	
	public Vector2() {
		
	}
	
	public Vector2(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	@Override
	public String toString() {
		return this.x + "|" + this.y;
	}
}
