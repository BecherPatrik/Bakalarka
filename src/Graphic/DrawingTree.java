package Graphic;

import java.util.ArrayList;
import java.util.List;

import Aplication.WindowController;
import Trees.AnimatedAction;
import Trees.INode;
import Trees.ITree;
import Trees.RecordOfAnimation;
import Trees.Result;
import Trees.Side;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.StrokeTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class DrawingTree {
	private WindowController windowController;
	private List<IGraphicNode> nodes = new ArrayList<>();	

	private ReadOnlyDoubleProperty paneWight;
	private Pane paneTree;
	private DoubleProperty speed = new SimpleDoubleProperty();
	
	private final static double ROOTBORDER = 20;	
	private final static double DOWNMARGIN = 40;
	
	private final int SLOWANIMATION = 250;
	private final int FASTANIMATION = 105;
	
	private int maxLevel;
	private double moreSpace = 0;
	
	private DoubleProperty rootY = new SimpleDoubleProperty();;
	private DoubleProperty rootX = new SimpleDoubleProperty();	
	
	private IGraphicNode newIGraphicNode;	
	
	//animation variable
	private ArrayList<IGraphicNode> wayList;
	private ArrayList<RecordOfAnimation> recordOfAnimations;
	
	private int index = 0;
	private int indexAnimation = 0;
	private boolean redraw = false;
	
	private DoubleProperty xAnimatedNode = new SimpleDoubleProperty();
	private DoubleProperty yAnimatedNode = new SimpleDoubleProperty();

	public DrawingTree(ITree<?> tree, Pane paneTree, DoubleProperty speed, ReadOnlyDoubleProperty stageWidthProperty, WindowController windowController) {
		this.paneWight = stageWidthProperty;
		this.paneTree = paneTree;
		//this.tree = tree;		
		this.speed = speed;
		this.windowController = windowController;
	//	Class<T> animationClass;
	//	this.treeAnimation = animationClass.getConstructor(ITree.class, Canvas.class).newInstance(this.canvas, this.tree);
	}	

	public List<IGraphicNode> getNodes() {
		return nodes;
	}
	
	public void setNodes(List<IGraphicNode> oldGraphicTreeNodes) {
		nodes.clear();
		nodes.addAll(oldGraphicTreeNodes);		
	}
	
	/**
	 * VloûenÌ ko¯enu
	 * @param root
	 */
	public void insertRoot(INode<?> rootNode){
		IGraphicNode root = rootNode.getGraphicNode();
		root.setLevel(0);
		
		rootY.bind(new SimpleDoubleProperty(ROOTBORDER));		
		rootX.bind(paneWight.subtract(31).divide(2.0).add(root.getSize() / 2));			
		
		
		DoubleProperty startNodeX = new SimpleDoubleProperty();	
		DoubleProperty startNodeY = new SimpleDoubleProperty();	
		
		startNodeX.bind(paneWight.subtract(80));	
		startNodeY.bind(new SimpleDoubleProperty(ROOTBORDER));	
		
		root.setX(startNodeX); // vloûÌm poË·teËnÌ sou¯adnice
		root.setY(startNodeY);		
		
		paneTree.getChildren().add(root.getNode());
		
		nodes.add(root);
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(10 * (FASTANIMATION - speed.get())),
				new KeyValue(root.getX(), rootX.get()),
				new KeyValue(root.getY(), rootY.get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {				
				root.setY(rootY);
				root.setX(rootX);
				windowController.enableButtons();				
			}
		});

		root.getX().unbind();
		root.getY().unbind();

		timeline.play();
	}
	
	/**
	 * VykreslenÌ novÈho listu 
	 * @param result
	 */
	public void insertNode(Result<?> result) {
		wayList = result.getWay();	
		
		if ((boolean) result.getRecordOfAnimations().get(0).getObject()) {
			startAnimation(result.getRecordOfAnimations());
			return;
		}
		
		xAnimatedNode = new SimpleDoubleProperty(); //sou¯adnice x listu	
		yAnimatedNode = new SimpleDoubleProperty();
		double computedX;	
		DoubleProperty startNodeX = new SimpleDoubleProperty();	
		DoubleProperty startNodeY = new SimpleDoubleProperty();	
		
		startNodeX.bind(paneWight.subtract(80));	
		startNodeY.bind(new SimpleDoubleProperty(ROOTBORDER));	
		
		newIGraphicNode = result.getNode().getGraphicNode(); //vkl·dan˝ list	
		
		newIGraphicNode.setParent(((INode<?>) result.getNode().getParent()).getGraphicNode());
		newIGraphicNode.setLevel(result.getWay().size());
		newIGraphicNode.setSide(result.getSide());
		
		paneTree.getChildren().add(newIGraphicNode.getNode()); //p¯id·m list 
		
		newIGraphicNode.setX(startNodeX); // vloûÌm poË·teËnÌ sou¯adnice
		newIGraphicNode.setY(startNodeY);		
		
		computedX = computeX(newIGraphicNode); //vypoËÌt·m posunutÌ od rodiËe
		
		if (result.getSide() == Side.LEFT) {
			xAnimatedNode.bind(newIGraphicNode.getParent().getX().subtract(computedX));	
		} else {
			xAnimatedNode.bind(newIGraphicNode.getParent().getX().add(computedX));	
		}
		yAnimatedNode.bind(newIGraphicNode.getParent().getY().add(DOWNMARGIN));
		
		//zavol·m animaci
		startAnimation(result.getRecordOfAnimations());	
		
		createBranch(computedX); //vloûenÌ vÏtve
		
		nodes.add(newIGraphicNode);	
		
		computeMoreSpace();		
	}
	
	/**
	 * VypoËÌt· posunutÌ od rodiËe
	 * @param node
	 * @return
	 */
	private double computeX(IGraphicNode node) {
		return (newIGraphicNode.getParent().getSize() / 2) * (5 - (node.getLevel() * 1.1)) + moreSpace;
	}
	
	/**
	 * VloûÌ vÏtev do pane
	 */
	private void insertBranch() {
		paneTree.getChildren().add(newIGraphicNode.getBranch());
		newIGraphicNode.getParent().getNode().toFront();
		newIGraphicNode.getNode().toFront();
	}
	
	/**
	 * Smaz·nÌ listu + zavol· p¯ekreslenÌ
	 * @param node
	 */
	public void deleteNode(Result<?> result) {
		if (result.getSide() != Side.NONE) {
			//TODO nenalezen
		} else {
			wayList = result.getWay();
			if (result.getRecordOfAnimations() != null) {
				recordOfAnimations = result.getRecordOfAnimations();
			}
			
			startAnimation(result.getRecordOfAnimations());
		}
	}
	
	/**
	 * NajÌtÌ listu 
	 * @param node
	 */
	public void searchNode(Result<?> result) {
		if (!(boolean)result.getRecordOfAnimations().get(0).getObject()) {
			//TODO nenalezen
		} else {
			wayList = result.getWay();			
			startAnimation(result.getRecordOfAnimations());			
		}
	}

	public void balanceTree() {
		
	}
	
	/**
	 * VloûenÌ vÏtve
	 * @param parent
	 * @param node
	 * @param side
	 * @param x - posunutÌ oproti rodiËovi
	 */
	private void createBranch(double x) {
		Pane branch = new Pane();
		int space = newIGraphicNode.getParent().getSize() / 2;
		Line line;
		//Line line2, line3;
		
		if (newIGraphicNode.getSide() == Side.LEFT) {
			line = new Line(0, 0, -x, DOWNMARGIN - space);	
		} else {
			line = new Line(0, 0, x, DOWNMARGIN - space);	
		}
		
		/*line2 = new Line(0, -100, 0,200);
		line3 = new Line(-100, 0, 200, 0);*/
		
		branch.translateXProperty().bind(newIGraphicNode.getParent().getX().add(space));
		branch.translateYProperty().bind(newIGraphicNode.getParent().getY().add(space));		
		
		branch.getChildren().add(line);
		//branch.getChildren().addAll(line,line2,line3);
		newIGraphicNode.setBranch(branch);		
	}  
	
	/**
	 * 
	 * @param node - list kam se m· vÏtev p¯esun˘t
	 */
	private void relocateNodeAndBranch(IGraphicNode node) {
		double computedX = computeX(node);
		int space = node.getParent().getSize() / 2;
		DoubleProperty newX = new SimpleDoubleProperty();
	
		if (node.getSide() == Side.LEFT) {
			 node.setBranchEndX(-computedX);	
			 newX.bind(node.getParent().getX().subtract(computedX));				 
		} else {
			newX.bind(node.getParent().getX().add(computedX));
			node.setBranchEndX(computedX);	
		}
		node.setX(newX);
		
		node.getBranch().translateXProperty().bind(node.getParent().getX().add(space));
		//branch.translateYProperty().bind(new SimpleDoubleProperty(newIGraphicNode.getParent().getY() + space));			
	}	
	
	/**
	 * VypoËte hodnoty moreSpace a maxLevel
	 */
	private void computeMoreSpace(){
		int max = maxLevel();
		//System.out.println(max);
		if (max != maxLevel) {						
			if(max < 3) {
				moreSpace = 0;
			} else {
				moreSpace = newIGraphicNode.getParent().getSize() * (max / 1.8);
			}
			maxLevel = max;
			redraw = true;
		}		
	}
	
	private int maxLevel() {
		int max = 0;
		for (IGraphicNode iGraphicNode : nodes) {
			if (iGraphicNode.getLevel() > max)
				max = iGraphicNode.getLevel();
		}		
		return max;			
	}
	
	/**
	 * P¯ekreslÌ cel˝ strom 
	 * TODO: listy nejs˘ nabindovanÈ + neposunuj˘ se + prvnÌ vÏtve upravit
	 */
	private void redraw() {	
		redraw = false;		
		for (IGraphicNode iGraphicNode : nodes.subList(1, nodes.size())) {
			relocateNodeAndBranch(iGraphicNode);
		}
		/*for (IGraphicNode iGraphicNode : nodes.subList(1, nodes.size())) {
			level = iGraphicNode.getLevel();
			if(iGraphicNode.getSide() == Side.LEFT) {				
				iGraphicNode.setX(new SimpleDoubleProperty(iGraphicNode.getX().get() - moreSpace * level));
				iGraphicNode.setBranchEndX(iGraphicNode.getBranchEndX() - moreSpace * level);
				iGraphicNode.setBranchStartX(iGraphicNode.getBranchStartX() - moreSpace * level);
			} else {
				iGraphicNode.setX(new SimpleDoubleProperty(iGraphicNode.getX().get() + moreSpace * level));
				iGraphicNode.setBranchEndX(iGraphicNode.getBranchEndX() + moreSpace * level);
				iGraphicNode.setBranchStartX(iGraphicNode.getBranchStartX() + moreSpace * level);
			}
		}*/
	}
	
	/**
	 * SnÌûÌ vöem potomk˘m level
	 * @param iNode
	 */
	private void decreaseLevel(INode<?> iNode) {
		IGraphicNode iGraphicNode;
		if (iNode.getLeft() != null) {
			iGraphicNode = ((INode<?>)iNode.getLeft()).getGraphicNode();
			iGraphicNode.setLevel(iGraphicNode.getLevel() - 1);
			decreaseLevel((INode<?>)iNode.getLeft());
		}
		
		if (iNode.getRight() != null) {
			iGraphicNode = ((INode<?>)iNode.getRight()).getGraphicNode();
			iGraphicNode.setLevel(iGraphicNode.getLevel() - 1);
			decreaseLevel((INode<?>)iNode.getRight());
		}
	}
	
	/***************************************************************************************
	 * Animace!
	 * @param recordOfAnimations 
	 ***************************************************************************************/	
	private void startAnimation(ArrayList<RecordOfAnimation> recordOfAnimations) {
		indexAnimation = 0;
		this.recordOfAnimations = recordOfAnimations;
		nextAnimation();
	}
	
	/**
	 * SpuötÌ dalöÌ animace po skonËenÌ p¯edchozÌch, aby na sebe navazovaly
	 */
	private void nextAnimation() {
		if (indexAnimation >= recordOfAnimations.size()) {
			if (redraw) {				
				redraw();
			}
			windowController.enableButtons();
			return;
		}
		
		switch (recordOfAnimations.get(indexAnimation).getAction()) {
		case SEARCH:
			index = 0;
			nextSearchNode();
			break;
		case INSERT:
			insertNodeAnimation();
			break;
		case DELETE:
			deleteNodeAnimation();			
			break;
		case MOVENODE:
			moveAnimation();			
			break;		
		case MOVEVALUE:
			moveValueAnimation();
			break;
		case SWAP:
			swapAnimation();
			break;
		}	
	}	

	/**
	 * Zavol· znovu metodu highlightNode pro kaûd˝ list zvl·öù
	 */
	private void nextSearchNode() {
		highlightNode(wayList.get(index));
	}		
	
	/**
	 * Animace vloûenÌ novÈho listu
	 */
	private void insertNodeAnimation() {		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(10 * (FASTANIMATION - speed.get())),
				new KeyValue(newIGraphicNode.getX(), xAnimatedNode.get()),
				new KeyValue(newIGraphicNode.getY(), yAnimatedNode.get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				insertBranch();
				newIGraphicNode.setX(xAnimatedNode);
				newIGraphicNode.setY(yAnimatedNode);
				indexAnimation++;
				nextAnimation();
			}
		});

		newIGraphicNode.getX().unbind();
		newIGraphicNode.getY().unbind();

		timeline.play();
	}

	private void deleteNodeAnimation() {
		IGraphicNode node = wayList.get(wayList.size() - 1);
		node.highlightFindNode(); // zv˝raznÌm mazan˝ node
		if ((boolean) recordOfAnimations.get(indexAnimation).getObject()) { //pokud m· dÏti
			node.setValue("");
			indexAnimation++;
			nextAnimation();
		} else {			
			FadeTransition fadeTransitionNode = new FadeTransition(Duration.millis(10 * (FASTANIMATION - speed.get())), node.getNode());
			fadeTransitionNode.setFromValue(1.0);
			fadeTransitionNode.setToValue(0.0);			
			
			FadeTransition fadeTransitionBranch = new FadeTransition(Duration.millis(10 * (FASTANIMATION - speed.get())), node.getBranch());
			fadeTransitionBranch.setFromValue(1.0);
			fadeTransitionBranch.setToValue(0.0);

			fadeTransitionBranch.play();
			fadeTransitionNode.play();
			
			fadeTransitionNode.setOnFinished(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					nodes.remove(node);
					paneTree.getChildren().remove(node.getNode());
					paneTree.getChildren().remove(node.getBranch());
					
					//musÌm zviditelnit kv˘li viditelnosti pro opakov·nÌ poslednÌho kroku
					FadeTransition fadeTransitionNode = new FadeTransition(Duration.millis(1), node.getNode());
					fadeTransitionNode.setFromValue(0.0);
					fadeTransitionNode.setToValue(1.0);			
					
					FadeTransition fadeTransitionBranch = new FadeTransition(Duration.millis(1), node.getBranch());
					fadeTransitionBranch.setFromValue(0.0);
					fadeTransitionBranch.setToValue(1.0);

					fadeTransitionBranch.play();
					fadeTransitionNode.play();
					node.setDefaultColorNode();
					
					indexAnimation++;
					computeMoreSpace();
					nextAnimation();					
				}
			});
		}
	}

	private void moveAnimation() {
		INode<?> iNodeRemoved = recordOfAnimations.get(indexAnimation).getNode1();
		INode<?> iNodeMoved = (INode<?>) recordOfAnimations.get(indexAnimation).getObject();
		
		iNodeMoved.getGraphicNode().highlightNode();
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(10 * (FASTANIMATION - speed.get())),
				new KeyValue(iNodeMoved.getGraphicNode().getX(), iNodeRemoved.getGraphicNode().getX().get()),
				new KeyValue(iNodeMoved.getGraphicNode().getY(), iNodeRemoved.getGraphicNode().getY().get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (iNodeMoved.getLeft() == null && iNodeMoved.getRight() == null) {
					iNodeRemoved.getGraphicNode().setValue(iNodeMoved.getGraphicNode().getValue());
					iNodeRemoved.getGraphicNode().setDefaultColorNode();

					paneTree.getChildren().remove(iNodeMoved.getGraphicNode().getNode());					
				} else {	
					if (iNodeRemoved.getParent() == null) {
						iNodeMoved.getGraphicNode().setX(rootX);
						nodes.remove(iNodeMoved.getGraphicNode()); //d·m roota na prvnÌ mÌsto
						nodes.add(0, iNodeMoved.getGraphicNode());
					}
					
					decreaseLevel(iNodeMoved); //snÌûÌm vöem potomk˘m level
					
					iNodeMoved.getGraphicNode().setLevel(iNodeRemoved.getGraphicNode().getLevel());
					iNodeMoved.getGraphicNode().setParent(iNodeRemoved.getGraphicNode().getParent());
					iNodeMoved.getGraphicNode().setDefaultColorNode();
					iNodeMoved.getGraphicNode().getNode().toFront();					

					paneTree.getChildren().remove(iNodeRemoved.getGraphicNode().getNode());	
					nodes.remove(iNodeRemoved.getGraphicNode());
					
					iNodeRemoved.setGraphicNode(iNodeMoved.getGraphicNode()); //zmÏnÌm INode1 jeho grafick˝ node... 
				}
				
				computeMoreSpace();
				indexAnimation++;
				nextAnimation();
			}
		});

		iNodeMoved.getGraphicNode().getX().unbind();
		iNodeMoved.getGraphicNode().getY().unbind();		
		paneTree.getChildren().remove(iNodeMoved.getGraphicNode().getBranch());

		timeline.play();		
		
	}
	
	private void moveValueAnimation() {
		IGraphicNode node1 = recordOfAnimations.get(indexAnimation).getNode1().getGraphicNode();
		IGraphicNode node2 = ((INode<?>) recordOfAnimations.get(indexAnimation).getObject()).getGraphicNode();
		
		node1.setValue(node2.getValue());
		node2.setValue("");
		node1.setDefaultColorNode();
		node2.setDefaultColorNode();

		indexAnimation++;
		nextAnimation();			
	}
	
	private void swapAnimation(){
		//TODO
	}

	/**
	 * Animace zv˝raznÏnÌ vÏtve a n·slednÏ listu 
	 * @param node
	 */
	private void highlightNode(IGraphicNode node) {
		StrokeTransition st1 = null;
		PauseTransition pt1 = null;
		StrokeTransition st2 = null;		
		SequentialTransition seqT;
		
		if(node.getBranch() != null) {
			st1 = new StrokeTransition(Duration.millis(SLOWANIMATION),(Line) node.getBranch().getChildren().get(0), Color.BLACK, Color.LIME);
			pt1 = new PauseTransition(Duration.millis(5 * (FASTANIMATION - speed.get())));
			st2 = new StrokeTransition(Duration.millis(SLOWANIMATION), (Line) node.getBranch().getChildren().get(0), Color.LIME, Color.BLACK);
		}
		
		StrokeTransition st3 = new StrokeTransition(Duration.millis(SLOWANIMATION), node.getShape(), Color.WHITE, Color.LIME);
		PauseTransition pt2 = new PauseTransition(Duration.millis(10 * (FASTANIMATION - speed.get())));
		StrokeTransition st4 = new StrokeTransition(Duration.millis(SLOWANIMATION), node.getShape(), Color.LIME, Color.WHITE);
		
		if(node.getBranch() != null) {
			seqT = new SequentialTransition(st1, pt1, st2, st3, pt2, st4);
		} else {
			seqT = new SequentialTransition(st3, pt2, st4);
		}
		
		seqT.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (++index < wayList.size()) {
					nextSearchNode();
				} else {
					if((boolean)recordOfAnimations.get(indexAnimation).getObject()) {
						highlightFindNode();
					} else {
						indexAnimation++;
						nextAnimation();
					}
				}
			}
		});
		
		seqT.play();
	}
	
	/**
	 * Zv˝raznÌ nalezen˝ list
	 */
	private void highlightFindNode() {
		StrokeTransition st3 = new StrokeTransition(Duration.millis(SLOWANIMATION), wayList.get(wayList.size() - 1).getShape(), Color.WHITE, Color.YELLOW);
		PauseTransition pt2 = new PauseTransition(Duration.millis(10 * (SLOWANIMATION - 50 - speed.get())));
		StrokeTransition st4 = new StrokeTransition(Duration.millis(SLOWANIMATION), wayList.get(wayList.size() - 1).getShape(), Color.YELLOW, Color.WHITE);
		
		SequentialTransition seqT = new SequentialTransition(st3, pt2, st4);		
		
		seqT.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				indexAnimation++;
				nextAnimation();
			}
		});
		
		seqT.play();		
	}
	/*private int iterMax(INode<?> node) {
		if (node.getLeft() != null) {
			if (node.getRight() != null) {
				return Math.max(iterMax((INode<?>) node.getLeft()), iterMax((INode<?>) node.getRight()));
			} else {
				return iterMax((INode<?>) node.getLeft());
			}
		} else if (node.getRight() != null) {
			return iterMax((INode<?>) node.getRight());
		} else {
			return node.ge
		}
	
		
	}*/

	
	
	/*private class Level {
		private int maxLevel;
		private int countNode;
		
		public Level(int maxLevel, int countNode) {
			this.maxLevel = maxLevel;
			this.countNode = countNode;
		}
		
		public void deleteMaxNode {
			countNode--;
			if (countNode == 0) {
				
			}
		}
//		
	}*/
	/*
	 * private-method private void iterDraw(double left, double top, U node, int
	 * numberIter) {
	 * 
	 * Grid grind = node.GraphicNode.Grind;
	 * 
	 * Canvas.SetLeft(grind, left); Canvas.SetTop(grind, top);
	 * _canvas.Children.Add(grind); redraw(); Thread.Sleep(500);
	 * 
	 * if (node.Left != null) iterDraw(left - (150 - (numberIter * 27)), top +
	 * 80, node.Left, numberIter + 1); if (node.Right != null) iterDraw(left +
	 * (150 - (numberIter * 27)), top + 80, node.Right, numberIter + 1); }
	 * #endregion
	 * 
	 * #region public-method public void draw() { iterDraw(_canvasWight / 2, 20,
	 * _tree.Root, 1); /// => tady je ta chyba...... !!! :D }
	 * 
	 * public void draw(INode<T> node, Side side) { Grid grind =
	 * node.GraphicNode.Grind;
	 * 
	 * if (side == Side.Null) { //jedn· se o root node.GraphicNode.Level = 0;
	 * Canvas.SetLeft(grind, _canvasWight / 2); Canvas.SetTop(grind, 10);
	 * _canvas.Children.Add(grind); Thread.Sleep(500); } else if (side ==
	 * Side.Left) { //TODO ....
	 * 
	 * } }
	 * 
	 * public static void redraw() { _canvas.Refresh(); } #endregion
	 */
}