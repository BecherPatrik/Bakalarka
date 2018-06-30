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
		super.getCircle().setFill(Color.RED);
	}
	
	@Override
	public void highlightFindNode() {
		super.highlightFindNode();
	}

	@Override
	public void setDefaultColorNode() {
		super.setDefaultColorNode();
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
	}
}