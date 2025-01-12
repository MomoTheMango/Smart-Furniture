#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

RF24 radio(7, 8); // Declase CE and CSN pins 
const byte address[6] = "00111"; // Use identical address for transmission and receiving

int posture;

void setup() {
  // put your setup code here, to run once:
  
  radio.begin();
  radio.setRetries(15, 15);
  radio.openWritingPipe(address); // Setting the address
  radio.setPALevel(RF24_PA_MIN); // Power amplifier level
  radio.stopListening();  // Set as transmitting mode

  
  Serial.begin(9600);
  pinMode(3, INPUT);
  pinMode(4, INPUT);
  pinMode(5, INPUT);
}

void loop() {

  char posture[1] = {0}; //Data to send
  
  delay(500);

  // Posture code depending on current posture
  if(digitalRead(5) == HIGH){
    if(digitalRead(3) == HIGH && digitalRead(4) == HIGH){
      posture[0] = '1';
    }
    else if(digitalRead(3) == HIGH){
      posture[0] = '2';
    }
    else if(digitalRead(4) == HIGH){
      posture[0] = '3';
    }
    else{
      posture[0] = '5';
    }
  }
  else{
    posture[0] = '0';
  }
  
  // Send data
  radio.write(&posture, sizeof(posture)); 


}
