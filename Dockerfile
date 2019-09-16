FROM openjdk:8u222-stretch AS build
COPY .dockertimestamp /

RUN mkdir /build

COPY .mvn/ /build/.mvn
COPY mvnw /build/

COPY pom.xml /build/
RUN cd /build && cat pom.xml | sed 's/@IGNORE-FOR-DEPS@-->//g' | sed 's/<!--@END-IGNORE-FOR-DEPS@//g' > pom-deps.xml
RUN cd /build && ./mvnw -f pom-deps.xml dependency:resolve -Dmdep.addParentPoms=true -Dmdep.copyPom=true

COPY tokamak-core/ /build/tokamak-core
COPY tokamak-server/ /build/tokamak-server
COPY tokamak-spark/ /build/tokamak-spark
COPY tokamak-test/ /build/tokamak-test

COPY .git /build/.git

RUN cd /build && ./mvnw clean package -DskipTests


FROM openjdk:8u222-stretch
COPY .dockertimestamp /

RUN mkdir /app
COPY --from=build build/tokamak-server/target/tokamak-server-*.tar.gz /app/tokamak-server-*.tar.gz
RUN cd /app && tar xvf tokamak-server-*.tar.gz
RUN rm /app/tokamak-server-*.tar.gz
RUN cd /app && ln -s $(find . -name 'tokamak-server-*' -type d | head -n 1) tokamak-server

WORKDIR /app/tokamak-server
CMD ["bin/tokamak"]
