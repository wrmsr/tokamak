FROM openjdk:8u222-stretch AS build
COPY .dockertimestamp /

RUN mkdir /build

COPY .mvn/ /build/.mvn
COPY mvnw /build/

COPY pom.xml /build/
COPY tokamak-core/pom.xml /build/tokamak-core/pom.xml
COPY tokamak-main/pom.xml /build/tokamak-main/pom.xml
COPY tokamak-spark/pom.xml /build/tokamak-spark/pom.xml
COPY tokamak-test/pom.xml /build/tokamak-test/pom.xml

RUN ( \
    cd /build && \
    for f in $(find . -name 'pom.xml') ; do \
        cat "$f" | sed 's/@BEGIN-IGNORE-FOR-DEPS@-->//g' | sed 's/<!--@END-IGNORE-FOR-DEPS@//g' > "$f.deps" && \
        mv "$f.deps" "$f" ; \
    done && \
    ./mvnw dependency:go-offline -Dmdep.addParentPoms=true -Dmdep.copyPom=true \
)

COPY pom.xml /build/
COPY tokamak-core/ /build/tokamak-core
COPY tokamak-main/ /build/tokamak-main
COPY tokamak-spark/ /build/tokamak-spark
COPY tokamak-test/ /build/tokamak-test

COPY .git /build/.git

RUN cd /build && ./mvnw package -DskipTests


FROM openjdk:8u222-stretch
COPY .dockertimestamp /

COPY --from=build build/tokamak-main/target/tokamak-main-*.tar.gz /tokamak-main-*.tar.gz
RUN tar xvf tokamak-main-*.tar.gz
RUN rm tokamak-main-*.tar.gz
RUN mv $(find . -name 'tokamak-main-*' -type d | head -n 1) tokamak
RUN cd /tokamak/bin && for f in $(find . -type f) ; do ln -s "/tokamak/bin/$f" "/usr/bin/$f" ; done

WORKDIR /root
CMD ["tokamak"]
