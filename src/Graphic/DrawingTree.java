package Graphic;

import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import Aplication.WindowController;
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
	
	private int maxLevel;
	private double moreSpace = 0; 
	
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
		//this.tree = tree;		
		this.animationSpeed = speed;
		this.windowController = windowController;
	//	Class<T> animationClass;
	//	this.treeAnimation = animationClass.getConstructor(ITree.class, Canvas.class).newInstance(this.canvas, this.tree);
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
		rootX.bind(paneTreeWeight.subtract(31).divide(2.0).add(root.getRadiusSize() / 2));		
		
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
		//double computedX;	
		DoubleProperty startNodeX = new SimpleDoubleProperty();	
		DoubleProperty startNodeY = new SimpleDoubleProperty();	
		
		startNodeX.bind(paneTreeWeight.subtract(paneTreeWeight.get()).add(80));	
		startNodeY.bind(new SimpleDoubleProperty(ROOTBORDER));	
		
		newIGraphicNode = result.getNode().getGraphicNode(); //vkládaný list	
		
	//	newIGraphicNode.setParent(((INode<?>) result.getNode().getParent()).getGraphicNode());
		newIGraphicNode.setLevel(result.getWay().size());
	//	newIGraphicNode.setSide(result.getSide());
		
		paneTree.getChildren().add(newIGraphicNode.getStackPaneNode()); //přidám list 		
		
		newIGraphicNode.setX(startNodeX); // vložím počáteční souřadnice
		newIGraphicNode.setY(startNodeY);		
		
	//	computedX = computeX(newIGraphicNode); //vypočítám posunutí od rodiče
		
		if (result.getSide() == Side.LEFT) {
			xAnimatedNode.bind(newIGraphicNode.getParent().getX().subtract(rootSize));	
		} else {
			xAnimatedNode.bind(newIGraphicNode.getParent().getX().add(rootSize));	
		}
		yAnimatedNode.bind(newIGraphicNode.getParent().getY().add(DOWNMARGIN));
		
		//zavolám animaci
		startAnimation(result.getRecordOfAnimations());	
		
	//	createBranch(computedX); //vložení větve
	//	createBranch(rootSize);
		createBranch2(newIGraphicNode);
		
		if(animationSpeed.get() == 0) {
			insertBranch(); //pokud neni animace vložím větev ihned nečekám na ukončení animace, která má větev vložit
		}
		
		listGraphicNodes.add(newIGraphicNode);	
		
		//balanceTree();
		//computeMoreSpace();		
	}
	
	/**
	 * Vypočítá posunutí od rodiče
	 * @param node
	 * @return
	 */
	private double computeX(IGraphicNode node) {
		//return (newIGraphicNode.getParent().getRadiusSize() / 2) * (5 - (node.getLevel() * 1.1)) + moreSpace;
		return newIGraphicNode.getParent().getRadiusSize();
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
	 * Upraví vzdálenosti listů 
	 */
	private void balanceTree() {
		Timeline timeline;
		KeyFrame kf;
		Duration duration;

		listBalanceAnimation = new ArrayList<>();

		listGraphicNodes.get(0).countChildren(); // nechám rekurzivně vypočítat
													// děti

		for (IGraphicNode iGraphicNode : listGraphicNodes.subList(1, listGraphicNodes.size())) {
			xAnimatedNode = new SimpleDoubleProperty();
			xAnimatedBranch = new SimpleDoubleProperty();

			if (iGraphicNode.getSide() == Side.LEFT) {
				xAnimatedNode.bind(iGraphicNode.getParent().getX().subtract(rootSize).subtract(rootSize * iGraphicNode.getRightChildrenCount()));
			} else {
				xAnimatedNode.bind(iGraphicNode.getParent().getX().add(rootSize).add(rootSize * iGraphicNode.getLeftChildrenCount()));
			}			

			if (iGraphicNode.getX().get() != xAnimatedNode.get()) {

				if (iGraphicNode.getSide() == Side.LEFT) { // vypočítám nový x pro větev
					xAnimatedBranch.bind(iGraphicNode.getParent().getX().subtract(rootSize / 2).subtract(rootSize * iGraphicNode.getRightChildrenCount()));
				} else {
					xAnimatedBranch.bind(iGraphicNode.getParent().getX().add(rootSize * 1.5).add(rootSize * iGraphicNode.getLeftChildrenCount()));
				}

				if (animationSpeed.get() == 0) {
					duration = Duration.millis(1);
				} else {
					duration = Duration.millis(10 * (FASTANIMATION - animationSpeed.get()));
				}
				timeline = new Timeline();

				kf = new KeyFrame(duration,
						new KeyValue(iGraphicNode.getX(), xAnimatedNode.get())
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

						iGraphicNode.setX(xAnimatedNode);
						iGraphicNode.setBranchEndX(xAnimatedBranch);

						if (iGraphicNode.getLeft() != null) {
							iGraphicNode.getLeft().getBranchStartX().bind(xAnimatedBranch);
						}

						if (iGraphicNode.getRight() != null) {
							iGraphicNode.getRight().getBranchStartX().bind(xAnimatedBranch);
						}

						balanceRedraw();
					}
				});

				iGraphicNode.getX().unbind();
				iGraphicNode.getBranchEndX().unbind();
				listBalanceAnimation.add(timeline);
			}
		}

		if (listBalanceAnimation.isEmpty()) {
			windowController.enableButtons();
		} else {
			for (Timeline t : listBalanceAnimation) {
				t.play();
			}
		}
	}

	/**
	 * Vložení větve
	 * @param parent
	 * @param node
	 * @param side
	 * @param x - posunutí oproti rodičovi
	 */
	private void createBranch(double x) {
		Pane branch = new Pane();
		//int space = newIGraphicNode.getParent().getRadiusSize() / 2;
		Line line;
		//Line line2, line3;
		
	/*	if (newIGraphicNode.getSide() == Side.LEFT) {
			line = new Line(0, 0, -x, DOWNMARGIN - space);	
		} else {
			line = new Line(0, 0, x, DOWNMARGIN - space);	
		}
		*/
		
		if (newIGraphicNode.getSide() == Side.LEFT) {
			line = new Line(0, 0, -x, DOWNMARGIN); //TODO?? -rootSize? 	
		} else {
			line = new Line(0, 0, x, DOWNMARGIN);	
		}
		
		/*line2 = new Line(0, -100, 0,200);
		line3 = new Line(-100, 0, 200, 0);*/
		
		branch.translateXProperty().bind(newIGraphicNode.getParent().getX().add(rootSize / 2));
		branch.translateYProperty().bind(newIGraphicNode.getParent().getY().add(rootSize / 2));		
		
		branch.getChildren().add(line);
		//***branch.getChildren().addAll(line,line2,line3);
	//	newIGraphicNode.setBranch(branch);		
	} 
	
	private void createBranch2(IGraphicNode node) {		
		Line line = new Line();
		line.startXProperty().bind(node.getParent().getX().add(rootSize / 2));
		line.startYProperty().bind(node.getParent().getY().add(rootSize / 2));
		
		if (node.getSide() == Side.LEFT) {
			line.endXProperty().bind(node.getParent().getX().subtract(rootSize / 2));	
		} else {
			line.endXProperty().bind(node.getParent().getX().add(rootSize * 1.5));	
		}
		
		line.endYProperty().bind(node.getY().add(rootSize / 2));		
		
		node.setBranch(line);
	}  
	
	/**
	 * Přemístí list 
	 * @param node - list kam se má větev přesunůt
	 */
	@SuppressWarnings("unused")
	private void relocateNodeAndBranch(IGraphicNode node) {
		double computedX = computeX(node);
		int space = node.getParent().getRadiusSize() / 2;
		DoubleProperty newX = new SimpleDoubleProperty();
	
		if (node.getSide() == Side.LEFT) {
	//		 node.setBranchEndX(-computedX);	
			 newX.bind(node.getParent().getX().subtract(computedX));				 
		} else {
			newX.bind(node.getParent().getX().add(computedX));
	//		node.setBranchEndX(computedX);	
		}
		node.setX(newX);
		
		node.getBranch().translateXProperty().bind(node.getParent().getX().add(space));
		//**branch.translateYProperty().bind(new SimpleDoubleProperty(newIGraphicNode.getParent().getY() + space));			
	}	
	
	/**
	 * Vypočte hodnoty moreSpace a maxLevel
	 */
	@SuppressWarnings("unused")
	private void computeMoreSpace(){
		int max = maxLevel();		
		if (max != maxLevel) {						
			if(max < 3) {
				moreSpace = 0;
			} else {
				moreSpace = newIGraphicNode.getParent().getRadiusSize() * (max / 1.8);
			}
			maxLevel = max;
			isRedraw = true;
		}		
	}
	
	/**
	 * Vypočítá maximální level
	 * @return
	 */
	private int maxLevel() {
		int max = 0;
		for (IGraphicNode iGraphicNode : listGraphicNodes) {
			if (iGraphicNode.getLevel() > max)
				max = iGraphicNode.getLevel();
		}		
		return max;			
	}
	
	/**
	 * 
	 */
	private void balanceRedraw() {
		if (++balanceRedraw == listBalanceAnimation.size()) {
			redraw();
			balanceRedraw = 0;
		}		
	}
	
	/**
	 * Překreslí celý strom 
	 * TODO: listy nejsů nabindované + neposunujů se + první větve upravit
	 */
	private void redraw() {	
		isRedraw = false;	
		for (IGraphicNode iGraphicNode : listGraphicNodes.subList(1, listGraphicNodes.size())) {
			xAnimatedNode = new SimpleDoubleProperty(); 	
			xAnimatedBranch = new SimpleDoubleProperty();
			
			if (iGraphicNode.getSide() == Side.LEFT) {
				xAnimatedNode.bind(iGraphicNode.getParent().getX().subtract(rootSize).subtract(rootSize * iGraphicNode.getRightChildrenCount()));	
				xAnimatedBranch.bind(iGraphicNode.getParent().getX().subtract(rootSize / 2).subtract(rootSize * iGraphicNode.getRightChildrenCount()));	
			} else {
				xAnimatedNode.bind(iGraphicNode.getParent().getX().add(rootSize).add(rootSize * iGraphicNode.getLeftChildrenCount()));	
				xAnimatedBranch.bind(iGraphicNode.getParent().getX().add(rootSize * 1.5).add(rootSize * iGraphicNode.getLeftChildrenCount()));	
			}
			
			iGraphicNode.setX(xAnimatedNode);			
			iGraphicNode.setBranchEndX(xAnimatedBranch);
			
			if (iGraphicNode.getLeft() != null) {							
				iGraphicNode.getLeft().getBranchStartX().bind(xAnimatedBranch);
			}
			
			if (iGraphicNode.getRight() != null) {								
				iGraphicNode.getRight().getBranchStartX().bind(xAnimatedBranch);
			}
		}
		
		windowController.enableButtons();
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
	 * Sníží všem potomkům level
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
			if (isRedraw) {				
				redraw();
			}
			balanceTree();
			//windowController.enableButtons();
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
			moveAnimation();			
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
		highlightNode(wayList.get(wayIndex));
	}		
	
	/**
	 * Animace vložení nového listu
	 */
	private void insertNodeAnimation() {	
		if (animationSpeed.get() == 0) {			
			newIGraphicNode.setX(xAnimatedNode);
			newIGraphicNode.setY(yAnimatedNode);
			indexAnimation++;
			nextAnimation();
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
				listGraphicNodes.remove(node);
				paneTree.getChildren().remove(node.getStackPaneNode());
				paneTree.getChildren().remove(node.getBranch());
				
				indexAnimation++;
			//	computeMoreSpace();
			//	balanceTree();
				nextAnimation();
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
					listGraphicNodes.remove(node);
					paneTree.getChildren().remove(node.getStackPaneNode());
					paneTree.getChildren().remove(node.getBranch());
					
					//musím zviditelnit kvůli viditelnosti pro opakování posledního kroku
					FadeTransition fadeTransitionNode = new FadeTransition(Duration.millis(1), node.getStackPaneNode());
					fadeTransitionNode.setFromValue(0.0);
					fadeTransitionNode.setToValue(1.0);			
					
					FadeTransition fadeTransitionBranch = new FadeTransition(Duration.millis(1), node.getBranch());
					fadeTransitionBranch.setFromValue(0.0);
					fadeTransitionBranch.setToValue(1.0);

					fadeTransitionBranch.play();
					fadeTransitionNode.play();
					node.setDefaultColorNode();
					
					indexAnimation++;
					//balanceTree();
					//computeMoreSpace();
					nextAnimation();					
				}
			});
		}
	}

	/**
	 * Nahradí mazaný list novým listem
	 */
	private void moveAnimation() {
		INode<?> iNodeRemoved = recordOfAnimations.get(indexAnimation).getNode1();
		INode<?> iNodeMoved = (INode<?>) recordOfAnimations.get(indexAnimation).getObject();
		
		if (animationSpeed.get() == 0) {
			if (iNodeMoved.getLeft() == null && iNodeMoved.getRight() == null) {
				iNodeRemoved.getGraphicNode().setValue(iNodeMoved.getGraphicNode().getValue());
				iNodeRemoved.getGraphicNode().setDefaultColorNode();

				paneTree.getChildren().remove(iNodeMoved.getGraphicNode().getStackPaneNode());					
			} else {	
				if (iNodeRemoved.getParent() == null) {
					iNodeMoved.getGraphicNode().setX(rootX);
					listGraphicNodes.remove(iNodeMoved.getGraphicNode()); //dám roota na první místo
					listGraphicNodes.add(0, iNodeMoved.getGraphicNode());
				}			
				
				decreaseLevel(iNodeMoved); //snížím všem potomkům level
				
				iNodeMoved.getGraphicNode().setLevel(iNodeRemoved.getGraphicNode().getLevel());
				iNodeMoved.getGraphicNode().setParent(iNodeRemoved.getGraphicNode().getParent());
				iNodeMoved.getGraphicNode().getStackPaneNode().toFront();					

				paneTree.getChildren().remove(iNodeRemoved.getGraphicNode().getStackPaneNode());	
				listGraphicNodes.remove(iNodeRemoved.getGraphicNode());
				
				iNodeRemoved.setGraphicNode(iNodeMoved.getGraphicNode()); //změním INode1 jeho grafický node... 
			}
			
			//computeMoreSpace();
			//balanceTree();
			indexAnimation++;
			nextAnimation();
			return;
		}		
		
		iNodeMoved.getGraphicNode().highlightNode();
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(10 * (FASTANIMATION - animationSpeed.get())),
				new KeyValue(iNodeMoved.getGraphicNode().getX(), iNodeRemoved.getGraphicNode().getX().get()),
				new KeyValue(iNodeMoved.getGraphicNode().getY(), iNodeRemoved.getGraphicNode().getY().get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (iNodeMoved.getLeft() == null && iNodeMoved.getRight() == null) {
					iNodeRemoved.getGraphicNode().setValue(iNodeMoved.getGraphicNode().getValue());
					iNodeRemoved.getGraphicNode().setDefaultColorNode();

					paneTree.getChildren().remove(iNodeMoved.getGraphicNode().getStackPaneNode());					
				} else {	
					if (iNodeRemoved.getParent() == null) {
						iNodeMoved.getGraphicNode().setX(rootX);
						listGraphicNodes.remove(iNodeMoved.getGraphicNode()); //dám roota na první místo
						listGraphicNodes.add(0, iNodeMoved.getGraphicNode());
					}
					
					decreaseLevel(iNodeMoved); //snížím všem potomkům level
					
					iNodeMoved.getGraphicNode().setLevel(iNodeRemoved.getGraphicNode().getLevel());
					iNodeMoved.getGraphicNode().setParent(iNodeRemoved.getGraphicNode().getParent());
					iNodeMoved.getGraphicNode().setDefaultColorNode();
					iNodeMoved.getGraphicNode().getStackPaneNode().toFront();					

					paneTree.getChildren().remove(iNodeRemoved.getGraphicNode().getStackPaneNode());	
					listGraphicNodes.remove(iNodeRemoved.getGraphicNode());
					
					iNodeRemoved.setGraphicNode(iNodeMoved.getGraphicNode()); //změním INode1 jeho grafický node... 
				}
				
			//	computeMoreSpace();
			//	balanceTree();
				indexAnimation++;
				nextAnimation();
			}
		});

		iNodeMoved.getGraphicNode().getX().unbind();
		iNodeMoved.getGraphicNode().getY().unbind();		
		paneTree.getChildren().remove(iNodeMoved.getGraphicNode().getBranch());

		timeline.play();		
		
	}
	/**
	 * Přesune hodnotu do jiného listu
	 */
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
	 * Animace zvýraznění větve a následně listu 
	 * @param node
	 */
	private void highlightNode(IGraphicNode node) {
		if (animationSpeed.get() == 0) { //když nebude animace
			if((boolean)recordOfAnimations.get(indexAnimation).getObject()) {
				highlightFindNode();
			} else {
				indexAnimation++;
				nextAnimation();
			}
			return;
		}
		
		StrokeTransition st1 = null;
		PauseTransition pt1 = null;
		StrokeTransition st2 = null;		
		SequentialTransition seqT;
		
		if(node.getBranch() != null) {
	//		st1 = new StrokeTransition(Duration.millis(SLOWANIMATION),(Line) node.getBranch().getChildren().get(0), Color.BLACK, Color.LIME);
			st1 = new StrokeTransition(Duration.millis(SLOWANIMATION),(Line) node.getBranch(), Color.BLACK, Color.LIME);
			pt1 = new PauseTransition(Duration.millis(5 * (FASTANIMATION - animationSpeed.get())));
	//		st2 = new StrokeTransition(Duration.millis(SLOWANIMATION), (Line) node.getBranch().getChildren().get(0), Color.LIME, Color.BLACK);
			st2 = new StrokeTransition(Duration.millis(SLOWANIMATION), (Line) node.getBranch(), Color.LIME, Color.BLACK);
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
	
		
	}*/}