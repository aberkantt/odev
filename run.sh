#!/bin/sh
# Projeyi derler ve çalıştırır (Mac / Linux). Gerekli: JDK 17 veya üstü
cd "$(dirname "$0")" || exit 1
rm -rf out
javac -encoding UTF-8 -d out -sourcepath src src/hastane/Main.java || exit 1
java -cp out hastane.Main
