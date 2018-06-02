package Graphic;

import java.util.ArrayList;
import java.util.List;

import Aplication.WindowController;
import Trees.BinaryNode;
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
import javafx.util.Duration;

public class DrawingTree {
	private WindowController windowController;
	private List<IGraphicNode> listGraphicNodes = new ArrayList<>(); //root je na místě 0

	private ReadOnlyDoubleProperty paneTreeWeight;
	private Pane paneTree;
	private DoubleProperty animationSpeed = new SimpleDoubleProperty();
	
	private final static double ROOTBORDER = 20;	
	private final static double DOWNMARGIN = 40;	
	
	private double rootSize = 0;
	
	private DoubleProperty rootY = new SimpleDoubleProperty();;
	private DoubleProperty rootX = new SimpleDoubleProperty();	
	
	private IGraphicNode newIGraphicNode;	
	
	//animation variable
	private ArrayList<IGraphicNode> wayList;
	private ArrayList<RecordOfAnimation> recordOfAnimations;
	
	private final int SLOWANIMATION = 250;
	private final int FASTANIMATION = 105;
	
	private int wayIndex = 0;
	private int indexAnimation = 0;
	private boolean isRedraw = false;
	
	private int balanceRedraw = 0;
	
	private DoubleProperty xAnimatedNode = new SimpleDoubleProperty();
	private DoubleProperty yAnimatedNode = new SimpleDoubleProperty();
	
	private DoubleProperty xAnimatedBranch = new SimpleDoubleProperty();
	private DoubleProperty yAnimatedBranch = new SimpleDoubleProperty();
	
	private List<Timeline> listBalanceAnimation = new ArrayList<>();	

	public DrawingTree(ITree<?> tree, Pane paneTree, DoubleProperty speed, ReadOnlyDoubleProperty stageWidthProperty, WindowController windowController) {
		this.paneTreeWeight = stageWidthProperty;
		this.paneTree = paneTree;
		this.animationSpeed = speed;
		this.windowController = windowController;
	}
	
	/**
	 * Vložení kořenu
	 * @param root
	 */
	public void insertRoot(INode<?> rootNode){
		IGraphicNode root = rootNode.getGraphicNode();
		rootSize = root.getRadiusSize();
		root.setLevel(0);
		
		rootY.bind(new SimpleDoubleProperty(ROOTBORDER));	
		rootX.bind(paneTreeWeight.subtract(31).divide(2.0));	
		
		DoubleProperty startNodeX = new SimpleDoubleProperty();	
		DoubleProperty startNodeY = new SimpleDoubleProperty();	
		
		startNodeX.bind(paneTreeWeight.subtract(paneTreeWeight.get()).add(80));	
		startNodeY.bind(new SimpleDoubleProperty(ROOTBORDER));			
		
		paneTree.getChildren().add(root.getStackPaneNode());
		
		listGraphicNodes.add(root);
		
		if (animationSpeed.get() == 0) { //žádná animace
			root.setY(rootY);
			root.setX(rootX);			
			windowController.enableButtons();
			return;
		}
		
		root.setX(startNodeX); // vložím počáteční souřadnice
		root.setY(startNodeY);	
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(10 * (FASTANIMATION - animationSpeed.get())),
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
	 * Vykreslení nového listu 
	 * @param result
	 */
	public void insertNode(Result<?> result) {
		wayList = result.getWay();	
		
		if ((boolean) result.getRecordOfAnimations().get(0).getObject()) {
			startAnimation(result.getRecordOfAnimations());
			return;
		}
		
		xAnimatedNode = new SimpleDoubleProperty(); //souřadnice x listu	
		yAnimatedNode = new SimpleDoubleProperty();
		DoubleProperty startNodeX = new SimpleDoubleProperty();	
		DoubleProperty startNodeY = new SimpleDoubleProperty();	
		
		startNodeX.bind(paneTreeWeight.subtract(paneTreeWeight.get()).add(80));	
		startNodeY.bind(new SimpleDoubleProperty(ROOTBORDER));	
		
		newIGraphicNode = result.getNode().getGraphicNode(); //vkládaný list	
		//newIGraphicNode.setParent(((INode<?>)result.getNode().getParent()).getGraphicNode());
		
		newIGraphicNode.setLevel(result.getWay().size());
		
		paneTree.getChildren().add(newIGraphicNode.getStackPaneNode()); //přidám list 		
		
		newIGraphicNode.setX(startNodeX); // vložím počáteční souřadnice
		newIGraphicNode.setY(startNodeY);		
		
		if (result.getSide() == Side.LEFT) {
			xAnimatedNode.bind(newIGraphicNode.getParent().getX().subtract(rootSize));	
		} else {
			xAnimatedNode.bind(newIGraphicNode.getParent().getX().add(rootSize));	
		}
		
		yAnimatedNode.bind(newIGraphicNode.getParent().getY().add(DOWNMARGIN));
		
		//zavolám animaci
		startAnimation(result.getRecordOfAnimations());			
		
	/*	if(animationSpeed.get() != 0) {
			createBranch(newIGraphicNode);			
		}*/
		
		//listGraphicNodes.add(newIGraphicNode);	
	}
	
	/**
	 * Smazání listu + zavolá překreslení
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
	 * Najítí listu 
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
	
	/**
	 * Vložení větve
	 * @param node
	 */	
	private void createBranch(IGraphicNode node) {
		if (node.getBranch() != null) {
			return;
		}
		
		Line line = new Line();
		line.startXProperty().bind(node.getParent().getX().add(rootSize / 2));
		line.startYProperty().bind(node.getParent().getY().add(rootSize / 2));
		line.setStroke(Color.WHITE);
		
		if (node.getSide() == Side.LEFT) {
			line.endXProperty().bind(node.getParent().getX().subtract(rootSize / 2));	
		} else {
			line.endXProperty().bind(node.getParent().getX().add(rootSize * 1.5));	
		}
		
		line.endYProperty().bind(node.getY().add(rootSize / 2));		
		
		node.setBranch(line);
	}
		
	/**
	 * Vloží větev do paneTree
	 */
	private void insertBranch() {
		paneTree.getChildren().add(newIGraphicNode.getBranch());
		newIGraphicNode.getParent().getStackPaneNode().toFront();
		newIGraphicNode.getStackPaneNode().toFront();
	}
	
	/**
	 * Upraví vzdálenosti listů 
	 */
	private void balanceTree() {		
		listBalanceAnimation = new ArrayList<>();
		
		//listGraphicNodes.forEach(x -> x.createBackUpBranch());

		if (listGraphicNodes.isEmpty()) {
			windowController.enableButtons();
			return;
		} else {
			listGraphicNodes.get(0).countChildren(); // nechám rekurzivně vypočítat děti
			balanceRedraw = 1;
			balanceTreeNext();
		}
	}
	
	/**
	 * Postupné animace balancování stromu
	 */
	private void balanceTreeNext() {
		if (listGraphicNodes.size() == balanceRedraw) {
			balanceRedraw = 0;
			checkBranches();
			redraw();
			return;
		}
		
		Timeline timeline;
		KeyFrame kf;
		Duration duration;
	
		xAnimatedNode = new SimpleDoubleProperty();
		yAnimatedNode = new SimpleDoubleProperty();
		
		xAnimatedBranch = new SimpleDoubleProperty();
		yAnimatedBranch = new SimpleDoubleProperty();
		
		IGraphicNode iGraphicNode = listGraphicNodes.get(balanceRedraw);
		
		//System.out.println("balance pro: "+ iGraphicNode.getValue());

		if (iGraphicNode.getSide() == Side.LEFT) {
			xAnimatedNode.bind(iGraphicNode.getParent().getX().subtract(rootSize).subtract(rootSize * iGraphicNode.getRightChildrenCount()));
		} else {
			xAnimatedNode.bind(iGraphicNode.getParent().getX().add(rootSize).add(rootSize * iGraphicNode.getLeftChildrenCount()));
		}
		
		yAnimatedNode.bind(iGraphicNode.getParent().getY().add(DOWNMARGIN));
		
		if (iGraphicNode.getX().get() != xAnimatedNode.get()) {		
			if (iGraphicNode.getSide() == Side.LEFT) { // vypočítám nový x pro větev
				xAnimatedBranch.bind(iGraphicNode.getParent().getX().subtract(rootSize / 2).subtract(rootSize * iGraphicNode.getRightChildrenCount()));
			} else {
				xAnimatedBranch.bind(iGraphicNode.getParent().getX().add(rootSize * 1.5).add(rootSize * iGraphicNode.getLeftChildrenCount()));
			}
			

			if (animationSpeed.get() == 0) {
				duration = Duration.millis(0.5);
			} else {
				duration = Duration.millis(10 * (FASTANIMATION - animationSpeed.get()));
			}
			timeline = new Timeline();

			kf = new KeyFrame(duration,
					new KeyValue(iGraphicNode.getX(), xAnimatedNode.get()),
					new KeyValue(iGraphicNode.getY(), yAnimatedNode.get())
					//,	new KeyValue(iGraphicNode.getBranchEndX(), xAnimatedBranch.get())
					);

			timeline.getKeyFrames().add(kf);

			timeline.setOnFinished(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					xAnimatedNode = new SimpleDoubleProperty();
					xAnimatedBranch = new SimpleDoubleProperty();
					if (iGraphicNode.getSide() == Side.LEFT) {
						xAnimatedNode.bind(iGraphicNode.getParent().getX().subtract(rootSize).subtract(rootSize * iGraphicNode.getRightChildrenCount()));
						xAnimatedBranch.bind(iGraphicNode.getParent().getX().subtract(rootSize / 2).subtract(rootSize * iGraphicNode.getRightChildrenCount()));
					} else {
						xAnimatedNode.bind(iGraphicNode.getParent().getX().add(rootSize).add(rootSize * iGraphicNode.getLeftChildrenCount()));
						xAnimatedBranch.bind(iGraphicNode.getParent().getX().add(rootSize * 1.5).add(rootSize * iGraphicNode.getLeftChildrenCount()));
					}					
					
					yAnimatedNode.bind(iGraphicNode.getParent().getY().add(DOWNMARGIN));
					yAnimatedBranch.bind(iGraphicNode.getParent().getY().add(rootSize / 2).add(DOWNMARGIN));

					iGraphicNode.setX(xAnimatedNode);
					iGraphicNode.setY(yAnimatedNode);
					
					iGraphicNode.setBranchEndX(xAnimatedBranch);
					iGraphicNode.setBranchEndY(yAnimatedBranch);

					if (iGraphicNode.getLeft() != null) {
						iGraphicNode.getLeft().getBranchStartX().bind(xAnimatedBranch);
						iGraphicNode.getLeft().getBranchStartY().bind(yAnimatedBranch);
					}

					if (iGraphicNode.getRight() != null) {
						iGraphicNode.getRight().getBranchStartX().bind(xAnimatedBranch);
						iGraphicNode.getRight().getBranchStartY().bind(yAnimatedBranch);
					}				

					balanceRedraw++;
					balanceTreeNext();
				}
			});

			iGraphicNode.getX().unbind();
			iGraphicNode.getY().unbind();
			
			iGraphicNode.getBranchEndX().unbind();
			iGraphicNode.getBranchEndY().unbind();
			paneTree.getChildren().remove(iGraphicNode.getBranch());
			timeline.play();
		} else {
			balanceRedraw++;
			balanceTreeNext();
		}
	}
	
	/**
	 * Zjistí jestli se ukončily všechny animace balance a zavolá překleslení
	 */
	private void balanceRedraw() {
		if (++balanceRedraw == listBalanceAnimation.size()) {			
			redraw();
			balanceRedraw = 0;
		}		
	}
	
	/**
	 * Doplní chybějící větve
	 */
	private void checkBranches() {
		for (IGraphicNode iGraphicNode : listGraphicNodes.subList(1, listGraphicNodes.size())) {
			if (paneTree.getChildren().indexOf(iGraphicNode.getBranch()) == -1) { //pokud tam ještě větev neni
				paneTree.getChildren().add(iGraphicNode.getBranch());
			}						
			
			iGraphicNode.getBranch().toBack();			
		}		
	}

	/**
	 * Přebinduje celý strom 
	 */
	private void redraw() {	
		isRedraw = false;

		xAnimatedNode = new SimpleDoubleProperty();
		yAnimatedNode = new SimpleDoubleProperty();
		
		xAnimatedBranch = new SimpleDoubleProperty();
		yAnimatedBranch = new SimpleDoubleProperty();
		
		IGraphicNode root = listGraphicNodes.get(0);
		rootY.bind(new SimpleDoubleProperty(ROOTBORDER));		
		rootX.bind(paneTreeWeight.subtract(31).divide(2.0));	
		
		root.countChildren();
		
		root.setX(rootX);
		root.setY(rootY);		
		
		xAnimatedBranch.bind(root.getX().add(rootSize / 2));
		yAnimatedBranch.bind(root.getY().add(rootSize / 2));
		
		if (root.getLeft() != null) {				
			root.getLeft().getBranchStartX().bind(xAnimatedBranch);	
			root.getLeft().getBranchStartY().bind(yAnimatedBranch);
		}
		
		if (root.getRight() != null) {				
			root.getRight().getBranchStartX().bind(xAnimatedBranch);
			root.getRight().getBranchStartY().bind(yAnimatedBranch);
		}
		
		for (IGraphicNode iGraphicNode : listGraphicNodes.subList(1, listGraphicNodes.size())) {			
			xAnimatedNode = new SimpleDoubleProperty();
			yAnimatedNode = new SimpleDoubleProperty();
			
			xAnimatedBranch = new SimpleDoubleProperty();
			yAnimatedBranch = new SimpleDoubleProperty();
			
			if (iGraphicNode.getSide() == Side.LEFT) {
				xAnimatedNode.bind(iGraphicNode.getParent().getX().subtract(rootSize).subtract(rootSize * iGraphicNode.getRightChildrenCount()));	
				xAnimatedBranch.bind(iGraphicNode.getParent().getX().subtract(rootSize / 2).subtract(rootSize * iGraphicNode.getRightChildrenCount()));	
			} else {
				xAnimatedNode.bind(iGraphicNode.getParent().getX().add(rootSize).add(rootSize * iGraphicNode.getLeftChildrenCount()));	
				xAnimatedBranch.bind(iGraphicNode.getParent().getX().add(rootSize * 1.5).add(rootSize * iGraphicNode.getLeftChildrenCount()));	
			}
			
			yAnimatedNode.bind(iGraphicNode.getParent().getY().add(DOWNMARGIN));
			yAnimatedBranch.bind(iGraphicNode.getParent().getY().add(rootSize / 2).add(DOWNMARGIN));

			iGraphicNode.setX(xAnimatedNode);
			iGraphicNode.setY(yAnimatedNode);
			
			iGraphicNode.setBranchEndX(xAnimatedBranch);
			iGraphicNode.setBranchEndY(yAnimatedBranch);

			if (iGraphicNode.getLeft() != null) {
				iGraphicNode.getLeft().getBranchStartX().bind(xAnimatedBranch);
				iGraphicNode.getLeft().getBranchStartY().bind(yAnimatedBranch);
			}

			if (iGraphicNode.getRight() != null) {
				iGraphicNode.getRight().getBranchStartX().bind(xAnimatedBranch);
				iGraphicNode.getRight().getBranchStartY().bind(yAnimatedBranch);
			}				
		}
		
		windowController.enableButtons();		
	}	
	
	/********************************************************************************************************
	 * Animace!
	 * 
	 *******************************************************************************************************/
	
	/**
	 * Nachystá prostředí pro nové animace a spustí je
	 * @param recordOfAnimations
	 */
	private void startAnimation(ArrayList<RecordOfAnimation> recordOfAnimations) {
		indexAnimation = 0;
		this.recordOfAnimations = recordOfAnimations;
		nextAnimation();
	}
	
	/**
	 * Spuští další animace po skončení předchozích, aby na sebe navazovaly
	 */
	private void nextAnimation() {
		if (indexAnimation >= recordOfAnimations.size()) {
			balanceTree();
			
			if (isRedraw) {				
				redraw();
			}			
			return;
		}
		
		switch (recordOfAnimations.get(indexAnimation).getAction()) {
		case SEARCH:
			wayIndex = 0;
			nextSearchNode();
			break;
		case INSERT:
			insertNodeAnimation();
			break;
		case DELETE:
			deleteNodeAnimation();			
			break;
		case MOVENODE:
			moveNodeAnimation();
			//isRedraw = true;
			break;		
		case MOVEVALUE:
			moveValueAnimation();
			break;
		case SWAP:
			swapAnimation();
			break;
		case MOVEVALUEFINISH:
			break;
		default:
			break;
		}	
	}	

	/**
	 * Zavolá znovu metodu highlightNode pro každý list zvlášť
	 */
	private void nextSearchNode() {		
		highlightNodeAnimation(wayList.get(wayIndex));
	}		
	
	/**
	 * Animace vložení nového listu
	 */
	private void insertNodeAnimation() {
		if (animationSpeed.get() == 0) {			
			insertNodeAnimationFinished();
			return;
		}
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(10 * (FASTANIMATION - animationSpeed.get())),
				new KeyValue(newIGraphicNode.getX(), xAnimatedNode.get()),
				new KeyValue(newIGraphicNode.getY(), yAnimatedNode.get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				insertNodeAnimationFinished();
			}
		});

		newIGraphicNode.getX().unbind();
		newIGraphicNode.getY().unbind();

		timeline.play();
	}
	
	private void insertNodeAnimationFinished() {
		newIGraphicNode.setX(xAnimatedNode);
		newIGraphicNode.setY(yAnimatedNode);
		
		createBranch(newIGraphicNode);
		insertBranch();		
		listGraphicNodes.add(newIGraphicNode);

		if (newIGraphicNode.getSide() == Side.LEFT) {
			newIGraphicNode.getParent().setLeft(newIGraphicNode);
		} else if (newIGraphicNode.getSide() == Side.RIGHT) {
			newIGraphicNode.getParent().setRight(newIGraphicNode);
		}

		indexAnimation++;
		nextAnimation();
	}
	
	/**
	 * Smazání listu animace
	 */
	private void deleteNodeAnimation() {
		IGraphicNode node = wayList.get(wayList.size() - 1);
		
		node.highlightFindNode(); // zvýrazním mazaný node
		if ((boolean) recordOfAnimations.get(indexAnimation).getObject()) { //pokud má děti
			node.setValue("");
			indexAnimation++;
			nextAnimation();
		} else {			
			if (animationSpeed.get() == 0) { //neni animace
				deleteNodeAnimationFinished(node);
				return;
			}
			
			FadeTransition fadeTransitionNode = new FadeTransition(Duration.millis(10 * (FASTANIMATION - animationSpeed.get())), node.getStackPaneNode());
			fadeTransitionNode.setFromValue(1.0);
			fadeTransitionNode.setToValue(0.0);			
			
			FadeTransition fadeTransitionBranch = new FadeTransition(Duration.millis(10 * (FASTANIMATION - animationSpeed.get())), node.getBranch());
			fadeTransitionBranch.setFromValue(1.0);
			fadeTransitionBranch.setToValue(0.0);

			fadeTransitionBranch.play();
			fadeTransitionNode.play();
			
			fadeTransitionNode.setOnFinished(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {	
					deleteNodeAnimationFinished(node);										
				}
			});
		}
	}
	
	private void deleteNodeAnimationFinished(IGraphicNode node) {
		listGraphicNodes.remove(node);					
		paneTree.getChildren().remove(node.getStackPaneNode());
		paneTree.getChildren().remove(node.getBranch());
		
		indexAnimation++;					
		nextAnimation();
	}

	/**
	 * Nahradí mazaný list novým listem
	 */
	private void moveNodeAnimation() {
		IGraphicNode graphicNodeRemoved = recordOfAnimations.get(indexAnimation).getNode1(); //zaloha už je 
		IGraphicNode graphicNodeMoved = (IGraphicNode) recordOfAnimations.get(indexAnimation).getObject();
		
		if (animationSpeed.get() == 0) {
			moveNodeAnimationFinished(graphicNodeRemoved, graphicNodeMoved);
		}		
		
		graphicNodeMoved.highlightNode();
		graphicNodeMoved.getStackPaneNode().toFront();
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(10 * (FASTANIMATION - animationSpeed.get())),
				new KeyValue(graphicNodeMoved.getX(), graphicNodeRemoved.getX().get()),
				new KeyValue(graphicNodeMoved.getY(), graphicNodeRemoved.getY().get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				moveNodeAnimationFinished(graphicNodeRemoved, graphicNodeMoved);
			}
		});

		graphicNodeMoved.getX().unbind();
		graphicNodeMoved.getY().unbind();		
		paneTree.getChildren().remove(graphicNodeMoved.getBranch());

		timeline.play();				
	}
	
	private void moveNodeAnimationFinished(IGraphicNode graphicNodeRemoved, IGraphicNode graphicNodeMoved) {
		if (graphicNodeMoved.getLeft() == null && graphicNodeMoved.getRight() == null) {
			graphicNodeRemoved.setValue(graphicNodeMoved.getValue());
			graphicNodeRemoved.setDefaultColorNode();

			paneTree.getChildren().remove(graphicNodeMoved.getStackPaneNode());	
			paneTree.getChildren().remove(graphicNodeMoved.getBranch());
			listGraphicNodes.remove(graphicNodeMoved);
			
		} else {	
			if (graphicNodeRemoved.getParent() == null) {
				graphicNodeMoved.setX(rootX);
				listGraphicNodes.remove(graphicNodeMoved); //dám roota na první místo
				listGraphicNodes.add(0, graphicNodeMoved);
			} else {
				//graphicNodeMoved.setParent(graphicNodeRemoved.getParent()); /** smazané **/
			}
							
			graphicNodeMoved.setDefaultColorNode();
			graphicNodeMoved.getStackPaneNode().toFront();					

			paneTree.getChildren().remove(graphicNodeRemoved.getStackPaneNode());	
			paneTree.getChildren().remove(graphicNodeRemoved.getBranch());
			createBranch(graphicNodeMoved);
			listGraphicNodes.remove(graphicNodeRemoved);
		}
		
		indexAnimation++;
		nextAnimation();
	}
	
	/**
	 * Přesune hodnotu do jiného listu
	 */
	private void moveValueAnimation() {
		IGraphicNode node1 = recordOfAnimations.get(indexAnimation).getNode1();
		IGraphicNode node2 = ((IGraphicNode) recordOfAnimations.get(indexAnimation).getObject());
		
		node1.setValue(node2.getValue());
		node2.setValue("");
		node1.setDefaultColorNode();
		node2.setDefaultColorNode();

		indexAnimation++;
		nextAnimation();			
	}
	
	private void swapAnimation(){
		//createBackUp();
		//TODO
	}

	/**
	 * Animace zvýraznění větve a následně listu 
	 * @param node
	 */
	private void highlightNodeAnimation(IGraphicNode node) {
		if (animationSpeed.get() == 0) { //když nebude animace
			highlightNodeAnimationFinished();
			return;
		}
		
		StrokeTransition st1 = null;
		PauseTransition pt1 = null;
		StrokeTransition st2 = null;		
		SequentialTransition seqT;
		
		if(node.getBranch() != null) {
			st1 = new StrokeTransition(Duration.millis(SLOWANIMATION),(Line) node.getBranch(), Color.WHITE, Color.LIME);
			pt1 = new PauseTransition(Duration.millis(5 * (FASTANIMATION - animationSpeed.get())));
			st2 = new StrokeTransition(Duration.millis(SLOWANIMATION), (Line) node.getBranch(), Color.LIME, Color.WHITE);
		}
		
		StrokeTransition st3 = new StrokeTransition(Duration.millis(SLOWANIMATION), node.getCircleShape(), Color.WHITE, Color.LIME);
		PauseTransition pt2 = new PauseTransition(Duration.millis(10 * (FASTANIMATION - animationSpeed.get())));
		StrokeTransition st4 = new StrokeTransition(Duration.millis(SLOWANIMATION), node.getCircleShape(), Color.LIME, Color.WHITE);
		
		if(node.getBranch() != null) {
			seqT = new SequentialTransition(st1, pt1, st2, st3, pt2, st4);
		} else {
			seqT = new SequentialTransition(st3, pt2, st4);
		}
		
		seqT.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (++wayIndex < wayList.size()) {
					nextSearchNode();
				} else {
					highlightNodeAnimationFinished();
				}
			}
		});
		
		seqT.play();
	}
	
	private void highlightNodeAnimationFinished() {
		if((boolean)recordOfAnimations.get(indexAnimation).getObject()) {
			highlightFindNode();
		} else {
			indexAnimation++;
			nextAnimation();
		}		
	}
	
	/**
	 * Zvýrazní nalezený list
	 */
	private void highlightFindNode() {
		StrokeTransition st3 = new StrokeTransition(Duration.millis(SLOWANIMATION), wayList.get(wayList.size() - 1).getCircleShape(), Color.WHITE, Color.YELLOW);
		PauseTransition pt2 = new PauseTransition(Duration.millis(10 * (SLOWANIMATION - 50 - animationSpeed.get())));
		StrokeTransition st4 = new StrokeTransition(Duration.millis(SLOWANIMATION), wayList.get(wayList.size() - 1).getCircleShape(), Color.YELLOW, Color.WHITE);

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
	
	/********************************************************************************************************
	 * GETS & SETS
	 * 
	 *******************************************************************************************************/
	
	public List<IGraphicNode> getListGraphicNodes() {
		return listGraphicNodes;
	}
	
	public void setListGraphicNodes(List<IGraphicNode> oldGraphicTreeNodes) {
		listGraphicNodes.clear();
		listGraphicNodes.addAll(oldGraphicTreeNodes);		
	}

	public void setRedraw() {
		redraw();
	}
}