
import java.io.InputStream;
import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Main extends Application {

//Flag to prevent opening the same port multiple times
public static int opened = 0;

//Arduino ports and input stream
public static SerialPort serialposture = SerialPort.getCommPort("COM9");
public static InputStream posturein = serialposture.getInputStream();
public static SerialPort sp = SerialPort.getCommPort("COM8");
public static InputStream in = sp.getInputStream();

public static void main(String[] args) {
	launch(args);
}

public void start(Stage primaryStage) throws Exception {
	try {

		//Open port
		if(opened==0) {
			serialposture.openPort(0);
			sp.openPort();
			opened=1;
		}

		//Load FXML
		Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
		Scene scene = new Scene(root);

		//Disallow resize
		primaryStage.setResizable(false);
		primaryStage.setScene(scene);

		//Set application icon
		Image image = new Image("resources/Images/main/homeicon.png");
		primaryStage.getIcons().add(image);

		//Application title
		primaryStage.setTitle("Smart Furniture");
		primaryStage.show();
		
		
	} catch(Exception e) {
		e.printStackTrace();
	}
}

@Override
public void stop() throws Exception{

		//Ending the application properly
		super.stop();

		//Closing all opened ports and input streams
		serialposture.closePort();
		posturein.close();
		sp.closePort();
		in.close();

		//Stop refreshing the data once application close
		controller.scheduledExecutorService.shutdownNow();
	    controller.menutimer.cancel();

	    if(controller.chairdetailopened == 1) {
			chairinfoController.chairscheduledExecutorService.shutdownNow();
			chairinfoController.chairtimer.cancel();
		}

	    if(controller.humiditydetailopened == 1) {
			humidityController.humidityscheduledExecutorService.shutdownNow();
			humidityController.humiditytimer.cancel();
		}

}

}
