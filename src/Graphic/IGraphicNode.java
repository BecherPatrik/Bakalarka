package Graphic;

import Trees.Side;
import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
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
	
	/**
	 * Sečte všechny své děti
	 */
	int countChildren();
	
	void subtractLeftChild();
	
	void subtractRightChild();
	
	void addLeftChild();
	
	void addRightChild();	

	/**
	 * Vytvoří zálohu x, y a value
	 */
	void createBackUp();	

	void createBackUpBranch();

	/**
	 * Použije zálohu a odstraní ji
	 */
	void useBackUp();

	void deleteBackUp();
	
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
	
	IGraphicNode getLeft();
	void setLeft(IGraphicNode left);
	
	IGraphicNode getRight();
	void setRight(IGraphicNode right);
	
	Side getSide();
	void setSide(Side side);	
	
	int getRightChildrenCount();
	int getLeftChildrenCount();
	
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
	
	Line getBranch();
	void setBranch(Line branch);
	
	DoubleProperty getBranchStartX();
	void setBranchStartX(DoubleProperty x);
	
	DoubleProperty getBranchStartY();
	void setBranchStartY(DoubleProperty y);
	
	DoubleProperty getBranchEndX();
	void setBranchEndX(DoubleProperty x);
	
	DoubleProperty getBranchEndY();
	void setBranchEndY(DoubleProperty y);	
	
	IGraphicNode clone();
}