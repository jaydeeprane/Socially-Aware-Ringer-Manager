# Socially-Aware-Ringer-Manager

This is a java application for a managing the ringer mode of a user’s mobile based on his/her location and surroundings. When the callee chose to pick up/ignore the phone call, then factors such as the callee's location, callee's ringer mode at that point in time, reaction of people in callee's vicinity as well as the caller's reaction was taken into consideration before dynamically updating the ringer mode.  


The simulation for this application was done using REST api calls and interpeting JSON objects.

**src** folder contains the following:
* **RingerManager.java** :  This is the main code of the application.
* **Caller.java** :  Caller class having attributes of caller.
* **Feedbacks.java** :  Feedbacks class describing multiple feedbacks.
* **Neighbours.java** :  Neighbours class indicating relationship of neighbour with callee, expected ringer mode etc.
* **PlaceDetailsFeedback.java** :  Location specific feedback details.
