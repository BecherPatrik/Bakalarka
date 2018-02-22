package Trees;

import Graphic.IGraphicNode;

public interface INode<T> {
    void deleteLeft();
    void deleteRight();
    /**
     * Nahradí aktuální INode (změní value, right, left)
     * @param node
     */
    void setNode(T node);
    
    /********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/
    int getValue();
    void setValue(int value);
    
    T getParent();
    void setParent(T node);
    
    T getRight();
    /**
     * Změní pravého potomka a nastaví mu sebe za rodiče
     * @param node
     */
    void setRight(T node);
    
    T getLeft();
    /**
     * Změní levého potomka a nastaví mu sebe za rodiče
     * @param node
     */
    void setLeft(T node);
    
    IGraphicNode getGraphicNode();
    void setGraphicNode(IGraphicNode graphicNode);        	
	
	boolean equals(Object obj);
}
