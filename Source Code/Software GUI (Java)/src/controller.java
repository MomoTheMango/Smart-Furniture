
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class controller implements Initializable{

	//FXML gui objects
	@FXML public Label humiditystatus, posturelabel, postureadjust;
	@FXML private Button buttontochair, buttontohumidity;
	@FXML private ImageView posturelevel, humiditylevel;
	@FXML private AnchorPane helppane, main;

	//Visibility flag of help pane
	private int helpvisible = 1;
	//Flag to check if panel mid transition
	public static int midtransition=0;
	//Timer for starting refresh
	public static Timer menutimer = new Timer();
	//Flag to check access to chair details
	public static int chairdetailopened = 0;
	//Flag to check access to humidity details
	public static int humiditydetailopened = 0;
	//To refresh displayed data
	public static ScheduledExecutorService scheduledExecutorService;
	
	//Posture variables
	private char[] currentposture = new char[50];
	private int posturestringcount = 0;

	//Humidity variables
	private char[] inputstring = new char[50];
	private int inputstringcount = 0;
    private char[] temp = new char[4];
    private char[] humidity = new char[4];
    private int tempcount = 0;
    private int humidcount = 0;
    private double humidityvalue;

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
            		posturelabel.setText("Not Seated");
            		postureadjust.setText("No adjustments required");
            		Image image = new Image("resources/Images/main/grey.png");
            		posturelevel.setImage(image);
            	}
            	else if(posturecode == 1) {
            		posturelabel.setText("Good Posture");
            		postureadjust.setText("No adjustments required");
            		
            		File file = new File("/resources/Images/main/correct.png");
            		Image image = new Image("/resources/Images/main/correct.png");
            		posturelevel.setImage(image);
            	}
            	else {
            		
            		posturelabel.setText("Not Seated");
            		
            		if(posturecode == 2) {
						posturelabel.setText(" Bad Posture");
            			postureadjust.setText("Please lean your hips against the chair");
            			File file = new File("resources/Images/main/warning.png");
                		Image image = new Image("resources/Images/main/warning.png");
                		posturelevel.setImage(image);
            		}
            		else if(posturecode == 3) {
						posturelabel.setText(" Bad Posture");
            			postureadjust.setText("Please lean your back against the chair");
            			File file = new File("resources/Images/main/warning.png");
                		Image image = new Image("resources/Images/main/warning.png");
                		posturelevel.setImage(image);
            		}
            		else if(posturecode == 5) {
						posturelabel.setText(" Bad Posture");
            			postureadjust.setText("Please lean your back and hips against the chair");
            			File file = new File("resources/Images/main/warning.png");
                		Image image = new Image("resources/Images/main/warning.png");
                		posturelevel.setImage(image);
            		}
            	}
            	
    }

    //Read and display humidity data
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

    	//Display depending on current humidity
		if(humidityvalue>=75) {
    		Image image = new Image("resources/Images/main/humid.png");
    		humiditylevel.setImage(image);
    		humiditystatus.setText("HIGH");
    	}
    	else if(humidityvalue>=50){
    		Image image = new Image("resources/Images/main/dry.png");
    		humiditylevel.setImage(image);
    		humiditystatus.setText("MEDIUM");
    	}
    	else {
    		Image image = new Image("resources/Images/main/dry.png");
    		humiditylevel.setImage(image);
    		humiditystatus.setText("LOW");
    	}

    	humidcount = 0;
    	tempcount = 0;
    }

	//Display data
	private void furniturestatus() {

    	//Refresh data every 500 milliseconds
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

		scheduledExecutorService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
            	
            	humidity();
            	posture();
            		
            });
        }, 0, 500, TimeUnit.MILLISECONDS);
		
	}

	//Move to chair details panel
	public void maintochair() {

    	//Flag to check access to chair details
		chairdetailopened = 1;

		//Stop the refreshing while transition animation is happening
		if(midtransition==0) {
		
			midtransition=1;
			scheduledExecutorService.shutdownNow();

			//Fade animation when transitioning
			FadeTransition fadeTransition = new FadeTransition();
			fadeTransition.setDuration(Duration.millis(500));
			fadeTransition.setNode(main);
			fadeTransition.setFromValue(1);
			fadeTransition.setToValue(0);
			fadeTransition.setOnFinished(event -> {
				try {
					tochairinfo();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			fadeTransition.play();
		}
	}

	//Setting chair details panel
	public void tochairinfo() throws IOException {
		
		Parent newpane = FXMLLoader.load(getClass().getResource("chairinfo.fxml"));
		Scene newscene = new Scene(newpane);
		
		Stage current = (Stage) main.getScene().getWindow();
		current.setScene(newscene);
	}

	//Move to humidity details panel
	public void maintohumidity() {

    	//Flag to check access to humidity details
    	humiditydetailopened = 1;

    	//Stop the refreshing while transition animation is happening
		if(midtransition==0) {
			
			midtransition=1;
			scheduledExecutorService.shutdownNow();

			//Fade animation when transitioning
			FadeTransition fadeTransition = new FadeTransition();
			fadeTransition.setDuration(Duration.millis(500));
			fadeTransition.setNode(main);
			fadeTransition.setFromValue(1);
			fadeTransition.setToValue(0);
			fadeTransition.setOnFinished(event -> {
				try {
					tohumidityinfo();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			fadeTransition.play();
		}
	}

	//Setting humidity details panel
	public void tohumidityinfo() throws IOException {
		Parent newpane = FXMLLoader.load(getClass().getResource("humidityinfo.fxml"));
		Scene newscene = new Scene(newpane);
		
		Stage current = (Stage) main.getScene().getWindow();
		current.setScene(newscene);
	}

	//Open or close help pane
	public void helpbutton() {
		if(helpvisible==1) {
			helppane.setVisible(false);
			helpvisible=0;
		}
		else {
			helppane.setVisible(true);
			helpvisible=1;
		}
	}

	//Fade animation when transiting into this panel
	private void fadeinanimation() {
		FadeTransition fadeTransition = new FadeTransition();
		fadeTransition.setDuration(Duration.millis(500));
		fadeTransition.setNode(main);
		fadeTransition.setFromValue(0);
		fadeTransition.setToValue(1);

		//Re-enable transitioning to other panels once animation complete
		fadeTransition.setOnFinished(event -> {

			buttontochair.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					maintochair();
				}
			});
			
			buttontohumidity.setOnAction((EventHandler<ActionEvent>) new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					maintohumidity();
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
		buttontochair.setOnAction(null);
		buttontohumidity.setOnAction(null);

		//Flag to check mid transition
		midtransition=0;

		//Fade in animation
		main.setOpacity(0);
		fadeinanimation();

		//Starting the refresh once panel has been fully loaded
		menutimer.schedule( 
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
