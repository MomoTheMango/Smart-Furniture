#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>
#include <DHT.h>

#define DHTPIN 2
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);

// Structure of transmitting data
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
  radio.begin();
  radio.setRetries(15, 15);
  radio.openWritingPipe(address); // Setting the address
  radio.setPALevel(RF24_PA_MIN); // Power amplifier level
  radio.stopListening();  // Set as transmitting mode
  dht.begin();
}
void loop() {
  // Send data
  data.humidity = dht.readHumidity();
  data.temperature = dht.readTemperature();
  radio.write(&data, sizeof(data)); 
  delay(500);
}
