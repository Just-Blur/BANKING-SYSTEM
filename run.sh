#!/bin/bash
# Compiles and runs the Banking System (view it on the Codespace desktop: Ports tab > 6080)
set -e
rm -rf out
mkdir out
javac --module-path lib/javafx --add-modules javafx.controls,javafx.fxml -cp "lib/sqlite-jdbc-3.36.0.3.jar" -d out $(find src -name "*.java")
cp -r src/resources out/
java -Dprism.order=sw --module-path lib/javafx --add-modules javafx.controls,javafx.fxml -cp "out:lib/sqlite-jdbc-3.36.0.3.jar" BankingApplication
