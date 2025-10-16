# DJI Streamer V5

Application based on the DJI Mobile SDK V5 for streaming synchronized video data and flight data using websockets to a custom-made server. It provides basic control functionality, such as in the DJI Go 4 / DJI Fly apps. The app should be compatible with DJI drones supported by MSDKV5 (tested on Mavic Mini 3).

<img src=gui.png>

## How to connect to a server and receive the data

- Implement a WebSocket server, [here is our Unity example](https://github.com/robofit/drone_vstool/blob/minimal_client/DroCo/Assets/Scripts/WebSocketServer.cs).
- In the drone GUI, click the **Server IP** button and fill in the IP address of the server and port (make sure that the port is open).
- The drone, as a WebSocket client, tries to establish the connection.
- If successful, the drone sends a handshake message, with the information about the drone type (ctype 0 means the client is a drone):
  ```json
  {
     "type":"hello",
     "data":{
        "ctype":0,
        "drone_name":"MavicPro",
        "serial":"12345"
     }
  }
  ```
- The server needs to implement a handshake reply. The drone expects a reply message in the following format (**client_id** is handled by the server, can be any identification string, for instance, an ID of the websocket client):
  ```json
  {
     "type":"hello_resp",
     "data":{
        "client_id":"41f28cb6",
     }
  }
  ```
- When successfully exchanged handshake messages, the drone is ready to start sending its data. To start, press the **Live** button.
- The drone starts sending its data in binary format, encoded as:
  ```bash
  | First 4 bytes == JSON bytes length | JSON bytes | 4 bytes + JSON bytes length + 4 bytes == JPEG image bytes length | JPEG bytes |
  |------------------------------------|------------|------------------------------------------------------------------|------------| 
  ```
  Binary data is split into a JSON message containing the flight data and JPEG bytes containing the current frame of the drone video feed. The first 4 bytes tell you the length of the JSON message, and the 4 bytes starting from (4 + length of JSON message) tell you the length of the JPEG image.
- When decoding the JSON bytes to a string using UTF-8, the following structure of the flight data should be received (filled with example data):
  ```json
  {
     "type":"data_broadcast",
     "data":{
        "client_id":"41f28cb6",
        "altitude":234.6,
        "relative_altitude":10,
        "gps":{
           "latitude":49.24031521243185,
           "longitude":16.613207555321484
        },
        "aircraft_orientation":{
           "pitch":-4.0,
           "roll":-1.2,
           "yaw":81.2,
           "compass":81.2
        },
        "aircraft_velocity":{
           "velocity_x":0.1,
           "velocity_y":1.0,
           "velocity_z":0.1
        },
        "gimbal_orientation":{
           "pitch":0.0,
           "roll":0.0,
           "yaw":81.1,
           "yaw_relative":0.0
        },
        "satellite_count":16,
        "gps_signal_level":3,
        "battery":60,
        "sticks":{
          "left_stick": {
            "x":0
            "y":0
          }
          "right_stick": {
            "x":0
            "y":0
          }
        }
        "timestamp":"2025-06-27 14:45:06.843"
     }
  }
  ```
  Where:
  - **altitude** is AMSL (height above mean sea level), to work, the takeoff altitude needs to be manually set using the button **Altitude**.
  - **relative_altitude** stands for AGL (above ground level) altitude.
  - **sticks** report the current configuration of the physical remote controller. Values are in the range of [-660, 660]. Zero means the sticks are centered, in the default position.
- JPEG byte array can be directly decoded and displayed in the remote GUI.
 
## How to control the drone using virtual sticks

- To create some level of autonomy, virtual sticks functionality can be used. To enable virtual stick mode, send from the server following message:
  ```json
  {
     "type":"enable_control",
     "data":{
        "enable":true,
     }
  }
  ```
- The drone starts listening for command messages with the velocities of individual drone axes, in the following format:
  ```json
  {
     "type":"control_command",
     "data":{
        "pitch":0,  
        "roll":0,
        "yaw":0,
        "throttle":0,
        "gimbal_pitch":0
     }
  }
  ```
  Where:
  - **pitch** (forward / backward), **roll** (left / right), **throttle** (up / down)  are velocities in m/s. For slow speed, use values around 0.1.
  - **yaw** is velocity in deg/s. For slow speed, use values around 10.
  - **gimbal_pitch** is velocity in deg/s for tilting the gimbal up/down. For slow speed, use values around 10.

  
