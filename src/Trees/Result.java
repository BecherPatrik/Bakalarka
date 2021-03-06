package trees;

import java.util.ArrayList;

import graphic.IGraphicNode;

public class Result {
	private INode node;
	private Side side; 
	private ArrayList<IGraphicNode> way;
	private ArrayList<RecordOfAnimation> recordOfAnimations;
	//kolekce směrů
	
	public Result(INode node) {
		super();
		this.node = node;
		this.way = new ArrayList<>();
		this.recordOfAnimations = new ArrayList<>();
	}

	public INode getNode() {
		return node;
	}	
	
	public void setNode(INode node) {
		this.node = node;
	}
	
	public Side getSide(){
		return side;
	}	
	
	public void setSide(Side side){
		this.side = side;
	}		

	/**
	 * Přídá navštívený uzel
	 * @param side
	 */
	public void addNodeToWay(IGraphicNode node) {
		way.add(node);
	}
	
	public ArrayList<IGraphicNode> getWay() {
		return way;
	}
	
	/**
	 * Přidá záznam o animaci do seznamu animací pro provedení
	 * <p>
	 * <b>Příklady použití:</b>
	 * <br>
	 * (SEARCH, nalezený/rodič, (boolean) nalezen)<br>
	 * (INSERT, null, null)<br>
	 * (DELETE, null, (boolean) děti)<br>
	 * (MOVEVALUE, cíl, (INode) zdroj)<br>
	 * (MOVENODE, cíl, (INode) zdroj)<br>
	 * (UPDATEFACTOR, začátek, (boolean) animovat)<br>
	 * (RECOLOR, rodič, (boolean) animovat)<br>
	 * </p>
	 * @param action
	 * @param node1
	 * @param object	 
	 */
	public void addAnimation(AnimatedAction action, IGraphicNode node, Object object) {
		recordOfAnimations.add(new RecordOfAnimation(action, node, object));		
	}
	
	public ArrayList<RecordOfAnimation> getRecordOfAnimations() {
		return recordOfAnimations;
	}
}