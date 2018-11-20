import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// Controller for Server Connection / User Information window
public class ConnectController implements Initializable {
	@FXML
	private Label close;
	@FXML
	private TextField serverHN, serverPort, userHN, userName, userPort;
	@FXML
	private ChoiceBox speedDropMenu;

	private double x, y;

	private User user;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Fill options for Speed Dropdown Menu
		ObservableList<String> speedOptions = FXCollections.observableArrayList("DSL", "Ethernet", "T1", "T3",
				"Fiber Optic", "Wireless");
		speedDropMenu.setItems(speedOptions);

		// Preset Values for testing
		serverHN.setText("123.45.678.90");
		serverPort.setText("3158");
		userName.setText("johndoe");
		userHN.setText("147.85.236.90");
		userPort.setText("7894");
		speedDropMenu.getSelectionModel().selectFirst();

	}

	// Handles closing of window
	public void closeBtnAction() {
		Stage stage = (Stage) close.getScene().getWindow();
		System.out.println("Application closed.");
		stage.close();
		System.exit(1);
	}

	// Prints user input to system
	public void printConnectInput() {
		System.out.println("Server Hostname: " + serverHN.getText() + "\nServer Port: " + serverPort.getText()
				+ "\nUsername: " + userName.getText() + "\nUser Hostname: " + userHN.getText() + "\nUser Port: "
				+ userPort.getText() + "\nSpeed: " + speedDropMenu.getSelectionModel().getSelectedItem());
	}

	// Handles "Connect" Button. Submits connection/user information.
	// Changes window to FileTable window.
	public void connectBtnPushed(ActionEvent event) throws IOException {
		printConnectInput();
		user = new User();
		user.makeConnection(userName.getText(), serverHN.getText(), serverPort.getText(),
				(String) speedDropMenu.getSelectionModel().getSelectedItem(), userHN.getText(), userPort.getText());

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("FileTable.fxml"));
		Parent fileTableParent = loader.load();

		// Setup for window (stage) change.
//        Parent fileTableParent = FXMLLoader.load(getClass().getResource("FileTable.fxml"));
		Scene fileTableScene = new Scene(fileTableParent);

		FileTableController controller = loader.getController();
		controller.initData(user);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setScene(fileTableScene);

		// Handles "click and drag" functionality of window
		fileTableParent.setOnMousePressed(e -> {
			x = e.getSceneX();
			y = e.getSceneY();
		});
		fileTableParent.setOnMouseDragged(e -> {
			window.setX(e.getScreenX() - x);
			window.setY(e.getScreenY() - y);
		});

		window.show();
	}

}