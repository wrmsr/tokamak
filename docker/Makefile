DOCKER_USER=wrmsr

DOCKER_DB_IMAGES:= \
	tokamak-elasticsearch \
	tokamak-kafka \
	tokamak-memcached \
	tokamak-minio \
	tokamak-mysql \
	tokamak-postgres \
	tokamak-redis \
	tokamak-zookeeper \

DOCKER_IMAGES:= \
	$(DOCKER_DB_IMAGES) \
	tokamak-dev \

DOCKER_COMPOSE_DB_SERVICES:= \
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

DOCKER_COMPOSE_SERVICES:= \
	$(DOCKER_COMPOSE_DB_SERVICES) \
	tokamak-dev \

.PHONY: all
all: clean dev-images

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

.PHONY: db-images
db-images:
	for IMG in $(DOCKER_DB_IMAGES) ; do \
		(cd .. && docker build --tag "$(DOCKER_USER)/$$IMG" -f "docker/$$IMG/Dockerfile" .) ; \
	done

.PHONY: dev-images
dev-images: db-images
	(cd .. && docker build --tag "$(DOCKER_USER)/tokamak-dev" -f docker/tokamak-dev/Dockerfile .)

.PHONY: stop
stop:
	docker-compose stop

.PHONY: clean
clean: stop
	for SVC in $(DOCKER_COMPOSE_SERVICES) ; do \
		docker-compose rm -fv "$$SVC" ; \
	done

.PHONY: dev-start
dev-start: dev-images
	docker-compose start $(DOCKER_COMPOSE_DB_SERVICES) tokamak-dev

.PHONY: dev-up
dev-up: dev-images
	docker-compose up $(DOCKER_COMPOSE_DB_SERVICES) tokamak-dev

.PHONY: dev-reup
dev-reup: clean dev-up

.PHONY: enable-ptrace
enable-ptrace:
	docker run --privileged -it alpine:3.7 sh -c 'echo 0 > /proc/sys/kernel/yama/ptrace_scope'
