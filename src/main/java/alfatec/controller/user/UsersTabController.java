package alfatec.controller.user;

import java.io.IOException;

import com.jfoenix.controls.JFXButton;

import alfatec.dao.user.LoginDataDAO;
import alfatec.dao.user.UserDAO;
import alfatec.dao.wrappers.UserLoginDAO;
import alfatec.model.enums.RoleEnum;
import alfatec.model.user.LoginData;
import alfatec.model.user.User;
import alfatec.view.utils.Utility;
import alfatec.view.wrappers.UserLoginConnection;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import util.DateUtil;
import util.Password;

public class UsersTabController {

	@FXML
	private TableView<UserLoginConnection> usersTableView;

	@FXML
	private TableColumn<UserLoginConnection, String> userColumn;

	@FXML
	private LineChart<?, ?> activityChart;

	@FXML
	private VBox mainVbox, popupVbox;

	@FXML
	private Button closeButton, clearButton;

	@FXML
	private JFXButton addUserButton, editUserButton, deleteButton, saveUserButton;

	@FXML
	private Label usernameLabel, roleLabel, emailLabel, contactLabel, dateCreatedLabel, totalAddedLabel;

	@FXML
	private TextField searchUserTextField, firstNameTextField, lastNameTextField, emailTextField, contactTextField;

	@FXML
	private Label firstNameErrorLabel, lastNameErrorLabel, emailErrorLabel, contactErrorLabel, roleErrorLabel,
			passwordErrorLabel, confirmPasswordErrorLabel;

	@FXML
	private ComboBox<RoleEnum> roleComboBox;

	@FXML
	private PasswordField passwordField, confirmPasswordField;

	private static final int FIRST_NAME_LENGTH = 30;
	private static final int LAST_NAME_LENGTH = 50;
	private static final int EMAIL_LENGTH = 50;
	private static final int PASSWORD_LENGTH = 50;

	private UserLoginConnection userData;
	private ObservableList<UserLoginConnection> users;
	String role;
	String email;
	private boolean editAction;

	@FXML
	private void initialize() throws IOException {
		populateUsersTable();
		Utility.setUpStringCell(usersTableView);
		usersTableView.setItems(users);
		handleSearch();
	}

	private void populateUsersTable() {
		users = UserLoginDAO.getInstance().getAllData();
		userColumn.setCellValueFactory(cellData -> cellData.getValue().getUser().getUserFirstNameProperty().concat(" ")
				.concat(cellData.getValue().getUser().getUserLastNameProperty()));
		usersTableView.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) {
					UserLoginConnection userData = usersTableView.getSelectionModel().getSelectedItem();
					transitionPopupX(mainVbox, 1200, 0, Interpolator.EASE_IN, 500);
					mainVbox.setVisible(true);
					usernameLabel.setText(userData.getUser().getUserFirstName());
					roleLabel.setText(userData.getLoginData().getRoleName());
					emailLabel.setText(userData.getLoginData().getUserEmail());
					contactLabel.setText(userData.getUser().getContactTelephone());
					dateCreatedLabel.setText(DateUtil.format(userData.getUser().getCreatedTimeProperty().get()));
				}
			}
		});
	}

	@FXML
	void addUser(ActionEvent event) {
		closePopup();
		transitionPopupX(popupVbox, 940, 0, Interpolator.EASE_IN, 500);
		roleComboBox.setItems(FXCollections.observableArrayList(RoleEnum.values()));
		popupVbox.setVisible(true);
	}

	@FXML
	void editUser(ActionEvent event) {
		closePopup();
		isEditAction(true);
		UserLoginConnection user = usersTableView.getSelectionModel().getSelectedItem();
		if (user != null) {
			transitionPopupX(popupVbox, 340, 0, Interpolator.EASE_IN, 500);
			roleComboBox.setItems(FXCollections.observableArrayList(RoleEnum.values()));
			setUser(user);
			email = user.getLoginData().getUserEmail();
			popupVbox.setVisible(true);
		}
	}

	@FXML
	void deleteUser(ActionEvent event) {
		UserLoginConnection userData = usersTableView.getSelectionModel().getSelectedItem();
		if (userData != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirm action:");
			alert.setHeaderText("Are you sure you want to delete credentials for user "
					+ userData.getUser().getUserFirstName().concat(" ").concat(userData.getUser().getUserLastName())
					+ "?");
			alert.showAndWait();
			if (alert.getResult() == ButtonType.OK) {
				UserDAO.getInstance().deleteUser(userData.getUser());
				UserDAO.getInstance().getAllUsers().remove(userData.getUser());
				users.remove(userData);
				usersTableView.getItems().remove(userData);
				int row = users.size() - 1;
				usersTableView.requestFocus();
				usersTableView.getSelectionModel().select(row);
				usersTableView.scrollTo(row);
				closeDetails();
			}
		}
	}

	private void handleEditUser() {
		setFirstName();
		setLastName();
		setContact();
		setEmail();
		setRoleType();
		setPassword();
		usersTableView.refresh();
	}

	private void handleSearch() {
		FilteredList<UserLoginConnection> filteredData = new FilteredList<>(users, p -> true);
		searchUserTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredData.setPredicate(UserLoginConnection -> {
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}
				String lowerCaseFilter = newValue.toLowerCase();
				if (cyrillicToLatin(String.valueOf(UserLoginConnection.getUser().getUserFirstName()).toLowerCase())
						.startsWith(lowerCaseFilter)) {
					return true;
				} else if (cyrillicToLatin(
						String.valueOf(UserLoginConnection.getUser().getUserLastName()).toLowerCase())
								.startsWith(lowerCaseFilter)) {
					return true;
				} else if (String.valueOf(UserLoginConnection.getLoginData().getUserEmail()).toLowerCase()
						.startsWith(lowerCaseFilter)) {
					return true;
				}
				return false;
			});
		});
		SortedList<UserLoginConnection> sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(usersTableView.comparatorProperty());
		usersTableView.setItems(sortedData);
	}

	@FXML
	void saveUser(ActionEvent event) {
		if (isValidInput() && !isEmailAlreadyInDB() && !isEditAction() && isValidConfirmPassword()) {
			UserLoginConnection userData = getNewUser();
			users.add(userData);
			usersTableView.refresh();
			usersTableView.requestFocus();
			int row = users.size() - 1;
			usersTableView.getSelectionModel().select(row);
			usersTableView.scrollTo(row);
			closePopup();
		} else if (isValidInput() && isEditAction() && isValidConfirmPassword()) {
			if (isEmailAlreadyInDB()) {
				if (emailTextField.getText().equals(email)) {
					handleEditUser();
					usersTableView.refresh();
					usersTableView.requestFocus();
					int row = users.size() - 1;
					usersTableView.getSelectionModel().select(row);
					usersTableView.scrollTo(row);
					closePopup();
				} else {
					emailErrorLabel.setText("Already exists.");
				}
			} else {
				handleEditUser();
				usersTableView.refresh();
				usersTableView.requestFocus();
				int row = users.size() - 1;
				usersTableView.getSelectionModel().select(row);
				usersTableView.scrollTo(row);
				closePopup();
			}

		} else if (isEmailAlreadyInDB()) {
			emailErrorLabel.setText("Already exists.");
		}
	}

	private boolean isEditAction() {
		return editAction;
	}

	private void isEditAction(boolean action) {
		this.editAction = action;
	}

	public void setUser(UserLoginConnection userData) {
		this.userData = userData;
		firstNameTextField.setText(userData.getUser().getUserFirstName());
		lastNameTextField.setText(userData.getUser().getUserLastName());
		emailTextField.setText(userData.getLoginData().getUserEmail());
		contactTextField.setText(userData.getUser().getContactTelephone());
		passwordField.setText(userData.getLoginData().getPasswordHash());
		confirmPasswordField.setText(userData.getLoginData().getPasswordHash());
		roleComboBox.setItems(FXCollections.observableArrayList(RoleEnum.values()));
		roleComboBox.getSelectionModel().select(userData.getLoginData().getRoleID() - 1);
	}

	public UserLoginConnection getNewUser() {
		if (isValidInput()) {
			User user = UserDAO.getInstance().createUser(firstNameTextField.getText(), lastNameTextField.getText(),
					contactTextField.getText());
			LoginData ld = LoginDataDAO.getInstance().createLoginData(emailTextField.getText(), passwordField.getText(),
					user.getUserID(), roleComboBox.getSelectionModel().getSelectedItem().getRoleID());
			userData = new UserLoginConnection(user, ld);
		}
		return userData;
	}

	private boolean isValidInput() {
		firstNameErrorLabel.setText((isValidFirstName() && firstNameTextField.getLength() < FIRST_NAME_LENGTH) ? ""
				: "Empty first name field.");
		lastNameErrorLabel.setText((isValidLastName() && lastNameTextField.getLength() < LAST_NAME_LENGTH) ? ""
				: "Empty last name field.");
		emailErrorLabel.setText(
				(isValidEmail() && emailTextField.getLength() < EMAIL_LENGTH) ? "" : "Empty or invalid email field.");
		roleErrorLabel.setText(isValidRole() ? "" : "Please select your role");
		passwordErrorLabel
				.setText((isValidPassword() && !editAction ? passwordField.getLength() < PASSWORD_LENGTH : true) ? ""
						: "Empty or invalid password field.");
		confirmPasswordErrorLabel.setText(isValidConfirmPassword() ? "" : "Empty or invalid confirm password field.");

		return isValidFirstName() && isValidLastName() && isValidEmail() && isValidRole() && isValidPassword()
				&& isValidConfirmPassword();
	}

	public static String cyrillicToLatin(String text) {
		char[] abcCyr = { 'а', 'б', 'в', 'г', 'д', 'ђ', 'е', 'ж', 'з', 'и', 'ј', 'к', 'л', 'љ', 'м', 'н', 'њ', 'о', 'п',
				'р', 'с', 'т', 'ћ', 'у', 'ф', 'х', 'ц', 'ч', 'џ', 'ш' };
		String[] abcLat = { "a", "b", "v", "g", "d", "đ", "e", "ž", "z", "i", "j", "k", "l", "lj", "m", "n", "nj", "o",
				"p", "r", "s", "t", "ć", "u", "f", "h", "c", "č", "dž", "š" };
		StringBuilder builder = new StringBuilder();
		outer: for (int i = 0; i < text.length(); i++) {
			for (int x = 0; x < abcCyr.length; x++)
				if (text.charAt(i) == abcCyr[x]) {
					builder.append(abcLat[x]);
					continue outer;
				}
			builder.append(text.charAt(i));
		}
		return builder.toString();
	}

	private boolean isValidFirstName() {
		return firstNameTextField.getText() != null && firstNameTextField.getText().length() != 0;
	}

	private boolean isValidLastName() {
		return lastNameTextField.getText() != null && lastNameTextField.getText().length() != 0;
	}

	private boolean isValidEmail() {
		return emailTextField.getText() != null && emailTextField.getText().length() != 0
				&& emailTextField.getText().contains(".") && emailTextField.getText().contains("@");
	}

	private boolean isEmailAlreadyInDB() {
		return LoginDataDAO.getInstance().findUserDataByExactEmail(emailTextField.getText()) != null;
	}

	private boolean isValidRole() {
		return roleComboBox.getSelectionModel().getSelectedItem() != null;
	}

	private boolean isValidPassword() {
		return passwordField.getText() != null && passwordField.getText().length() != 0;
	}

	private boolean isValidConfirmPassword() {
		return confirmPasswordField.getText() != null && confirmPasswordField.getText().equals(passwordField.getText());
	}

	public void setFirstName() {
		if (!firstNameTextField.getText().equalsIgnoreCase(userData.getUser().getUserFirstName()))
			UserDAO.getInstance().updateUserFirstName(userData.getUser(), firstNameTextField.getText());
	}

	public void setLastName() {
		if (!lastNameTextField.getText().equalsIgnoreCase(userData.getUser().getUserLastName()))
			UserDAO.getInstance().updateUserLastName(userData.getUser(), lastNameTextField.getText());
	}

	public void setEmail() {
		if (!emailTextField.getText().equalsIgnoreCase(userData.getLoginData().getUserEmail()))
			LoginDataDAO.getInstance().updateEmail(userData.getLoginData(), emailTextField.getText());
	}

	public void setContact() {
		if (!contactTextField.getText().equalsIgnoreCase(userData.getUser().getContactTelephone()))
			UserDAO.getInstance().updateUserTelephone(userData.getUser(), contactTextField.getText());
	}

	public void setRoleType() {
		if (!roleComboBox.getSelectionModel().getSelectedItem().name()
				.equalsIgnoreCase(userData.getLoginData().getRoleName()))
			LoginDataDAO.getInstance().updateRole(userData.getLoginData(),
					roleComboBox.getSelectionModel().getSelectedItem().name().toUpperCase());
	}

	public void setPassword() {
		if (!Password.checkPassword(passwordField.getText(), userData.getLoginData().getPasswordHash()))
			LoginDataDAO.getInstance().updatePassword(userData.getLoginData(), passwordField.getText());
	}

	private void transitionPopupX(Node node, double endingCoordinate, double startingCoordinate,
			Interpolator interpolator, int durrationInMillis) {
		node.translateXProperty().set(endingCoordinate);
		KeyValue kv = new KeyValue(node.translateXProperty(), startingCoordinate, interpolator);
		KeyFrame kf = new KeyFrame(Duration.millis(durrationInMillis), kv);
		Timeline tl = new Timeline(kf);
		tl.play();
	}

	@FXML
	private void clearPopup(ActionEvent event) {
		firstNameTextField.clear();
		lastNameTextField.clear();
		emailTextField.clear();
		contactTextField.clear();
		roleComboBox.setValue(RoleEnum.USER);
		passwordField.clear();
		confirmPasswordField.clear();
	}

	@FXML
	private void closePopup(ActionEvent event) {
		transitionPopupX(popupVbox, 0, 340, Interpolator.EASE_IN, 500);
		firstNameTextField.clear();
		lastNameTextField.clear();
		emailTextField.clear();
		contactTextField.clear();
		roleComboBox.setValue(RoleEnum.USER);
		passwordField.clear();
		confirmPasswordField.clear();
	}

	private void closePopup() {
		transitionPopupX(popupVbox, 0, 340, Interpolator.EASE_IN, 500);
		firstNameTextField.clear();
		lastNameTextField.clear();
		emailTextField.clear();
		contactTextField.clear();
		roleComboBox.setValue(RoleEnum.USER);
		passwordField.clear();
		confirmPasswordField.clear();
	}

	void closeDetails() {
		transitionPopupX(mainVbox, 0, 1200, Interpolator.EASE_IN, 500);
	}

}
