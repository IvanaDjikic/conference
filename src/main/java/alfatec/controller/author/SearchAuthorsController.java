package alfatec.controller.author;

import com.jfoenix.controls.JFXButton;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class SearchAuthorsController {

	@FXML
	private TableView<?> authorsTableView;

	@FXML
	private TableColumn<?, ?> authorNameColumn;

	@FXML
	private TableColumn<?, ?> authorLastNameColumn;

	@FXML
	private TableColumn<?, ?> institutionColumn;

	@FXML
	private TableColumn<?, ?> institutionNameColumn;

	@FXML
	private TableColumn<?, ?> emailColumn;

	@FXML
	private TableColumn<?, ?> countryColumn;

	@FXML
	private TextField searchAuthorTextField;

	@FXML
	private JFXButton selectAuthorButton;

	@FXML
	private Button closeButton;

	private Stage display;
	private double x = 0;
	private double y = 0;
	private Node node;

	@FXML
	void pressed(MouseEvent event) {
		x = event.getSceneX();
		y = event.getSceneY();
	}

	@FXML
	void dragged(MouseEvent event) {
		node = (Node) event.getSource();
		display = (Stage) node.getScene().getWindow();
		display.setX(event.getScreenX() - x);
		display.setY(event.getScreenY() - y);
	}

	@FXML
	void close(ActionEvent event) {
		Node node = (Node) event.getSource();
		Stage stage = (Stage) node.getScene().getWindow();
		stage.close();
	}

	@FXML
	void selectAuthor(ActionEvent event) {

	}

	public void setDisplayStage(Stage stage) {
		this.display = stage;
	}

}
