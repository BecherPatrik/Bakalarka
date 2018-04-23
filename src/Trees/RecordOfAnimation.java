package Trees;

import Graphic.IGraphicNode;

public class RecordOfAnimation {
	private AnimatedAction action;
	private IGraphicNode node;
	private Object object; //objekt k animaci pokud je potřeba
	
	public RecordOfAnimation(AnimatedAction action, IGraphicNode node, Object object) {
		super();
		this.action = action;
		this.node = node;
		this.object = object;
	}

	public AnimatedAction getAction() {
		return action;
	}

	public IGraphicNode getNode1() {
		return node;
	}

	/**
	 * Získá druhý pomocný objekt (každá animace pozná o co se jedná)
	 * @return
	 */
	public Object getObject() {
		return object;
	}
}