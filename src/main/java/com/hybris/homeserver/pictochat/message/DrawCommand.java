package com.hybris.homeserver.pictochat.message;

public class DrawCommand {

	private int id;
	private int type;
	private Vector2 startPos;
	private Vector2 endPos;
	private String value;
	private int penSize;
	private String penColor;
	
	public DrawCommand() {
		
	}
	
	public DrawCommand(int id, int type, Vector2 startPos, Vector2 endPos, String value, int penSize, String penColor) {
		this.id = id;
		this.type = type;
		this.startPos = startPos;
		this.endPos = endPos;
		this.value = value;
		this.penSize = penSize;
		this.penColor = penColor;
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
	
	public Vector2 getStartPos() {
		return startPos;
	}
	
	public Vector2 getEndPos() {
		return endPos;
	}
	
	public String getValue() {
		return value;
	}
	
	public int getPenSize() {
		return penSize;
	}
	
	public String getPenColor() {
		return penColor;
	}
	
	@Override
	public String toString() {
		return String.format("DrawCommand: id[%d] type[%d] startPos[%s] endPos[%s] value[%s] penSize[%d] penColor[%s]",
				id, type, startPos.toString(), endPos.toString(), value, penSize, penColor);
	}
}
