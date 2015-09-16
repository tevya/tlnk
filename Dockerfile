FROM java:8

# preserve Java 8  from the maven install.
RUN mv /etc/alternatives/java /etc/alternatives/java8
RUN apt-get update -y && apt-get install maven -y

# Restore Java 8
RUN mv -f /etc/alternatives/java8 /etc/alternatives/java
RUN ls -l /usr/bin/java	&& java -version

# Now set up and build TLNK from source.
ENV TLNK_DATA /usr/share/tlnkdata
ENV TLNK_CODE /usr/share/tlnk
RUN mkdir $TLNK_DATA
COPY . $TLNK_CODE
WORKDIR $TLNK_CODE
RUN mvn package
EXPOSE 8080
CMD java -jar target/tlnk-1.0.jar