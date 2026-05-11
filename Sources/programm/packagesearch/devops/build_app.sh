#!/bin/bash

APP=prodcatservice

REPO=oz.it-alnc.ru:5443/repository/smp
TAG=`grep "4.5.*" ~/tags/version_mortgage`

docker login -u admin -p admin oz.it-alnc.ru:5443
docker build ../. -t $REPO/ita-$APP:$TAG
docker push $REPO/ita-$APP:$TAG
docker rmi $REPO/ita-$APP:$TAG
