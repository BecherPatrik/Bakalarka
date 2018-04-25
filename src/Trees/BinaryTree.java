package Trees;

public class BinaryTree implements ITree<BinaryNode> {
    private BinaryNode root = null;
    
    public BinaryTree() {
        
    }
    
    @Override
	public Result<BinaryNode> insert(int value) {
		if (root == null) {
			root = new BinaryNode(value);
			return null;
		}
		Result<BinaryNode> result = search(value);
	    Side side = result.getSide(); //ověříme poslední stranu
	    BinaryNode parent = (BinaryNode) result.getNode();  // vrátí prvek (side = null) nebo rodiče a místo kam uložit (side = R, L)
	    
	    if (side == Side.LEFT) {
	    	parent.setLeft(new BinaryNode(value, parent, side));
	    	result.setNode(parent.getLeft()); //změním výsledek z rodiče na nový node
	    } else if (side == Side.RIGHT) {
	    	parent.setRight(new BinaryNode(value, parent, side));
	    	result.setNode(parent.getRight());
	    } else {
	    	result.setNode(null); //už je obsažen  
	    	return result;
	    }
	    
	    result.addAnimation(AnimatedAction.INSERT, null, null);
	    return result;
	}
    
    @Override
	public Result<BinaryNode> delete(int value) {
    	BinaryNode removedNode, helpNode = null;
        
        Result<BinaryNode> result = search(value);
        Side side = result.getSide(); //zjistím směr
        removedNode = (BinaryNode) result.getNode();

        if (side != Side.NONE)  //pokud ho nenajdu TODO
            return null;

		if ((removedNode.getLeft() != null) && (removedNode.getRight() != null)) { // pokud má dva potomnky 1.
			helpNode = removedNode.getRight(); // dosadím pravého

			if (!(helpNode.getLeft() == null && helpNode.getRight() == null)) { //pokud dosazovaný má potomky 1.1

				/*if (helpNode.getLeft() == null) { // pokud pravý potomek nemá levého 1.2
					removedNode.setRight(helpNode.getRight()); 
				} else {
					while (helpNode.getLeft() != null) { // dokud nemám poslední levý 1.3
						helpNode = helpNode.getLeft();
					}					
				}*/
				if (helpNode.getLeft() != null) { // pokud pravý potomek nemá levého 1.2
					while (helpNode.getLeft() != null) { // dokud nemám poslední levý 1.3
						helpNode = helpNode.getLeft();
					}					
				}
			}             
            
            removedNode.setValue(helpNode.getValue()); //uložím jeho hodnotu do toho co mažu
            
            
            result.addAnimation(AnimatedAction.DELETE, null, true);            
            
            if (helpNode.getRight() == null) { //0.1
            	if (helpNode.getGraphicNode().getSide() == Side.RIGHT) { //0.1.1
            		helpNode.getParent().deleteRight();
            	} else { //0.1.2
            		helpNode.getParent().deleteLeft(); 
            	}
                
                result.addAnimation(AnimatedAction.MOVENODE, removedNode.getGraphicNode(), helpNode.getGraphicNode()); 
                removedNode.setGraphicNode(helpNode.getGraphicNode());
                
            } else { //0.2
            	if (helpNode.getGraphicNode().getSide() == Side.RIGHT) { //0.2.1
            		helpNode.getParent().setRight(helpNode.getRight());            		
            	} else { //0.2.2
            		helpNode.getParent().setLeft(helpNode.getRight());  //nebo dosadím místo něho jeho pravého
            	}
            	
            	result.addAnimation(AnimatedAction.MOVEVALUE, result.getNode().getGraphicNode(), helpNode.getGraphicNode());
            	result.addAnimation(AnimatedAction.MOVENODE, helpNode.getGraphicNode(), helpNode.getRight().getGraphicNode());
            	//result.addAnimation(AnimatedAction.MOVEVALUEFINISH, result.getNode().getGraphicNode(), helpNode.getGraphicNode());
            	
            	//helpNode.setGraphicNode(removedNode.getRight().getGraphicNode()); /******nové******/
            }
        } else if (removedNode.getLeft() != null) {   //zjistím jakého potomka nemá mazaný  2.
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVENODE, result.getNode().getGraphicNode(), removedNode.getLeft().getGraphicNode());
            
            result.getNode().setGraphicNode(removedNode.getLeft().getGraphicNode()); /******nové******/
            
            removedNode.setNode(removedNode.getLeft());
            
        } else if (removedNode.getRight() != null) { // 3.
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVENODE, result.getNode().getGraphicNode(), removedNode.getRight().getGraphicNode());
            
            result.getNode().setGraphicNode(removedNode.getRight().getGraphicNode()); /******nové******/
            
            removedNode.setNode(removedNode.getRight());            
        } else { // 4.       	
        	result.addAnimation(AnimatedAction.DELETE, null, false); //pokud nemá děti 
            if (removedNode.getGraphicNode().getSide() == Side.LEFT) { //nemá žádného potomka, tak je to list => smažu ho
                removedNode.getParent().deleteLeft();               
            } else if (removedNode.equals(root)) { //osamocený root
            	root = null;
            } else {
            	removedNode.getParent().deleteRight();  //pravý            	
            }
            return result; 
        } 
        
        return result;
	}	
	
	/**
	 * @param value - hledaný list
	 * @return ResultNode<BinaryNode> - vrací nalezený list (side = NONE) nebo vrací rodiče a stranu
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
        	resultNode.addAnimation(AnimatedAction.SEARCH, result.getGraphicNode(), true);
        	resultNode.setNode(result);
        } else {
        	resultNode.addAnimation(AnimatedAction.SEARCH, parent.getGraphicNode(), false);
        	resultNode.setNode(parent);
        }
        return resultNode;
    }
	
	/********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/
	
	@Override
	public BinaryNode getRoot() {
		return root;
	}
}