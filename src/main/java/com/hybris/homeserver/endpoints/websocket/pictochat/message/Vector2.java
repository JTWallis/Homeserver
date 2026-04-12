package com.hybris.homeserver.endpoints.websocket.pictochat.message;

public class Vector2 {
	private double x;
	private double y;
	
	public Vector2() {
		
	}
	
	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}
	
	@Override
	public String toString() {
		return String.format("%5f|%5f", x, y);
	}
}
