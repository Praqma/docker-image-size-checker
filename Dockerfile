FROM groovy:2.4.9-jdk8-alpine
MAINTAINER Mads <man@praqma.net>
RUN mkdir /home/groovy/imagesize
COPY imageSizeChecker.groovy /home/groovy/imagesize
USER root
RUN chown groovy:groovy /home/groovy/imagesize/imageSizeChecker.groovy && \
    chmod +x /home/groovy/imagesize/imageSizeChecker.groovy && \
    ln -s /home/groovy/imagesize/imageSizeChecker.groovy /usr/bin/imagecheck 
USER groovy



