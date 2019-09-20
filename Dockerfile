FROM openjdk:8u222-stretch AS build
COPY .dockertimestamp /

RUN mkdir /build

COPY .mvn/ /build/.mvn
COPY mvnw /build/

COPY pom.xml /build/
COPY tokamak-api/pom.xml /build/tokamak-api/pom.xml
COPY tokamak-core/pom.xml /build/tokamak-core/pom.xml
COPY tokamak-main/pom.xml /build/tokamak-main/pom.xml
COPY tokamak-spark/pom.xml /build/tokamak-spark/pom.xml
COPY tokamak-test/pom.xml /build/tokamak-test/pom.xml
COPY tokamak-util/pom.xml /build/tokamak-util/pom.xml

RUN ( \
    cd /build && \
    for f in $(find . -name 'pom.xml') ; do \
        cat "$f" | sed 's/@BEGIN-IGNORE-FOR-DEPS@-->//g' | sed 's/<!--@END-IGNORE-FOR-DEPS@//g' > "$f.deps" && \
        mv "$f.deps" "$f" ; \
    done && \
    ./mvnw dependency:go-offline -Dmdep.addParentPoms=true -Dmdep.copyPom=true \
)

COPY pom.xml /build/
COPY tokamak-api/ /build/tokamak-api
COPY tokamak-core/ /build/tokamak-core
COPY tokamak-main/ /build/tokamak-main
COPY tokamak-spark/ /build/tokamak-spark
COPY tokamak-test/ /build/tokamak-test
COPY tokamak-util/ /build/tokamak-util

COPY .git /build/.git

RUN cd /build && ./mvnw package -DskipTests


FROM openjdk:8u222-stretch
COPY .dockertimestamp /

COPY --from=build build/tokamak-main/target/tokamak-*.tar.gz /tokamak-*.tar.gz

RUN ( \
    tar xvf tokamak-*.tar.gz && \
    rm tokamak-*.tar.gz && \
    mv $(find . -name 'tokamak-*' -type d | head -n 1) tokamak && \
    (cd /tokamak/bin && for f in $(find * -type f -print) ; do ln -s "/tokamak/bin/$f" "/usr/bin/$f" ; done) \
)

WORKDIR /root
ENTRYPOINT ["tokamak"]
