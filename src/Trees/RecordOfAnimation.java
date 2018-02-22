package Trees;

public class RecordOfAnimation {
	private AnimatedAction action;
	private INode<?> node;
	private Object object; //objekt k animaci pokud je potřeba
	
	public RecordOfAnimation(AnimatedAction action, INode<?> node, Object object) {
		super();
		this.action = action;
		this.node = node;
		this.object = object;
	}

	public AnimatedAction getAction() {
		return action;
	}

	public INode<?> getNode1() {
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