package Aplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) throws Exception {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("Window.fxml"));
		Parent root = loader.load();

		WindowController controller = loader.getController();		

		primaryStage.setTitle("Stromy - Bin�rn� strom");
		primaryStage.setMinHeight(450);
		primaryStage.setMinWidth(831);
		primaryStage.setScene(new Scene(root, 815, 505));
		primaryStage.centerOnScreen();
		primaryStage.show();
		
		controller.setPrimaryStage(primaryStage);
	}

	public static void main(String[] args) {
		launch(args);	
	}
}