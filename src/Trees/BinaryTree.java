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
        Side side = result.getSide(); //zjistím smìr
        removedNode = (BinaryNode) result.getNode();

        if (side != Side.NONE)  //pokud ho nenajdu TODO
            return null;

        if ((removedNode.getLeft() != null) && (removedNode.getRight() != null)) { //pokud má dva potomky (bere se nejlevìjší z pravé vìtve)
        	
        	helpNode = removedNode.getRight(); //dosadím pravého 

            if (helpNode.getLeft() == null) { //pokud pravý potomek nemá levého    
            	removedNode.setValue(helpNode.getValue());
            	removedNode.setRight(helpNode.getRight());
            } else {
            	while (helpNode.getLeft()!= null) {  //dokud nemám poslední levý
                    helpNode = helpNode.getLeft();
                }
            //	helpNode.setLeft(removedNode.getLeft()); 
            //	helpNode.setRight(removedNode.getRight());
            }
             // uložím levého potomka mazaného do toho co ho nahradí
            
            removedNode.setValue(helpNode.getValue()); //uložím jeho hodnotu do toho co mažu
            
            
            result.addAnimation(AnimatedAction.DELETE, null, true);            
            
            if (helpNode.getRight() == null) {
                helpNode.getParent().deleteLeft();  //smažu nejlevìjšího  
                result.addAnimation(AnimatedAction.MOVE, result.getNode(), helpNode);
            } else {
            	helpNode.getParent().setLeft(helpNode.getRight());  //nebo dosadím místo nìho jeho pravého
            	result.addAnimation(AnimatedAction.MOVEVALUE, result.getNode(), helpNode);
            	result.addAnimation(AnimatedAction.MOVE, helpNode, helpNode.getRight());
            }
        } else if (removedNode.getLeft() != null) {   //zjistím jakého potomka nemá jeho rodiè    
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVE, result.getNode(), removedNode.getLeft());
            
            removedNode.setNode(removedNode.getLeft());
            
        } else if (removedNode.getRight() != null) {
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVE, result.getNode(), removedNode.getRight());
            
            removedNode.setNode(removedNode.getRight());            
        } else {
        	
        	result.addAnimation(AnimatedAction.DELETE, null, false); //pokud nemá dìti 
            if (side == Side.LEFT) { //nemá žádného potomka, tak je to list => smažu ho
                removedNode.getParent().deleteLeft();
            } else {
                removedNode.getParent().deleteRight();
            }
            return result; //bez animace zmìny koøenu
        } 
        
        return result;
	}
    
	@Override
	public Result<BinaryNode> insert(int value) {
		Result<BinaryNode> result = search(value);
	    Side side = result.getSide(); //ovìøíme poslední stranu
	    BinaryNode parent = (BinaryNode) result.getNode();  // vrátí prvek (side = null) nebo rodièe a místo kam uložit (side = R, L)
	    
	    if (side == Side.LEFT) {
	    	parent.setLeft(new BinaryNode(value, parent));
	    	result.setNode(parent.getLeft()); //zmìním výsledek z rodièe na nový node
	    } else if (side == Side.RIGHT) {
	    	parent.setRight(new BinaryNode(value, parent));
	    	result.setNode(parent.getRight());
	    } else {
	    	result.setNode(null); //už je obsažen  
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
	 * @param value - hledaný list
	 * @return ResultNode<BinaryNode> - vrací nalezený list (side = NONE) nebo vrací rodièe a stranu
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