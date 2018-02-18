package Trees;

public class RecordOfAnimation {
	private AnimatedAction action;
	private INode<?> node;
	private Object object; //objekt k animaci pokud je pot�eba
	
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
	 * Z�sk� druh� pomocn� objekt (ka�d� animace pozn� o co se jedn�)
	 * @return
	 */
	public Object getObject() {
		return object;
	}
}