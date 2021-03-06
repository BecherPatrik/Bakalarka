package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;

import javax.imageio.ImageIO;

import graphic.DrawingTree;
import graphic.IGraphicNode;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import trees.AVLTree;
import trees.AnimatedAction;
import trees.BinaryTree;
import trees.Color;
import trees.INode;
import trees.ITree;
import trees.RedBlackNode;
import trees.RedBlackTree;
import trees.Result;

public class WindowController implements Initializable {

	private Stage primaryStage;

	private @FXML
	Button btnTrees;
	private @FXML
	Button btnBinary;
	private @FXML
	Button btnAVL;
	private @FXML
	Button btnRedBlack;

	private @FXML
	Button btnTreesActual;
	private @FXML 
	Button btnExit;

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
	private @FXML
	ScrollPane scrollPane;

	private ITree tree;
	private DrawingTree graphicTree;	
	private List<IGraphicNode> listOldGraphicTreeNodes = new ArrayList<>();
	
	private final int maxTextLength = 4;	
	
	private Result lastResult = null;
	private AnimatedAction lastAction;
	
	private Set<Integer> randomValueList;
	
	private List<Integer> listHistory = new ArrayList<>();
	private List<trees.Color> listHistoryColor = new ArrayList<>();
	private int lastValue;

	private boolean isRedraw = false;	
	private boolean randomTree = false;
	private boolean isRedBlack = false;
	private boolean isAnimationDisable = false;
	private boolean isLoad = false;

	private int finishAnimation = 0;
	private double oldSpeed;
	
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
		menuFormat();
	}	

	/**
	 * Nastaví Stage
	 * @param primaryStage
	 */
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		sliderSpeed.setValue(50);
		
		newEmptyTree();		
		inputNumber.requestFocus();
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
		
		inputNumber.setOnKeyPressed(new EventHandler<KeyEvent>() {			 
		    @Override
		    public void handle(KeyEvent event) {
		        if(event.getCode().equals(KeyCode.ENTER)) {
		             if (!(btnInsert.isDisable())) {
		            	 insertNumber();
		             }
		        } else if (event.getCode().equals(KeyCode.DELETE)) {
		        	if (!(btnDelete.isDisable())) {
		            	 deleteNumber();
		             }
		        } else if (event.getCode().equals(KeyCode.F)) {
		        	if (!(btnSearch.isDisable())) {
		            	 searchNumber();
		             }
		        }
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
		btnInsert.setTooltip(new Tooltip("Vloží zadanou hodnotu\nKlávesová zkratka: ENTER"));
		btnSearch.setTooltip(new Tooltip("Vyhledá zadanou hodnotu\nKlávesová zkratka: F"));
		btnDelete.setTooltip(new Tooltip("Smaže zadanou hodnotu\nKlávesová zkratka: DELETE"));
		btnNewTree.setTooltip(new Tooltip("Vytvoří nový strom..."));
		btnRepeat.setTooltip(new Tooltip("Zopakuje poslední krok"));
		sliderSpeed.setTooltip(new Tooltip("Nastavení rychlosti animace"));
	}
	
	/**
	 * Nastaví menu
	 */
	private void menuFormat() {
		treesMenu.spacingProperty().bind(bpWindow.heightProperty().subtract(395));
		btnExit.setOnAction(e -> Platform.exit());
	}

	/**
	 * Funkce pro vkládání čísla
	 */
	@FXML
	private void insertNumber() {		
		graphicTree.clearText();
		lastAction = AnimatedAction.INSERT;
		createHistory();		
		
		disableButtons();		
		
		lastResult = tree.insert(lastValue);
		
		if (lastResult != null) {
			graphicTree.insertNode(lastResult, lastValue);			
		} else {
			graphicTree.insertRoot((INode)tree.getRoot());
		}
	}
	
	/**
	 * Funkce pro hledání čísla
	 */
	@FXML
	private void searchNumber() {
		lastAction = AnimatedAction.SEARCH;
		
		graphicTree.clearText();
		
		disableButtons();
		lastValue = Integer.parseInt(inputNumber.getText());
		lastResult = tree.search(lastValue);		
		graphicTree.searchNode(lastResult, lastValue);		
	}

	/**
	 * Funkce pro mazání čísla
	 */
	@FXML
	private void deleteNumber() {
		graphicTree.clearText();
		
		lastAction = AnimatedAction.DELETE;
		createHistory();	
		
		disableButtons();
		
		lastResult = tree.delete(Integer.parseInt(inputNumber.getText()));		
		graphicTree.deleteNode(lastResult, lastValue);	
	}
	
	/**
	 * Funkce na testování
	 */
	@FXML 
	private void dialogNewTree0() {
		newRandomTree();
	}
	
	/**
	 * Vytvoření nového stromu přes tlačítko
	 */
	@FXML 
	private void dialogNewTree() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Nový strom");
		alert.setHeaderText("Chcete pouze smazat aktuální strom,\nnebo vytvořit nový s náhodnými hodnotami?");
		alert.setContentText("Vyberte si možnost:");

		ButtonType buttonTypeOne = new ButtonType("Smazat aktuální");
		ButtonType buttonTypeTwo = new ButtonType("Nový náhodný...");		
		ButtonType buttonTypeCancel = new ButtonType("Zrušit", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.get() == buttonTypeOne){
			listHistory.clear();
		    newEmptyTree();
		} else if (result.get() == buttonTypeTwo) {
			listHistory.clear();
		    newRandomTree();		   
		} else {
		    return;
		}		
	}
	
	/**
	 * Nový strom z menu 
	 */
	@FXML
	private void newEmptyTreeFXML() {
		if ((tree == null || tree.getRoot() == null)) {
			hideMenu();
		} else if (dialogChangeTreeNew()) {
			listHistory.clear();
	    	newEmptyTree();
	    	hideMenu();
		}
	}
	
	/**
	 * Fuknce Uložit... volaná z menu
	 * @throws ClassNotFoundException
	 */
	@FXML
	private void save() throws ClassNotFoundException {
		FileChooser fileChooser = new FileChooser();
		List<Integer> oldListHistory = listHistory;

		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("(*.tree)", "*.tree"));

		File file = fileChooser.showSaveDialog(null);

		if (!(file == null)) {
			try {
				FileOutputStream f = new FileOutputStream(file);
				ObjectOutputStream o = new ObjectOutputStream(f);

				o.writeObject(btnTreesActual.getId());
				
				createHistory();
				o.writeObject(listHistory);
				listHistory = oldListHistory;
				
				o.writeObject(listHistoryColor);

				o.flush();
				f.flush();
				
				o.close();
				f.close();
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR, "Podrobnosti: "+ e.getMessage());
				alert.setTitle("Chyba");			
				alert.setHeaderText("Při ukládání došlo k chybě.");
				alert.showAndWait();
			} 
		}
	}
	
	/**
	 * Funkce načíst volaná z menu
	 */
	@SuppressWarnings("unchecked")
	@FXML private void load() {
		FileChooser fileChooser = new FileChooser();
		String btnTreesActualString;
		Button selectedButton = null;

		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("(*.tree)", "*.tree"));

		File file = fileChooser.showOpenDialog(null);

		if (!(file == null)) {
			try {
				FileInputStream fi = new FileInputStream(file);
				ObjectInputStream oi = new ObjectInputStream(fi);
				
				btnTreesActualString = (String) oi.readObject();
				listHistory =  (List<Integer>) oi.readObject();
				listHistoryColor =  (List<Color>) oi.readObject();
				
				oi.close(); 
				fi.close();				
				
				switch (btnTreesActualString) {
				case "btnBinary":
					selectedButton = btnBinary;
					break;
					
				case "btnAVL":
					selectedButton = btnAVL;
					break;
					
				case "btnRedBlack":
					selectedButton = btnRedBlack;
					break;

				default:
					break;
				}
				
				if (tree == null || tree.getRoot() == null || dialogChangeTreeLoad()) {		
					isLoad = true;					
					btnTreesActual.getStyleClass().clear();
					btnTreesActual.getStyleClass().add("tree-button");	
					
					selectedButton.getStyleClass().clear();
					selectedButton.getStyleClass().add("tree-button-focus");

					primaryStage.setTitle("Stromy - " + selectedButton.getText());

					btnTreesActual = selectedButton;
					hideMenu();					
					
					oldSpeed = sliderSpeed.getValue();

					newEmptyTree();

					sliderSpeed.setValue(0);
					finishAnimation = 0;
					isRedraw  = true;
					tree.disableBalance();
					
					int index = 1;
					Result result;
					RedBlackNode redBlackNode;
					graphicTree.setInsertAnimation(false);
					
					if (!(listHistory.isEmpty())) {
						graphicTree.hideText();
						
						tree.insert(listHistory.get(0));
						graphicTree.insertRoot((INode)tree.getRoot());

						for (int value : listHistory.subList(1, listHistory.size())) {
							result = tree.insert(value);
							graphicTree.insertNode(result, lastValue);
							if (isRedBlack) {
								redBlackNode = (RedBlackNode)result.getNode();
								redBlackNode.setColor(listHistoryColor.get(index));
								redBlackNode.getGraphicNode().setColor(listHistoryColor.get(index));
								index++;
							}
						}
						
						graphicTree.setInsertAnimation(true);
						
						graphicTree.clearText();
						graphicTree.showText();
					}
					
					sliderSpeed.setValue(oldSpeed);	
				}
				
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR, "Podrobnosti: "+ e.getMessage());
				alert.setTitle("Chyba");			
				alert.setHeaderText("Při načítání došlo k chybě.");
				alert.showAndWait();
			}
		}
	}
	
	/**
	 * Funkce pro ukládání obrázku v png
	 */
	@FXML private void savePicture() {
		SnapshotParameters snapshotParameters = new SnapshotParameters();
		
		//snapshotParameters.setTransform(new Scale(2, 2));
		snapshotParameters.setFill(Paint.valueOf("#1d1d1d"));
		
		graphicTree.hideText();
		WritableImage image = paneTree.snapshot(snapshotParameters, null);
		PixelReader reader = image.getPixelReader();

	    int startX = (int) graphicTree.getLeftX() - 1;
	    int startY = 38;
	    int endX =  (int) paneTree.getWidth();
	    int endY = (int) paneTree.getHeight();	    

	    image = new WritableImage(reader, startX, startY, endX - startX, endY - startY);
		
		graphicTree.showText();
		
		FileChooser fileChooser = new FileChooser();

	    //Set extension filter
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("(*.png)", "*.png"));

	    //Prompt user to select a file
	    File file = fileChooser.showSaveDialog(null);

	    if (!(file == null)) {
	    	try {
	        	ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
	    	} catch (Exception e) {
	    		Alert alert = new Alert(AlertType.ERROR, "Podrobnosti: "+ e.getMessage());
				alert.setTitle("Chyba");			
				alert.setHeaderText("Při exportování obrázku došlo k chybě.");
				alert.showAndWait();
	    	}
	    }
	    
	    hideMenu();
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
			isRedBlack = false;
			graphicTree = new DrawingTree(paneTree, sliderSpeed.valueProperty(), primaryStage.widthProperty(), primaryStage.heightProperty(), this);
			tree = new BinaryTree();	
			//isRedBlack = true;
			//tree = new RedBlackTree();
			break;
			
		case "btnAVL":
			isRedBlack = false;
			graphicTree = new DrawingTree(paneTree, sliderSpeed.valueProperty(), primaryStage.widthProperty(), primaryStage.heightProperty(), this);
			tree = new AVLTree();
			break;
			
		case "btnRedBlack":
			isRedBlack = true;
			graphicTree = new DrawingTree(paneTree, sliderSpeed.valueProperty(), primaryStage.widthProperty(), primaryStage.heightProperty(), this);
			tree = new RedBlackTree();
			
			break;
		default:
			break;
		}		
	}	
	
	/**
	 * Funkce pro testování
	 */
	@SuppressWarnings("unused")
	private void newRandomTree0() {
		double oldSpeed = sliderSpeed.getValue();
		newEmptyTree();
		sliderSpeed.setValue(0);
		graphicTree.hideText();	
		
		graphicTree.setInsertAnimation(false);	
		
		graphicTree.showText();
		graphicTree.clearText();
		sliderSpeed.setValue(oldSpeed);
	}
	/**
	 * Vytvoření nového náhodného stromu 
	 * @param count
	 */
	private void newRandomTree() {	
		int count = dialogRandomTree();
		int index = 1;
		oldSpeed = sliderSpeed.getValue();
		graphicTree.hideText();
		
		if (count > 0) {			
			newEmptyTree();
			randomValueList = new HashSet<>();
			generateRandomTreeList(count);					
			
			sliderSpeed.setValue(0);
			
			graphicTree.setAnimation(false);
			graphicTree.setInsertAnimation(false);
			
			for (int value : randomValueList) {
				disableButtons();	
				if (index == randomValueList.size()) {
					graphicTree.setAnimation(true);
				}
				
				lastResult = tree.insert(value);				
				if (lastResult != null) {
					graphicTree.insertNode(lastResult, lastValue);			
				} else {					
					graphicTree.insertRoot((INode)tree.getRoot());
					graphicTree.hideText();
				}
				index++;
			}			
			
			graphicTree.setInsertAnimation(true);
			randomTree = true;
			
			lastResult = null;
			lastAction = null;
			listOldGraphicTreeNodes.clear();
			checkEnableButtons();
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

			primaryStage.setTitle("Stromy - " + selectedButton.getText());

			btnTreesActual = selectedButton;
			hideMenu();
			listHistory.clear();
			newEmptyTree();
		}
	}

	/**
	 * Dialog pro vytvoření nového náhodného stromu
	 */
	private int dialogRandomTree() {		
		TextInputDialog dialog = new TextInputDialog("10");
		((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Zrušit");
		
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
		ButtonType b1 = new ButtonType("Ok", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
		ButtonType b2 = new ButtonType("Zrušit", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
		
		Alert alert = new Alert(AlertType.CONFIRMATION,"", b1, b2);
		alert.setTitle("Změna typu stromu");
		alert.setHeaderText("Změnou typu stromu bude smazán aktuální strom.");		

		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.get() == b1){
		    return true;
		}
		return false;
	}
	
	/**
	 * Dialog pro změnu stromu načteným
	 * @return
	 */
	private boolean dialogChangeTreeLoad() {		
		ButtonType b1 = new ButtonType("Ok", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
		ButtonType b2 = new ButtonType("Zrušit", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
		
		Alert alert = new Alert(AlertType.CONFIRMATION,"", b1, b2);
		alert.setTitle("Načtení stromu");			
		alert.setHeaderText("Načtením stromu bude smazán aktuální strom.");
		
		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.get() == b1){
		    return true;
		}
		return false;
	}
	
	/**
	 * Dialog pro Nový z menu
	 * @return
	 */
	private boolean dialogChangeTreeNew() {		
		ButtonType b1 = new ButtonType("Ano", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
		ButtonType b2 = new ButtonType("Zrušit", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
		
		Alert alert = new Alert(AlertType.CONFIRMATION,"", b1, b2);
		alert.setTitle("Nový strom");			
		alert.setHeaderText("Smazat aktuální strom?");
		
		Optional<ButtonType> result = alert.showAndWait();
		
		if (result.get() == b1){
		    return true;
		}
		return false;
	}
	
	/**
	 * Vytvoří historii pro animace
	 */
	private void createHistory() {
		if (!(inputNumber.getText().isEmpty())) {
			lastValue = Integer.parseInt(inputNumber.getText());
		}
		listHistory.clear();
		listHistoryColor.clear();
		createHistoryRecursion(tree.getRoot());
	}
	
	/**
	 * Rekuzre pro tvorbu historie
	 * @param object
	 */
	private void createHistoryRecursion(Object object) {		
		INode node = (INode) object;
		if (node != null) {
			listHistory.add(node.getValue());
			
			if (isRedBlack) {
				listHistoryColor.add(((RedBlackNode)node).getColor());
			}

			if (node.getLeft() != null) {
				createHistoryRecursion(node.getLeft());
			}

			if (node.getRight() != null) {
				createHistoryRecursion(node.getRight());
			}
		}
	}
	
	/**
	 * Změní strom na předchůdce
	 */
	private void getHistoryTree() {
		oldSpeed = sliderSpeed.getValue();

		newEmptyTree();

		sliderSpeed.setValue(0);
		finishAnimation = 0;
		isRedraw  = true;
		tree.disableBalance();
		
		int index = 1;
		Result result;
		RedBlackNode redBlackNode;
		graphicTree.setInsertAnimation(false);
		
		if (!(listHistory.isEmpty())) {
			graphicTree.hideText();
			
			tree.insert(listHistory.get(0));
			graphicTree.insertRoot((INode)tree.getRoot());

			for (int value : listHistory.subList(1, listHistory.size())) {
				result = tree.insert(value);
				graphicTree.insertNode(result, lastValue);
				if (isRedBlack) {
					redBlackNode = (RedBlackNode)result.getNode();
					redBlackNode.setColor(listHistoryColor.get(index));
					redBlackNode.getGraphicNode().setColor(listHistoryColor.get(index));
					index++;
				}
			}
			
			graphicTree.setInsertAnimation(true);
			
			graphicTree.clearText();
			graphicTree.showText();
		} else {			
			repeatLastAction();
		}	

		sliderSpeed.setValue(oldSpeed);			
	}
	
	/**
	 * Zopakuje poslední animaci
	 */
	@FXML
	private void repeatLastAnimation() {		
		disableButtons();
		graphicTree.clearText();
		
		switch (lastAction) {
		case INSERT:	
			getHistoryTree();			
			break;

		case DELETE:	
			getHistoryTree();
			break;

		case SEARCH:
			graphicTree.searchNode(tree.search(lastValue), lastValue);
			break;

		default:
			break;
		}
	}
	
	/**
	 * Zopakuje poslední akci pokud se musel obnovit strom (metoda se zavolá automaticky po obnovení stromu)
	 * pro INSERT a DELETE
	 */
	private void repeatLastAction() {	
		isRedraw = false;
		sliderSpeed.setValue(oldSpeed);	
		graphicTree.clearText();
		tree.enableBalance();
		switch (lastAction) {
		case INSERT:	
			if (tree.getRoot() == null) {
				lastResult = tree.insert(lastValue);
				graphicTree.insertRoot((INode)tree.getRoot());			
			} else {
				graphicTree.insertNode(tree.insert(lastValue), lastValue);
			}
			break;

		case DELETE:	
			graphicTree.deleteNode(tree.delete(lastValue), lastValue);
			break;		

		default:
			break;
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
			
		if (isAnimationDisable || paneTree.getChildren().isEmpty() || (listHistory.isEmpty() && lastAction == null)) {
			btnRepeat.setDisable(true);
		} else {
			if (!inputNumber.isDisable()) {
				btnRepeat.setDisable(false);
			}			
		}
	}
	
	/**
	 * Povolí manipulaci s tlačítkami po ukončení animace
	 */
	public void enableButtons() {		
		if (isRedraw) {
			if (++finishAnimation == listHistory.size()) {
				if (isLoad) {
					isRedraw = false;
					sliderSpeed.setValue(oldSpeed);
					listHistory.clear();
					lastAction = null;
					graphicTree.clearText();
					tree.enableBalance();
					isLoad = false;	
					enableButtons();
				} else {				
					repeatLastAction();
				}
			}			
			return;
		}
		
		if (randomTree) {
			graphicTree.clearText();
			graphicTree.showText();
			sliderSpeed.setValue(oldSpeed);
			randomTree = false;
		}		
		
		primaryStage.setResizable(true);
		
		inputNumber.setDisable(false);
		
		checkEnableButtons();
		
		btnTrees.setDisable(false);
		if (bpWindow.getLeft() != null) {
			bpWindow.getLeft().setDisable(false);
		}
		btnNewTree.setDisable(false);		
		
		sliderSpeed.setDisable(false);		
		
		inputNumber.clear();
		inputNumber.requestFocus();
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
		if (bpWindow.getLeft() != null) {
			bpWindow.getLeft().setDisable(true);
		}
		btnNewTree.setDisable(true);
		
		sliderSpeed.setDisable(true);
		inputNumber.setDisable(true);
	}
	
	/**
	 * Výpis stromu (pro testování)
	 */
	@SuppressWarnings("unused")
	private void treeLog() {
		paneTree.getChildren().forEach(x-> System.out.println(x));
		INode root = (INode)tree.getRoot();
		if (root == null) {
			return;
		}
		System.out.println(treeLogRecursion(root));
	}
	
	private String treeLogRecursion(INode n) {
		INode parent = (INode) n.getParent();
		String s;
		if (parent == null) {
			s = ""+ n.getValue() + " - " + n.getGraphicNode().getValue() + " = " + n.getGraphicNode() + " ->" + n.getGraphicNode().getStackPaneNode() +"\n";
		} else {
			s = ""+ n.getValue() + " - " + n.getGraphicNode().getValue() + " = " + n.getGraphicNode() + " ->" + n.getGraphicNode().getStackPaneNode() +  "\n rodič: " +
					parent.getValue() + " - " + parent.getGraphicNode().getValue() + " = " + parent.getGraphicNode() + " ->" + parent.getGraphicNode().getStackPaneNode() + "\n*********************************\n";
		}
		
		if (n.getLeft() != null) {
			if (n.getRight() != null) {
				return s + "\t" + treeLogRecursion((INode) n.getLeft()) + treeLogRecursion((INode) n.getRight());
			} else {
				return s + "\t" + treeLogRecursion((INode) n.getLeft());
			}
		} else if (n.getRight() != null) {
			return s + "\t" + treeLogRecursion((INode) n.getRight());		
		} else {
			return s;
		}
	}
}