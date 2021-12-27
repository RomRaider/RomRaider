# See: https://hub.docker.com/r/romraider/builder Overview for run details

FROM --platform="linux/386" i386/ubuntu:18.04 AS rr_builder

RUN apt-get -y update && \
    apt-get -y upgrade && \
    apt-get -y install ant openjdk-8-jdk unzip && \
    apt-get clean

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-i386/jre
RUN /usr/bin/update-alternatives --set java ${JAVA_HOME}/bin/java

RUN useradd -ms /bin/bash romraider && \
    mkdir /home/romraider/RomRaider && \
    mkdir /home/romraider/java && \
    chown romraider:romraider /home/romraider/RomRaider && \
    chown romraider:romraider /home/romraider/java

WORKDIR /home/romraider/java
ARG JAVA6_SRC=jre-6u45-windows-i586.zip
ADD --chown=romraider:romraider https://romraider.com/roms/dl.php?file=${JAVA6_SRC} ./${JAVA6_SRC}

USER romraider:romraider
RUN unzip -q /home/romraider/java/${JAVA6_SRC}

ARG maven_url="https://search.maven.org/remotecontent?filepath="
ARG junit_url="${maven_url}junit/junit/4.13.2/junit-4.13.2.jar"
ARG hamcrest_core_url="${maven_url}org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
ENV JRE_DIR=/home/romraider/java

USER root
ADD ${junit_url} ${JRE_DIR}/lib/junit.jar
ADD ${hamcrest_core_url} ${JRE_DIR}/lib/hamcrest-core.jar
RUN chmod 644 ${JRE_DIR}/lib/junit.jar && \
    chmod 644 ${JRE_DIR}/lib/hamcrest-core.jar

USER romraider:romraider
WORKDIR /home/romraider/RomRaider
RUN java -version && \
    echo "RomRaider build environment created."
