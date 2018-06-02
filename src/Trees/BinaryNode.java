package Trees;

import Graphic.BinaryGraphicNode;
import Graphic.IGraphicNode;

public class BinaryNode
implements INode<BinaryNode> {
	private int value;
	private BinaryNode parent = null;
	private BinaryNode right = null;
	private BinaryNode left = null;
	private BinaryGraphicNode graphicNode;

	public BinaryNode(int value, BinaryNode parent, Side side) {
		this.value = value;
		this.parent = parent;
		this.graphicNode = new BinaryGraphicNode(value);
		this.graphicNode.setSide(side);
	}
	
	public BinaryNode(int value) {
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
	public void setNode(BinaryNode node) {
		value = node.getValue();
		right = node.getRight();
		left = node.getLeft();
		
		node.getGraphicNode().setSide(graphicNode.getSide()); //před změnou musím uložit aktuální stranu node
		graphicNode.setLeft(node.getGraphicNode().getLeft());
		graphicNode.setRight(node.getGraphicNode().getRight());
		
		//graphicNode = node.getGraphicNode(); //změním graphicNode
		
		if (parent != null) {
			graphicNode.setParent(parent.getGraphicNode());

			if (graphicNode.getSide() == Side.LEFT) {
				graphicNode.getParent().setLeft(graphicNode);
			} else if (graphicNode.getSide() == Side.RIGHT) {
				graphicNode.getParent().setRight(graphicNode);
			}
		}
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
	public BinaryNode getParent() {
		return parent;
	}

	@Override
	public void setParent(BinaryNode node) {
		parent = node;
		graphicNode.setParent(node.getGraphicNode());
	}
	
	@Override
	public BinaryNode getRight() {
		return right;
	}

	@Override
	public void setRight(BinaryNode node) {
		right = node;
		graphicNode.setRight(node.getGraphicNode());
		node.setParent(this);
	}
	
	@Override
	public BinaryNode getLeft() {
		return left;
	}

	@Override
	public void setLeft(BinaryNode node) {
		left = node;
		graphicNode.setLeft(node.getGraphicNode());
		node.setParent(this);	
	}

	@Override
	public BinaryGraphicNode getGraphicNode() {
		return graphicNode;
	}
	
	@Override
	public void setGraphicNode(IGraphicNode graphicNode) {
		this.graphicNode = (BinaryGraphicNode) graphicNode;
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
	
	@Override
	public boolean equals(Object obj) {
		BinaryNode node = (BinaryNode) obj;
		return (value == node.getValue());
	}
}
