package Graphic;

import Trees.Side;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

public class BinaryGraphicNode implements IGraphicNode {
	private final int radiusSize = 20;

	private IGraphicNode parent = null;
	private IGraphicNode left = null;
	private IGraphicNode right = null;
	
	private int leftChildrenCount = 0;
	private int rightChildrenCount = 0;
	private Side side;

	private Text value;	

	private DoubleProperty x;
	private DoubleProperty y;
	
	private StackPane stackPaneNode;
	private Circle circle;
	
	private Line branch = null;
	
	/** Zálohy **/
/*	private IGraphicNode Oldleft = null;
	private IGraphicNode Oldright = null;
	
	private String oldValue;
	
	private double oldX;
	private double oldY;
	
	private double oldXBranch;
	private double oldYBranch;
	
	private Line OldBranch = null;*/

	public BinaryGraphicNode(int value) {
		this.value = new Text(Integer.toString(value));
		stackPaneNode = new StackPane();
		createStackPaneNode();		
	}

	@Override
	public void createStackPaneNode() {		
		circle = new Circle(radiusSize);
		circle.setStrokeWidth(1.5);
		circle.setStroke(Color.WHITE);

		value.setBoundsType(TextBoundsType.VISUAL);
		value.setFill(Color.WHITE);
		value.setFont(new Font(value.getFont().toString(), 14));

		stackPaneNode.getChildren().addAll(circle, value);
		stackPaneNode.setPrefHeight(30+5);
	}

	@Override
	public void highlightNode() {
		circle.setStroke(Color.BLUE);
	}

	@Override
	public void highlightFindNode() {
		circle.setStroke(Color.YELLOW);
	}

	@Override
	public void setDefaultColorNode() {
		circle.setStroke(Color.WHITE);
	}
	

	@Override
	public int countChildren() {
		if (left != null) {
			leftChildrenCount = 1 + left.countChildren();
		} else {
			leftChildrenCount = 0;
		}

		if (right != null) {
			rightChildrenCount = 1 + right.countChildren();
		} else {
			rightChildrenCount = 0;
		}
		
		return leftChildrenCount + rightChildrenCount;
	}	

	@Override
	public void subtractLeftChild() {
		leftChildrenCount--;		
	}

	@Override
	public void subtractRightChild() {
		rightChildrenCount--;		
	}

	@Override
	public void addLeftChild() {
		leftChildrenCount++;		
	}

	@Override
	public void addRightChild() {
		rightChildrenCount++;	
	}
	
	/*@Override
	public void createBackUp() {
		if (!(value.getText().equals(""))) {
			oldValue = value.getText();
		} else {
			return; //pokud je node prázdný už má zálohu a je do něho přesunován jiný viz 0.2
		}
		
		oldX = x.get();
		oldY = y.get();
		Oldright = right;
		Oldleft = left;
	}

	@Override 
	public void createBackUpBranch() {
		if (Oldright == null && rightChildrenCount != 0) {
			Oldright = right;
		}
		
		if (Oldleft == null && leftChildrenCount != 0) {
			Oldleft = left;
		}
		
		if (branch == null) { //pokud se jedná o roota
			return;
		}
		
		DoubleProperty x = new SimpleDoubleProperty(getBranchStartX().get());
		DoubleProperty y = new SimpleDoubleProperty(getBranchStartY().get());
		DoubleProperty x2 = new SimpleDoubleProperty(getBranchEndX().get());
		DoubleProperty y2 = new SimpleDoubleProperty(getBranchEndY().get());
		
		OldBranch = new Line();	
		
		OldBranch.startXProperty().bind(x);
		OldBranch.startYProperty().bind(y);
		OldBranch.endXProperty().bind(x2);
		OldBranch.endYProperty().bind(y2);
		
		oldXBranch = this.x.get();
		oldYBranch = this.y.get();
	}
	
	@Override
	public void useBackUp() {
		DoubleProperty x;
		DoubleProperty y;
		if (oldValue != null) {
			value = new Text(oldValue);
			stackPaneNode.getChildren().clear();
			createStackPaneNode();
		}	
		
		if (oldX != 0) {
			x = new SimpleDoubleProperty(oldX);
			y = new SimpleDoubleProperty(oldY);
			
			this.x.bind(x);
			this.y.bind(y);			
		} else if (oldXBranch != 0) {
			x = new SimpleDoubleProperty(oldXBranch);
			y = new SimpleDoubleProperty(oldYBranch);
			
			this.x.bind(x);
			this.y.bind(y);	
		}
		
		if (OldBranch != null) {
			branch = OldBranch;	
		}
		
		right = Oldright;
		left = Oldleft;
		
		deleteBackUp();
	}
	
	@Override
	public void deleteBackUp() {
		oldValue = null;
		oldX = 0;
		oldY = 0;
		oldXBranch = 0;
		oldYBranch = 0;
		OldBranch = null;
		Oldright = null;
		Oldleft = null;
	}*/
	
	/********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/

	@Override
	public int getRadiusSize() {
		return 2 * radiusSize;
	}

	@Override
	public IGraphicNode getParent() {
		return parent;
	}

	@Override
	public void setParent(IGraphicNode parent) {
		this.parent = parent;
	}

	@Override
	public IGraphicNode getLeft() {
		return left;
	}

	@Override
	public void setLeft(IGraphicNode left) {
		this.left = left;	
		if (left != null) {
			this.left.setParent(this);
			this.left.setSide(Side.LEFT);
		}		
	}

	@Override
	public IGraphicNode getRight() {
		return right;
	}

	@Override
	public void setRight(IGraphicNode right) {
		this.right = right;
		if (right != null) {
			this.right.setParent(this);
			this.right.setSide(Side.RIGHT);
		}		
	}

	@Override
	public Side getSide() {
		return side;
	}

	@Override
	public void setSide(Side side) {
		this.side = side;
	}
	
	@Override
	public int getRightChildrenCount() {
		return rightChildrenCount;
	}

	@Override
	public int getLeftChildrenCount() {
		return leftChildrenCount;
	}

	@Override
	public String getValue() {
		return this.value.getText();
	}		

	@Override
	public void setValue(String value) {
		this.value = new Text(value);
		Text text = (Text) this.stackPaneNode.getChildren().get(1);
		text.setText(value);
	}

	@Override
	public DoubleProperty getX() {
		return x;
	}

	@Override
	public void setX(DoubleProperty x) {
		this.x = x;
		stackPaneNode.layoutXProperty().bind(x);
	}

	@Override
	public DoubleProperty getY() {
		return y;
	}

	@Override
	public void setY(DoubleProperty y) {
		this.y = y;
		stackPaneNode.layoutYProperty().bind(y);
	}

	@Override
	public Shape getCircleShape() {
		return this.circle;
	}

	@Override
	public StackPane getStackPaneNode() {
		return stackPaneNode;
	}

	public Circle getCircle() {
		return circle;
	}

	@Override
	public Line getBranch() {
		return branch;
	}

	@Override
	public void setBranch(Line branch) {
		this.branch = branch;
	}

	@Override
	public DoubleProperty getBranchStartX() {
		return branch.startXProperty();
	}

	@Override
	public void setBranchStartX(DoubleProperty x) {
		branch.startXProperty().bind(x);
	}

	@Override
	public DoubleProperty getBranchStartY() {
		return branch.startYProperty();
	}

	@Override
	public void setBranchStartY(DoubleProperty y) {
		branch.startYProperty().bind(y);
	}

	@Override
	public DoubleProperty getBranchEndX() {
		return branch.endXProperty();
	}

	@Override
	public void setBranchEndX(DoubleProperty x) {		
		branch.endXProperty().bind(x);		
	}

	@Override
	public DoubleProperty getBranchEndY() {
		return branch.endYProperty();
	}

	@Override
	public void setBranchEndY(DoubleProperty y) {
		branch.endYProperty().bind(y);
	}
	
	@Override 
	public BinaryGraphicNode clone() {
		BinaryGraphicNode clone = new BinaryGraphicNode(Integer.parseInt(this.getValue()));		
		clone.setX(new SimpleDoubleProperty(this.x.doubleValue()));
		clone.setY(new SimpleDoubleProperty(this.y.doubleValue()));	
		clone.setBranch(this.branch);
		return clone;		
	}
}