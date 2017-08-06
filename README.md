# Protei
Open Hardware Oil Spill Cleaning Sailing Robot

As seen on TED, Protei is a modular, shape shifting boat conceived by Cesar Harada for cleaning up oil spills in the ocean. 
### [TED talk](https://www.youtube.com/watch?v=iNDXvJjSniI)

[<img src="http://img.youtube.com/vi/iNDXvJjSniI/0.jpg" width="400px">](https://www.youtube.com/watch?v=iNDXvJjSniI)


# Protei v12.0 - Protei Rohak 

This is the first Protei which has an Android phone on board - extending its computational power and bringing it a step closer towards autonomous navigation. A laptop sends commands to the Phone via a server and the phone is connected to an Arduino Uno, which controls the motors, via Bluetooth.


## [Android](https://github.com/Scoutbots/Protei/tree/master/Protei-app)

<img src="https://c1.staticflickr.com/5/4422/35557019743_1877e6619c_b.jpg" width="300px">        <img src="https://c1.staticflickr.com/5/4409/36228766821_feac418a6c_b.jpg" width="300px">

The Protei App facilitates communication between the server and Arduino. The App connects to the Arduino via Bluetooth and recieves/sends commands to the server via MQTT. In addition to this, it also computes the position of the Sail and the Rudder, displays the latest message exchange and the orientation of the boat (Gyroscope).

* [References](https://github.com/Scoutbots/Protei#references)

## [Arduino](https://github.com/Scoutbots/Protei/tree/master/Protei-arduino)

<img src="https://github.com/Scoutbots/Protei/blob/master/G_20170804_1549443.gif" width="300px">    <img src="https://c1.staticflickr.com/5/4387/36364310955_81f06dce3b_b.jpg" width="500px">

The Arduino subscribes to MQTT feed on the server through the Protei App. It controls two high torque motors for the sail and rudder of Protei using a motor controller and 7.4V battery, all of which were reused from Seeed Studio's Hercules kit (see [references](https://github.com/Scoutbots/Protei#references)).

## [Python](https://github.com/Scoutbots/Protei/blob/master/keys.py)

The keys.py programme is to send commands to Protei via MQTT channels to control the position of the sail and rudder.

## [Testing](https://www.youtube.com/watch?v=5ecuIcRUJYU)

[<img src="https://github.com/Scoutbots/Protei/blob/master/G_20170804_1303358.gif" width="500px">](https://www.youtube.com/watch?v=5ecuIcRUJYU)


## Making

### [Video](https://www.youtube.com/watch?v=PAQ16yVeyIc)

[<img src="http://img.youtube.com/vi/PAQ16yVeyIc/0.jpg" width="500px">](https://www.youtube.com/watch?v=PAQ16yVeyIc)

### [Flickr Album](https://www.flickr.com/photos/77868571@N03/albums/72157684488165694)

<img src="https://c1.staticflickr.com/5/4412/35528578294_5a7509abd1_b.jpg" width="200px">   <img src="https://c1.staticflickr.com/5/4354/36364325165_da5da45235_b.jpg" width="200px">   <img src="https://c1.staticflickr.com/5/4404/36195758412_10b2299927_b.jpg" width="200px">

### [Flickr Gallery](https://www.flickr.com/photos/77868571@N03/galleries/72157684487211454/)
  
<img src="https://c1.staticflickr.com/5/4330/35485191904_e4c592886a_b.jpg" width="200px">  <img src="https://c1.staticflickr.com/5/4324/35512352003_b9d0cb85c8_b.jpg" width="400px">



### References

1. [Android Bluetooth](https://developer.android.com/guide/topics/connectivity/bluetooth.html)
2. [Android Motion Sensors](https://developer.android.com/guide/topics/sensors/sensors_motion.html)
3. [MQTT Serial Tethering](https://github.com/ahmadsayed/MQTTSerialTethering)
4. [Android GPS](https://developer.android.com/guide/topics/location/strategies.html#Updates)
5. [Arduino Bluetooth](https://github.com/ahmadsayed/Mqtt_Bluetooth)
6. [Seeed Studio Hercules](http://wiki.seeed.cc/Hercules_Dual_15A_6-20V_Motor_Controller/)
