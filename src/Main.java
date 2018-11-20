
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
	private double x, y;

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Setup for app primary settings
		Parent root = FXMLLoader.load(getClass().getResource("Connect.fxml"));
		primaryStage.setScene(new Scene(root));
		primaryStage.initStyle(StageStyle.UNDECORATED);

		// Handles "click and drag" functionality of window
		root.setOnMousePressed(e -> {
			x = e.getSceneX();
			y = e.getSceneY();
		});
		root.setOnMouseDragged(e -> {
			primaryStage.setX(e.getScreenX() - x);
			primaryStage.setY(e.getScreenY() - y);
		});

		primaryStage.show();
	}

	// Launch application
	public static void main(String[] args) {
		launch(args);
	}
}
