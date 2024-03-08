How to set up for the QRCode Scanner:
	•	Go To Device Manager
	•	Click on the menu button for the virtual device (three buttons right next to run button)
	•	Choose edit option
	•	Choose Show Advanced Settings at the bottom left of the window.
	•	Change the configuration of Back Camera field to Virtual Scene. 

	•	Go to your user directory -> Library -> Android- > sdk -> emulator -> resources
	•	Save the checkin and promo QR pictures in this directory
	•	Name the QR code to be displayed promoQR.png or checkinQR.png

For testing with promo QR code:
	•	Open the file named Toren1BD.poster
	•	Copy and paste the following:

poster wall
size 2 2
position -0.807 0.320 5.316
rotation 0 -150 0
default poster.png

poster table
size 1 1
position -2.205 -0.077 3.949
rotation -90 0 120

poster custom
size 2 2
position 0 0 -1.8
rotation 0 0 0
default promoQR.png

For testing with checkin QR code:
	•	Open the file named Toren1BD.poster
	•	Copy and paste the following:

poster wall
size 2 2
position -0.807 0.320 5.316
rotation 0 -150 0
default poster.png

poster table
size 1 1
position -2.205 -0.077 3.949
rotation -90 0 120

poster custom
size 2 2
position 0 0 -1.8
rotation 0 0 0
default checkinQR.png

Now go back to android studio. Go to device manager. Click on the same menu option. Choose the Cold Boot.
Let the device reboot and run the test. 

***IMPORTANT***
The device needs to have a cold boot every time a change is made in Toren1BD.poster
Make sure the pngs for checkin and promo qrs are in the same directory(resources) as the Toren1BD.poster
