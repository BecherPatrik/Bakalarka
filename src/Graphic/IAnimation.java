package Graphic;

import Trees.ITree;

public interface IAnimation {

	void addNode(IGraphicNode node);

	void removeNode(IGraphicNode node);

	void changeNode(IGraphicNode node1, IGraphicNode node2);

	void balanceTree();
}