#!/bin/bash

set -e

image="hyperhq/jenkins-slave-centos"
docker build -t ${image}:7.2 .
docker push ${image}:7.2

docker tag ${image}:7.2 ${image}:latest
docker push ${image}:latest
