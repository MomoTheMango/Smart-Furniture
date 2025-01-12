#include <SPI.h> 
#include <nRF24L01.h>
#include <RF24.h>

// Structure of incoming data
struct package
{
float humidity ;
float temperature ;
};
typedef struct package Package;
Package data;

RF24 radio(7, 8); // Declase CE and CSN pins 
const byte address[6] = "00001"; // Use identical address for transmission and receiving

void setup() {
  Serial.begin(9600);
  radio.begin();
  radio.openReadingPipe(0, address); // Setting the address
  radio.setPALevel(RF24_PA_MIN); // Power amplifier level
  radio.startListening();   // Set as receiving mode
}
void loop() {
  if (radio.available()) {
    radio.read(&data, sizeof(data)); // Read incoming data
    //Print data to serial port
    Serial.print("Humidity: "); 
    Serial.print(data.humidity,1);
    Serial.print(" %, Temp: ");
    Serial.print(data.temperature,1);
    Serial.println(" Celsius  #");
    
    delay(500);
  }
}
