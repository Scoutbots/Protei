/*
turns on a motor at different speeds
27/6/2011
by gabriella levine, Qiuyang Zhou for Protei, XBee Serial communication
*/


int incomingData;

//const int analogInPin = A0;  // Analog input pin that the potentiometer is attached to
const int analogOutPin11 = 11; // Analog output pin that the Motor is attached to
const int analogOutPin9 = 6; // Analog output pin that the LED is attached to


void setup() {
  // initialize serial communications at 9600 bps:
  pinMode(analogOutPin11,PWM);
  Serial1.begin(9600); 

}

void loop() {

   
  if(Serial1.available()>0){
    incomingData = Serial1.read();
    Serial1.print(incomingData);}


if(incomingData=='1')
{
pwmWrite(analogOutPin11, 1000);
}
else if(incomingData=='2')
{
pwmWrite(analogOutPin11, 5000);
}
else if(incomingData=='3')
{
pwmWrite(analogOutPin11, 10000);
}
else if(incomingData=='4')
{
pwmWrite(analogOutPin11, 15000);
}
else if(incomingData=='5')
{
pwmWrite(analogOutPin11, 25000);
}
else if(incomingData=='6')
{
pwmWrite(analogOutPin11, 35000);
}
else if(incomingData=='7')
{
pwmWrite(analogOutPin11,45000);
}
else if(incomingData=='8')
{
pwmWrite(analogOutPin11, 55000);
}
else if(incomingData=='9')
{
pwmWrite(analogOutPin11, 65000);
}
else if(incomingData=='0')
{
pwmWrite(analogOutPin11, 0);
}
else {pwmWrite(analogOutPin11, 0);}

}
