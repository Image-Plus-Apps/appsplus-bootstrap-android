./gradlew data:assembleRelease -Pversion="1" &&
./gradlew data:publishToMavenLocal -Pversion="1" &&
./gradlew ui:assembleRelease -Pversion="1" &&
./gradlew ui:publishToMavenLocal -Pversion="1" &&
./gradlew testing:assembleRelease -Pversion="1" &&
./gradlew testing:publishToMavenLocal -Pversion="1"