package Graphic;

import Trees.Side;
import javafx.beans.property.DoubleProperty;
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
	private final int size = 20;
	private int level = 0;
	private DoubleProperty x;
	private DoubleProperty y;

	private Text value;
	private StackPane node;
	private Circle circle;
	private Pane branch = null;
	
	private IGraphicNode parent;
	private Side side;

	public BinaryGraphicNode(int value) {
		this.value = new Text(Integer.toString(value));
		node = new StackPane();		
		createNode();
	}

	@Override
	public void createNode() {
		circle = new Circle(size);
		circle.setStrokeWidth(1.5);
		circle.setStroke(Color.WHITE);

	/*	circle.prefWidth(size);
		circle.prefHeight(size);*/

		value.setBoundsType(TextBoundsType.VISUAL);
		value.setFill(Color.WHITE);
		value.setFont(new Font(value.getFont().toString(), 14));

		node.getChildren().addAll(circle, value);
	}
	
	@Override
	public Shape getShape(){
		return this.circle;
	}

	@Override
	public void highlightFindNode() {
		circle.setStroke(Color.YELLOW);
	}
	
	@Override
	public void highlightNode() {
		circle.setStroke(Color.BLUE);
	}
	
	@Override
	public void setDefaultColorNode(){
		circle.setStroke(Color.WHITE);
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
	public DoubleProperty getX() {
		return x;
	}

	@Override
	public void setX(DoubleProperty x) {
		this.x = x;		
		node.layoutXProperty().bind(x);
	}

	@Override
	public DoubleProperty getY() {
		return y;
	}

	@Override
	public void setY(DoubleProperty y) {
		this.y = y;
		node.layoutYProperty().bind(y);
	}

	@Override
	public void setValue(String value) {
		this.value = new Text(value);
		Text text = (Text) this.node.getChildren().get(1);
		text.setText(value);
	}
	
	@Override
	public String getValue(){
		return this.value.getText();
	}

	@Override
	public StackPane getNode() {
		return node;
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
	public int getSize() {
		return 2 * size;
	}

	@Override
	public void setSize() {
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
	public Side getSide() {
		return side;
	}

	@Override
	public void setSide(Side side) {
		this.side = side;
	}

	@Override 
	public void setBranchEndX(double x) {
		Line l = (Line) branch.getChildren().get(0);
		l.setEndX(x);
	}
	
	@Override 
	public double getBranchEndX() {
		Line l = (Line) branch.getChildren().get(0);
		return l.getEndX();
	}
	
	@Override
	public void setBranchEndY(double y) {
		Line l = (Line) branch.getChildren().get(0);
		l.setEndY(y);
	}
	
	@Override 
	public void setBranchStartX(double x) {
		Line l = (Line) branch.getChildren().get(0);
		l.setStartX(x);
	}
	
	@Override 
	public double getBranchStartX() {
		Line l = (Line) branch.getChildren().get(0);
		return l.getStartX();
	}
	
}