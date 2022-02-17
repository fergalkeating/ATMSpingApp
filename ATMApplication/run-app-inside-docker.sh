#!/usr/bin/env bash


APP_TARGET_FOLDER=docker/app/files/ATMApplication

mkdir -p ${APP_TARGET_FOLDER}

echo "Generating the Jar file"

mvn clean package -DskipTests

echo "Copying application jar to docker folder"
cp target/ATMApplication*.jar ${APP_TARGET_FOLDER}/ATMApplication.jar


echo "Starting containers"
docker-compose -f docker/docker-compose.yml up --build -d
