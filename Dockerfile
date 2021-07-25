FROM openjdk:8-jdk
ENV JRE_DIR=/usr/local/openjdk-8/jre

RUN apt-get -y update && \
    apt-get -y upgrade && \
    apt-get -y install ant

ENV maven_url="https://search.maven.org/remotecontent?filepath="
ENV junit_url="${maven_url}junit/junit/4.13.2/junit-4.13.2.jar"
ENV hamcrest_core_url="${maven_url}org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"

RUN curl -Lo ${JRE_DIR}/lib/junit.jar ${junit_url} && \
    curl -Lo ${JRE_DIR}/lib/hamcrest-core.jar ${hamcrest_core_url}

RUN useradd -m romraider
WORKDIR /home/romraider
USER romraider

COPY --chown=romraider . /home/romraider/RomRaider/

WORKDIR /home/romraider/RomRaider

RUN ant unittest && \
    ant build-linux
