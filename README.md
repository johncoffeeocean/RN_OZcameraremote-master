This app utilises the device's gyroscope for pan and tilt control and it
utilises an FTDI USB to serial cable adapted to receive LANC messages from a
Manfrotto tripod zoom handle.

Video streaming is by the means of a Janus WebRTC server whose client runs in
a browser inside the app. A modified version of
https://janus.conf.meetecho.com/streamingtest.html is used to connect to the
Janus server.
See http://v2.ozapi.net:2080/cameracontrol.html for the actual javascript
client the app uses.

The app exposes some controls to the Javascript client running in the browser.

The app communicates the pan/tilt/zoom commands via a UDP stream to the gimbal
control server.

### Clone

git clone https://github.com/johncoffeeocean
