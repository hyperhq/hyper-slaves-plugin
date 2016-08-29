#!/bin/bash

set -e

image="hyperhq/jenkins-slave-golang"
docker build -t ${image}:1.6 .
docker push ${image}:1.6

docker tag ${image}:1.6 ${image}:latest
docker push ${image}:latest
