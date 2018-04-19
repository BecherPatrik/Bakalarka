package Graphic;

import Trees.Side;
import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;

public interface IGraphicNode {	
	/**
	 * Vytvoří Node a uloží ho do StackPane
	 */
	void createStackPaneNode();
	
	/**
	 * Obyčejné zvýraznění procházeného listu
	 */
	void highlightNode();
	
	/**
	 * Zvýraznění nalezeného listu
	 */
	void highlightFindNode();
	
	/**
	 * Odstraní jakékoliv zvýraznění
	 */
	void setDefaultColorNode();
	
	/********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/
	
	/**
	 * Vrátí POLOMĚR Circle
	 * @return
	 */
	int getRadiusSize();
	
	IGraphicNode getParent();
	void setParent(IGraphicNode parent);
	
	Side getSide();
	void setSide(Side side);	
	
	int getLevel();	
	void setLevel(int level);
	
	String getValue();
	void setValue(String value);
	
	DoubleProperty getX();
	void setX(DoubleProperty x) ;
	
	DoubleProperty getY();
	void setY(DoubleProperty rootY);
	
	/**
	 * Vrátí Circle pro možnost animovat zvíraznění
	 * @return
	 */
	Shape getCircleShape();
	
	/**
	 * Vrátí celý StackPane
	 * @return
	 */
	StackPane getStackPaneNode();
	
	Pane getBranch();
	void setBranch(Pane branch);
	
	double getBranchStartX();
	void setBranchStartX(double x);
	
	double getBranchEndX();
	void setBranchEndX(double x);
	
	void setBranchEndY(double y);	
	
	IGraphicNode clone();
}