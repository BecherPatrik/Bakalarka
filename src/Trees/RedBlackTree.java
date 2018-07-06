package Trees;

import java.util.ArrayList;

public class RedBlackTree implements ITree<RedBlackNode> {
	
	private RedBlackNode root = null;
	private ArrayList<RedBlackNode> nodes = new ArrayList<>();
	
	private boolean balance = true;
	private boolean dblack = false;
	private boolean dblackColor = false;

	public RedBlackTree() {}	
	
	@Override
	public Result<RedBlackNode> insert(int value) {
		if (root == null) {
			root = new RedBlackNode(value);
			root.setColor(Color.BLACK);
			root.getGraphicNode().setColor(Color.BLACK);
			nodes.add(root);
			return null;
		}
		Result<RedBlackNode> result = search(value);
	    Side side = result.getSide(); //ověříme poslední stranu
	    RedBlackNode parent = (RedBlackNode) result.getNode();  // vrátí prvek (side = null) nebo rodiče a místo kam uložit (side = R, L)
	    
	    if (side == Side.LEFT) {
	    	parent.setLeftWithGraphic(new RedBlackNode(value, parent, side));
	    	result.setNode(parent.getLeft()); //změním výsledek z rodiče na nový node
	    } else if (side == Side.RIGHT) {
	    	parent.setRightWithGraphic(new RedBlackNode(value, parent, side));
	    	result.setNode(parent.getRight());
	    } else {
	    	result.setNode(null); //už je obsažen  
	    	return result;
	    }
	    nodes.add((RedBlackNode)result.getNode());
	    result.addAnimation(AnimatedAction.INSERT, null, null);
	    
	    if (balance) {
	    	return balanceTree(result, (RedBlackNode)result.getNode());
	    } else {    	
	    	return result;
	    }	    
	}
    
    @Override
	public Result<RedBlackNode> delete(int value) {
    	RedBlackNode removedNode, helpNode = null;
        
        Result<RedBlackNode> result = search(value);
        Side side = result.getSide(); //zjistím směr
        removedNode = (RedBlackNode) result.getNode();
        RedBlackNode parent;

        if (side != Side.NONE) {  //pokud ho nenajdu 
            return result;
        }        

		if ((removedNode.getLeft() != null) && (removedNode.getRight() != null)) { // pokud má 2 potomky 1.
			helpNode = removedNode.getRight(); // dosadím pravého

			if (helpNode.getLeft() != null || helpNode.getRight() != null) { //pokud dosazovaný má levé potomky 1.1
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
            		side = Side.RIGHT;
            	} else { //0.1.2
            		helpNode.getParent().deleteLeftWithGraphic();
            		side = Side.LEFT;
            	}
                
                result.addAnimation(AnimatedAction.MOVENODE, removedNode.getGraphicNode(), helpNode.getGraphicNode());
                parent = helpNode.getParent();
                removedNode.setGraphicNode(helpNode.getGraphicNode());
                
            	result.addAnimation(AnimatedAction.RECOLOR, helpNode.getGraphicNode(), removedNode.getColor());/***/
                
                if (helpNode.getColor() == Color.BLACK) {
                	return doubleBlack(result, parent, side); /** B1 **/ 
                } else {
                	return result;
                }
                
            } else { //0.2
            	if (helpNode.getGraphicNode().getSide() == Side.RIGHT) { //0.2.1
            		helpNode.getParent().setRightWithGraphic(helpNode.getRight());
            	} else { //0.2.2
            		helpNode.getParent().setLeftWithGraphic(helpNode.getRight());  //nebo dosadím místo něho jeho pravého
            	}
            	
            	result.addAnimation(AnimatedAction.MOVEVALUE, result.getNode().getGraphicNode(), helpNode.getGraphicNode());
            	result.addAnimation(AnimatedAction.MOVENODE, helpNode.getGraphicNode(), helpNode.getRight().getGraphicNode());
            	
            	result.addAnimation(AnimatedAction.RECOLOR, helpNode.getRight().getGraphicNode(), helpNode.getColor()); /***/
            	
            	if (helpNode.getRight().getColor() == Color.BLACK) {
            		return doubleBlack(result, helpNode, Side.RIGHT); /** B1 **/
            	} else {
            		return result;
            	}            	
            }
        } else if (removedNode.getLeft() != null) {   //zjistím jakého potomka má mazaný  2.
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVENODE, result.getNode().getGraphicNode(), removedNode.getLeft().getGraphicNode());
            
            helpNode = removedNode.getLeft();
            removedNode.setNodeWithGraphic(removedNode.getLeft());  
            
            result.addAnimation(AnimatedAction.RECOLOR, helpNode.getGraphicNode(), removedNode.getColor()); /***/
        	
        	if (helpNode.getColor() == Color.BLACK) {
        		return doubleBlack(result, removedNode, Side.LEFT); /** B1 **/
        	} else {
        		return result;
        	}
        } else if (removedNode.getRight() != null) { // 3.
        	result.addAnimation(AnimatedAction.DELETE, null, true);
            result.addAnimation(AnimatedAction.MOVENODE, result.getNode().getGraphicNode(), removedNode.getRight().getGraphicNode());
            
            helpNode = removedNode.getRight();
            removedNode.setNodeWithGraphic(removedNode.getRight());  
            
            result.addAnimation(AnimatedAction.RECOLOR, helpNode.getGraphicNode(), removedNode.getColor()); /***/
        	
        	if (helpNode.getColor() == Color.BLACK) {
        		return doubleBlack(result, removedNode, Side.RIGHT); /** B1 **/
        	} else {
        		return result;
        	}
        } else { // 4.   
        	result.addAnimation(AnimatedAction.DELETE, null, false); //pokud nemá děti
        	
        	if (removedNode.getGraphicNode().getSide() == Side.LEFT) { //nemá žádného potomka, tak je to list => smažu ho
                removedNode.getParent().deleteLeftWithGraphic(); 
                
                if (removedNode.getColor() == Color.BLACK) {
                	return doubleBlack(result, removedNode.getParent(), Side.LEFT); /** B1 **/
                }
                
            } else if (removedNode.equals(root)) { //osamocený root
            	root = null;
            	return result;
            } else {
            	removedNode.getParent().deleteRightWithGraphic();  //pravý
            	
            	if (removedNode.getColor() == Color.BLACK) {
                	return doubleBlack(result, removedNode.getParent(), Side.RIGHT); /** B1 **/
                }
            }
            
            return result;
        }       
	}	
	
	/**
	 * @param value - hledaný list
	 * @return ResultNode<RedBlackNode> - vrací nalezený list (side = NONE) nebo vrací rodiče a stranu
	 * 
	 */
	@Override
    public Result<RedBlackNode> search(int value) {
		Result<RedBlackNode> resultNode = new Result<>(root);
		RedBlackNode result = root;
		RedBlackNode parent = root;

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
	 * Zavolá funkci pro ohodnocení listů a případně přidá akce pro balancování stromu
	 * @param result
	 * @return
	 */
	private Result<RedBlackNode> balanceTree(Result<RedBlackNode> result, RedBlackNode startNode) {			
		RedBlackNode balanceNode = startNode.getParent();
		if (balanceNode == null) {
			result.addAnimation(AnimatedAction.RECOLOR, root.getGraphicNode(), Color.BLACK);
			return result;
		}		
		
		while (balanceNode != null) {			
			if (balanceNode.getColor() == Color.RED) {
				if (balanceNode.getParent().getLeft() != null && balanceNode.getParent().getRight() != null
						&& balanceNode.getParent().getLeft().getColor() == balanceNode.getParent().getRight().getColor()) {
					balanceNode.getParent().getLeft().setColor(Color.BLACK);
					balanceNode.getParent().getRight().setColor(Color.BLACK);					
					
					result.addAnimation(AnimatedAction.RECOLOR, balanceNode.getParent().getLeft().getGraphicNode(), Color.BLACK);
					result.addAnimation(AnimatedAction.RECOLOR, balanceNode.getParent().getRight().getGraphicNode(), Color.BLACK);
					if (balanceNode.getParent().getParent() != null) {
						balanceNode.getParent().setColor(Color.RED);
						result.addAnimation(AnimatedAction.RECOLOR, balanceNode.getParent().getGraphicNode(), Color.RED);
						if (!(balanceNode.getParent().getParent().getColor() == Color.RED)) {
							balanceNode = balanceNode.getParent(); //musím ho přeskočit
						} else {
							balanceNode = balanceNode.getParent();
							startNode = balanceNode;
						}												
					}					
				} else {
					result.addAnimation(AnimatedAction.RECOLOR, balanceNode.getGraphicNode(), balanceNode.getGraphicNode().getColor());
					break;
				}
			} else {
				balanceNode = null;
				break;
			}
			
			balanceNode = balanceNode.getParent();
		}
		
		if (balanceNode != null) {
			if (startNode.equals(balanceNode.getLeft())) {
				if (balanceNode.getGraphicNode().getSide() == Side.LEFT) {
					return rrBalance(result, balanceNode.getParent());
				} else {
					return lrBalance(result, balanceNode.getParent());
				}
			} else if (startNode.equals(balanceNode.getRight())) {
				if (balanceNode.getGraphicNode().getSide() == Side.LEFT) {
					return rlBalance(result, balanceNode.getParent());
				} else {
					return llBalance(result, balanceNode.getParent());
				}
			}			
		}		
		
		return result;		
	}	
	
	private Result<RedBlackNode> llBalance(Result<RedBlackNode> result, RedBlackNode nodeB) {
		RedBlackNode nodeA = nodeB.getRight();	
		
		if (nodeB.getParent() == null) {
			root = nodeA;
			nodeA.setParent(null);
		} else {
			if (nodeB.getGraphicNode().getSide() == Side.LEFT) {
				nodeB.getParent().setLeft(nodeA);
			} else {
				nodeB.getParent().setRight(nodeA);
			}			
		}
		
		nodeB.setRight(nodeA.getLeft());
		nodeA.setLeft(nodeB);		
		
		result.addAnimation(AnimatedAction.LL, nodeB.getGraphicNode(), null);
		
		if (dblackColor) {
			nodeA.setColor(nodeB.getColor());
			result.addAnimation(AnimatedAction.RECOLOR, nodeA.getGraphicNode(), nodeB.getColor());
			
			nodeB.setColor(Color.BLACK);
			result.addAnimation(AnimatedAction.RECOLOR, nodeB.getGraphicNode(), Color.BLACK);
			
			nodeA.getRight().setColor(Color.BLACK);
			result.addAnimation(AnimatedAction.RECOLOR, nodeA.getRight().getGraphicNode(), Color.BLACK);
			
			dblackColor = false;
		} else {
			nodeA.setColor(Color.BLACK);
			nodeB.setColor(Color.RED);
			result.addAnimation(AnimatedAction.RECOLOR, nodeA.getGraphicNode(), Color.BLACK);
			result.addAnimation(AnimatedAction.RECOLOR, nodeB.getGraphicNode(), Color.RED);
		}		
		
		if (dblack) {
			result.addAnimation(AnimatedAction.SETDOUBLEBLACK, null, null);
			dblack = false;
		}
		
		return result;
	}

	private Result<RedBlackNode> lrBalance(Result<RedBlackNode> result, RedBlackNode nodeC) {
		RedBlackNode nodeA = nodeC.getRight();
		RedBlackNode nodeB = nodeA.getLeft();
		
		if (nodeC.getParent() == null) {
			root = nodeB;
			nodeB.setParent(null);
		} else {
			if (nodeC.getGraphicNode().getSide() == Side.LEFT) {
				nodeC.getParent().setLeft(nodeB);
			} else {
				nodeC.getParent().setRight(nodeB);
			}			
		}
		
		nodeC.setRight(nodeB.getLeft());
		nodeA.setLeft(nodeB.getRight());
		nodeB.setLeft(nodeC);
		nodeB.setRight(nodeA);
		
		result.addAnimation(AnimatedAction.LR, nodeC.getGraphicNode(), null);
		
		if (dblackColor) {
			nodeB.setColor(nodeC.getColor());
			result.addAnimation(AnimatedAction.RECOLOR, nodeB.getGraphicNode(), nodeC.getColor());
			
			nodeA.setColor(Color.BLACK);
			result.addAnimation(AnimatedAction.RECOLOR, nodeA.getGraphicNode(), Color.BLACK);
			
			dblackColor = false;
		} else {
			nodeC.setColor(Color.RED);
			nodeB.setColor(Color.BLACK);		
					
			result.addAnimation(AnimatedAction.RECOLOR, nodeC.getGraphicNode(), Color.RED);
			result.addAnimation(AnimatedAction.RECOLOR, nodeB.getGraphicNode(), Color.BLACK);
		}		
		
		if (dblack) {
			result.addAnimation(AnimatedAction.SETDOUBLEBLACK, null, null);
			dblack = false;
		}
		
		return result;
	}

	private Result<RedBlackNode> rrBalance(Result<RedBlackNode> result, RedBlackNode nodeB) {
		RedBlackNode nodeA = nodeB.getLeft();
		
		if (nodeB.getParent() == null) {
			root = nodeA;
			nodeA.setParent(null);
		} else {
			if (nodeB.getGraphicNode().getSide() == Side.LEFT) {
				nodeB.getParent().setLeft(nodeA);
			} else {
				nodeB.getParent().setRight(nodeA);
			}			
		}
		
		nodeB.setLeft(nodeA.getRight());
		nodeA.setRight(nodeB);
		
		result.addAnimation(AnimatedAction.RR, nodeB.getGraphicNode(), null);
		
		if (dblackColor) {
			nodeA.setColor(nodeB.getColor());
			result.addAnimation(AnimatedAction.RECOLOR, nodeA.getGraphicNode(), nodeB.getColor());
			
			nodeB.setColor(Color.BLACK);
			result.addAnimation(AnimatedAction.RECOLOR, nodeB.getGraphicNode(), Color.BLACK);
			
			nodeA.getRight().setColor(Color.BLACK);
			result.addAnimation(AnimatedAction.RECOLOR, nodeA.getRight().getGraphicNode(), Color.BLACK);
			
			dblackColor = false;
		} else {
			nodeA.setColor(Color.BLACK);
			nodeB.setColor(Color.RED);
			result.addAnimation(AnimatedAction.RECOLOR, nodeA.getGraphicNode(), Color.BLACK);
			result.addAnimation(AnimatedAction.RECOLOR, nodeB.getGraphicNode(), Color.RED);
		}
		
		if (dblack) {
			result.addAnimation(AnimatedAction.SETDOUBLEBLACK, null, null);
			dblack = false;
		}
		
		return result;
	}

	private Result<RedBlackNode> rlBalance(Result<RedBlackNode> result, RedBlackNode nodeC) {
		RedBlackNode nodeA = nodeC.getLeft();
		RedBlackNode nodeB = nodeA.getRight();
		
		if (nodeC.getParent() == null) {
			root = nodeB;
			nodeB.setParent(null);
		} else {
			if (nodeC.getGraphicNode().getSide() == Side.LEFT) {
				nodeC.getParent().setLeft(nodeB);
			} else {
				nodeC.getParent().setRight(nodeB);
			}			
		}
		
		nodeC.setLeft(nodeB.getRight());
		nodeA.setRight(nodeB.getLeft());
		nodeB.setRight(nodeC);
		nodeB.setLeft(nodeA);
		
		nodeB.setColor(Color.BLACK);
		nodeC.setColor(Color.RED);
		
		result.addAnimation(AnimatedAction.RL, nodeC.getGraphicNode(), null);	
		result.addAnimation(AnimatedAction.RECOLOR, nodeB.getGraphicNode(), Color.BLACK);
		result.addAnimation(AnimatedAction.RECOLOR, nodeC.getGraphicNode(), Color.RED);
		
		return result;
	}
	
	/**
	 * Řeší dvakrát obarvený černý list
	 * 
	 * @param result
	 * @param parent
	 * @param side
	 * @return
	 */
	private Result<RedBlackNode> doubleBlack(Result<RedBlackNode> result, RedBlackNode parent, Side side) {
		RedBlackNode helpNode;
		if (!dblack) { //pokud to volám znovu nebudu dělat výpis
			result.addAnimation(AnimatedAction.DOUBLEBLACK, parent.getGraphicNode(), side);
		} else {
			dblack = false;
		}
		
		if (side == Side.LEFT) {
			if (parent.getRight().getColor() == Color.BLACK) {
				helpNode = parent.getRight();
				if (helpNode.getRight() != null && helpNode.getRight().getColor() == Color.RED) {
					dblackColor = true;
					dblack = true;
					result.addAnimation(AnimatedAction.REDBLACKINFO, null, 1);
					return llBalance(result, parent);
				} else if (helpNode.getLeft() != null && helpNode.getLeft().getColor() == Color.RED) {
					dblackColor = true;
					dblack = true;
					result.addAnimation(AnimatedAction.REDBLACKINFO, null, 1);
					return lrBalance(result, parent);
				} else {
					if (parent.getColor() == Color.RED) {
						parent.setColor(Color.BLACK);
						helpNode.setColor(Color.RED);
						
						result.addAnimation(AnimatedAction.REDBLACKINFO, null, 2);
						result.addAnimation(AnimatedAction.RECOLOR, parent.getGraphicNode(), Color.BLACK);
						result.addAnimation(AnimatedAction.RECOLOR, helpNode.getGraphicNode(), Color.RED);
						result.addAnimation(AnimatedAction.SETDOUBLEBLACK, null, null);
					} else {
						helpNode.setColor(Color.RED);
						
						result.addAnimation(AnimatedAction.REDBLACKINFO, null, 3);
						result.addAnimation(AnimatedAction.SETDOUBLEBLACK, parent.getGraphicNode(), null);
						result.addAnimation(AnimatedAction.RECOLOR, helpNode.getGraphicNode(), Color.RED);
						
						if (parent.equals(root)) { //přebarvím kořen
							result.addAnimation(AnimatedAction.SETDOUBLEBLACK, parent.getGraphicNode(), true);
						} else {
							dblack = true;							
							return doubleBlack(result, parent.getParent(), parent.getGraphicNode().getSide());
						}
					}
				}				
			} else {
				dblack = true;
				result.addAnimation(AnimatedAction.REDBLACKINFO, null, 4);
				return llBalance(result, parent);				
			}			
		} else {
			if (parent.getLeft().getColor() == Color.BLACK) {
				helpNode = parent.getLeft();
				if (helpNode.getRight() != null && helpNode.getRight().getColor() == Color.RED) {
					dblackColor = true;
					dblack = true;
					result.addAnimation(AnimatedAction.REDBLACKINFO, null, 1);
					return rrBalance(result, parent);
				} else if (helpNode.getLeft() != null && helpNode.getLeft().getColor() == Color.RED) {
					dblackColor = true;
					dblack = true;
					result.addAnimation(AnimatedAction.REDBLACKINFO, null, 1);
					return rlBalance(result, parent);
				} else {
					if (parent.getColor() == Color.RED) {
						parent.setColor(Color.BLACK);
						helpNode.setColor(Color.RED);
						
						result.addAnimation(AnimatedAction.REDBLACKINFO, null, 2);
						result.addAnimation(AnimatedAction.RECOLOR, parent.getGraphicNode(), Color.BLACK);
						result.addAnimation(AnimatedAction.RECOLOR, helpNode.getGraphicNode(), Color.RED);
						result.addAnimation(AnimatedAction.SETDOUBLEBLACK, null, null);
					} else {
						helpNode.setColor(Color.RED);
						
						result.addAnimation(AnimatedAction.REDBLACKINFO, null, 3);
						result.addAnimation(AnimatedAction.SETDOUBLEBLACK, parent.getGraphicNode(), null);
						result.addAnimation(AnimatedAction.RECOLOR, helpNode.getGraphicNode(), Color.RED);
						
						if (parent.equals(root)) { //přebarvím kořen
							result.addAnimation(AnimatedAction.SETDOUBLEBLACK, parent.getGraphicNode(), true);
						} else {
							dblack = true;
							return doubleBlack(result, parent.getParent(), parent.getGraphicNode().getSide());
						}
					}
				}				
			} else {
				dblack = true;
				result.addAnimation(AnimatedAction.REDBLACKINFO, null, 4);
				return rrBalance(result, parent);				
			}
		}		
		
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
	public RedBlackNode getRoot() {
		return root;
	}    	
}