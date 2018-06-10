package Trees;

public class AVLTree implements ITree<AVLNode> {
	
	private AVLNode root = null;

	public AVLTree() {}	
	
	@Override
	public Result<AVLNode> insert(int value) {
		if (root == null) {
			root = new AVLNode(value);
			return null;
		}
		Result<AVLNode> result = search(value);
	    Side side = result.getSide(); //ov���me posledn� stranu
	    AVLNode parent = (AVLNode) result.getNode();  // vr�t� prvek (side = null) nebo rodi�e a m�sto kam ulo�it (side = R, L)
	    
	    if (side == Side.LEFT) {
	    	parent.setLeft(new AVLNode(value, parent, side));
	    	result.setNode(parent.getLeft()); //zm�n�m v�sledek z rodi�e na nov� node
	    } else if (side == Side.RIGHT) {
	    	parent.setRight(new AVLNode(value, parent, side));
	    	result.setNode(parent.getRight());
	    } else {
	    	result.setNode(null); //u� je obsa�en  
	    	return result;
	    }
	    
	    result.addAnimation(AnimatedAction.INSERT, null, null);
	    return result;
	}
    
    @Override
	public Result<AVLNode> delete(int value) {
    	AVLNode removedNode, helpNode = null;
        
        Result<AVLNode> result = search(value);
        Side side = result.getSide(); //zjist�m sm�r
        removedNode = (AVLNode) result.getNode();

        if (side != Side.NONE) {  //pokud ho nenajdu TODO
            return result;
        }

		if ((removedNode.getLeft() != null) && (removedNode.getRight() != null)) { // pokud m� dva potomnky 1.
			helpNode = removedNode.getRight(); // dosad�m prav�ho

			if (helpNode.getLeft() != null || helpNode.getRight() != null) { //pokud dosazovan� m� potomky 1.1
				if (helpNode.getLeft() != null) { // pokud prav� potomek nem� lev�ho 1.2
					while (helpNode.getLeft() != null) { // dokud nem�m posledn� lev� 1.3
						helpNode = helpNode.getLeft();
					}					
				}
			}             
            
            removedNode.setValue(helpNode.getValue()); //ulo��m jeho hodnotu do toho co ma�u
            
            result.addAnimation(AnimatedAction.DELETE, null, true);            
            
            if (helpNode.getRight() == null) { //0.1
            	if (helpNode.getGraphicNode().getSide() == Side.RIGHT) { //0.1.1
            		helpNode.getParent().deleteRight();
            		System.out.println("\n0.1.1\n");
            	} else { //0.1.2
            		helpNode.getParent().deleteLeft(); 
            		System.out.println("\n0.1.2\n");
            	}
                
                result.addAnimation(AnimatedAction.MOVENODE, removedNode.getGraphicNode(), helpNode.getGraphicNode()); 
                removedNode.setGraphicNode(helpNode.getGraphicNode());
                
            } else { //0.2
            	if (helpNode.getGraphicNode().getSide() == Side.RIGHT) { //0.2.1
            		helpNode.getParent().setRight(helpNode.getRight());
            		System.out.println("\n0.2.1\n");
            	} else { //0.2.2
            		helpNode.getParent().setLeft(helpNode.getRight());  //nebo dosad�m m�sto n�ho jeho prav�ho
            		System.out.println("\n0.2.2\n");
            	}
            	
            	result.addAnimation(AnimatedAction.MOVEVALUE, result.getNode().getGraphicNode(), helpNode.getGraphicNode());
            	result.addAnimation(AnimatedAction.MOVENODE, helpNode.getGraphicNode(), helpNode.getRight().getGraphicNode());
            	//result.addAnimation(AnimatedAction.MOVEVALUEFINISH, result.getNode().getGraphicNode(), helpNode.getGraphicNode());
            	
            	//helpNode.setGraphicNode(removedNode.getRight().getGraphicNode()); /******nov�******/
            }
        } else if (removedNode.getLeft() != null) {   //zjist�m jak�ho potomka m� mazan�  2.
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVENODE, result.getNode().getGraphicNode(), removedNode.getLeft().getGraphicNode());
            
            //result.getNode().setGraphicNode(removedNode.getLeft().getGraphicNode()); /******nov�******/
            
            removedNode.setNode(removedNode.getLeft());
            
            System.out.println("\n2.\n");            
            
        } else if (removedNode.getRight() != null) { // 3.
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVENODE, result.getNode().getGraphicNode(), removedNode.getRight().getGraphicNode());
            
          //  result.getNode().setGraphicNode(removedNode.getRight().getGraphicNode()); /******nov�******/
            
            removedNode.setNode(removedNode.getRight());  
            System.out.println("\n3.\n");
        } else { // 4.   
        	System.out.println("\n4.\n");
        	result.addAnimation(AnimatedAction.DELETE, null, false); //pokud nem� d�ti 
            if (removedNode.getGraphicNode().getSide() == Side.LEFT) { //nem� ��dn�ho potomka, tak je to list => sma�u ho
                removedNode.getParent().deleteLeft();               
            } else if (removedNode.equals(root)) { //osamocen� root
            	root = null;
            } else {
            	removedNode.getParent().deleteRight();  //prav�            	
            }
            return result; 
        } 
        
        return result;
	}	
	
	/**
	 * @param value - hledan� list
	 * @return ResultNode<AVLNode> - vrac� nalezen� list (side = NONE) nebo vrac� rodi�e a stranu
	 * 
	 */
	@Override
    public Result<AVLNode> search(int value) {
		Result<AVLNode> resultNode = new Result<>(root);
		AVLNode result = root;
		AVLNode parent = root;

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
	public AVLNode getRoot() {
		return root;
	}
	
	
    	
}
