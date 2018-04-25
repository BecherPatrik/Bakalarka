package Aplication;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;

import com.sun.crypto.provider.AESParameters;

import Graphic.DrawingTree;
import Graphic.IGraphicNode;
import Trees.AnimatedAction;
import Trees.BinaryTree;
import Trees.INode;
import Trees.ITree;
import Trees.Result;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class WindowController implements Initializable {

	private Stage primaryStage;

	private @FXML
	Button btnTrees;
	private @FXML
	Button btnBinary;
	private @FXML
	Button btnTreesActual;

	private @FXML
	Button btnSearch;
	private @FXML
	Button btnDelete;
	private @FXML
	Button btnInsert;
	private @FXML
	Button btnRepeat;
	private @FXML
	Button btnNewTree;

	private @FXML
	TextField inputNumber;
	private @FXML
	Slider sliderSpeed;

	private @FXML
	HBox hBoxInput;
	private @FXML
	VBox treesMenu;

	private @FXML
	BorderPane bpWindow;
	private @FXML
	BorderPane menu;
	private @FXML
	BorderPane borderPaneTree;
	private @FXML
	Pane paneTree;

	private ITree<?> tree;
	private DrawingTree graphicTree;	
	private List<IGraphicNode> listOldGraphicTreeNodes = new ArrayList<>();
	
	private final int maxTextLength = 4;
	boolean isAnimationDisable = false;
	
	private Result<?> lastResult = null;
	private AnimatedAction lastAction;
	
	private Set<Integer> randomValueList;	

	/**
	 * Inicializace okna
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnTreesActual = btnBinary;
		
		hideMenu();
		numberOnly();
		sliderFormat();
		toolTips();
	}	

	/**
	 * Nastaví Stage
	 * @param primaryStage
	 */
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		sliderSpeed.setValue(25);
		
		newEmptyTree();		
	}
	
	/**
	 * Funkce pro animaci skrytí menu
	 */
	@FXML
	private void hideMenu() {
		FadeTransition hideFileRootTransition = new FadeTransition(Duration.millis(500), treesMenu);
		hideFileRootTransition.setFromValue(1.0);
		hideFileRootTransition.setToValue(0.0);

		hideFileRootTransition.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				bpWindow.setLeft(null);
				menu.setLeft(btnTrees);
			}
		});

		hideFileRootTransition.play();
	}
	
	/**
	 * Funkce pro animaci zobrazení menu
	 */
	@FXML
	private void showMenu() {
		bpWindow.setLeft(treesMenu);
		menu.setLeft(null);

		FadeTransition showFileRootTransition = new FadeTransition(Duration.millis(500), treesMenu);
		showFileRootTransition.setFromValue(0.0);
		showFileRootTransition.setToValue(1.0);

		showFileRootTransition.play();
	}
	
	/**
	 * Nastaví validátor pro textField inputNumber
	 */
	private void numberOnly() {
		inputNumber.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("(\\d*)")) {					
					inputNumber.setText(newValue.replaceAll("[^\\d]", ""));						
				} 
				
				if (newValue.length() > maxTextLength) {		                
					inputNumber.setText(oldValue);
				}
				
				checkEnableButtons();
			}
		});
	}
	
	/**
	 * Naformátuje Slider
	 */
	private void sliderFormat() {
		sliderSpeed.setSnapToTicks(true);
		sliderSpeed.setShowTickMarks(true);
        sliderSpeed.setShowTickLabels(true);
        sliderSpeed.setMinorTickCount(0);        
        
		sliderSpeed.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n == 0) return "VYP.";
                if (n == 25) return "25%";
                if (n == 50) return "50%";
                if (n == 75) return "75%";
                return "100%";              
            }

            @Override
            public Double fromString(String s) {
                return 0d;                
            }
        });
		
		sliderSpeed.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue.doubleValue() == 0) {
					isAnimationDisable = true;
				} else {
					isAnimationDisable = false;
				}
				
				checkEnableButtons();
			}
		});		
	}
	
	/**
	 * Nastaví toolTips
	 */
	private void toolTips() {
		inputNumber.setTooltip(new Tooltip("Zadávejte pouze hodnoty\nod 0 do 10 000"));
		btnInsert.setTooltip(new Tooltip("Vloží zadanou hodnotu"));
		btnSearch.setTooltip(new Tooltip("Vyhledá zadanou hodnotu"));
		btnDelete.setTooltip(new Tooltip("Smaže zadanou hodnotu"));
		btnNewTree.setTooltip(new Tooltip("Vytvoří nový strom..."));
		btnRepeat.setTooltip(new Tooltip("Zopakuje poslední krok"));
		sliderSpeed.setTooltip(new Tooltip("Nastavení rychlosti animace"));
	}

	/**
	 * Funkce pro vkládání čísla
	 */
	@FXML
	private void insertNumber() {
		treeLog();
		graphicTree.getListGraphicNodes().forEach(x -> x.deleteBackUp());
		listOldGraphicTreeNodes.clear();
		listOldGraphicTreeNodes.addAll(graphicTree.getListGraphicNodes());
		lastAction = AnimatedAction.INSERT;
		
		disableButtons();		
		
		lastResult = tree.insert(Integer.parseInt(inputNumber.getText()));
		
		if (lastResult != null) {
			graphicTree.insertNode(lastResult);			
		} else {
			graphicTree.insertRoot((INode<?>)tree.getRoot());
		}				
	}
	
	/**
	 * Funkce pro hledání čísla
	 */
	@FXML
	private void searchNumber() {
		graphicTree.getListGraphicNodes().forEach(x -> x.deleteBackUp());
		listOldGraphicTreeNodes.clear();
		listOldGraphicTreeNodes.addAll(graphicTree.getListGraphicNodes());
		lastAction = AnimatedAction.SEARCH;
		
		disableButtons();
		
		lastResult = tree.search(Integer.parseInt(inputNumber.getText()));		
		graphicTree.searchNode(lastResult);		
	}

	/**
	 * Funkce pro mazání čísla
	 */
	@FXML
	private void deleteNumber() {
		graphicTree.getListGraphicNodes().forEach(x -> x.deleteBackUp()); //smažu zálohy 
		listOldGraphicTreeNodes.clear();
		listOldGraphicTreeNodes.addAll(graphicTree.getListGraphicNodes());
		
		lastAction = AnimatedAction.DELETE;
		
		disableButtons();
		
		lastResult = tree.delete(Integer.parseInt(inputNumber.getText()));		
		graphicTree.deleteNode(lastResult);		
	}
	@FXML private void dialogNewTree() {
		newRandomTree();
	}
	/**
	 * Vytvoření nového stromu přes tlačítko
	 */
	@FXML 
	private void dialogNewTree2() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Nový strom");
		alert.setHeaderText("Chcete pouze smazat aktuální strom,\nnebo vytvořit nový s náhodnýma hodnotama?");
		alert.setContentText("Vyberte si možnost:");

		ButtonType buttonTypeOne = new ButtonType("Smazat aktuální");
		ButtonType buttonTypeTwo = new ButtonType("Nový náhodný...");		
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.get() == buttonTypeOne){
		     newEmptyTree();
		} else if (result.get() == buttonTypeTwo) {
		    newRandomTree();		   
		} else {
		    return;
		}		
	}
	
	/**
	 * Vytvoření nového prázdného stromu
	 */
	private void newEmptyTree() {
		listOldGraphicTreeNodes = new ArrayList<>();
		lastResult = null;
		
		paneTree.getChildren().clear();
		
		checkEnableButtons();
		
		switch (btnTreesActual.getId()) {
		case "btnBinary":
			graphicTree = new DrawingTree(tree, paneTree, sliderSpeed.valueProperty(), primaryStage.widthProperty(), this);
			tree = new BinaryTree();			
			break;
			
		case "btnAVL":
			graphicTree = new DrawingTree(tree, paneTree, sliderSpeed.valueProperty(), primaryStage.widthProperty(), this);
			tree = new BinaryTree();
			//tree = new AVLTree();
			break;
			
		case "btnRedBlack":
			graphicTree = new DrawingTree(tree, paneTree, sliderSpeed.valueProperty(), primaryStage.widthProperty(), this);
			tree = new BinaryTree();
			//tree = new RedBlackTree();
			break;
		default:
			break;
		}
		
	}	
	
	private void newRandomTree() {
		double oldSpeed = sliderSpeed.getValue();
		newEmptyTree();
		sliderSpeed.setValue(0);
		
		lastResult = tree.insert(5);
		graphicTree.insertRoot((INode<?>)tree.getRoot());
		
		lastResult = tree.insert(3);
		graphicTree.insertNode(lastResult);
		
		lastResult = tree.insert(8);
		graphicTree.insertNode(lastResult);
		
		/*lastResult = tree.insert(6);
		graphicTree.insertNode(lastResult);*/
		
		lastResult = tree.insert(9);
		graphicTree.insertNode(lastResult);
		
		lastResult = null;
		listOldGraphicTreeNodes.clear();
		sliderSpeed.setValue(oldSpeed);
	}
	/**
	 * Vytvoření nového náhodného stromu 
	 * @param count
	 */
	private void newRandomTree2() {	
		int count = dialogRandomTree();
		double oldSpeed = sliderSpeed.getValue();
		if (count > 0) {			
			newEmptyTree();
			randomValueList = new HashSet<>();
			
			sliderSpeed.setValue(0);
			
			for (int value : randomValueList) {
				disableButtons();
				lastResult = tree.insert(value);				
				if (lastResult != null) {
					graphicTree.insertNode(lastResult);			
				} else {					
					graphicTree.insertRoot((INode<?>)tree.getRoot());
				}
			}
			
			lastResult = null;
			listOldGraphicTreeNodes.clear();
			sliderSpeed.setValue(oldSpeed);
		}		
	}
	
	/**
	 * Vygeneruje seznam náhodných hodnot
	 * @param count
	 */
	private void generateRandomTreeList(int count) {
		randomValueList = new HashSet<>();
		Random r = new Random();
		int low = 1;
		int high = 1000;

		while (randomValueList.size() != count) {
			randomValueList.add(r.nextInt(high - low) + low);
		}		
	}	

	/**
	 * Funkce pro volbu stromu z menu
	 * @param event
	 * @throws IOException
	 */
	@FXML
	private void changeTree(ActionEvent event) throws IOException {
		if (btnTreesActual != (Button) event.getTarget() && (tree == null || tree.getRoot() == null || dialogChangeTree())) {
			Button selectedButton = (Button) event.getTarget();
			selectedButton.getStyleClass().clear();
			selectedButton.getStyleClass().add("tree-button-focus");
			
			btnTreesActual.getStyleClass().clear();
			btnTreesActual.getStyleClass().add("tree-button");			

			primaryStage.setTitle("Trees - " + selectedButton.getText());

			btnTreesActual = selectedButton;
			hideMenu();
			newEmptyTree();
		}
	}

	/**
	 * Dialog pro vytvoření nového náhodného stromu
	 */
	private int dialogRandomTree() {		
		TextInputDialog dialog = new TextInputDialog("10");
		dialog.getEditor().textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("(\\d*)")) {
					dialog.getEditor().setText(newValue.replaceAll("[^\\d]", ""));					
				}

				if (!dialog.getEditor().getText().isEmpty()) {
					int count = Integer.parseInt(dialog.getEditor().getText());
					
					if (count < 1) {
						dialog.getEditor().setText("1");
					} else if (count > 25) {
						dialog.getEditor().setText("25");
					}
				} 
			}
		});
		
		dialog.setTitle("Náhodný strom");
		dialog.setHeaderText("Zadejte počet hodnot (od 1 do 25)");
		dialog.setContentText("Počet hodnot:");
		
		Optional<String> result = dialog.showAndWait();
		
		if (result.isPresent() && !result.get().isEmpty()){
			return Integer.parseInt(result.get());
		}
		
		return 0;		
	}
	
	/**
	 * Dialog pro změnu stromu
	 * @return
	 */
	private boolean dialogChangeTree() {		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Změna typu stromu");
		alert.setHeaderText("Změnou typu stromu bude smazán aktuální strom.");		

		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.get() == ButtonType.OK){
		    return true;
		}
		return false;
	}

	/**
	 * Zopakuje poslední animaci
	 */
	@FXML
	private void repeatLastAnimation() {		
		disableButtons();		
		updatePaneTree();
		
		graphicTree.setRedraw(); //zapnu vynucené překreslování
		disableButtons();
		
		switch (lastAction) {
		case INSERT:			
			if (lastResult != null) {
				graphicTree.insertNode(lastResult);			
			} else {
				graphicTree.insertRoot((INode<?>)tree.getRoot());
			}
			break;

		case DELETE:			
			graphicTree.deleteNode(lastResult);
			break;

		case SEARCH:
			graphicTree.searchNode(lastResult);
			break;

		default:
			break;
		}
	}
	
	private void updatePaneTree() {
		listOldGraphicTreeNodes.forEach(x -> x.useBackUp());		
		
		graphicTree.setListGraphicNodes(listOldGraphicTreeNodes);		

		paneTree.getChildren().clear();

		for (IGraphicNode node : graphicTree.getListGraphicNodes()) {
			paneTree.getChildren().add(node.getStackPaneNode());			

			if (node.getBranch() != null) {
				paneTree.getChildren().add(node.getBranch());
				node.getParent().getStackPaneNode().toFront();
				node.getStackPaneNode().toFront();
			}
		}
	}
	
	/**
	 * Povoluje a ruší povolení používání tlačítek podle možností co se s daným stromem dá dělat
	 */
	private void checkEnableButtons() {
		if (!inputNumber.getText().isEmpty()) {
			btnInsert.setDisable(false);
		} else {
			btnInsert.setDisable(true);
		}
		
		if (!inputNumber.getText().isEmpty() && tree != null && tree.getRoot() != null) {
			btnDelete.setDisable(false);
			btnSearch.setDisable(false);
		} else {
			btnDelete.setDisable(true);
			btnSearch.setDisable(true);
		}
		
		if (isAnimationDisable || (listOldGraphicTreeNodes.isEmpty() && paneTree.getChildren().isEmpty()) || listOldGraphicTreeNodes.isEmpty())  {
			btnRepeat.setDisable(true);
		} else {
			btnRepeat.setDisable(false);
		}
	}
	
	/**
	 * Povolí manipulaci s tlačítkami po ukončení animace
	 */
	public void enableButtons() {
		primaryStage.setResizable(true);
		
		checkEnableButtons();
		
		btnTrees.setDisable(false);
		btnNewTree.setDisable(false);		
		
		sliderSpeed.setDisable(false);
		
		inputNumber.setDisable(false);
		inputNumber.clear();
	}
	
	/**
	 * Znemožní používání tlačítek v průběhu animace 
	 */
	private void disableButtons() {			
		primaryStage.setResizable(false); 
		
		btnInsert.setDisable(true);
		btnDelete.setDisable(true);
		btnSearch.setDisable(true);
		
		btnRepeat.setDisable(true);		
		
		btnTrees.setDisable(true);
		btnNewTree.setDisable(true);
		
		sliderSpeed.setDisable(true);
		inputNumber.setDisable(true);
	}
	
	private void treeLog() {
		INode<?> root = (INode<?>)tree.getRoot();
		System.out.println(treeLogIter(root));
	}
	
	private String treeLogIter(INode<?> n) {
		INode<?> parent = (INode<?>) n.getParent();
		String s;
		if (parent == null) {
			s = ""+ n.getValue() + " - " + n.getGraphicNode().getValue() + " = " + n.getGraphicNode() + "\n";
		} else {
			s = ""+ n.getValue() + " - " + n.getGraphicNode().getValue() + " = " + n.getGraphicNode() + "\n rodič: " +
					parent.getValue() + " - " + parent.getGraphicNode().getValue() + " = " + parent.getGraphicNode() + "\n*********************************\n";
		}
		
		if (n.getLeft() != null) {
			if (n.getRight() != null) {
				return s + "\t" + treeLogIter((INode<?>) n.getLeft()) + treeLogIter((INode<?>) n.getRight());
			} else {
				return s + "\t" + treeLogIter((INode<?>) n.getLeft());
			}
		} else if (n.getRight() != null) {
			return s + "\t" + treeLogIter((INode<?>) n.getRight());		
		} else {
			return s;
		}
	}
}