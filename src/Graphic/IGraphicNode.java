package Graphic;

import Trees.Side;
import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;

public interface IGraphicNode {	
	void createNode();	
	void highlightNode();
	DoubleProperty getX();
	void setX(DoubleProperty x) ;
	DoubleProperty getY();
	void setY(DoubleProperty rootY);
	void setValue(String value);
	String getValue();
	StackPane getNode();
	int getLevel();	
	void setLevel(int level);	
	int getSize();
	void setSize(); //k čemu?
	Pane getBranch();
	void setBranch(Pane branch);
	Shape getShape();
	void setBranchEndY(double y);
	void setBranchEndX(double x);
	void setSide(Side side);
	Side getSide();	
	double getBranchEndX();
	void setBranchStartX(double x);
	double getBranchStartX();
	IGraphicNode getParent();
	void setParent(IGraphicNode parent);
	void setDefaultColorNode();
	void highlightFindNode();
}