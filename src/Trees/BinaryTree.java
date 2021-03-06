package trees;

public class BinaryTree implements ITree {
    private BinaryNode root = null;
    
    public BinaryTree() {}
    
    @Override
	public Result insert(int value) {
		if (root == null) {
			root = new BinaryNode(value);
			return null;
		}
		Result result = search(value);
	    Side side = result.getSide(); //ověříme poslední stranu
	    BinaryNode parent = (BinaryNode) result.getNode();  // vrátí prvek (side = null) nebo rodiče a místo kam uložit (side = R, L)
	    
	    if (side == Side.LEFT) {
	    	parent.setLeftWithGraphic(new BinaryNode(value, parent, side));
	    	result.setNode(parent.getLeft()); //změním výsledek z rodiče na nový node
	    } else if (side == Side.RIGHT) {
	    	parent.setRightWithGraphic(new BinaryNode(value, parent, side));
	    	result.setNode(parent.getRight());
	    } else {
	    	result.setNode(null); //už je obsažen  
	    	return result;
	    }
	    
	    result.addAnimation(AnimatedAction.INSERT, null, null);
	    return result;
	}
    
    @Override
	public Result delete(int value) {
    	BinaryNode removedNode, helpNode = null;
        
        Result result = search(value);
        Side side = result.getSide(); //zjistím směr
        removedNode = (BinaryNode) result.getNode();

        if (side != Side.NONE) {  
            return result;
        }

		if ((removedNode.getLeft() != null) && (removedNode.getRight() != null)) { // pokud má dva potomnky 1.
			helpNode = removedNode.getRight(); // dosadím pravého

			if (helpNode.getLeft() != null || helpNode.getRight() != null) { //pokud dosazovaný má potomky 1.1
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
            		helpNode.getParent().deleteRightWithGraphic();
            		//System.out.println("\n0.1.1\n");
            	} else { //0.1.2
            		helpNode.getParent().deleteLeftWithGraphic(); 
            		//System.out.println("\n0.1.2\n");
            	}
                
                result.addAnimation(AnimatedAction.MOVENODE, removedNode.getGraphicNode(), helpNode.getGraphicNode()); 
                removedNode.setGraphicNode(helpNode.getGraphicNode());
                
            } else { //0.2
            	if (helpNode.getGraphicNode().getSide() == Side.RIGHT) { //0.2.1
            		helpNode.getParent().setRightWithGraphic(helpNode.getRight());
            		//System.out.println("\n0.2.1\n");
            	} else { //0.2.2
            		helpNode.getParent().setLeftWithGraphic(helpNode.getRight());  //nebo dosadím místo něho jeho pravého
            		//System.out.println("\n0.2.2\n");
            	}
            	
            	result.addAnimation(AnimatedAction.MOVEVALUE, result.getNode().getGraphicNode(), helpNode.getGraphicNode());
            	result.addAnimation(AnimatedAction.MOVENODE, helpNode.getGraphicNode(), helpNode.getRight().getGraphicNode());            	
            }
        } else if (removedNode.getLeft() != null) {   //zjistím jakého potomka má mazaný  2.
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVENODE, result.getNode().getGraphicNode(), removedNode.getLeft().getGraphicNode());            
            
            removedNode.setNodeWithGraphic(removedNode.getLeft());
            
            //System.out.println("\n2.\n");            
            
        } else if (removedNode.getRight() != null) { // 3.
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVENODE, result.getNode().getGraphicNode(), removedNode.getRight().getGraphicNode());            
            
            removedNode.setNodeWithGraphic(removedNode.getRight());  
            //System.out.println("\n3.\n");
        } else { // 4.   
        	//System.out.println("\n4.\n");
        	result.addAnimation(AnimatedAction.DELETE, null, false); //pokud nemá děti 
            if (removedNode.getGraphicNode().getSide() == Side.LEFT) { //nemá žádného potomka, tak je to list => smažu ho
                removedNode.getParent().deleteLeftWithGraphic();               
            } else if (removedNode.equals(root)) { //osamocený root
            	root = null;
            } else {
            	removedNode.getParent().deleteRightWithGraphic();  //pravý            	
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
    public Result search(int value) {
		Result resultNode = new Result(root);
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
            	resultNode.addNodeToWay(result.getGraphicNode());
                break;
            }            
            
            resultNode.addNodeToWay(parent.getGraphicNode());
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
	
	@Override
	public void disableBalance() {
		return;		
	}
	
	@Override
	public void enableBalance() {
		return;
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