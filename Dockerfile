FROM openjdk:8u222-stretch AS build
COPY .dockertimestamp /

RUN mkdir /build
COPY .mvn/ /build/.mvn
COPY mvnw /build/
COPY pom.xml /build/
COPY tokamak-core/ /build/tokamak-core
COPY tokamak-server/ /build/tokamak-server
COPY tokamak-spark/ /build/tokamak-spark
COPY tokamak-test/ /build/tokamak-test

RUN cd /build && ./mvnw clean package -DskipTests


FROM openjdk:8u222-stretch
COPY .dockertimestamp /

RUN mkdir /app
COPY --from=build build/tokamak-server/target/tokamak-server-0.1-SNAPSHOT.tar.gz /app/tokamak-server-0.1-SNAPSHOT.tar.gz
RUN cd /app && tar xvf tokamak-server-0.1-SNAPSHOT.tar.gz
RUN rm /app/tokamak-server-0.1-SNAPSHOT.tar.gz

WORKDIR /app/tokamak-server-0.1-SNAPSHOT
EXPOSE 8080
ENTRYPOINT ["bin/tokamak"]
