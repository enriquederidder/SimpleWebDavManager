## SD Card + ESP8266 Adapter UI Manager

### Description
This application allows you to manage files on the SD Card + ESP8266 adapter for devices that lack network capabilities. It utilizes the Sardine Android and Okhttp libraries. The adapter operates a WebDav Server, enabling communication via Https commands.

### Libraries Used
- [Sardine Android](https://github.com/thegrizzlylabs/sardine-android)
- [Okhttp](https://github.com/square/okhttp)

### Functionalities
- Explore files in the hosted folder of the WebDav server.
- Delete, modify, download and manage items within the server.
- Select a desired WebDav server to explore by entering its address.
- Or select the auto scan feature, that will try to scan your locale network for the address

### Getting Started
1. Set up the ESP:
   - Place an init file with network credentials on the SD card.
   - This allows the ESP to connect to your network.
   - Insert the ESP in a device, or plug the device in via a usb cable.

2. Determine the ESP's IP:
   - Find the IP assigned by your router.
   - Or try to find the IP via the scan feature.

3. Configure the App:
   - Enter the ESP's IP in the app settings.
   - You should now be able to manage files from your phone to the ESP.

![alt text](https://i.imgur.com/g1aCwcg.png)
