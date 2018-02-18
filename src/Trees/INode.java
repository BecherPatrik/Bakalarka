package Trees;

import Graphic.IGraphicNode;

public interface INode<T> {
    void deleteLeft();
    void deleteRight();
    boolean equals(Object obj);
    IGraphicNode getGraphicNode();
    T getLeft();
    void setLeft(T node);
    T getRight();
    void setRight(T node);
    T getParent();
    void setParent(T node);
    void setNode(T node);
    int getValue();
    void setValue(int value);	
	void setGraphicNode(IGraphicNode graphicNode);          
}
