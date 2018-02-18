package Trees;

public class BinaryTree implements ITree<BinaryNode> {
    private BinaryNode root;
    
    public BinaryTree(int value) {
        this.root = new BinaryNode(value);
    }
    
    @Override
	public Result<BinaryNode> delete(int value) {
    	BinaryNode removedNode, helpNode = null;
        
        Result<BinaryNode> result = search(value);
        Side side = result.getSide(); //zjist�m sm�r
        removedNode = (BinaryNode) result.getNode();

        if (side != Side.NONE)  //pokud ho nenajdu TODO
            return null;

        if ((removedNode.getLeft() != null) && (removedNode.getRight() != null)) { //pokud m� dva potomky (bere se nejlev�j�� z prav� v�tve)
        	
        	helpNode = removedNode.getRight(); //dosad�m prav�ho 

            if (helpNode.getLeft() == null) { //pokud prav� potomek nem� lev�ho    
            	removedNode.setValue(helpNode.getValue());
            	removedNode.setRight(helpNode.getRight());
            } else {
            	while (helpNode.getLeft()!= null) {  //dokud nem�m posledn� lev�
                    helpNode = helpNode.getLeft();
                }
            //	helpNode.setLeft(removedNode.getLeft()); 
            //	helpNode.setRight(removedNode.getRight());
            }
             // ulo��m lev�ho potomka mazan�ho do toho co ho nahrad�
            
            removedNode.setValue(helpNode.getValue()); //ulo��m jeho hodnotu do toho co ma�u
            
            
            result.addAnimation(AnimatedAction.DELETE, null, true);            
            
            if (helpNode.getRight() == null) {
                helpNode.getParent().deleteLeft();  //sma�u nejlev�j��ho  
                result.addAnimation(AnimatedAction.MOVE, result.getNode(), helpNode);
            } else {
            	helpNode.getParent().setLeft(helpNode.getRight());  //nebo dosad�m m�sto n�ho jeho prav�ho
            	result.addAnimation(AnimatedAction.MOVEVALUE, result.getNode(), helpNode);
            	result.addAnimation(AnimatedAction.MOVE, helpNode, helpNode.getRight());
            }
        } else if (removedNode.getLeft() != null) {   //zjist�m jak�ho potomka nem� jeho rodi�    
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVE, result.getNode(), removedNode.getLeft());
            
            removedNode.setNode(removedNode.getLeft());
            
        } else if (removedNode.getRight() != null) {
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVE, result.getNode(), removedNode.getRight());
            
            removedNode.setNode(removedNode.getRight());            
        } else {
        	
        	result.addAnimation(AnimatedAction.DELETE, null, false); //pokud nem� d�ti 
            if (side == Side.LEFT) { //nem� ��dn�ho potomka, tak je to list => sma�u ho
                removedNode.getParent().deleteLeft();
            } else {
                removedNode.getParent().deleteRight();
            }
            return result; //bez animace zm�ny ko�enu
        } 
        
        return result;
	}
    
	@Override
	public Result<BinaryNode> insert(int value) {
		Result<BinaryNode> result = search(value);
	    Side side = result.getSide(); //ov���me posledn� stranu
	    BinaryNode parent = (BinaryNode) result.getNode();  // vr�t� prvek (side = null) nebo rodi�e a m�sto kam ulo�it (side = R, L)
	    
	    if (side == Side.LEFT) {
	    	parent.setLeft(new BinaryNode(value, parent));
	    	result.setNode(parent.getLeft()); //zm�n�m v�sledek z rodi�e na nov� node
	    } else if (side == Side.RIGHT) {
	    	parent.setRight(new BinaryNode(value, parent));
	    	result.setNode(parent.getRight());
	    } else {
	    	result.setNode(null); //u� je obsa�en  
	    	return result;
	    }
	    
	    result.addAnimation(AnimatedAction.INSERT, null, null);
	    return result;
	}
	
	@Override
	public BinaryNode getRoot() {
		return root;
	}
	
	/**
	 * @param value - hledan� list
	 * @return ResultNode<BinaryNode> - vrac� nalezen� list (side = NONE) nebo vrac� rodi�e a stranu
	 * 
	 */
	@Override
    public Result<BinaryNode> search(int value) {
		Result<BinaryNode> resultNode = new Result<>(root);
		BinaryNode result = root;
		BinaryNode parent = root;

        while (result != null) {
            if (value < result.getValue()) {
            	parent = result;
                result = result.getLeft();
                
                resultNode.setSide(Side.LEFT); 
            } else if (value > result.getValue()) {
            	parent = result;
            	result = result.getRight();
            	
            	resultNode.setSide(Side.RIGHT); 
            } else {               
            	resultNode.setSide(Side.NONE);
            	resultNode.addSide(result.getGraphicNode());
                break;
            }            
            
            resultNode.addSide(parent.getGraphicNode());
        }
      
        if (resultNode.getSide() == Side.NONE) {
        	resultNode.addAnimation(AnimatedAction.SEARCH, result, true);
        	resultNode.setNode(result);
        } else {
        	resultNode.addAnimation(AnimatedAction.SEARCH, parent, false);
        	resultNode.setNode(parent);
        }
        return resultNode;
    }

}