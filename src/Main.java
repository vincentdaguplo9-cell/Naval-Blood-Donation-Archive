import javafx.application.Application;
import javafx.stage.Stage;
import ui.LoginFrame;

// JavaFX application entry point.
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        LoginFrame login = new LoginFrame();
        login.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
