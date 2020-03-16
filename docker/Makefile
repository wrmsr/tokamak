DOCKER_USER=wrmsr

DOCKER_IMAGES:= \
	tokamak-dev \
	tokamak-elasticsearch \
	tokamak-kafka \
	tokamak-memcached \
	tokamak-minio \
	tokamak-mysql \
	tokamak-postgres \
	tokamak-redis \
	tokamak-zookeeper \

DOCKER_COMPOSE_SERVICES:= \
	tokamak-dev \
	tokamak-elasticsearch \
	tokamak-kafka \
	tokamak-memcached \
	tokamak-minio \
	tokamak-mysql-master \
	tokamak-mysql-slave \
	tokamak-postgres-master \
	tokamak-postgres-slave \
	tokamak-redis \
	tokamak-zookeeper \

.PHONY: all
all: clean images

.PHONY: pull
pull:
	for IMG in $(DOCKER_IMAGES) ; do \
		docker pull "$(DOCKER_USER)/$$IMG" ; \
	done

.PHONY: push
push:
	for IMG in $(DOCKER_IMAGES) ; do \
		docker push "$(DOCKER_USER)/$$IMG" ; \
	done

.PHONY: images
images:
	for IMG in $(DOCKER_IMAGES) ; do \
		(cd .. && docker build --tag "$(DOCKER_USER)/$$IMG" -f "docker/$$IMG/Dockerfile" .) ; \
	done

.PHONY: stop
stop:
	docker-compose stop

.PHONY: clean
clean: stop
	for SVC in $(DOCKER_COMPOSE_SERVICES) ; do \
		docker-compose rm -fv "$$SVC" ; \
	done

.PHONY: start
start: images
	docker-compose start $(DOCKER_COMPOSE_SERVICES)

.PHONY: up
up: images
	docker-compose up $(DOCKER_COMPOSE_SERVICES)

.PHONY: reup
reup: clean up

.PHONY: down
down:
	docker-compose down

.PHONY: enable-ptrace
enable-ptrace:
	docker run --privileged -it alpine:3.7 sh -c 'echo 0 > /proc/sys/kernel/yama/ptrace_scope'
