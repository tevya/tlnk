FROM java:8

RUN apt-get update -y && apt-get install maven -y
ENV TLNK_DATA /usr/share/tlnkdata
ENV TLNK_CODE /usr/share/tlnk
RUN mkdir $TLNK_DATA
COPY . $TLNK_CODE
WORKDIR $TLNK_CODE
RUN mvn package
EXPOSE 8080
CMD java -jar target/tlnk-1.0.jar