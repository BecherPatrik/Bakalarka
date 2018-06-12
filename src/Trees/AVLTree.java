package Trees;

import java.util.ArrayList;

public class AVLTree implements ITree<AVLNode> {
	
	private AVLNode root = null;
	private ArrayList<AVLNode> nodes = new ArrayList<>();

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
	    return balanceTree(result);
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
            		helpNode.getParent().deleteRightWithGraphic();
            		System.out.println("\n0.1.1\n");
            	} else { //0.1.2
            		helpNode.getParent().deleteLeftWithGraphic(); 
            		System.out.println("\n0.1.2\n");
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
            } else {
            	removedNode.getParent().deleteRightWithGraphic();  //prav�            	
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
	
	/**
	 * Zavol� funkci pro ohodnocen� list� a p��padn� p�id� akce pro balancov�n� stromu
	 * @param result
	 * @return
	 */
	private Result<AVLNode> balanceTree(Result<AVLNode> result) {
		root.countFactor();
		
		AVLNode balanceNode = null;
		
		for (AVLNode node : nodes) {
			if (node.getFactor() == 2 || node.getFactor() == -2) {
				balanceNode = node;
				break;
			}
		}
		
		if (balanceNode != null) {
			if (balanceNode.getFactor() == 2) {
				if (balanceNode.getLeft().getFactor() == -1) {
					return rlBalance();
				} else {
					return rrBalance();
				}
			} else {
				if (balanceNode.getRight().getFactor() == 1) {
					return lrBalance();
				} else {
					return llBalance();
				}
			}
		}
		
		return result;		
	}
	
	private Result<AVLNode> llBalance() {
		// TODO Auto-generated method stub
		return null;
	}

	private Result<AVLNode> lrBalance() {
		// TODO Auto-generated method stub
		return null;
	}

	private Result<AVLNode> rrBalance() {
		// TODO Auto-generated method stub
		return null;
	}

	private Result<AVLNode> rlBalance() {
		// TODO Auto-generated method stub
		return null;
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