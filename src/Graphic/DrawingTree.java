package Graphic;

import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicTreeUI.TreePageAction;

import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolver;

import Aplication.WindowController;
import Trees.AVLNode;
import Trees.AnimatedAction;
import Trees.INode;
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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeSortMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.util.Duration;
import sun.reflect.generics.tree.Tree;

public class DrawingTree {
	private WindowController windowController;
	
	private List<IGraphicNode> listGraphicNodes = new ArrayList<>(); //root je na místě 0
	
	private int value;	
	private TextArea text;
	private String oldText = "";
	private String newText;
	private ReadOnlyDoubleProperty paneTreeWeight;
	private ReadOnlyDoubleProperty paneTreeHeight;
	private Pane paneTree;
	private DoubleProperty animationSpeed = new SimpleDoubleProperty();
	
	private final static double ROOTBORDER = 40;	
	private final static double DOWNMARGIN = 40;	
	
	private double rootSize = 0;
	private double stackPaneHeight = 0;
	
	private DoubleProperty rootY = new SimpleDoubleProperty();
	private DoubleProperty rootX = new SimpleDoubleProperty();	
	
	private IGraphicNode newIGraphicNode;	
	
	//animation variable
	private ArrayList<IGraphicNode> wayList;
	private ArrayList<RecordOfAnimation> recordOfAnimations;
	
	//private final int SLOWANIMATION = 250;
	//private final int FASTANIMATION = 105;
	
	private int slowAnimationSpeed = 500;
	private int fastAnimationSpeed = 150;
	
	private int wayIndex = 0;
	private int indexAnimation = 0;
	private boolean isRedraw = false;
	private boolean notFind = false;
	private boolean isBalance = false;
	private boolean isReColor = false;
	private boolean isRedBlack = false;
	private boolean isMoveNode = false;
	private boolean isAnimation = true;	
	private boolean isInsertAnimation = true;

	private int balanceRedraw = 0;
	
	private DoubleProperty xAnimatedNode = new SimpleDoubleProperty();
	private DoubleProperty yAnimatedNode = new SimpleDoubleProperty();
	
	private DoubleProperty xAnimatedBranch = new SimpleDoubleProperty();
	private DoubleProperty yAnimatedBranch = new SimpleDoubleProperty();
	
	RedBlackGraphicNode nullNode;
	
	double orgSceneX, orgSceneY;
    double orgTranslateX, orgTranslateY;

	public DrawingTree(Pane paneTree, DoubleProperty speed, ReadOnlyDoubleProperty stageWidthProperty, ReadOnlyDoubleProperty stageHeightProperty,  WindowController windowController) {
		this.paneTreeWeight = stageWidthProperty;
		this.paneTreeHeight = stageHeightProperty;
		this.paneTree = paneTree;
		this.animationSpeed = speed;
		this.windowController = windowController;
		
		//Přidá text
		text = new TextArea("");		
		text.setMaxWidth(280);
		text.setMaxHeight(105);
		text.setEditable(false);		
		text.setFont(new Font(text.getFont().toString(), 15));
		text.layoutXProperty().bind(stageWidthProperty.subtract(300));	
		
		text.setStyle( "-fx-border-style: solid;" + 
                 "-fx-border-width: 5;" +
                 "-fx-border-insets: 0;" + 
                 "-fx-border-radius: 0;" + 
                 "-fx-border-color: gray;");

		text.addEventFilter(MouseEvent.MOUSE_PRESSED, textPressedEventHandler);
		
		text.setOnMouseDragged(textDraggedEventHandler);
		
		paneTree.getChildren().add(text);
	}
	
	EventHandler<MouseEvent> textPressedEventHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent t) {
			orgSceneX = t.getSceneX();
			orgSceneY = t.getSceneY();
			orgTranslateX = ((TextArea) (t.getSource())).getTranslateX();
			orgTranslateY = ((TextArea) (t.getSource())).getTranslateY();
		}
	};

	EventHandler<MouseEvent> textDraggedEventHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent t) {
			double offsetX = t.getSceneX() - orgSceneX;
			double offsetY = t.getSceneY() - orgSceneY;
			double newTranslateX = orgTranslateX + offsetX;
			double newTranslateY = orgTranslateY + offsetY;
			
			if (newTranslateX >= 3) {
				((TextArea) (t.getSource())).setTranslateX(2);
			} else if (newTranslateX <= 300 - paneTreeWeight.get()) {
				((TextArea) (t.getSource())).setTranslateX(299 - paneTreeWeight.get() + 1);
			} else {
				((TextArea) (t.getSource())).setTranslateX(newTranslateX);
			}
			
			if (newTranslateY >= paneTreeHeight.get() - 250) {
				((TextArea) (t.getSource())).setTranslateY(paneTreeHeight.get() - 251);
			} else if (newTranslateY <= -1) {
				((TextArea) (t.getSource())).setTranslateY(0);
			} else {
				((TextArea) (t.getSource())).setTranslateY(newTranslateY);
			}			
		}
	};
	
	/**
	 * Vložení kořenu
	 * @param root
	 */
	public void insertRoot(INode<?> rootNode){
		newIGraphicNode = rootNode.getGraphicNode();
		rootSize = newIGraphicNode.getRadiusSize();
		stackPaneHeight = newIGraphicNode.getStackPaneNode().getPrefHeight();
		
		rootY.bind(new SimpleDoubleProperty(ROOTBORDER));	
		rootX.bind(paneTreeWeight.subtract(31).divide(2.0));	
		
		DoubleProperty startNodeX = new SimpleDoubleProperty();	
		DoubleProperty startNodeY = new SimpleDoubleProperty();	
		
		startNodeX.bind(paneTreeWeight.subtract(paneTreeWeight.get()).add(80));	
		startNodeY.bind(new SimpleDoubleProperty(ROOTBORDER));			
		
		paneTree.getChildren().add(newIGraphicNode.getStackPaneNode());
		
		listGraphicNodes.add(newIGraphicNode);
		
		if (animationSpeed.get() == 0) { //žádná animace
			newIGraphicNode.setY(rootY);
			newIGraphicNode.setX(rootX);			
			windowController.enableButtons();
			return;
		}
		
		newIGraphicNode.setX(startNodeX); // vložím počáteční souřadnice
		newIGraphicNode.setY(startNodeY);
		newIGraphicNode.highlightFindNode();
		
		checkSpeed();
		
		//*PauseTransition pt2 = new PauseTransition(Duration.millis(slowAnimationSpeed - 50 - animationSpeed.get()));
		//*StrokeTransition st4 = new StrokeTransition(Duration.millis(slowAnimationSpeed), newIGraphicNode.getCircleShape(), Color.YELLOW, Color.WHITE);
		
		PauseTransition pt2 = new PauseTransition(Duration.millis(slowAnimationSpeed));
		StrokeTransition st4 = new StrokeTransition(Duration.millis(slowAnimationSpeed), newIGraphicNode.getCircleShape(), Color.YELLOW, Color.WHITE);

		SequentialTransition seqT = new SequentialTransition(pt2, st4);

		seqT.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				windowController.enableButtons();
				newIGraphicNode.setDefaultColorNode();
			}
		});		
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(4 * fastAnimationSpeed),
				new KeyValue(newIGraphicNode.getX(), rootX.get()),
				new KeyValue(newIGraphicNode.getY(), rootY.get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {				
				newIGraphicNode.setY(rootY);
				newIGraphicNode.setX(rootX);
				text.appendText("\n • Vložení proběhlo úspěšně.");
				if (!isInsertAnimation) { 
					windowController.enableButtons();
					newIGraphicNode.setDefaultColorNode();
				} else {
					seqT.play();
				}
											
			}
		});

		newIGraphicNode.getX().unbind();
		newIGraphicNode.getY().unbind();

		text.setText("VLOŽENÍ PRVKU " + newIGraphicNode.getValue() +":");
		timeline.play();
	}
	
	/**
	 * Vykreslení nového listu 
	 * @param result
	 */
	public void insertNode(Result<?> result, int value) {
		wayList = result.getWay();	
		this.value = value;
		
		if ((boolean) result.getRecordOfAnimations().get(0).getObject()) {
			startAnimation(result.getRecordOfAnimations()); //nalezen
			return;
		}
		
		xAnimatedNode = new SimpleDoubleProperty(); //souřadnice x listu	
		yAnimatedNode = new SimpleDoubleProperty();
		DoubleProperty startNodeX = new SimpleDoubleProperty();	
		DoubleProperty startNodeY = new SimpleDoubleProperty();	
		
		startNodeX.bind(paneTreeWeight.subtract(paneTreeWeight.get()).add(80));	
		startNodeY.bind(new SimpleDoubleProperty(ROOTBORDER));	
		
		newIGraphicNode = result.getNode().getGraphicNode(); //vkládaný list	
		
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
		newIGraphicNode.highlightFindNode();
		startAnimation(result.getRecordOfAnimations());	
	}
	
	/**
	 * Smazání listu + zavolá překreslení
	 * @param node
	 */
	public void deleteNode(Result<?> result, int value) {
		this.value = value;
		if (result.getSide() != Side.NONE) {
			notFind = true;
			wayIndex = 0;
			nextSearchNode();
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
	public void searchNode(Result<?> result, int value) {
		this.value = value;
		wayList = result.getWay();	
		checkSpeed();
		startAnimation(result.getRecordOfAnimations());			
	}	
	
	/**
	 * Vložení větve
	 * @param node
	 */	
	private void createBranch(IGraphicNode node) {
		if (node.getParent() == null) {
			node.setBranch(null);
			return;
		}
		
		if (node.getBranch() != null) {
			return;
		}		
		
		Line line = new Line();
		line.startXProperty().bind(node.getParent().getX().add(rootSize / 2));
		line.startYProperty().bind(node.getParent().getY().add(stackPaneHeight / 2));
		line.setStroke(Color.WHITE);
		
		if (node.getSide() == Side.LEFT) {
			line.endXProperty().bind(node.getParent().getX().subtract(rootSize / 2));	
		} else {
			line.endXProperty().bind(node.getParent().getX().add(rootSize * 1.5));	
		}
		
		line.endYProperty().bind(node.getY().add(stackPaneHeight / 2));		
		
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
		if (listGraphicNodes.isEmpty() || !isAnimation) {			
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
			balanceRedraw = 1;
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
				duration = Duration.millis(3.5 * fastAnimationSpeed);
			}
			timeline = new Timeline();

			kf = new KeyFrame(duration,
					new KeyValue(iGraphicNode.getX(), xAnimatedNode.get()),
					new KeyValue(iGraphicNode.getY(), yAnimatedNode.get()));

			timeline.getKeyFrames().add(kf);

			timeline.setOnFinished(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if (iGraphicNode.getParent() == null) { //pokud dojde ke změně rootu mezi animací
						balanceRedraw++;
						balanceTreeNext();
						return;
					}
					
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
					yAnimatedBranch.bind(iGraphicNode.getParent().getY().add(stackPaneHeight / 2).add(DOWNMARGIN));

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
			
			hideMovedBranchRecursive(iGraphicNode);
			
			timeline.play();
		} else {
			balanceRedraw++;
			balanceTreeNext();
		}
	}	

	/**
	 * Skryje všechny zainteresované větve kvůli animaci
	 * @param iGraphicNode
	 */
	private void hideMovedBranchRecursive(IGraphicNode iGraphicNode) {
		paneTree.getChildren().remove(iGraphicNode.getBranch());		
		
		if (iGraphicNode.getLeft() != null) {
			hideMovedBranchRecursive(iGraphicNode.getLeft());
		}
		
		if (iGraphicNode.getRight() != null) {
			hideMovedBranchRecursive(iGraphicNode.getRight());
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
		yAnimatedBranch.bind(root.getY().add(stackPaneHeight / 2));
		
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
			yAnimatedBranch.bind(iGraphicNode.getParent().getY().add(stackPaneHeight / 2).add(DOWNMARGIN));

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
		if (indexAnimation >= recordOfAnimations.size()) {
			windowController.enableButtons();
			return;
		} else {
			indexAnimation++;
			nextAnimation();
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
		notFind = false;
		isBalance = false;
		isMoveNode = false;
		isReColor = false;
		this.recordOfAnimations = recordOfAnimations;
		checkSpeed();
		nextAnimation();
	}	
	
	/**
	 * Zjistí rychlost animací a podle teho nastaví hodnoty
	 */
	private void checkSpeed() {
		switch ((int)animationSpeed.get()) {
		case 0:
			slowAnimationSpeed = 250;
			fastAnimationSpeed = 105;
			break;
			
		case 25:
			slowAnimationSpeed = 700;
			fastAnimationSpeed = 200;
			break;
			
		case 50:
			slowAnimationSpeed = 500;
			fastAnimationSpeed = 180;
			break;
			
		case 75:
			slowAnimationSpeed = 300;
			fastAnimationSpeed = 150;
			break;			
			
		case 100:
			slowAnimationSpeed = 150;
			fastAnimationSpeed = 120;
			break;
			
		default:
			break;
		}
		
	}
	
	/**
	 * Spuští další animace po skončení předchozích, aby na sebe navazovaly
	 */
	private void nextAnimation() {
		if (indexAnimation >= recordOfAnimations.size()) {
			balanceTree();
			//if (isRedraw) {				
			//	redraw();
			//}			
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
			
		case RR:
			rrAnimation();
			break;
			
		case LL:
			llAnimation();
			break;
			
		case RL:
			rlAnimation();
			break;
			
		case LR:
			lrAnimation();
			break;
			
		case UPDATEFACTOR:
			updateFactor();
			break;
			
		case RECOLOR: 
			reColor();
			break;
			
		case DOUBLEBLACK:
			doubleBlack();
			break;
		
		case SETDOUBLEBLACK:
			setDoublBlack();
			break;
			
		case REDBLACKINFO:
			redBlackDeleteInfo();
			break;
			
		default:
			break;
		}	
	}	

	/**
	 * Výpis pro ččstrom 
	 */
	private void redBlackDeleteInfo() {
		switch ((int)recordOfAnimations.get(indexAnimation).getObject()) {
		case 1:
			appendNewText("\n • Sourozenec označeného je černý,\n  navíc má červeného následníka.");
			break;
			
		case 2:
			appendNewText("\n • Sourozenec označeného je černý,\nale nemá červeného následníka.\n"
					+ "  označený má červeného předchůdce.");
			appendNewText("\n • Předchůdce získá černou barvu,\n  sourozenec červenou.");
			
			break;
			
		case 3: 
			appendNewText("\n • Sourozenec označeného je černý,\nale nemá červeného následníka.\n"
					+ "  označený má černého předchůdce.");
			appendNewText("\n • Předchůdce získá dvojité označení,\n  sourozenec červenou.");
			break;
			
		case 4: 
			appendNewText("\n • Sourozenec označeného je červený.");
			
		case 5: 
			balanceTree();
			return;
		default:
			break;
		}
		
		indexAnimation++;
		nextAnimation();		
	}

	/**
	 * Zavolá znovu metodu highlightNode pro každý list zvlášť
	 */
	private void nextSearchNode() {		
		highlightNodeAnimation(wayList.get(wayIndex));
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
		SequentialTransition seqT2 = null;
		
		if(node.getBranch() != null) {
			st1 = new StrokeTransition(Duration.millis(slowAnimationSpeed),(Line) node.getBranch(), Color.WHITE, Color.LIME);
			pt1 = new PauseTransition(Duration.millis(1.5 * fastAnimationSpeed));
			st2 = new StrokeTransition(Duration.millis(slowAnimationSpeed), (Line) node.getBranch(), Color.LIME, Color.WHITE);
		}
		
		StrokeTransition st3 = new StrokeTransition(Duration.millis(slowAnimationSpeed), node.getCircleShape(), Color.WHITE, Color.LIME);
		PauseTransition pt2 = new PauseTransition(Duration.millis(4 * fastAnimationSpeed));
		StrokeTransition st4 = new StrokeTransition(Duration.millis(slowAnimationSpeed), node.getCircleShape(), Color.LIME, Color.WHITE);
		
		if(node.getBranch() != null) {
			seqT2 = new SequentialTransition(st1, pt1, st2);
			seqT = new SequentialTransition(st3, pt2, st4);
			int oldValue = Integer.parseInt(wayList.get(wayIndex-1).getValue());
			
			if (value > oldValue) {
				text.setText("HLEDÁNÍ PRVKU: " + value + "\n • Porovnání " + value + " > " + oldValue + "\n" + oldText);
				oldText = (" • Porovnání " + value + " > " + oldValue + "\n" + oldText);				
			} else if (value < oldValue) {
				text.setText("HLEDÁNÍ PRVKU: " + value + "\n • Porovnání " + value + " < " + oldValue + "\n" + oldText);
				oldText = (" • Porovnání " + value + " < " + oldValue + "\n" + oldText);
			}
			
			seqT2.play();
		} else {
			seqT = new SequentialTransition(st3, pt2, st4);
			oldText = text.getText();
			text.setText("HLEDÁNÍ PRVKU: " + value + "\n • Porovnání " + value + " a " + node.getValue() + "\n" + oldText);
			seqT.play();
		}
		
		seqT.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (++wayIndex < wayList.size()) {
					nextSearchNode();
				} else {				
					if (value > Integer.parseInt(node.getValue())) {
						text.setText("HLEDÁNÍ PRVKU: " + value + "\n • Porovnání " + value + " > " + node.getValue() + "\n" + oldText);
						oldText = (" • Porovnání " + value + " > " + node.getValue() + "\n" + oldText);
					} else if (value < Integer.parseInt(node.getValue())) {
						text.setText("HLEDÁNÍ PRVKU: " + value + "\n • Porovnání " + value + " < " + node.getValue() + "\n" + oldText);
						oldText = (" • Porovnání " + value + " < " + node.getValue() + "\n" + oldText);
					} else {
						text.setText("HLEDÁNÍ PRVKU: " + value + "\n • Porovnání " + value + " = " + node.getValue() + "\n" + oldText);
						oldText = (" • Porovnání " + value + " = " + node.getValue() + "\n" + oldText);
					}
					highlightNodeAnimationFinished();
				}
			}
		});
		
		if (seqT2 != null) {
			seqT2.setOnFinished(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {		
					text.setText("HLEDÁNÍ PRVKU: " + value + "\n • Porovnání " + value + " a " + node.getValue() + "\n" + oldText);
					seqT.play();
				}
			});
		}		
		
		if(node.getBranch() != null) {
			seqT2.play();
		} else {
			seqT.play();
		}		
	}
	
	private void highlightNodeAnimationFinished() {
		if(!notFind && ((boolean)recordOfAnimations.get(indexAnimation).getObject())) {
			if (animationSpeed.get() == 0) { //když nebude animace
				text.setText("HLEDÁNÍ PRVKU: " + value + "\n • Prvek byl nalezen!");
			} else {
				text.setText("HLEDÁNÍ PRVKU: " + value +"\n • Prvek byl nalezen!" + "\n" + oldText);
			}
			
			highlightFindNode();
		} else {
			if (animationSpeed.get() == 0) { //když nebude animace
				text.setText("HLEDÁNÍ PRVKU: " + value + "\n • Prvek nebyl nalezen!");
			} else {
				text.setText("HLEDÁNÍ PRVKU: " + value +"\n • Prvek nebyl nalezen." + "\n"+ oldText);
			}
			if (notFind) {
				windowController.enableButtons();
				return;
			}
			indexAnimation++;
			nextAnimation();
		}		
	}
	
	/**
	 * Zvýrazní nalezený list
	 */
	private void highlightFindNode() {
		StrokeTransition st3 = new StrokeTransition(Duration.millis(slowAnimationSpeed), wayList.get(wayList.size() - 1).getCircleShape(), Color.WHITE, Color.YELLOW);
		//PauseTransition pt2 = new PauseTransition(Duration.millis(10 * (SLOWANIMATION - 50 - animationSpeed.get())));
		PauseTransition pt2 = new PauseTransition(Duration.millis(3 * (slowAnimationSpeed - 50 - animationSpeed.get())));
		StrokeTransition st4 = new StrokeTransition(Duration.millis(slowAnimationSpeed), wayList.get(wayList.size() - 1).getCircleShape(), Color.YELLOW, Color.WHITE);

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
	
	/**
	 * po vložení odbarví node
	 */
	private void highlightInsertNode() {
		if (!isInsertAnimation) { 
			indexAnimation++;
			nextAnimation();
			newIGraphicNode.setDefaultColorNode();
			return;
		}
		PauseTransition pt2 = new PauseTransition(Duration.millis(slowAnimationSpeed));
		StrokeTransition st4 = new StrokeTransition(Duration.millis(slowAnimationSpeed), newIGraphicNode.getCircleShape(), Color.YELLOW, Color.WHITE);

		SequentialTransition seqT = new SequentialTransition(pt2, st4);

		seqT.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				indexAnimation++;
				nextAnimation();
				newIGraphicNode.setDefaultColorNode();
			}
		});

		seqT.play();
	}
	
	/**
	 * Animace vložení nového listu
	 */
	private void insertNodeAnimation() {
		oldText = text.getText();
		
		if (animationSpeed.get() == 0) {	
			if (newIGraphicNode.getSide() == Side.LEFT) {
				setTextWithHistory("VLOŽENÍ PRVKU " + newIGraphicNode.getValue()+": \n • Prvek "+ newIGraphicNode.getValue() + " < rodič "
						+ newIGraphicNode.getParent().getValue() + ".\n • Bude vložen VLEVO.");
			} else if (newIGraphicNode.getSide() == Side.RIGHT) {
				setTextWithHistory("VLOŽENÍ PRVKU " + newIGraphicNode.getValue()+": \n • Prvek "+ newIGraphicNode.getValue() + " > rodič "
						+ newIGraphicNode.getParent().getValue() + ".\n • Bude vložen VPRAVO.");
			}
			
			insertNodeAnimationFinished();
			return;
		}
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(4 * fastAnimationSpeed),
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
		
		if (newIGraphicNode.getSide() == Side.LEFT) {
			setTextWithHistory("VLOŽENÍ PRVKU " + newIGraphicNode.getValue()+": \n • Prvek "+ newIGraphicNode.getValue() + " < rodič "
					+ newIGraphicNode.getParent().getValue() + ".\n • Bude vložen VLEVO.");
		} else if (newIGraphicNode.getSide() == Side.RIGHT) {
			setTextWithHistory("VLOŽENÍ PRVKU " + newIGraphicNode.getValue()+": \n • Prvek "+ newIGraphicNode.getValue() + " > rodič "
					+ newIGraphicNode.getParent().getValue() + ".\n • Bude vložen VPRAVO.");
		}

		timeline.play();
	}
	
	private void insertNodeAnimationFinished() {
		newIGraphicNode.setX(xAnimatedNode);
		newIGraphicNode.setY(yAnimatedNode);
		
		createBranch(newIGraphicNode);
		insertBranch();		
		listGraphicNodes.add(newIGraphicNode);

		appendNewText("\n • Vložení proběhlo úspěšně.");	
		
		highlightInsertNode();		
	}
	
	/**
	 * Smazání listu animace
	 */
	private void deleteNodeAnimation() {
		IGraphicNode node = wayList.get(wayList.size() - 1);
		oldText = text.getText();
		
		node.highlightFindNode(); // zvýrazním mazaný node
		setTextWithHistory("MAZÁNÍ PRVKU "+ node.getValue() + ":");
		if ((boolean) recordOfAnimations.get(indexAnimation).getObject()) { //pokud má děti
			appendNewText("\n • Mazaný list má potomky.");
			
			node.setValue("");
			indexAnimation++;
			nextAnimation();
		} else {	
			appendNewText("\n • Mazaný je list.");
			if (animationSpeed.get() == 0) { //neni animace
				deleteNodeAnimationFinished(node);
				return;
			}
			
			FadeTransition fadeTransitionNode = new FadeTransition(Duration.millis(10 * fastAnimationSpeed), node.getStackPaneNode());
			fadeTransitionNode.setFromValue(1.0);
			fadeTransitionNode.setToValue(0.0);			
			
			FadeTransition fadeTransitionBranch = new FadeTransition(Duration.millis(10 * fastAnimationSpeed), node.getBranch());
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
		appendNewText("\n • Smazání proběhlo úspěšně.");
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
			return;
		}		
		
		graphicNodeMoved.highlightNode();
		graphicNodeMoved.getStackPaneNode().toFront();
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(5 * fastAnimationSpeed),
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
		
		if (!(isMoveNode)) {
			appendNewText("\n • Mazaný uzel nahradí:");
			text.appendText("");
			if (graphicNodeRemoved.getRight() == null || graphicNodeRemoved.getLeft() == null) {
				if (graphicNodeMoved.getX().get() <= graphicNodeRemoved.getX().get()) {
					appendNewText("\n\tLEVÝ potomek " + graphicNodeMoved.getValue());
				} else {
					appendNewText("\n\tPRAVÝ potomek " + graphicNodeMoved.getValue());
				}
			} else if(graphicNodeRemoved.getRight() != null || graphicNodeRemoved.getLeft() != null) {
				appendNewText("\n\tNEJLEVĚJŠÍ potomek "+  graphicNodeMoved.getValue() + "\n\tz PRAVÉHO podstromu");			
			}	
		}
		
		//odstraním větve
		hideMovedBranchRecursive(graphicNodeMoved);

		timeline.play();				
	}
	
	private void moveNodeAnimationFinished(IGraphicNode graphicNodeRemoved, IGraphicNode graphicNodeMoved) {
		checkBranches(); //doplním větve
		
		if (graphicNodeMoved.getLeft() == null && graphicNodeMoved.getRight() == null
				&& !(graphicNodeMoved.getParent().equals(graphicNodeRemoved.getParent()))) { //pokud mají stejného rodiče případ 0.2.1/2
			graphicNodeRemoved.setValue(graphicNodeMoved.getValue());
			graphicNodeRemoved.setDefaultColorNode();

			paneTree.getChildren().remove(graphicNodeMoved.getStackPaneNode());	
			paneTree.getChildren().remove(graphicNodeMoved.getBranch());
			
			//dosadím na místo mazaného ten co ho nahradí
			listGraphicNodes.remove(graphicNodeRemoved);
			listGraphicNodes.add(listGraphicNodes.indexOf(graphicNodeMoved), graphicNodeRemoved);
			listGraphicNodes.remove(graphicNodeMoved);
			
		} else {	
			if (graphicNodeRemoved.getParent() == null) {
				graphicNodeMoved.setX(rootX);
				graphicNodeMoved.setParent(null);
				listGraphicNodes.remove(graphicNodeMoved); //dám roota na první místo
				listGraphicNodes.add(0, graphicNodeMoved);
			}
							
			graphicNodeMoved.setDefaultColorNode();
			graphicNodeMoved.getStackPaneNode().toFront();					

			paneTree.getChildren().remove(graphicNodeRemoved.getStackPaneNode());	
			paneTree.getChildren().remove(graphicNodeMoved.getBranch());
			paneTree.getChildren().remove(graphicNodeRemoved.getBranch());
			createBranch(graphicNodeMoved);
			
			//dosadím na místo mazaného ten co ho nahradí
			listGraphicNodes.remove(graphicNodeMoved);
			listGraphicNodes.add(listGraphicNodes.indexOf(graphicNodeRemoved), graphicNodeMoved);
			listGraphicNodes.remove(graphicNodeRemoved);			
		}
		
		appendNewText("\n • Smazání proběhlo úspěšně.");
		
		indexAnimation++;
		nextAnimation();
	}
	
	/**
	 * Přesune hodnotu do jiného listu
	 */
	private void moveValueAnimation() {
		IGraphicNode graphicNodeRemoved = recordOfAnimations.get(indexAnimation).getNode1();
		IGraphicNode graphicNodeMoved = ((IGraphicNode) recordOfAnimations.get(indexAnimation).getObject());
		
		graphicNodeRemoved.setValue(graphicNodeMoved.getValue());
		graphicNodeMoved.setValue("");
		graphicNodeRemoved.setDefaultColorNode();
		graphicNodeMoved.setDefaultColorNode();
		
		isMoveNode = true;
		
		appendNewText("\n • Mazaný uzel nahradí:");
		text.appendText("");
		
		if (graphicNodeRemoved.getRight() == null || graphicNodeRemoved.getLeft() == null) {
			if (graphicNodeMoved.getX().get() <= graphicNodeRemoved.getX().get()) {
				appendNewText("\n\tLEVÝ potomek " + graphicNodeMoved.getValue());
			} else {
				appendNewText("\n\tPRAVÝ potomek " + graphicNodeMoved.getValue());
			}
		} else if(graphicNodeRemoved.getRight() != null || graphicNodeRemoved.getLeft() != null) {
			appendNewText("\n\tNEJLEVĚJŠÍ potomek "+  graphicNodeMoved.getValue() + "\n\tz PRAVÉHO podstromu");			
		}

		indexAnimation++;
		nextAnimation();			
	}
	
	/**
	 * Aktualizuje ohodnocení listů
	 */
	private void updateFactor() {
		if (!(isBalance) && animationSpeed.get() != 0) {
			indexAnimation--;
			isBalance = true;
			balanceTree();
			return;
		}
		
		isBalance = false;
		
		if (animationSpeed.get() == 0 || (!(boolean)recordOfAnimations.get(indexAnimation).getObject())) { //když nebude animace
			for (IGraphicNode iGraphicNode : listGraphicNodes) {
				((AVLGraphicNode)iGraphicNode).updateFactor();
			}
			indexAnimation++;
			nextAnimation();
			return;
		}
		
		oldText = text.getText();
		
		AVLGraphicNode start = (AVLGraphicNode)recordOfAnimations.get(indexAnimation).getNode1();
		nextUpdateFactor(start);		
	}
	
	private void nextUpdateFactor(AVLGraphicNode node) {				
		SequentialTransition seqT;
		
		if (node == null) {
			appendNewText("\n • Strom je vyvážený.");
			indexAnimation++;
			nextAnimation();
			return;
		}
		
		StrokeTransition st3 = new StrokeTransition(Duration.millis(0.7 * slowAnimationSpeed), node.getCircleShape(), Color.WHITE, Color.LIME);
		PauseTransition pt2 = new PauseTransition(Duration.millis(4 * fastAnimationSpeed));
		StrokeTransition st4 = new StrokeTransition(Duration.millis(0.7 * slowAnimationSpeed), node.getCircleShape(), Color.LIME, Color.WHITE);
		
		setTextWithHistory("VÝPOČET FAKTORU VYVÁŽENÍ:");
		
		seqT = new SequentialTransition(st3, pt2, st4);		
		
		seqT.play();
		
		seqT.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				node.updateFactor();
				int factor = Integer.parseInt(node.getFactor().getText().toString());
				
				if (factor == -2 || factor == 2) {
					node.highlightFindNode();
					appendNewText("\n • Strom je nevyvážený.");
					indexAnimation++;
					nextAnimation();
				} else {
					if(node.getBranch() != null) {
						StrokeTransition st1 = new StrokeTransition(Duration.millis(slowAnimationSpeed),(Line) node.getBranch(), Color.WHITE, Color.LIME);
						PauseTransition pt1 = new PauseTransition(Duration.millis(5 * (fastAnimationSpeed - animationSpeed.get())));
						StrokeTransition st2 = new StrokeTransition(Duration.millis(slowAnimationSpeed), (Line) node.getBranch(), Color.LIME, Color.WHITE);
						SequentialTransition seqT2 = new SequentialTransition(st1, pt1, st2);
						
						seqT2.setOnFinished(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {		
								nextUpdateFactor((AVLGraphicNode)node.getParent());
							}
						});
						
						seqT2.play();
					} else {
						appendNewText("\n • Strom je vyvážený.");
						indexAnimation++;
						nextAnimation();
					}					
				}				
			}
		});			
	}	

	/**
	 * Změní barvu u vybraného node
	 */
	private void reColor() {
		isRedBlack = true;
		if (!(isReColor) && nullNode == null) {		
			oldText = text.getText();
			setTextWithHistory("PŘEBARVENÍ STROMU:");
			isReColor = true;
		}
		
		RedBlackGraphicNode node = (RedBlackGraphicNode) recordOfAnimations.get(indexAnimation).getNode1();
		Trees.Color color = (Trees.Color) recordOfAnimations.get(indexAnimation).getObject();		
		
		node.setColor(color);		
				
		indexAnimation++;
		if (indexAnimation >= recordOfAnimations.size()) {
			appendNewText("\n • Přebarvení proběhlo úspěšně.");
		}
		nextAnimation();
	}
	
	/**
	 * Doplní dvojtě černý uzel
	 */
	private void doubleBlack() {
	/*	if (!(isBalance) && animationSpeed.get() != 0) {
			indexAnimation--;
			isBalance = true;
			balanceTree();
			return;
		}*/
		isRedBlack = true;
		oldText = text.getText();
		setTextWithHistory("VYVÁŽENÍ STROMU:");
		appendNewText("\n • Smazaný list byl černý.");		
		
		xAnimatedNode = new SimpleDoubleProperty(); //souřadnice x listu	
		yAnimatedNode = new SimpleDoubleProperty();		
		
		nullNode = new RedBlackGraphicNode(-1);			
		
		if (((Side)recordOfAnimations.get(indexAnimation).getObject()) == Side.LEFT) {
			xAnimatedNode.bind(recordOfAnimations.get(indexAnimation).getNode1().getX().subtract(rootSize));
			recordOfAnimations.get(indexAnimation).getNode1().setLeft(nullNode);
		} else {
			xAnimatedNode.bind(recordOfAnimations.get(indexAnimation).getNode1().getX().add(rootSize));	
			recordOfAnimations.get(indexAnimation).getNode1().setRight(nullNode);
		}
		
		yAnimatedNode.bind(recordOfAnimations.get(indexAnimation).getNode1().getY().add(DOWNMARGIN));
		
		nullNode.setX(xAnimatedNode); 
		nullNode.setY(yAnimatedNode);
		
		nullNode.setColor(Trees.Color.BLACK);
		nullNode.doubleBlackHighlight();
		
		nullNode.setParent(recordOfAnimations.get(indexAnimation).getNode1());
		createBranch(nullNode);		
		
		paneTree.getChildren().addAll(nullNode.getBranch(), nullNode.getStackPaneNode());	
		nullNode.getParent().getStackPaneNode().toFront();		
		
		if (animationSpeed.get() == 0) {
			indexAnimation++;
			nextAnimation();
			return;
		}
		
		PauseTransition pt2 = new PauseTransition(Duration.millis(slowAnimationSpeed));

		SequentialTransition seqT = new SequentialTransition(pt2);

		seqT.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				indexAnimation++;
				nextAnimation();
				return;
			}
		});	
		
		seqT.play();
	}
	
	/**
	 * Odstraní NULL list případně přesune označení na jiný list
	 */
	private void setDoublBlack() {
		RedBlackGraphicNode node;
		if ((recordOfAnimations.get(indexAnimation).getNode1() != null) && recordOfAnimations.get(indexAnimation).getObject() == null) {
			node = (RedBlackGraphicNode) recordOfAnimations.get(indexAnimation).getNode1();
			nullNode.removeDoubleBlackHighlight();
			appendNewText("\n • Přesunu označení.");
			if (nullNode.getValue().equals("NULL")) {
				paneTree.getChildren().remove(nullNode.getStackPaneNode());
				paneTree.getChildren().remove(nullNode.getBranch());
				if (nullNode.getSide() == Side.LEFT) {
					nullNode.getParent().setLeft(null);
				} else {
					nullNode.getParent().setRight(null);
				}
			}			
			
			nullNode = node;	
			nullNode.doubleBlackHighlight();
			
			if (animationSpeed.get() == 0) {
				indexAnimation++;
				nextAnimation();
				return;
			}
			
			PauseTransition pt2 = new PauseTransition(Duration.millis(slowAnimationSpeed));

			SequentialTransition seqT = new SequentialTransition(pt2);

			seqT.setOnFinished(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					indexAnimation++;
					nextAnimation();
					return;
				}
			});		

			seqT.play();
		} else {
			if (nullNode != null && nullNode.getValue().equals("NULL")) {
				appendNewText("\n • Odstraním NULL list.");
				
				if (animationSpeed.get() == 0) { 
					paneTree.getChildren().remove(nullNode.getStackPaneNode());
					paneTree.getChildren().remove(nullNode.getBranch());
					if (nullNode.getSide() == Side.LEFT) {
						nullNode.getParent().setLeft(null);
					} else {
						nullNode.getParent().setRight(null);
					}
					nullNode = null;					
					
					indexAnimation++;
					nextAnimation();
					return;
				}
				
				FadeTransition fadeTransitionNode = new FadeTransition(Duration.millis(10 * fastAnimationSpeed), nullNode.getStackPaneNode());
				fadeTransitionNode.setFromValue(1.0);
				fadeTransitionNode.setToValue(0.0);			
				
				FadeTransition fadeTransitionBranch = new FadeTransition(Duration.millis(10 * fastAnimationSpeed), nullNode.getBranch());
				fadeTransitionBranch.setFromValue(1.0);
				fadeTransitionBranch.setToValue(0.0);

				fadeTransitionBranch.play();
				fadeTransitionNode.play();
				
				fadeTransitionNode.setOnFinished(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {	
						paneTree.getChildren().remove(nullNode.getStackPaneNode());
						paneTree.getChildren().remove(nullNode.getBranch());
						if (nullNode.getSide() == Side.LEFT) {
							nullNode.getParent().setLeft(null);
						} else {
							nullNode.getParent().setRight(null);
						}
						nullNode = null;
						
						indexAnimation++;
						nextAnimation();
					}
				});
				
				return;				
								
			} else if (recordOfAnimations.get(indexAnimation).getObject() != null) {				
				appendNewText("\n • Kořen je vždy černý.");				
				nullNode.removeDoubleBlackHighlight();
				
				if (nullNode.getValue().equals("NULL")) {
					paneTree.getChildren().remove(nullNode.getStackPaneNode());
					paneTree.getChildren().remove(nullNode.getBranch());
					if (nullNode.getSide() == Side.LEFT) {
						nullNode.getParent().setLeft(null);
					} else {
						nullNode.getParent().setRight(null);
					}
				}
				
				indexAnimation++;
				nextAnimation();
			} else {
				appendNewText("\n • Odstraním označení listu.");
				nullNode.removeDoubleBlackHighlight();
				
				if (nullNode.getValue().equals("NULL")) {
					paneTree.getChildren().remove(nullNode.getStackPaneNode());
					paneTree.getChildren().remove(nullNode.getBranch());
					if (nullNode.getSide() == Side.LEFT) {
						nullNode.getParent().setLeft(null);
					} else {
						nullNode.getParent().setRight(null);
					}
				}
				
				indexAnimation++;
				nextAnimation();
			}
		}				
	}
	
	private void rrAnimation() {		
		IGraphicNode nodeB = recordOfAnimations.get(indexAnimation).getNode1();
		IGraphicNode nodeA = nodeB.getLeft();
		
		if (animationSpeed.get() == 0) {
			oldText = text.getText();
			if (!isRedBlack) {
				setTextWithHistory("VYVÁŽENÍ STROMU:");
			} else if (!(isReColor)) {
				setTextWithHistory("PŘEBARVENÍ STROMU:");
			}
			appendNewText("\n • Rotace RR.");
			hideMovedBranchRecursive(nodeB);
			rrAnimationFinished(nodeA, nodeB);
			return;
		}		
		
		nodeA.highlightNode();	
		
		if (isRedBlack) {
			nodeA.getLeft().highlightNode();
		}
		
		appendNewText("\n • Rotace RR.");
		
		xAnimatedNode = new SimpleDoubleProperty(nodeB.getX().get() + rootSize);	
		yAnimatedNode = new SimpleDoubleProperty(nodeB.getY().get() + DOWNMARGIN);		
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(5 * fastAnimationSpeed),
				new KeyValue(nodeA.getX(), nodeB.getX().get()),
				new KeyValue(nodeA.getY(), nodeB.getY().get()),
				new KeyValue(nodeB.getX(), xAnimatedNode.get()),
				new KeyValue(nodeB.getY(), yAnimatedNode.get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				rrAnimationFinished(nodeA, nodeB);
			}
		});
		
		if (nodeA.getRight() != null) {
			nodeA.getRight().getX().unbind();
			nodeA.getRight().getY().unbind();
		}
		
		nodeA.getX().unbind();
		nodeA.getY().unbind();
		
		nodeB.getX().unbind();
		nodeB.getY().unbind();
		
		hideMovedBranchRecursive(nodeB);
		timeline.play();		
	}
	
	private void rrAnimationFinished(IGraphicNode nodeA, IGraphicNode nodeB) {	
		if (!isRedBlack) {
			((AVLNode)recordOfAnimations.get(indexAnimation).getObject()).countFactor();
		}		
		
		nodeB.setLeft(nodeA.getRight());
		
		nodeA.setParent(nodeB.getParent());
		nodeA.setSide(nodeB.getSide());
		if (nodeA.getSide() == Side.LEFT) {
			nodeA.getParent().setLeft(nodeA);
		} else if(nodeA.getSide() == Side.RIGHT) {			
			nodeA.getParent().setRight(nodeA);
		}
		
		nodeA.setRight(nodeB);	
		
		if (nodeA.getParent() == null) {
			xAnimatedNode.bind(rootX);
			yAnimatedNode.bind(rootY);
			
			paneTree.getChildren().remove(nodeA.getBranch());
			nodeA.setBranch(null);			
			
			createBranch(nodeB);
			paneTree.getChildren().remove(nodeB.getBranch());
		} else {
			if (nodeA.getSide() == Side.LEFT) {
				xAnimatedNode.bind(nodeA.getParent().getX().subtract(rootSize));
			} else if(nodeA.getSide() == Side.RIGHT) {			
				xAnimatedNode.bind(nodeA.getParent().getX().add(rootSize));
			}		
			//nodeA.setX(xAnimatedNode);
			//nodeA.setY(yAnimatedNode);
		}
		
		//xAnimatedBranch.bind(nodeB.getParent().getX().add(rootSize));
		//yAnimatedBranch.bind(nodeB.getParent().getY().add(DOWNMARGIN));
		//nodeB.setX(xAnimatedBranch);
		//nodeB.setY(yAnimatedBranch);
		
		nodeA.setDefaultColorNode();
		nodeB.setDefaultColorNode();
		
		if (isRedBlack) {
			nodeA.getLeft().setDefaultColorNode();
		}
		
		listGraphicNodes.remove(nodeA); //posunu list A před B
		listGraphicNodes.add(listGraphicNodes.indexOf(nodeB), nodeA);
		
		if (!isRedBlack) {
			appendNewText("\n • Rotace dokončena. \n • Vyvážení proběhlo úspěšně.");
			
			recordOfAnimations.add(new RecordOfAnimation(AnimatedAction.UPDATEFACTOR, null, false));
		} else {
			if (nullNode == null) {
				appendNewText("\n • Rotace dokončena.");
			}						
		}		
		
		indexAnimation++;
		nextAnimation();
	}	
	
	private void rlAnimation() {
		IGraphicNode nodeC = recordOfAnimations.get(indexAnimation).getNode1();
		IGraphicNode nodeA = nodeC.getLeft();
		IGraphicNode nodeB = nodeA.getRight();
		
		if (animationSpeed.get() == 0) {
			oldText = text.getText();
			if (!isRedBlack) {
				setTextWithHistory("VYVÁŽENÍ STROMU:");
			} else if (!(isReColor)) {
				setTextWithHistory("PŘEBARVENÍ STROMU:");
			}
			appendNewText("\n • Rotace RL.");
			hideMovedBranchRecursive(nodeC);
			rlAnimationFinished(nodeA, nodeB, nodeC);
			return;
		}		
		
		nodeA.highlightNode();	
		nodeB.highlightNode();
		
		appendNewText("\n • Rotace RL.");
		
		xAnimatedNode = new SimpleDoubleProperty(nodeC.getX().get() + rootSize);	
		yAnimatedNode = new SimpleDoubleProperty(nodeC.getY().get() + DOWNMARGIN);		
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(5 * fastAnimationSpeed),
				new KeyValue(nodeB.getX(), nodeC.getX().get()),
				new KeyValue(nodeB.getY(), nodeC.getY().get()),
				new KeyValue(nodeC.getX(), xAnimatedNode.get()),
				new KeyValue(nodeC.getY(), yAnimatedNode.get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				rlAnimationFinished(nodeA, nodeB, nodeC);
			}
		});
		
		if (nodeB.getRight() != null) {
			nodeB.getRight().getX().unbind();
			nodeB.getRight().getY().unbind();
		}
		
		if (nodeB.getLeft() != null) {
			nodeB.getLeft().getX().unbind();
			nodeB.getLeft().getY().unbind();
		}
		
		nodeC.getX().unbind();
		nodeC.getY().unbind();
		
		nodeB.getX().unbind();
		nodeB.getY().unbind();
		
		nodeA.getX().unbind();
		nodeA.getY().unbind();		
		
		hideMovedBranchRecursive(nodeC);
		
		timeline.play();		
	}
	
	private void rlAnimationFinished(IGraphicNode nodeA, IGraphicNode nodeB, IGraphicNode nodeC) {
		if (!isRedBlack) {
			((AVLNode)recordOfAnimations.get(indexAnimation).getObject()).countFactor();
		}
		
		nodeA.setRight(nodeB.getLeft());
		nodeC.setLeft(nodeB.getRight());
		
		nodeB.setLeft(nodeA);		
		
		nodeB.setParent(nodeC.getParent());
		nodeB.setSide(nodeC.getSide());
		if (nodeB.getSide() == Side.LEFT) {
			nodeB.getParent().setLeft(nodeB);
		} else if(nodeB.getSide() == Side.RIGHT) {			
			nodeB.getParent().setRight(nodeB);
		}
		
		nodeB.setRight(nodeC);	
		
		if (nodeB.getParent() == null) {
			xAnimatedNode.bind(rootX);
			yAnimatedNode.bind(rootY);
			
			paneTree.getChildren().remove(nodeA.getBranch());
			nodeB.setBranch(null);			
			
			createBranch(nodeC);
			paneTree.getChildren().remove(nodeC.getBranch());
		} else {
			if (nodeB.getSide() == Side.LEFT) {
				xAnimatedNode.bind(nodeB.getParent().getX().subtract(rootSize));
			} else if(nodeB.getSide() == Side.RIGHT) {			
				xAnimatedNode.bind(nodeB.getParent().getX().add(rootSize));
			}			
			//nodeB.setX(xAnimatedNode);
			//nodeB.setY(yAnimatedNode);
		}				
		
		
		//xAnimatedBranch.bind(nodeC.getParent().getX().add(rootSize));
		//yAnimatedBranch.bind(nodeC.getParent().getY().add(DOWNMARGIN));
		//nodeC.setX(xAnimatedBranch);
		//nodeC.setY(yAnimatedBranch);
		
		nodeA.setDefaultColorNode();
		nodeB.setDefaultColorNode();
		nodeC.setDefaultColorNode();
		
		listGraphicNodes.remove(nodeB); //posunu list B před C
		listGraphicNodes.add(listGraphicNodes.indexOf(nodeC), nodeB);
		
		if (!isRedBlack) {
			appendNewText("\n • Rotace dokončena. \n • Vyvážení proběhlo úspěšně.");
			
			recordOfAnimations.add(new RecordOfAnimation(AnimatedAction.UPDATEFACTOR, null, false));
		} else {
			if (nullNode == null) {
				appendNewText("\n • Rotace dokončena.");
			}			
		}
		
		indexAnimation++;
		nextAnimation();		
	}

	private void llAnimation() {
		IGraphicNode nodeB = recordOfAnimations.get(indexAnimation).getNode1();
		IGraphicNode nodeA = nodeB.getRight();
		
		if (animationSpeed.get() == 0) {
			oldText = text.getText();
			if (!isRedBlack) {
				setTextWithHistory("VYVÁŽENÍ STROMU:");
			} else if (!(isReColor)) {
				setTextWithHistory("PŘEBARVENÍ STROMU:");
			}
			appendNewText("\n • Rotace LL.");
			hideMovedBranchRecursive(nodeB);
			llAnimationFinished(nodeA, nodeB);
			return;
		}		
		
		nodeA.highlightNode();	
		
		if (isRedBlack) {
			nodeA.getRight().highlightNode();
		}
		
		appendNewText("\n • Rotace LL.");
		
		xAnimatedNode = new SimpleDoubleProperty(nodeB.getX().get() - rootSize);	
		yAnimatedNode = new SimpleDoubleProperty(nodeB.getY().get() + DOWNMARGIN);
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(5 * fastAnimationSpeed),
				new KeyValue(nodeA.getX(), nodeB.getX().get()),
				new KeyValue(nodeA.getY(), nodeB.getY().get()),
				new KeyValue(nodeB.getX(), xAnimatedNode.get()),
				new KeyValue(nodeB.getY(), yAnimatedNode.get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				llAnimationFinished(nodeA, nodeB);
			}
		});
		
		if (nodeA.getLeft() != null) {
			nodeA.getLeft().getX().unbind();
			nodeA.getLeft().getY().unbind();
		}
		
		nodeA.getX().unbind();
		nodeA.getY().unbind();
		
		nodeB.getX().unbind();
		nodeB.getY().unbind();		
		
		hideMovedBranchRecursive(nodeB);
		timeline.play();		
	}
	
	private void llAnimationFinished(IGraphicNode nodeA, IGraphicNode nodeB) {
		if (!isRedBlack) {
			((AVLNode)recordOfAnimations.get(indexAnimation).getObject()).countFactor();
		}
		
		nodeB.setRight(nodeA.getLeft());
		
		nodeA.setParent(nodeB.getParent());
		nodeA.setSide(nodeB.getSide());
		if (nodeA.getSide() == Side.LEFT) {
			nodeA.getParent().setLeft(nodeA);
		} else if(nodeA.getSide() == Side.RIGHT) {			
			nodeA.getParent().setRight(nodeA);
		}
		
		nodeA.setLeft(nodeB);	
		
		if (nodeA.getParent() == null) {
			xAnimatedNode.bind(rootX);
			yAnimatedNode.bind(rootY);
			
			paneTree.getChildren().remove(nodeA.getBranch());
			nodeA.setBranch(null);			
			
			createBranch(nodeB);
			paneTree.getChildren().remove(nodeB.getBranch());
		} else {
			if (nodeA.getSide() == Side.LEFT) {
				xAnimatedNode.bind(nodeA.getParent().getX().subtract(rootSize));
			} else if(nodeA.getSide() == Side.RIGHT) {			
				xAnimatedNode.bind(nodeA.getParent().getX().add(rootSize));
			}				
			//nodeA.setX(xAnimatedNode);
			//nodeA.setY(yAnimatedNode);
		}
		
		//xAnimatedBranch.bind(nodeB.getParent().getX().subtract(rootSize));
		//yAnimatedBranch.bind(nodeB.getParent().getY().add(DOWNMARGIN));
		//nodeB.setX(xAnimatedBranch);
		//nodeB.setY(yAnimatedBranch);
		
		nodeA.setDefaultColorNode();
		nodeB.setDefaultColorNode();
		
		if (isRedBlack) {
			nodeA.getRight().setDefaultColorNode();
		}
		
		listGraphicNodes.remove(nodeA); //posunu list A před B
		listGraphicNodes.add(listGraphicNodes.indexOf(nodeB), nodeA);
		
		if (!isRedBlack) {
			appendNewText("\n • Rotace dokončena. \n • Vyvážení proběhlo úspěšně.");
			
			recordOfAnimations.add(new RecordOfAnimation(AnimatedAction.UPDATEFACTOR, null, false));
		} else {
			if (nullNode == null) {
				appendNewText("\n • Rotace dokončena.");
			}			
		}
		
		indexAnimation++;
		nextAnimation();		
	}

	private void lrAnimation() {
		IGraphicNode nodeC = recordOfAnimations.get(indexAnimation).getNode1();
		IGraphicNode nodeA = nodeC.getRight();
		IGraphicNode nodeB = nodeA.getLeft();
		
		if (animationSpeed.get() == 0) {
			oldText = text.getText();
			if (!isRedBlack) {
				setTextWithHistory("VYVÁŽENÍ STROMU:");
			} else if (!(isReColor)) {
				setTextWithHistory("PŘEBARVENÍ STROMU:");
			}
			appendNewText("\n • Rotace LR.");
			hideMovedBranchRecursive(nodeC);
			lrAnimationFinished(nodeA, nodeB, nodeC);
			return;
		}		
		
		nodeA.highlightNode();	
		nodeB.highlightNode();
		
		appendNewText("\n • Rotace LR.");
		
		xAnimatedNode = new SimpleDoubleProperty(nodeC.getX().get() - rootSize);	
		yAnimatedNode = new SimpleDoubleProperty(nodeC.getY().get() + DOWNMARGIN);		
		
		Timeline timeline = new Timeline();

		KeyFrame kf = new KeyFrame(Duration.millis(5 * fastAnimationSpeed),
				new KeyValue(nodeB.getX(), nodeC.getX().get()),
				new KeyValue(nodeB.getY(), nodeC.getY().get()),
				new KeyValue(nodeC.getX(), xAnimatedNode.get()),
				new KeyValue(nodeC.getY(), yAnimatedNode.get()));

		timeline.getKeyFrames().add(kf);

		timeline.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				lrAnimationFinished(nodeA, nodeB, nodeC);
			}
		});
		
		if (nodeB.getRight() != null) {
			nodeB.getRight().getX().unbind();
			nodeB.getRight().getY().unbind();
		}
		
		if (nodeB.getLeft() != null) {
			nodeB.getLeft().getX().unbind();
			nodeB.getLeft().getY().unbind();
		}
		
		nodeC.getX().unbind();
		nodeC.getY().unbind();
		
		nodeB.getX().unbind();
		nodeB.getY().unbind();
		
		nodeA.getX().unbind();
		nodeA.getY().unbind();		
		
		hideMovedBranchRecursive(nodeC);
		
		timeline.play();		
	}
	
	private void lrAnimationFinished(IGraphicNode nodeA, IGraphicNode nodeB, IGraphicNode nodeC) {		
		if (!isRedBlack) {
			((AVLNode)recordOfAnimations.get(indexAnimation).getObject()).countFactor();
		}
		
		nodeA.setLeft(nodeB.getRight());
		nodeC.setRight(nodeB.getLeft());
		
		nodeB.setRight(nodeA);		
		
		nodeB.setParent(nodeC.getParent());
		nodeB.setSide(nodeC.getSide());
		if (nodeB.getSide() == Side.LEFT) {
			nodeB.getParent().setLeft(nodeB);
		} else if(nodeB.getSide() == Side.RIGHT) {			
			nodeB.getParent().setRight(nodeB);
		}
		
		nodeB.setLeft(nodeC);	
		
		if (nodeB.getParent() == null) {
			xAnimatedNode.bind(rootX);
			yAnimatedNode.bind(rootY);
			
			paneTree.getChildren().remove(nodeA.getBranch());
			nodeB.setBranch(null);			
			
			createBranch(nodeC);
			paneTree.getChildren().remove(nodeC.getBranch());
		} else {
			if (nodeB.getSide() == Side.LEFT) {
				xAnimatedNode.bind(nodeB.getParent().getX().subtract(rootSize));
			} else if(nodeB.getSide() == Side.RIGHT) {			
				xAnimatedNode.bind(nodeB.getParent().getX().add(rootSize));
			}			
			yAnimatedNode.bind(nodeB.getParent().getY().add(DOWNMARGIN));			
			//nodeB.setX(xAnimatedNode);
			//nodeB.setY(yAnimatedNode);
		}				
		
		
		//xAnimatedBranch.bind(nodeC.getParent().getX().subtract(rootSize));
		//yAnimatedBranch.bind(nodeC.getParent().getY().add(DOWNMARGIN));
		//nodeC.setX(xAnimatedBranch);
		//nodeC.setY(yAnimatedBranch);
		
		nodeA.setDefaultColorNode();
		nodeB.setDefaultColorNode();
		nodeC.setDefaultColorNode();
		
		listGraphicNodes.remove(nodeB); //posunu list B před C
		listGraphicNodes.add(listGraphicNodes.indexOf(nodeC), nodeB);
		
		if (!isRedBlack) {
			appendNewText("\n • Rotace dokončena. \n • Vyvážení proběhlo úspěšně.");
			
			recordOfAnimations.add(new RecordOfAnimation(AnimatedAction.UPDATEFACTOR, null, false));
		} else {
			if (nullNode == null) {
				appendNewText("\n • Rotace dokončena.");
			}			
		}
		
		indexAnimation++;
		nextAnimation();
	}

	/**
	 * Vymaže text
	 */
	public void clearText() {
		text.clear();
		oldText = "";
	}
	
	/**
	 * Skryje text
	 */
	public void hideText() {
		paneTree.getChildren().remove(text);
	}
	
	/**
	 * Zobrazí text
	 */
	public void showText() {
		paneTree.getChildren().remove(text);
		paneTree.getChildren().add(text);		
	}
	
	/**
	 * Přidá text do TextArea opačně 
	 * řeší to bug se scroll barem
	 * @param s
	 */
	private void setTextWithHistory(String s) {
		newText = s;
		text.setText(newText + "\n\n" + oldText);		
	}
	
	/**
	 * Přidá k nově přidanému textu řetězec (zachovává historii)
	 * @param s
	 */
	private void appendNewText(String s) {
		newText = newText.concat(s);
		text.setText(newText + "\n\n" + oldText);
	}
	
	/********************************************************************************************************
	 * GETS & SETS
	 * 
	 ********************************************************************************************************/
	
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

	public void setAnimation(boolean isAnimation) {
		this.isAnimation = isAnimation;
	}	

	public void setInsertAnimation(boolean isInsertAnimation) {
		this.isInsertAnimation = isInsertAnimation;
	}	
}