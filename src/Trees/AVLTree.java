package Trees;

import java.util.ArrayList;

public class AVLTree implements ITree<AVLNode> {
	
	private AVLNode root = null;
	private ArrayList<AVLNode> nodes = new ArrayList<>();
	
	private boolean balance = true;

	public AVLTree() {}	
	
	@Override
	public Result<AVLNode> insert(int value) {
		if (root == null) {
			root = new AVLNode(value);
			nodes.add(root);
			return null;
		}
		Result<AVLNode> result = search(value);
	    Side side = result.getSide(); //ov���me posledn� stranu
	    AVLNode parent = (AVLNode) result.getNode();  // vr�t� prvek (side = null) nebo rodi�e a m�sto kam ulo�it (side = R, L)
	    
	    if (side == Side.LEFT) {
	    	parent.setLeftWithGraphic(new AVLNode(value, parent, side));
	    	result.setNode(parent.getLeft()); //zm�n�m v�sledek z rodi�e na nov� node
	    } else if (side == Side.RIGHT) {
	    	parent.setRightWithGraphic(new AVLNode(value, parent, side));
	    	result.setNode(parent.getRight());
	    } else {
	    	result.setNode(null); //u� je obsa�en  
	    	return result;
	    }
	    nodes.add((AVLNode)result.getNode());
	    result.addAnimation(AnimatedAction.INSERT, null, null);
	    
	    if (balance) {
	    	return balanceTree(result, (AVLNode)result.getNode());
	    } else {
	    	root.countFactor();
	    	result.addAnimation(AnimatedAction.UPDATEFACTOR, null, false);
	    	return result;
	    }	    
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

		if ((removedNode.getLeft() != null) && (removedNode.getRight() != null)) { // pokud m� 2 potomky 1.
			helpNode = removedNode.getRight(); // dosad�m prav�ho

			if (helpNode.getLeft() != null || helpNode.getRight() != null) { //pokud dosazovan� m� lev� potomky 1.1
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
            		helpNode.getParent().deleteRightWithGraphic();
            	} else { //0.1.2
            		helpNode.getParent().deleteLeftWithGraphic(); 
            	}
                
                result.addAnimation(AnimatedAction.MOVENODE, removedNode.getGraphicNode(), helpNode.getGraphicNode()); 
                removedNode.setGraphicNode(helpNode.getGraphicNode());
                
            } else { //0.2
            	if (helpNode.getGraphicNode().getSide() == Side.RIGHT) { //0.2.1
            		helpNode.getParent().setRightWithGraphic(helpNode.getRight());
            	} else { //0.2.2
            		helpNode.getParent().setLeftWithGraphic(helpNode.getRight());  //nebo dosad�m m�sto n�ho jeho prav�ho
            	}
            	
            	result.addAnimation(AnimatedAction.MOVEVALUE, result.getNode().getGraphicNode(), helpNode.getGraphicNode());
            	result.addAnimation(AnimatedAction.MOVENODE, helpNode.getGraphicNode(), helpNode.getRight().getGraphicNode());
            }
        } else if (removedNode.getLeft() != null) {   //zjist�m jak�ho potomka m� mazan�  2.
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVENODE, result.getNode().getGraphicNode(), removedNode.getLeft().getGraphicNode());
            
            removedNode.setNodeWithGraphic(removedNode.getLeft());            
        } else if (removedNode.getRight() != null) { // 3.
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVENODE, result.getNode().getGraphicNode(), removedNode.getRight().getGraphicNode());
            
            removedNode.setNodeWithGraphic(removedNode.getRight());  
        } else { // 4.   
        	result.addAnimation(AnimatedAction.DELETE, null, false); //pokud nem� d�ti
        	
        	if (removedNode.getGraphicNode().getSide() == Side.LEFT) { //nem� ��dn�ho potomka, tak je to list => sma�u ho
                removedNode.getParent().deleteLeftWithGraphic();  
            } else if (removedNode.equals(root)) { //osamocen� root
            	root = null;
            	return balanceTree(result, null);
            } else {
            	removedNode.getParent().deleteRightWithGraphic();  //prav�            	
            }
            
            return balanceTree(result, removedNode.getParent());
        } 
        
        return balanceTree(result, removedNode);
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
	
	/**
	 * Zavol� funkci pro ohodnocen� list� a p��padn� p�id� akce pro balancov�n� stromu
	 * @param result
	 * @return
	 */
	private Result<AVLNode> balanceTree(Result<AVLNode> result, AVLNode startNode) {
		root.countFactor();
		
		AVLNode balanceNode = startNode;
		
		result.addAnimation(AnimatedAction.UPDATEFACTOR, startNode.getGraphicNode(), true);
		
		while (balanceNode != null) {
			if (balanceNode.getFactor() == 2 || balanceNode.getFactor() == -2) {
				break;
			}
			balanceNode = balanceNode.getParent();
		}
		
		if (balanceNode != null) {
			if (balanceNode.getFactor() == 2) {
				if (balanceNode.getLeft().getFactor() == -1) {
					return rlBalance(result, balanceNode);
				} else {
					return rrBalance(result, balanceNode);
				}
			} else {
				if (balanceNode.getRight().getFactor() == 1) {
					return lrBalance(result, balanceNode);
				} else {
					return llBalance(result, balanceNode);
				}
			}
		}		
		
		return result;		
	}
	
	private Result<AVLNode> llBalance(Result<AVLNode> result, AVLNode nodeB) {
		AVLNode nodeA = nodeB.getRight();
		
		if (nodeB.getParent() == null) {
			root = nodeA;
		}
		
		nodeB.setRight(nodeA.getLeft());
		nodeA.setLeft(nodeB);
		
		result.addAnimation(AnimatedAction.LL, nodeB.getGraphicNode(), root);
		
		root.countFactor();
		
		return result;
	}

	private Result<AVLNode> lrBalance(Result<AVLNode> result, AVLNode nodeC) {
		AVLNode nodeA = nodeC.getRight();
		AVLNode nodeB = nodeA.getLeft();
		
		if (nodeC.getParent() == null) {
			root = nodeB;
		}
		
		nodeC.setRight(nodeB.getLeft());
		nodeA.setLeft(nodeB.getRight());
		nodeB.setLeft(nodeC);
		nodeB.setRight(nodeA);
		
		result.addAnimation(AnimatedAction.LR, nodeB.getGraphicNode(), root);		
		
		return result;
	}

	private Result<AVLNode> rrBalance(Result<AVLNode> result, AVLNode nodeB) {
		AVLNode nodeA = nodeB.getLeft();
		
		if (nodeB.getParent() == null) {
			root = nodeA;
		}
		
		nodeB.setLeft(nodeA.getRight());
		nodeA.setRight(nodeB);
		
		result.addAnimation(AnimatedAction.RR, nodeB.getGraphicNode(), root);		
		
		return result;
	}

	private Result<AVLNode> rlBalance(Result<AVLNode> result, AVLNode nodeC) {
		AVLNode nodeA = nodeC.getLeft();
		AVLNode nodeB = nodeA.getRight();
		
		if (nodeC.getParent() == null) {
			root = nodeB;
		}
		
		nodeC.setLeft(nodeB.getRight());
		nodeA.setRight(nodeB.getLeft());
		nodeB.setRight(nodeC);
		nodeB.setLeft(nodeA);
		
		result.addAnimation(AnimatedAction.RL, nodeB.getGraphicNode(), root);		
		
		return result;
	}
	
	@Override
	public void disableBalance() {
		this.balance = false;		
	}
	
	@Override
	public void enableBalance() {
		this.balance = true;		
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