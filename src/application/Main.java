package application;

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

		primaryStage.setTitle("Stromy - Binární vyhledávací strom");
		primaryStage.setMinHeight(700);
		primaryStage.setMinWidth(1131);
		primaryStage.setScene(new Scene(root, 1130, 700));
		primaryStage.centerOnScreen();
		primaryStage.show();
		
		controller.setPrimaryStage(primaryStage);
	}

	public static void main(String[] args) {
		launch(args);	
	}
}