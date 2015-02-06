#!/bin/bash
cd src/
find . -name "*.java" > sources.txt
javac -d bin @sources.txt
jar cvfe ../dist/STDE.jar Gui.GuiMain -C bin . -C . Resources

# java -jar dist/STDE.jar 

