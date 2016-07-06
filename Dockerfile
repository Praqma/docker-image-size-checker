# This Dockerfile is used to build an image containing basic stuff to be used as a Jenkins slave build node.
FROM ubuntu:14.04
MAINTAINER Mads <man@praqma.net>

# Install and configure a basic SSH server
RUN apt-get update &&\
    apt-get install -y apt-utils &&\
    apt-cache showpkg linkchecker &&\
    apt-get install -y linkchecker &&\
    apt-get install -y openssh-server &&\
    apt-get install -y git &&\
    apt-get clean -y && rm -rf /var/lib/apt/lists/* &&\
    sed -i 's|session    required     pam_loginuid.so|session    optional     pam_loginuid.so|g' /etc/pam.d/sshd &&\
    mkdir -p /var/run/sshd 

# Install JDK 7 (latest edition)
RUN apt-get update &&\
    apt-get install -y openjdk-7-jdk &&\
    apt-get clean -y && rm -rf /var/lib/apt/lists/*

# Set user jenkins to the image
RUN adduser --quiet jenkins &&\
    echo "jenkins:jenkins" | chpasswd

# Standard SSH port
EXPOSE 22

# Groovy install
ENV         GROOVY_VERSION 2.4.3
ENV         PATH $PATH:/opt/groovy/current/bin
ENV         JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64
RUN         apt-get update -qq -y && \
            apt-get install -y patch wget unzip openjdk-7-jre-headless && \
            wget http://dl.bintray.com/groovy/maven/groovy-binary-${GROOVY_VERSION}.zip && \
            mkdir -p /opt/groovy && \
            unzip groovy-binary-${GROOVY_VERSION}.zip -d /opt/groovy && \
            ln -s /opt/groovy/groovy-${GROOVY_VERSION} /opt/groovy/current && \
            rm groovy-binary-${GROOVY_VERSION}.zip && \
            apt-get remove -y wget && \
            apt-get clean && \
            rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Image size checker
COPY imageSizeChecker.groovy /home/jenkins/


CMD ["/usr/sbin/sshd", "-D"]
