package alfatec;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	private static Main instance;
	private Stage primaryStage;
	
	public Main() {
		instance = this;
	}

	public static void main(String[] args) {
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		Group root = new Group();
		Scene scene = new Scene(root);
		Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("resources/fxml/login.fxml"));
		root.getChildren().addAll(parent);
		scene.getStylesheets()
				.add(getClass().getClassLoader().getResource("resources/styles/login_style.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.setResizable(false);
		primaryStage.show();

	}

	public static Main getInstance() {
		return instance;
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

}
