
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class chairinfoController implements Initializable{

	//FXML gui objects
	@FXML private AnchorPane main;
	@FXML private Button buttontomain;
	@FXML private Label posturelabel, postureadjust;
	@FXML private ImageView mainstatus, backstatus, lowerbackstatus;

	//Timer for chair refresh
	public static Timer chairtimer = new Timer();

	//To refresh displayed data
	public static ScheduledExecutorService chairscheduledExecutorService;

	//posture variables
	private char[] currentposture = new char[50];
	private int posturestringcount = 0;

	//Read and display posture data
	private void posture() {

		//Read incoming data from posture Arduino
    	try {

			char comms = (char)Main.posturein.read();
			currentposture[posturestringcount] = comms;
			posturestringcount++;


			while(comms != '#') {
				comms = (char) Main.posturein.read();
				currentposture[posturestringcount] = comms;
				posturestringcount++;
			}

			Main.posturein.close();

		} catch (IOException e) {
    		//e.printStackTrace();
			System.out.println("Posture exception");
		}

		posturestringcount=0;

    	//Final posture code received
		int posturecode = Character.getNumericValue(currentposture[2]);

		//Display data depending on current posture
    	if(posturecode == 0) {
    		posturelabel.setText("NOT SEATED");
    		postureadjust.setText("No adjustments required");
    		Image image = new Image("resources/Images/chairinfo/greymedium.png");
    		mainstatus.setImage(image);
    		backstatus.setImage(image);
    		lowerbackstatus.setImage(image);
    	}
    	else if(posturecode == 1) {
    		posturelabel.setText("GOOD POSTURE");
    		postureadjust.setText("No adjustments required");
    		Image image = new Image("resources/Images/chairinfo/correct.png");
    		mainstatus.setImage(image);
    		backstatus.setImage(image);
    		lowerbackstatus.setImage(image);
    	}
    	else {

    		posturelabel.setText("NOT SEATED");

    		if(posturecode == 2) {
				posturelabel.setText("BAD POSTURE");
    			postureadjust.setText("Please lean your hips against the chair");
        		Image image = new Image("resources/Images/chairinfo/correct.png");
        		backstatus.setImage(image);
        		Image image2 = new Image("resources/Images/chairinfo/warning.png");
        		lowerbackstatus.setImage(image2);
        		mainstatus.setImage(image2);
    		}
    		else if(posturecode == 3) {
				posturelabel.setText("BAD POSTURE");
    			postureadjust.setText("Please lean your back against the chair");
        		Image image = new Image("resources/Images/chairinfo/warning.png");
        		backstatus.setImage(image);
        		mainstatus.setImage(image);
        		Image image2 = new Image("resources/Images/chairinfo/correct.png");
        		lowerbackstatus.setImage(image2);
    		}
    		else if(posturecode == 5) {
				posturelabel.setText("BAD POSTURE");
    			postureadjust.setText("Please lean your back and hips against the chair");
        		Image image = new Image("resources/Images/chairinfo/warning.png");
        		mainstatus.setImage(image);
        		backstatus.setImage(image);
        		lowerbackstatus.setImage(image);
    		}
    	}

	}

	//Display data
	private void furniturestatus() {

		//Refresh data every 500 milliseconds
		chairscheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

		chairscheduledExecutorService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {

            	posture();

            });
        }, 0, 500, TimeUnit.MILLISECONDS);

	}

	//Move to pain panel
	public void chairtomain() {

		//Stop refreshing while transition animation is happening
		if(controller.midtransition==0) {

			controller.midtransition=1;
			chairscheduledExecutorService.shutdownNow();

			//Fade animation when transitioning
			FadeTransition fadeTransition = new FadeTransition();
			fadeTransition.setDuration(Duration.millis(500));
			fadeTransition.setNode(main);
			fadeTransition.setFromValue(1);
			fadeTransition.setToValue(0);
			fadeTransition.setOnFinished(event -> {
				try {
					tomainmenu();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			fadeTransition.play();
		}
	}

	//Setting main panel
	public void tomainmenu() throws IOException {

		Parent newpane = FXMLLoader.load(getClass().getResource("gui.fxml"));
		Scene newscene = new Scene(newpane);

		Stage current = (Stage) main.getScene().getWindow();
		current.setScene(newscene);
	}

	//Fade animation when transitioning into this panel
	private void fadeinanimation() {
		FadeTransition fadeTransition = new FadeTransition();
		fadeTransition.setDuration(Duration.millis(500));
		fadeTransition.setNode(main);
		fadeTransition.setFromValue(0);
		fadeTransition.setToValue(1);

		//Re-enable transitioning to other panels once animation complete
		fadeTransition.setOnFinished(event -> {
			buttontomain.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					chairtomain();
				}
			});
		});
		fadeTransition.play();
	}

	//When the panel is loaded
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		//Flushing the input stream in case of outdated data
		try {
			while(Main.posturein.available()!=0){
				Main.posturein.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			while(Main.in.available()!=0){
				Main.in.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Disable transition to other panels until current panel is fully loaded
		buttontomain.setOnAction(null);

		//Flag to check mid transition
		controller.midtransition=0;

		//Fade in animation
		main.setOpacity(0);
		fadeinanimation();

		//Starting the refresh once panel has been fully loaded
		chairtimer.schedule(
		        new TimerTask() {
		            @Override
		            public void run() {
		        		furniturestatus();
		            }
		        },
		        500
		);

	}

}
