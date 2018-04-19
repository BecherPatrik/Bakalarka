package Graphic;

import Trees.Side;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
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

	private IGraphicNode parent;
	private int leftCount = 0;
	private int rightCount = 0;
	private Side side;
	private int level = 0;

	private Text value;

	private DoubleProperty x;
	private DoubleProperty y;

	private StackPane stackPaneNode;
	private Circle circle;
	private Pane branch = null;

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

		/*
		 * circle.prefWidth(size); circle.prefHeight(size);
		 */

		value.setBoundsType(TextBoundsType.VISUAL);
		value.setFill(Color.WHITE);
		value.setFont(new Font(value.getFont().toString(), 14));

		stackPaneNode.getChildren().addAll(circle, value);
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
	public Side getSide() {
		return side;
	}

	@Override
	public void setSide(Side side) {
		this.side = side;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
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

	@Override
	public Pane getBranch() {
		return branch;
	}

	@Override
	public void setBranch(Pane branch) {
		this.branch = branch;
	}

	@Override
	public double getBranchStartX() {
		Line l = (Line) branch.getChildren().get(0);
		return l.getStartX();
	}

	@Override
	public void setBranchStartX(double x) {
		Line l = (Line) branch.getChildren().get(0);
		l.setStartX(x);
	}

	@Override
	public double getBranchEndX() {
		Line l = (Line) branch.getChildren().get(0);
		return l.getEndX();
	}

	@Override
	public void setBranchEndX(double x) {
		Line l = (Line) branch.getChildren().get(0);
		l.setEndX(x);
	}

	@Override
	public void setBranchEndY(double y) {
		Line l = (Line) branch.getChildren().get(0);
		l.setEndY(y);
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