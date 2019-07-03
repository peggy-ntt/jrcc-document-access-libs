#!/bin/bash

#Build access-libs
cd jrcc-document-access-libs
mvn install

#Build access-spring-boot-autoconfigure
cd ../jrcc-access-spring-boot-autoconfigure
mvn install

#Build access-spring-boot-starter
cd ../jrcc-access-spring-boot-starter
mvn install