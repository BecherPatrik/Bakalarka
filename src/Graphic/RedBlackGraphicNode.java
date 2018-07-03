package Graphic;

import javafx.scene.paint.Color;

public class RedBlackGraphicNode extends BinaryGraphicNode {
	
	private Trees.Color color = Trees.Color.RED;

	public RedBlackGraphicNode(int value) {
		super(value);		
	}
	
	@Override
	public void createStackPaneNode() {		
		super.createStackPaneNode();
		super.getCircle().setFill(Color.DARKRED);
	}	
	
	public void doubleBlackHighlight() {
		super.getCircle().setStroke(Color.AQUA);
	}
	
	/********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/
	
	public Trees.Color getColor() {
		return color;
	}

	public void setColor(Trees.Color color) {
		this.color = color;
		if (color == Trees.Color.BLACK) {
			super.getCircle().setFill(Color.BLACK);
		} else {
			super.getCircle().setFill(Color.DARKRED);
		}		
	}
}