#include <SoftwareSerial.h>
#include <MqttSerial.h>
#include <ArduinoJson.h>

#define COUNT 100
#define COUNT2 100

SoftwareSerial bluetoothSerial(10,11);
MqttSerial mqttSerial ("arduino_lib",bluetoothSerial);
void callback(String, String) ;

unsigned long time1;
unsigned long time2;

void setup() {
  Serial.begin(9600);
   while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }
  Serial.println("Serial initialized");
  bluetoothSerial.begin(9600);
  
  pinMode(7, OUTPUT);
  pinMode(6, OUTPUT);

  pinMode(9, OUTPUT);
  pinMode(8, OUTPUT);

  
  //mqttSerial.begin("iot.eclipse.org", "1883", "krohak", "");  
  mqttSerial.begin("52.39.209.110", "1883", "krohak", ""); 
  mqttSerial.connect();
  mqttSerial.publish("krohak", "testing123");
  mqttSerial.subscribe("helloout");
  mqttSerial.subscribe("mar");
  

}

void right(){
  
  bool prev=0;
  int count=0;
  int ref=analogRead(A0);

  time1=millis();
   time2=millis();


while (time2-time1<COUNT){ //15=1 round

    
  int a= analogRead(A0);
  
  if(a==ref && prev==0){
    prev=1;
  analogWrite(7, 0);
  analogWrite(6, 255);
    }

  else if(a!=ref && prev==1){
    prev=0;
    count++;
    analogWrite(7, 0);
  analogWrite(6, 255);
    }

    time2=millis();
}

analogWrite(7, 0);
analogWrite(6, 0);
  }

void left(){

  bool prev=0;
  int count=0;
  

  time1=millis();
   time2=millis();

while (time2-time1<COUNT){ //15 = 1 round

  analogWrite(6, 0);
  analogWrite(7, 255);
  int a= analogRead(A0);
  
  if(a>=512 && prev==0){
    prev=1;
    }

  else if(a<512 && prev==1){
    prev=0;
    count++;
    //Serial.println(count + ": left");
    }

    time2=millis();
}

analogWrite(7, 0);
analogWrite(6, 0);  
  
  }


void up(){
  
  int prev=0;
  int count=0;
  int ref=analogRead(A1);

  time1=millis();
   time2=millis();

while (time2-time1<COUNT2){ //15=1 round

   analogWrite(8, 255);
  analogWrite(9, 0);
  
  int a= analogRead(A1);

  if(a>=512 && prev==0){
    prev=1;
    }

  else if(a<512 && prev==1){
    prev=0;
    count++;
    }
 

    time2=millis();
}

analogWrite(9, 0);
analogWrite(8, 0);
  }

void down(){

  bool prev=0;
  int count=0;

  time1=millis();
   time2=millis();

while (time2-time1<COUNT2){ //15 = 1 round

analogWrite(8, 0);
  analogWrite(9, 255);
  
  int a= analogRead(A1);
  
  if(a>=512 && prev==0){
    prev=1;
    }

  else if(a<512 && prev==1){
    prev=0;
    count++;
   // Serial.println(count + ": down");
    }
     time2=millis();
}

analogWrite(9, 0);
analogWrite(8, 0);  
  
  }

void callback(String topic, String payload) {

  if (topic == "helloout" && payload=="r") {

   right();
   bluetoothSerial.flush();
  }
  else if(topic == "helloout" && payload=="l"){
   
    left();
    bluetoothSerial.flush();
    }

  else if(topic == "mar" && payload=="u"){
   
    up();
    bluetoothSerial.flush();
    }

    else if(topic == "mar" && payload=="dow"){
   //Serial.println("down");
    down();
    bluetoothSerial.flush();
    
    }

    

    //Serial.println(topic + " : " + payload);
}
void loop() {
  mqttSerial.loop(callback);
  //mqttSerial.publish("krohak", "testing123");
  //int a= analogRead(A0);
  //Serial.println(a);
}
