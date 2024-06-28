
# A Certain Natural Codex (SE-Project-2024)

A full recreation of the board game "Codex Naturalis", made as our final project for the software engineering course held at the Polytechnic University of Milan.

## Features

📖  Accurate implementation of the game's complete rules\
🔗  Support for multiple network protocols (TCP/RMI)\
📱  A graphical user interface, based on the original game's design language\
🖥️  A command line interface fallback, for operating systems where JavaFX isn't supported\
🎮  Support for multiple concurrently running games on the same server\
🗨  In-game chat, including the ability to whisper to a limited set of players\
🔄  Full client reconnection support, regardless of which phase the game is in

## Screenshots
![In-game](deliverables/screenshots/ingame.png)
![Match-browser](deliverables/screenshots/matchbrowser.png)
![Connection-screen](deliverables/screenshots/connectionscreen.png)

## Setup

The client can be run from one of the executables found in the releases section which are already packaged with
the correct Java Runtime Environment version.
Windows users can simply run the executable file to install the game and add it to their Start Menu,
while those on Linux have to extract the zip archive and run the binary found in the /bin folder.
Alternatively, the client can also be used through the platform-specific jar, 
which requires JRE 22 or higher:
```shell
>java -jar CodexClient<PLATFORM SUFFIX>.jar
```
the ```-cli``` argument can be specified to run the game in command line mode. Be wary that this may present a few
graphical issues on Windows, due to UTF-8 encoding not being enabled by default in the terminal.

To start the server, get the multi-platform jar from the releases or from the deliverables folder. Then, run:
```shell
>java -jar CodexServer.jar
```
By default, the TCP Server will start on port 23435 while the RMI one will use port 1099. These can be changed by
editing the serverParameters.json file in the gameFiles folder found within the jar.
Sometimes, the RMI Server might refuse incoming connections. When that happens, it is recommended to verify
the correctness of the hostname property for the JVM it is being run on.



## Tools

- UML and sequence diagram creation: [Draw.io](https://draw.io)
- Main IDE: [IntelliJ IDEA Ultimate](https://www.jetbrains.com/idea/)
- Preliminary user interface draft: [Figma](https://www.figma.com/)
- Graphical user interface design: [Scene Builder](https://gluonhq.com/products/scene-builder/)

## Libraries

- [Jackson](https://github.com/FasterXML/jackson), to read JSON files
- [JavaFX](https://github.com/openjdk/jfx), to create a cohesive and responsive GUI
- [JUnit 5](https://github.com/junit-team/junit5), used for testing
## Authors

- [Andrea Fidanza](https://github.com/Andrea-Fidanza)
- [Marco Maiocchi](https://github.com/Maioch)
- [Francesco Saverio Nisoli](https://github.com/Frasavenix)
- [Guglielmo Gatti](https://github.com/topox19547)

## License
Codex Naturalis belongs to Cranio Creations and Studio Bombyx.
All of the copyrighted assets used in this project were supplied by the Polytechnic University of Milan with permission from the copyright holders.
