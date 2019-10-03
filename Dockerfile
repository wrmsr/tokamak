FROM openjdk:8u222-slim-buster AS build
COPY .dockertimestamp /

RUN mkdir /build

COPY .mvn/ /build/.mvn
COPY mvnw /build/

COPY pom-deps.xml /build/
RUN cd /build && ./mvnw -f pom-deps.xml dependency:go-offline -Dmdep.addParentPoms=true -Dmdep.copyPom=true

COPY pom.xml /build/
COPY tokamak-api/ /build/tokamak-api
COPY tokamak-core/ /build/tokamak-core
COPY tokamak-dist/ /build/tokamak-dist
COPY tokamak-main/ /build/tokamak-main
COPY tokamak-spark/ /build/tokamak-spark
COPY tokamak-test/ /build/tokamak-test
COPY tokamak-util/ /build/tokamak-util

COPY .git /build/.git

RUN cd /build && ./mvnw package -DskipTests

RUN ( \
    cd /build/tokamak-dist/target && \
    tar xvf tokamak-*.tar.gz && \
    mv $(find . -name 'tokamak-*' -type d | head -n 1) tokamak \
)


FROM openjdk:13-slim-buster
COPY .dockertimestamp /

COPY --from=build build/tokamak-dist/target/tokamak/ /tokamak

RUN cd /tokamak/bin && for f in $(find * -type f -print) ; do ln -s "/tokamak/bin/$f" "/usr/bin/$f" ; done

WORKDIR /root
ENTRYPOINT ["tokamak"]
