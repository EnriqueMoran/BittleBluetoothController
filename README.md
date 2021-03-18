# petoiBittleBluetoothController
Bluetooth controller for [Bittle](https://www.petoi.com/).

This app allow you to fully control your pet through Bluetooth without any previous configuration but pairing it with your Android phone.
Download Current version (v0.1) apk file in [Releases](https://github.com/EnriqueMoran/petoiBittleBluetoothController/releases/tag/v0.1) or [here](https://github.com/EnriqueMoran/petoiBittleBluetoothController/releases/download/v0.1/PetoiBittleBluetoothController.apk).

![alt tag](/readme_images/bittle_greeting.gif)

## How to use it
First of all, to control Bittle through a Bluetooth connection you must connect Bittle's official Bluetooth dongle.
After connecting Bluetooth dongle and turning on Bittle, next step is pairing it with your phone, for this take the following steps:
1. Swipe down from the top of your phone screen.
2. Turn on Bluetooth connection on your phone.
3. Access to Bluetooth configuration by pressing and holding Bluetooth icon.
4. Find Bittle on avaliable devices (_BittleSPP-XXXXXXX_) and tap it.
5. Use the following pin code _0000_ (or _1234_ in case it doesn't work).

Once your phone and Bittle are paired, you are ready to control it:
1. Open PetoiBittleBluetoothController app
2. Tap on _PAIRED DEVICES_.
3. Select Bittle (_BittleSPP-XXXXXXX_) and wait till the connection is stablished (Bittle will make the same sound it makes when turning on).
4. Control Bittle with your phone!

![alt tag](/readme_images/phone_view.png)

## Considerations
* This is a Beta version, notice that you could encounter some issues while using it.
* There is a 0.5 seconds delay when sending movement commands (to protect Bittle from performing actions too quickly). This delay can be modified in the code (in _MainActivity_, _directionPersecondLimit_ parameter) at your own risk.
* Before exit the app, send _DISCONNECT_ command.
* If Bittle won't listen to your movement commands, try sending it any other static command (like _SIT_).
* If Bittle won't connect to your Android phone, try unpairing and pairing again.

## Known Issues
* _GYRO_ button is not working properly.
* Sometimes Bittle won't listen to movement commands.
* Sometimes trying to connect to Bittle will cause it to not listen to connections, making it necessary to reboot.

## Roadmap
* Add a simple view mode, with less buttons on screen.
* Improve connection and disconnection process.
* Reconnecting to Bittle if connections is lost.
* Improve User Interface.

## Version history
* **v0.1 (03/12/21):** First public Beta version. 
    * Control Bittle through Bluetooth (after being previously paired).
    * The same buttons as IR remote controller included in Bittle startet kit are added (but _calibrate_).

