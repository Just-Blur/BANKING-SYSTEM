#!/bin/bash
# Installs the libraries the app needs inside the Codespace (JavaFX + SQLite)
sudo apt-get update
sudo apt-get install -y libgtk-3-0 libxtst6 libxxf86vm1 libgl1 libasound2 sqlite3
mkdir -p lib/javafx
FX=21.0.5
for m in base graphics controls fxml; do
  [ -f lib/javafx/javafx-$m-$FX-linux.jar ] || curl -fsSL -o lib/javafx/javafx-$m-$FX-linux.jar https://repo1.maven.org/maven2/org/openjfx/javafx-$m/$FX/javafx-$m-$FX-linux.jar
done
[ -f lib/sqlite-jdbc-3.36.0.3.jar ] || curl -fsSL -o lib/sqlite-jdbc-3.36.0.3.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar
echo "Setup finished."
