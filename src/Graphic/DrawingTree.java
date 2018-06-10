package Graphic;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.DefaultCaret;

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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.util.Duration;


public class DrawingTree {
	private WindowController windowController;
	private List<IGraphicNode> listGraphicNodes = new ArrayList<>(); //root je na místě 0
	private ITree<?> tree;
	private int value;
	private TextArea text;
	private String oldText = "";
	private String newText;

	private ReadOnlyDoubleProperty paneTreeWeight;
	private Pane paneTree;
	private DoubleProperty animationSpeed = new SimpleDoubleProperty();
	
	private final static double ROOTBORDER = 40;	
	private final static double DOWNMARGIN = 40;	
	
	private double rootSize = 0;
	private double stackPaneHeight = 0;
	
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
	private boolean notFind = false;
	
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
		this.tree = tree;
		
		text = new TextArea("");		
		//text.setDisable(true);
		text.setMaxWidth(250);
		text.setMaxHeight(95);
		text.setEditable(false);
	//	text.setBoundsType(TextBoundsType.VISUAL);
		//text.setFill(Color.WHITE);
		text.setFont(new Font(text.getFont().toString(), 15));
		text.layoutXProperty().bind(stageWidthProperty.subtract(270));
		//text.layoutYProperty().bind(0);
		
		text.textProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
				// text.scrol
				// fitToWidth and fitToHeight values. 
			}			
		});	
		
		
		
		//DefaultCaret caret = (DefaultCaret) text.getCaret();
	//	caret.setUpdatePolicy(ALWAYS_UPDATE);
		
		paneTree.getChildren().add(text);
	}
	
	/**
	 * Vložení kořenu
	 * @param root
	 */
	public void insertRoot(INode<?> rootNode){
		IGraphicNode root = rootNode.getGraphicNode();
		rootSize = root.getRadiusSize();
		stackPaneHeight = root.getStackPaneNode().getPrefHeight();
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
				text.appendText("\n • Vložení proběhlo úspěšně.");
				windowController.enableButtons();				
			}
		});

		root.getX().unbind();
		root.getY().unbind();

		text.setText("VLOŽENÍ PRVKU " + root.getValue() +":");
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
	public void deleteNode(Result<?> result, int value) {
		this.value = value;
		if (result.getSide() != Side.NONE) {
			notFind = true;
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
		//if (!(boolean)result.getRecordOfAnimations().get(0).getObject()) {
	//		//TODO nenalezen
	//	} else {
			wayList = result.getWay();			
			startAnimation(result.getRecordOfAnimations());			
	//	}
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
			
			iGraphicNode.getBranchEndX().unbind();
			iGraphicNode.getBranchEndY().unbind();			
			
			hideMovedBranchRecursive(iGraphicNode);
			
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
		notFind = false;
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
			st1 = new StrokeTransition(Duration.millis(SLOWANIMATION),(Line) node.getBranch(), Color.WHITE, Color.LIME);
			pt1 = new PauseTransition(Duration.millis(5 * (FASTANIMATION - animationSpeed.get())));
			st2 = new StrokeTransition(Duration.millis(SLOWANIMATION), (Line) node.getBranch(), Color.LIME, Color.WHITE);
		}
		
		StrokeTransition st3 = new StrokeTransition(Duration.millis(SLOWANIMATION), node.getCircleShape(), Color.WHITE, Color.LIME);
		PauseTransition pt2 = new PauseTransition(Duration.millis(10 * (FASTANIMATION - animationSpeed.get())));
		StrokeTransition st4 = new StrokeTransition(Duration.millis(SLOWANIMATION), node.getCircleShape(), Color.LIME, Color.WHITE);
		
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

		indexAnimation++;
		nextAnimation();
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
			appendNewText("\n • Mazaný má potomky.");
			
			node.setValue("");
			indexAnimation++;
			nextAnimation();
		} else {	
			appendNewText("\n • Mazaný je list.");
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
				listGraphicNodes.remove(graphicNodeMoved); //dám roota na první místo
				listGraphicNodes.add(0, graphicNodeMoved);
			}// else {
				//graphicNodeMoved.setParent(graphicNodeRemoved.getParent()); /** smazané **/
			///}
							
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
		paneTree.getChildren().add(2, text);
		text.toBack();
		text.toBack();
		text.toBack();
		text.toBack();
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