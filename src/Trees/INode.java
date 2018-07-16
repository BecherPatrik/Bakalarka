package trees;

import graphic.IGraphicNode;

public interface INode<T> {
    void deleteLeftWithGraphic();
    void deleteLeft();
    
    void deleteRightWithGraphic();
    void deleteRight();
    /**
     * Nahradí aktuální INode (změní value, right, left)
     * @param node
     */
    void setNodeWithGraphic(T node);
    
    /********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/
    int getValue();
    void setValue(int value);
    
    T getParent();
    
    void setParentWithGraphic(T node);
    void setParent(T node);
    
    T getRight();
    /**
     * Změní pravého potomka a nastaví mu sebe za rodiče
     * Mění i IGraphicNode
     * @param node
     */
    void setRightWithGraphic(T node);
    /**
     * Změní pravého potomka a nastaví mu sebe za rodiče
     * @param node
     */
    void setRight(T node);
    
    T getLeft();
    /**
     * Změní levého potomka a nastaví mu sebe za rodiče
     * Mění i IGraphicNode
     * @param node
     */
    void setLeftWithGraphic(T node);
    
    /**
     * Změní levého potomka a nastaví mu sebe za rodiče
     * @param node
     */
    void setLeft(T node);
    
    IGraphicNode getGraphicNode();
    void setGraphicNode(IGraphicNode graphicNode);        	
	
	boolean equals(Object obj);
}
