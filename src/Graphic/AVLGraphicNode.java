package Graphic;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

public class AVLGraphicNode extends BinaryGraphicNode {
	
	private Text factor;

	public AVLGraphicNode(int value) {
		super(value);		
	}
	
	@Override
	public void createStackPaneNode() {		
		super.createStackPaneNode();
		
		factor = new Text("0");
		factor.setBoundsType(TextBoundsType.VISUAL);
		factor.setFill(Color.WHITE);
		factor.setFont(new Font(factor.getFont().toString(), 14));	
		
		super.getStackPaneNode().getChildren().add(factor);
		super.getStackPaneNode().setPrefHeight(getRadiusSize() + 30);
		StackPane.setAlignment(factor, Pos.TOP_CENTER);		
	}	

	/********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/

	public Text getFactor() {
		return factor;
	}

	public void setFactor(String factor) {
		this.factor.setText(factor);
		Text text = (Text) super.getStackPaneNode().getChildren().get(2);
		text.setText(factor);
	}	
}