FROM openjdk:8-jre-stretch
ENV EMBULK_VERSION 0.9.17
WORKDIR /root

RUN wget -q https://dl.embulk.org/embulk-${EMBULK_VERSION}.jar -O /bin/embulk \
  && chmod +x /bin/embulk

RUN mkdir embulk
ADD libs/ embulk/

WORKDIR /root/embulk/libs
RUN embulk bundle
WORKDIR /root/embulk

CMD ["java", "-jar", "/bin/embulk"]
