package Trees;

import Graphic.BinaryGraphicNode;
import Graphic.IGraphicNode;

public class AVLNode implements INode<AVLNode> {
	
	private int value;
	private int factor = 0;
	private AVLNode parent = null;
	private AVLNode right = null;
	private AVLNode left = null;
	private BinaryGraphicNode graphicNode;

	public AVLNode(int value, AVLNode parent, Side side) {
		this.value = value;
		this.parent = parent;
		this.graphicNode = new BinaryGraphicNode(value);
		this.graphicNode.setSide(side);
	}
	
	public AVLNode(int value) {
        this.value = value;
        this.graphicNode = new BinaryGraphicNode(value);
    }
	
	@Override
	public void deleteLeft() {
		this.left = null;
		this.graphicNode.setLeft(null);
	}

	@Override
	public void deleteRight() {
		this.right = null;
		this.graphicNode.setRight(null);
	}
	
	@Override
	public void setNode(AVLNode node) {
		value = node.getValue();
		right = node.getRight();
		left = node.getLeft();
		
		node.getGraphicNode().setSide(graphicNode.getSide()); //pøed zmìnou musím uložit aktuální stranu node
		graphicNode.setLeft(node.getGraphicNode().getLeft());
		graphicNode.setRight(node.getGraphicNode().getRight());
		
		if (right != null || left != null) {
			graphicNode = node.getGraphicNode(); //zmìním graphicNode
		}
		
		if (parent != null) {
			graphicNode.setParent(parent.getGraphicNode());

			if (graphicNode.getSide() == Side.LEFT) {
				graphicNode.getParent().setLeft(graphicNode);
			} else if (graphicNode.getSide() == Side.RIGHT) {
				graphicNode.getParent().setRight(graphicNode);
			}
		}
	}
	
	public void subtractFactor() {
		this.factor--;
	}
	
	public void addFactor() {
		this.factor++;
	}
	
	/********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public void setValue(int value) {
		this.value = value;		
	}
	
	@Override
	public AVLNode getParent() {
		return parent;
	}

	@Override
	public void setParent(AVLNode node) {
		parent = node;
		graphicNode.setParent(node.getGraphicNode());
	}
	
	@Override
	public AVLNode getRight() {
		return right;
	}

	@Override
	public void setRight(AVLNode node) {
		right = node;
		graphicNode.setRight(node.getGraphicNode());
		node.setParent(this);
		node.getGraphicNode().setSide(Side.RIGHT);
	}
	
	@Override
	public AVLNode getLeft() {
		return left;
	}

	@Override
	public void setLeft(AVLNode node) {
		left = node;
		graphicNode.setLeft(node.getGraphicNode());
		node.setParent(this);	
		node.getGraphicNode().setSide(Side.LEFT);
	}

	@Override
	public BinaryGraphicNode getGraphicNode() {
		return graphicNode;
	}
	
	@Override
	public void setGraphicNode(IGraphicNode graphicNode) {
		Side side = this.graphicNode.getSide();
		
		this.graphicNode = (BinaryGraphicNode) graphicNode;
		this.graphicNode.setSide(side);
		
		if (left != null) {
			this.graphicNode.setLeft(left.getGraphicNode());
			left.graphicNode.setParent(this.graphicNode);
		}		
		
		if (right != null) {
			this.graphicNode.setRight(right.getGraphicNode());
			right.graphicNode.setParent(this.graphicNode);
		}
		
		if (parent != null) {
			this.graphicNode.setParent(parent.getGraphicNode());
			if (graphicNode.getSide() == Side.LEFT) {
				graphicNode.getParent().setLeft(graphicNode);
			} else if (graphicNode.getSide() == Side.RIGHT) {
				graphicNode.getParent().setRight(graphicNode);
			}
		} else {
			this.graphicNode.setParent(null);
		}		
	}
	
	public int getFactor() {
		return factor;
	}

	public void setFactor(int factor) {
		this.factor = factor;
	}
	
	@Override
	public boolean equals(Object obj) {
		AVLNode node = (AVLNode) obj;
		return (value == node.getValue());
	}
}