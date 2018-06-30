package Trees;

import Graphic.RedBlackGraphicNode;
import Graphic.IGraphicNode;

public class RedBlackNode implements INode<RedBlackNode> {
	
	private int value;
	private Color color = Color.RED;
	private RedBlackNode parent = null;
	private RedBlackNode right = null;
	private RedBlackNode left = null;
	private RedBlackGraphicNode graphicNode;

	public RedBlackNode(int value, RedBlackNode parent, Side side) {
		this.value = value;
		this.parent = parent;
		this.graphicNode = new RedBlackGraphicNode(value);
		this.graphicNode.setSide(side);
	}
	
	public RedBlackNode(int value) {
        this.value = value;
        this.graphicNode = new RedBlackGraphicNode(value);
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
	public void setNodeWithGraphic(RedBlackNode node) {
		value = node.getValue();
		right = node.getRight();
		left = node.getLeft();
		
		node.getGraphicNode().setSide(graphicNode.getSide()); //před změnou musím uložit aktuální stranu node
		graphicNode.setLeft(node.getGraphicNode().getLeft());
		graphicNode.setRight(node.getGraphicNode().getRight());
		
		if (right != null || left != null) {
			graphicNode = node.getGraphicNode(); //změním graphicNode
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
	
	public void reColor() {
		// TODO Auto-generated method stub		
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
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public RedBlackNode getParent() {
		return parent;
	}

	@Override
	public void setParentWithGraphic(RedBlackNode node) {
		parent = node;
		graphicNode.setParent(node.getGraphicNode());
	}
	
	@Override
	public void setParent(RedBlackNode node) {
		parent = node;
	}
	
	@Override
	public RedBlackNode getRight() {
		return right;
	}

	@Override
	public void setRightWithGraphic(RedBlackNode node) {
		right = node;
		graphicNode.setRight(node.getGraphicNode());
		node.setParentWithGraphic(this);
		node.getGraphicNode().setSide(Side.RIGHT);
		node.setParent(this);
	}
	
	@Override
	public void setRight(RedBlackNode node) {
		right = node;
		if (node != null) {
			node.setParent(this);
		}		
	}
	
	@Override
	public RedBlackNode getLeft() {
		return left;
	}

	@Override
	public void setLeftWithGraphic(RedBlackNode node) {
		left = node;
		graphicNode.setLeft(node.getGraphicNode());
		node.setParentWithGraphic(this);	
		node.getGraphicNode().setSide(Side.LEFT);
		node.setParent(this);
	}
	
	@Override
	public void setLeft(RedBlackNode node) {
		left = node;
		if (node != null) {
			node.setParent(this);
		}			
	}

	@Override
	public RedBlackGraphicNode getGraphicNode() {
		return graphicNode;
	}
	
	@Override
	public void setGraphicNode(IGraphicNode graphicNode) {
		Side side = this.graphicNode.getSide();
		
		this.graphicNode = (RedBlackGraphicNode) graphicNode;
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
	
	@Override
	public boolean equals(Object obj) {
		RedBlackNode node = (RedBlackNode) obj;
		if (node == null) {
			return false;
		}
		return (value == node.getValue());
	}

	
}