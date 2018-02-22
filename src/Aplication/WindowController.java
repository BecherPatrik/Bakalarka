package Aplication;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import Graphic.DrawingTree;
import Graphic.IGraphicNode;
import Trees.BinaryNode;
import Trees.BinaryTree;
import Trees.INode;
import Trees.ITree;
import Trees.Result;
import Trees.AnimatedAction;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
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
	private List<IGraphicNode> oldGraphicTreeNodes = new ArrayList<>();
	
	private final int maxTextLength = 4;	
	
	private Result<?> lastResult = null;
	private AnimatedAction lastAction;

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
	 * Nastav� Stage
	 * @param primaryStage
	 */
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		sliderSpeed.setValue(25);
		
		newEmptyTree();		
	}
	
	/**
	 * Funkce pro animaci skryt� menu
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
	 * Funkce pro animaci zobrazen� menu
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
	 * Nastav� valid�tor pro textField inputNumber
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
	 * Naform�tuje Slider
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
		
	}
	
	/**
	 * Nastav� toolTips
	 */
	private void toolTips() {
		inputNumber.setTooltip(new Tooltip("Zad�vejte pouze hodnoty\nod 0 do 10 000"));
		btnInsert.setTooltip(new Tooltip("Vlo�� zadanou hodnotu"));
		btnSearch.setTooltip(new Tooltip("Vyhled� zadanou hodnotu"));
		btnDelete.setTooltip(new Tooltip("Sma�e zadanou hodnotu"));
		btnNewTree.setTooltip(new Tooltip("Vytvo�� nov� strom..."));
		btnRepeat.setTooltip(new Tooltip("Zopakuje posledn� krok"));
		sliderSpeed.setTooltip(new Tooltip("Nastaven� rychlosti animace"));
	}

	/**
	 * Funkce pro vkl�d�n� ��sla
	 */
	@FXML
	private void insertNumber() {
		oldGraphicTreeNodes.clear();
		oldGraphicTreeNodes.addAll(graphicTree.getNodes());
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
	 * Funkce pro hled�n� ��sla
	 */
	@FXML
	private void searchNumber() {
		oldGraphicTreeNodes.clear();
		oldGraphicTreeNodes.addAll(graphicTree.getNodes());
		lastAction = AnimatedAction.SEARCH;
		
		disableButtons();
		
		lastResult = tree.search(Integer.parseInt(inputNumber.getText()));		
		graphicTree.searchNode(lastResult);		
	}

	/**
	 * Funkce pro maz�n� ��sla
	 */
	@FXML
	private void deleteNumber() {
		oldGraphicTreeNodes.clear();
		oldGraphicTreeNodes.addAll(graphicTree.getNodes());
		lastAction = AnimatedAction.DELETE;
		
		disableButtons();
		
		lastResult = tree.delete(Integer.parseInt(inputNumber.getText()));		
		graphicTree.deleteNode(lastResult);		
	}
	
	/**
	 * Vytvo�en� nov�ho stromu p�es tla��tko
	 */
	@FXML 
	private void newTree() {
		//TODO tree
		graphicTree = new DrawingTree(tree, paneTree, sliderSpeed.valueProperty(), primaryStage.widthProperty(), this);
	//	graphicTree.insertRoot(tree.getRoot().getGraphicNode());	//TODO
		checkEnableButtons();
	}
	
	/**
	 * Vytvo�en� nov�ho pr�zdn�ho stromu
	 */
	private void newEmptyTree() {
		oldGraphicTreeNodes = new ArrayList<>();
		
		paneTree.getChildren().clear();
		
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
	
	/**
	 * Vytvo�en� nov�ho n�hodn�ho stromu 
	 * @param count
	 */
	private void newRandomTree(int count) {
		//TODO vypn�t animace
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
	 * Dialog pro zm�nu stromu
	 * @return
	 */
	private boolean dialogChangeTree() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Zm�na typu stromu");
		alert.setHeaderText("Zm�nou typu stromu bude smaz�n aktu�ln� strom.");
		alert.setContentText("Smazat aktu�ln� strom?");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
		    return true;
		}
		return false;
	}

	/**
	 * Zopakuje posledn� animaci
	 */
	@FXML
	private void repeatLastAnimation() {
		graphicTree.setNodes(oldGraphicTreeNodes);
		
		paneTree.getChildren().clear();
		
		for (IGraphicNode node : graphicTree.getNodes()) {
			paneTree.getChildren().add(node.getNode());			
			if (node.getBranch() != null) {								
				paneTree.getChildren().add(node.getBranch());
				node.getParent().getNode().toFront();
				node.getNode().toFront();
			}			
		}

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
	
	/**
	 * Povoluje a ru�� povolen� pou��v�n� tla��tek podle mo�nost� co se s dan�m stromem d� d�lat
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
		
		if (oldGraphicTreeNodes.isEmpty() && paneTree.getChildren().isEmpty()) {
			btnRepeat.setDisable(true);
		} else {
			btnRepeat.setDisable(false);
		}
	}
	
	/**
	 * Povol� manipulaci s tla��tkami po ukon�en� animace
	 */
	public void enableButtons() {
	//	checkEnableButtons();
		
		btnTrees.setDisable(false);
		btnNewTree.setDisable(false);		
		
		sliderSpeed.setDisable(false);
		
		inputNumber.setDisable(false);
		inputNumber.clear();
	}
	
	/**
	 * Znemo�n� pou��v�n� tla��tek v pr�b�hu animace 
	 */
	private void disableButtons() {
		btnInsert.setDisable(true);
		btnDelete.setDisable(true);
		btnSearch.setDisable(true);
		
		btnRepeat.setDisable(true);		
		
		btnTrees.setDisable(true);
		btnNewTree.setDisable(true);
		
		sliderSpeed.setDisable(true);
		inputNumber.setDisable(true);
	}
}