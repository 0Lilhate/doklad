FROM oz.it-alnc.ru:5443/repository/alfabank/ita-forms/img-base-java:1.0.0.latest

ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS

WORKDIR /app
ADD work/ /
COPY target/fprodsearch-0.0.1-SNAPSHOT.jar .

CMD java ${JAVA_OPTS} -jar /app/fprodsearch-0.0.1-SNAPSHOT.jar

