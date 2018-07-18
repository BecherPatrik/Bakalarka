package trees;

import graphic.AVLGraphicNode;
import graphic.IGraphicNode;

public class AVLNode implements INode {
	
	private int value;
	private int factor = 0;
	private AVLNode parent = null;
	private AVLNode right = null;
	private AVLNode left = null;
	private AVLGraphicNode graphicNode;

	public AVLNode(int value, AVLNode parent, Side side) {
		this.value = value;
		this.parent = parent;
		this.graphicNode = new AVLGraphicNode(value);
		this.graphicNode.setSide(side);
	}
	
	public AVLNode(int value) {
        this.value = value;
        this.graphicNode = new AVLGraphicNode(value);
    }
	
	@Override
	public void deleteLeftWithGraphic() {
		deleteLeft();
		this.graphicNode.setLeft(null);
	}
	
	@Override
	public void deleteLeft() {
		this.left = null;
	}

	@Override
	public void deleteRightWithGraphic() {
		deleteRight();
		this.graphicNode.setRight(null);
	}
	
	@Override
	public void deleteRight() {
		this.right = null;		
	}
	
	@Override
	public void setNodeWithGraphic(INode node) {
		value = node.getValue();
		right = (AVLNode) node.getRight();
		left = (AVLNode) node.getLeft();
		
		node.getGraphicNode().setSide(graphicNode.getSide()); //před změnou musím uložit aktuální stranu node
		graphicNode.setLeft(node.getGraphicNode().getLeft());
		graphicNode.setRight(node.getGraphicNode().getRight());
		
		if (right != null || left != null) {
			graphicNode = (AVLGraphicNode) node.getGraphicNode(); //změním graphicNode
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
	
	/**
	 * Vypočítá rekurzivně ohodnocení listů
	 * @return vrací výšku pod stromů 
	 */
	public int countFactor() {		
		int l = 0;
		int r = 0;
		
		if (left != null) {
			l = left.countFactor();
		}
		
		if (right != null) {
			r = right.countFactor();
		}		
		
		factor = l - r;	
		
		graphicNode.setNewFactor(Integer.toString(factor));
		
		return Math.max(r, l) + 1;		
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
	public void setParentWithGraphic(INode node) {
		parent = (AVLNode) node;
		graphicNode.setParent(node.getGraphicNode());
	}
	
	@Override
	public void setParent(INode node) {
		parent = (AVLNode) node;
	}
	
	@Override
	public AVLNode getRight() {
		return right;
	}

	@Override
	public void setRightWithGraphic(INode node) {
		right = (AVLNode) node;
		graphicNode.setRight(node.getGraphicNode());
		node.setParentWithGraphic(this);
		node.getGraphicNode().setSide(Side.RIGHT);
		node.setParent(this);
	}
	
	@Override
	public void setRight(INode node) {
		right = (AVLNode) node;
		if (node != null) {
			node.setParent(this);
		}		
	}
	
	@Override
	public AVLNode getLeft() {
		return left;
	}

	@Override
	public void setLeftWithGraphic(INode node) {
		left = (AVLNode) node;
		graphicNode.setLeft(node.getGraphicNode());
		node.setParentWithGraphic(this);	
		node.getGraphicNode().setSide(Side.LEFT);
		node.setParent(this);
	}
	
	@Override
	public void setLeft(INode node) {
		left = (AVLNode) node;
		if (node != null) {
			node.setParent(this);
		}
			
	}

	@Override
	public AVLGraphicNode getGraphicNode() {
		return graphicNode;
	}
	
	@Override
	public void setGraphicNode(IGraphicNode graphicNode) {
		Side side = this.graphicNode.getSide();
		
		this.graphicNode = (AVLGraphicNode) graphicNode;
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