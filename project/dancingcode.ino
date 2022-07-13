#include <Servo.h> // add the servo libraries
Servo myservo1; // create servo object to control a servo
Servo myservo2;
Servo myservo3;
Servo myservo4;
float pos1=90, pos2=90, pos3=90, pos4=90; // define the variable of 4 servo angle,and assign the initial value (that is the boot posture
//angle value)
float speed = 1;
const int right_X = A2; // define the right X pin to A2
const int right_Y = A5; // define the right Y pin to A5
const int right_key = 8; // define the right key pin to 7（that is the value of Z）
const int left_X = A3; // define the left X pin to A3
const int left_Y = A4; // define the left X pin to A4
const int left_key = 7; //define the left key pin to 8（that is the value of Z）
int x1,y1,z1; // define the variable, used to save the joystick value it read.
int x2,y2,z2;
int raspSig;
int raspberry = 0;  
int myPins[] = {10 , 2,4 ,11, A1 , A0 ,12};
int target ,target2,target3;
int playerControlled = 0;
int reset= 13;
float high =0;
float pre1 = 0;
float low = 0;
int previous = 0;
boolean toggle = false;
void setup()
{
// boot posture
myservo1.write((int)pos1);
delay(1000);
myservo2.write((int)pos2);
myservo3.write((int)pos3);
myservo4.write((int)pos4);
delay(1500);
pinMode(right_key, INPUT); // set the right/left key to INPUT
pinMode(left_key, INPUT);
pinMode(raspberry, INPUT);
Serial.begin(9600); // set the baud rate to 9600
Serial.print("Starting Service");
}
void loop()
{
myservo1.attach(3); // set the control pin of servo 1 to D3  dizuo-servo1-3
myservo2.attach(5); // set the control pin of servo 2 to D5  arm-servo2-5
myservo3.attach(6); //set the control pin of servo 3 to D6   lower arm-servo-6
myservo4.attach(9); // set the control pin of servo 4 to D9  claw-servo-9
x2 = analogRead(right_X); //read the right X value
y2 = analogRead(right_Y); // read the right Y value
z2 = digitalRead(right_key); //// read the right Z value
x1 = analogRead(left_X); //read the left X value
y1 = analogRead(left_Y); //read the left Y value
z1 = digitalRead(left_key); // read the left Z value

//raspSig = digitalRead(10); // read the left Z value
int hell = getGPIO();

if(left_key==HIGH){
  speed =5;
}else{
  speed =1;
}
// claw
claw();
if( digitalRead(reset) == HIGH){
  
Serial.print(" Requesting Reset " );
  toggle = true;
  high = hell;
  low =hell;
  //do stuff
  
Serial.print("\nwaiting... " );
  while ( digitalRead(reset) == HIGH){
    
    delay(5);
  }
  
  hell = getGPIO();
  high = max(high ,hell);
    low = min(low ,hell);
    Serial.print(" min " );Serial.print( low );Serial.print( " High " );Serial.print( high );Serial.print( "\n"); 
}
//Serial.print(" min " );Serial.print( low );Serial.print( " High " );Serial.print( high ); Serial.print(" Cur : ");Serial.print( hell );Serial.print( "\n"); 
if( high >0){
  if( abs ( previous - hell) < 5 &&abs ( previous - hell) > 1) {
    if( pos4 ==90){
      pos4 = 120;
    }else{
      pos4=90;
    }
  }
  float scaled = (((float)hell) -low)/(high - low)* 40;
  int key = hell %12;
  float noteS = ((float)key)*6;//scaled note
  
  target = noteS*2 + scaled;
target2 = ((pre1 )*3/2)+10;
if(abs(pow(((float)( previous -hell )),0.2)*21) > abs(target3-90)){
    target3 = pow(((float)( previous -hell )),0.2)*30+90;
  
}
target3 = (int)lerp((float)target3 ,90.0f ,0.03f);
if(playerControlled==0){
pos3 = lerp( pos3,(float) target ,0.03f);
pos2 = lerp( pos2,(float) target2 ,0.01f);
pos1 = lerp( pos1,(float) target3 ,0.01f);

}
}
high = max( hell , high);
low = min( hell , high);


// rotate
turn();

// upper arm
upper_arm();

//lower arm
lower_arm();
previous = hell;
pre1 = lerp( pre1,(float) hell ,0.5f);
delay (5);
}
int getGPIO(){
  int num= 0;
  int inc =1;
  for(int i =0; i < 7 ; i ++ , inc *=2 ){
    
    if(digitalRead(myPins[i]) == HIGH)
      num += inc;
  }
  //Serial.print(num);
  //Serial.print("\n");
  return num;
}


//***************************************************
//claw
void claw()
{
//claw
pos4+=getSpeed(x1); 
if(pos4>120) //limit the largest angle when open the claw 
{
pos4=120;
}


if(pos4<45) // 
{
pos4=45; //limit the largest angle when close the claw
}


myservo4.write(pos4); // servo 4 operates the action, claw is gradually closed.

}



//******************************************************/
// turn
void turn()
{
pos1+=getSpeed(x2); 
myservo1.write(pos1); // arm turns left

if(pos1>180) //limit the angle when turn right 
{
pos1=180;
}



if(pos1<1) // limit the angle when turn left
{
pos1=1;
}

}




//**********************************************************/
// lower arm
void lower_arm()
{
  

pos2+=getSpeed(y2);
if(pos2<25) // limit the retracted angle
{
pos2=25;
}

if(pos2>180) // limit the stretched angle
{
pos2=180;
}
myservo2.write(pos2); // lower arm will stretch out


}

float lerp (float a , float b , float value){
  return a + value * (b - a);
}



//*************************************************************/

//upper arm
void upper_arm()
{

pos3+=getSpeed(y1);
if(pos3<1) //  limit the angle when go down 
{
pos3=1;
}
  if(pos3>135) //limit the lifting angle 
{
pos3=135;
}
myservo3.write(pos3); // the upper arm will lift


}


float getSpeed(float num){// A graoh for exponential speed
  float x = (num-500) /500  ;
  return (  x*x*x *speed);
}
