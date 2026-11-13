#!/bin/sh
# Testleri derler ve çalıştırır (Mac / Linux). Gerekli: JDK 17 veya üstü
cd "$(dirname "$0")" || exit 1
rm -rf out
javac -encoding UTF-8 -d out -sourcepath src src/hastane/test/SistemTestleri.java || exit 1
java -cp out hastane.test.SistemTestleri
