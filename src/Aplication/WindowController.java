package Aplication;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import Graphic.DrawingTree;
import Trees.BinaryNode;
import Trees.BinaryTree;
import Trees.ITree;
import Trees.Result;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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

	@FXML
	Button btnTrees;
	@FXML
	Button btnTreesOld = null;

	@FXML
	Button btnSearch;
	@FXML
	Button btnDelete;
	@FXML
	Button btnInsert;
	@FXML
	Button btnRepeat;

	@FXML
	TextField inputNumber;
	@FXML
	Slider sliderSpeed;

	@FXML
	HBox hBoxInput;
	@FXML
	VBox treesMenu;
/*	@FXML
	VBox vBox;*/

	@FXML
	BorderPane bpWindow;
	@FXML
	BorderPane menu;
	@FXML
	BorderPane borderPaneTree;
	@FXML
	Pane paneTree;

/*	@FXML
	ScrollPane scrollPane;
	@FXML
	ScrollBar sc;	*/

	ITree<?> actualTree;
	DrawingTree graphicTree;
	
	BinaryTree t = new BinaryTree(5);
	DrawingTree d;	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		hideMenu();
		numberOnly();
		sliderFormat();
	}

	

	private void numberOnly() {
		inputNumber.setTooltip(new Tooltip("Zadávejte hodnoty od 0 - 1000"));
		inputNumber.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("(\\d*)")) {					
					inputNumber.setText(newValue.replaceAll("[^\\d]", ""));	
					inputNumber.getTooltip().show(primaryStage); //TODO
					//inputNumber.getTooltip().hid
				} 
				if (!inputNumber.getText().isEmpty()) {
					btnInsert.setDisable(false);
				} else {
					btnInsert.setDisable(true);
				}
			}
		});
	}
	
	private void sliderFormat() {
		sliderSpeed.setSnapToTicks(true);
		sliderSpeed.setShowTickMarks(true);
        sliderSpeed.setShowTickLabels(true);
        sliderSpeed.setMinorTickCount(0);
        
        
		sliderSpeed.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n == 0) return "VYP";
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

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		sliderSpeed.setValue(25);
		
		d = new DrawingTree(t, paneTree, sliderSpeed.valueProperty(), primaryStage.widthProperty(), this);
		d.insertRoot(t.getRoot().getGraphicNode());	//TODO
		btnSearch.setDisable(false);
	}

	@FXML
	public void insertNumber() {
		disableButtons();
		Result<BinaryNode> n = t.insert(Integer.parseInt(inputNumber.getText()));		
		d.insertNode(n);
	}
	
	@FXML
	public void searchNumber() {
		disableButtons();
		Result<BinaryNode> n = t.search(Integer.parseInt(inputNumber.getText()));		
		d.searchNode(n);
	}

	@FXML
	public void deleteNumber() {
		disableButtons();
		Result<BinaryNode> n = t.delete(Integer.parseInt(inputNumber.getText()));		
		d.deleteNode(n);
	}
	
	@FXML 
	public void newTree() {
		//TODO
	}	

	@FXML
	public void showMenu() {
		bpWindow.setLeft(treesMenu);
		menu.setLeft(null);

		FadeTransition showFileRootTransition = new FadeTransition(Duration.millis(500), treesMenu);
		showFileRootTransition.setFromValue(0.0);
		showFileRootTransition.setToValue(1.0);

		showFileRootTransition.play();
	}

	@FXML
	public void hideMenu() {
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

	@FXML
	public void textFieldValidator() {
		
	}

	@FXML
	public void changeTree(ActionEvent event) throws IOException {
		Button selectedButton = (Button) event.getTarget();
		selectedButton.getStyleClass().clear();
		selectedButton.getStyleClass().add("tree-button-focus");

		if (btnTreesOld != null) {
			btnTreesOld.getStyleClass().clear();
			btnTreesOld.getStyleClass().add("tree-button");
		}

		primaryStage.setTitle("Trees - " + selectedButton.getText());

		btnTreesOld = selectedButton;
		hideMenu();
	}

	@FXML
	public void repeatLastAnimation() {
	}
	
	public void enableButtons() {
		btnInsert.setDisable(false);
		btnDelete.setDisable(false);
		btnRepeat.setDisable(false);
		btnSearch.setDisable(false);
		btnTrees.setDisable(false);
		inputNumber.setDisable(false);
		sliderSpeed.setDisable(false); 
		inputNumber.clear();
	}
	
	private void disableButtons() {
		btnInsert.setDisable(true);
		btnDelete.setDisable(true);
		btnRepeat.setDisable(true);
		btnSearch.setDisable(true);
		btnTrees.setDisable(true);
		sliderSpeed.setDisable(true);
		inputNumber.setDisable(true);
	}
}