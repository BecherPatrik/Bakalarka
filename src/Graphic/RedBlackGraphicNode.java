package graphic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class RedBlackGraphicNode extends BinaryGraphicNode {
	
	private trees.Color color = trees.Color.RED;
	private Circle doubleBlackCircle;

	public RedBlackGraphicNode(int value) {
		super(value);		
	}
	
	@Override
	public void createStackPaneNode() {		
		super.createStackPaneNode();
		super.getCircle().setFill(Color.DARKRED);
	}	
	
	/**
	 * Vytvoří dvojté obarvení
	 */
	public void doubleBlackHighlight() {
		super.getCircle().setStroke(Color.AQUA);		
		doubleBlackCircle = new Circle(super.getRadiusSize()/2 + 4);
		doubleBlackCircle.setStrokeWidth(1.5);
		doubleBlackCircle.setStroke(Color.AQUA);
		doubleBlackCircle.setFill(Color.TRANSPARENT);
		
		super.getStackPaneNode().getChildren().add(doubleBlackCircle);
	}
	
	/**
	 * Smaže dvojté obarvení
	 */
	public void removeDoubleBlackHighlight() {
		super.getStackPaneNode().getChildren().remove(doubleBlackCircle);
		super.setDefaultColorNode();
	}
	
	/********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/
	
	public trees.Color getColor() {
		return color;
	}

	public void setColor(trees.Color color) {
		this.color = color;
		if (color == trees.Color.BLACK) {
			super.getCircle().setFill(Color.BLACK);
		} else {
			super.getCircle().setFill(Color.DARKRED);
		}		
	}
}