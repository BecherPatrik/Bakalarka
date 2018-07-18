package trees;

import graphic.IGraphicNode;

public interface INode {
    void deleteLeftWithGraphic();
    void deleteLeft();
    
    void deleteRightWithGraphic();
    void deleteRight();
    /**
     * Nahradí aktuální INode (změní value, right, left)
     * @param node
     */
    void setNodeWithGraphic(INode node);
    
    /********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/
    int getValue();
    void setValue(int value);
    
    INode getParent();
    
    void setParentWithGraphic(INode node);
    void setParent(INode node);
    
    INode getRight();
    /**
     * Změní pravého potomka a nastaví mu sebe za rodiče
     * Mění i IGraphicNode
     * @param node
     */
    void setRightWithGraphic(INode node);
    /**
     * Změní pravého potomka a nastaví mu sebe za rodiče
     * @param node
     */
    void setRight(INode node);
    
    INode getLeft();
    /**
     * Změní levého potomka a nastaví mu sebe za rodiče
     * Mění i IGraphicNode
     * @param node
     */
    void setLeftWithGraphic(INode node);
    
    /**
     * Změní levého potomka a nastaví mu sebe za rodiče
     * @param node
     */
    void setLeft(INode node);
    
    IGraphicNode getGraphicNode();
    void setGraphicNode(IGraphicNode graphicNode);        	
	
	boolean equals(Object obj);
}
