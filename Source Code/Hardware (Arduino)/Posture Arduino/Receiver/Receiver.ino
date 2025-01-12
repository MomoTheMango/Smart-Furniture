#include <SPI.h> 
#include <nRF24L01.h>
#include <RF24.h>

RF24 radio(7, 8); // Declase CE and CSN pins 

const byte address[6] = "00111"; // Use identical address for transmission and receiving

void setup() {
  Serial.begin(9600);
  radio.begin();
  radio.openReadingPipe(0, address);// Setting the address
  radio.setPALevel(RF24_PA_MIN); // Power amplifier level
  radio.startListening();   // Set as receiving mode
}
void loop() {
  if (radio.available()) {
    char text[100] = ""; // To store incoming data
    radio.read(&text, sizeof(text)); // Read incoming data
    // Print data to serial port
    Serial.print(text);
    Serial.println("  #");

    delay(500);
  }
}
