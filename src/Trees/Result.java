package Trees;

import java.util.ArrayList;

import Graphic.IGraphicNode;

public class Result<T> {
	private INode<T> node;
	private Side side; 
	private ArrayList<IGraphicNode> way;
	private ArrayList<RecordOfAnimation> recordOfAnimations;
	//kolekce smìrù
	
	public Result(INode<T> node) {
		super();
		this.node = node;
		this.way = new ArrayList<>();
		this.recordOfAnimations = new ArrayList<>();
	}

	public INode<T> getNode() {
		return node;
	}	
	
	public Side getSide(){
		return side;
	}	
	
	public void setSide(Side side){
		this.side = side;
	}
	
	public void setNode(INode<T> node) {
		this.node = node;
	}	

	/**
	 * Pøídá smìr do kolekce smìrù
	 * @param side
	 */
	public void addSide(IGraphicNode node) {
		way.add(node);
	}
	
	public ArrayList<IGraphicNode> getWay() {
		return way;
	}
	
	/**
	 * Pøidá záznam o animaci do seznamu animací pro provedení
	 * @param action
	 * @param node1
	 * @param object
	 */
	public void addAnimation(AnimatedAction action, INode<?> node1, Object object) {
		recordOfAnimations.add(new RecordOfAnimation(action, node1, object));		
	}
	
	public ArrayList<RecordOfAnimation> getRecordOfAnimations() {
		return recordOfAnimations;
	}
	
	
}