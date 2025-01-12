
import java.io.IOException;
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

public class humidityController implements Initializable{

	//FXML gui objects
	@FXML AnchorPane main;
	@FXML Button buttontomain;
	@FXML Label currenthumidity, humidityreading, currenttemperature, temperaturereading;
	@FXML ImageView humidityimage, tempimage, humiditylevelimage, templevelimage;

	//Timer for starting refresh
	public static Timer humiditytimer = new Timer();
	//To refresh displayed data
	public static ScheduledExecutorService humidityscheduledExecutorService;

	//Posture variables
	private char[] inputstring = new char[50];
	private int inputstringcount = 0;
	private char[] temp = new char[4];
    private char[] humidity = new char[4];
    private int tempcount = 0;
    private int humidcount = 0;
    private double humidityvalue;
    private double tempvalue;

    //Read and display humidity and temperature data
	private void humidity() {

		//Read incoming data from humidity Arduino
		try
	    {
	        char comms = (char)Main.in.read();
			inputstring[inputstringcount] = comms;
			inputstringcount++;
			while(comms != '#') {
				comms = (char)Main.in.read();
				inputstring[inputstringcount] = comms;
				inputstringcount++;
			}

			inputstringcount = 0;

	    } 
		
		catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Humidity exception");
	    }

		//Extracting humidity and temperature value from input string
		for(int x = 0; x<4; x++) {
			humidity[x] = inputstring[12+x];
			temp[x] = inputstring[26+x];
		}
    	
    	if(Character.isDigit(humidity[0])) {
    		String humiditystring = new String(humidity);
    		humidityvalue = Double.parseDouble(humiditystring);
    	}
    	
    	humidityreading.setText(humidityvalue + "%");

    	//Display depending on current humidity
    	if(humidityvalue>=75) {
    		Image image = new Image("resources/Images/humidityinfo/humid2.png");
    		humidityimage.setImage(image);
    		Image image2 = new Image("resources/Images/humidityinfo/humid.png");
    		humiditylevelimage.setImage(image2);
    		currenthumidity.setText("HIGH");
    	}
    	else if(humidityvalue>=50){
    		Image image = new Image("resources/Images/humidityinfo/dry2.png");
    		humidityimage.setImage(image);
    		Image image2 = new Image("resources/Images/humidityinfo/dry.png");
    		humiditylevelimage.setImage(image2);
    		currenthumidity.setText("MEDIUM");
    	}
    	else {
    		Image image = new Image("resources/Images/humidityinfo/dry2.png");
    		humidityimage.setImage(image);
    		Image image2 = new Image("resources/Images/humidityinfo/dry.png");
    		humiditylevelimage.setImage(image2);
    		currenthumidity.setText("LOW");
    	}
    	
    	if(Character.isDigit(temp[0])) {
    		String tempstring = new String(temp);
    		tempvalue = Double.parseDouble(tempstring);
    	}
    	
    	temperaturereading.setText(tempvalue + " Degree Celsius");

    	//Display depending on current temperature
    	if(tempvalue>=35) {
    		Image image = new Image("resources/Images/humidityinfo/hot.png");
    		tempimage.setImage(image);
    		Image image2 = new Image("resources/Images/humidityinfo/hotlevel.png");
    		templevelimage.setImage(image2);
    		currenttemperature.setText("HOT");
    	}
    	else if(tempvalue>=23){
    		Image image = new Image("resources/Images/humidityinfo/neutral.png");
    		tempimage.setImage(image);
    		Image image2 = new Image("resources/Images/humidityinfo/neutral.png");
    		templevelimage.setImage(image2);
    		currenttemperature.setText("COOL");
    	}
    	else {
    		Image image = new Image("resources/Images/humidityinfo/cold.png");
    		tempimage.setImage(image);
    		Image image2 = new Image("resources/Images/humidityinfo/coldlevel.png");
    		templevelimage.setImage(image2);
    		currenttemperature.setText("COLD");
    	}
    	
    	if(Character.isDigit(temp[0])) {
    		String tempstring = new String(temp);
    		tempvalue = Double.parseDouble(tempstring);
    	}

    	humidcount = 0;
    	tempcount = 0;
    }

    //Display data
	private void furniturestatus() {

		//Refresh data every 500 milliseconds
		humidityscheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

		humidityscheduledExecutorService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
            	
            	humidity();
            		
            });
        }, 0, 1, TimeUnit.SECONDS);
		
	}

	//Move to main panel
	public void humiditytomain() {

		//Stop the refreshing while transition animation is happening
		if(controller.midtransition==0) {
			
			controller.midtransition=1;
			humidityscheduledExecutorService.shutdownNow();

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
					humiditytomain();
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

		//Starting the refresh once the panel has been fully loaded
		humiditytimer.schedule(
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
