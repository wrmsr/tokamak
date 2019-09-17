SHELL=/bin/bash
UNAME:=$(shell uname)

PYTHON_VERSION=3.7.3

SYSTEM_PYENV:=$(shell if command -v pyenv > /dev/null ; then echo 1 ; else echo 0 ; fi)
PIP_ARGS:=$(shell if ! [ -z "$$CIRCLECI" ]; then echo '--quiet --progress-bar=off' ; fi)

ALL: clean package test


# Java

.PHONY: java-home
java-home:
ifndef JAVA_HOME
ifeq ($(UNAME), Darwin)
	$(eval JAVA_HOME=$(shell find /Library/Java/JavaVirtualMachines -name 'jdk1.8.0_*' -type d -maxdepth 1 | sort | tail -n1)/Contents/Home)
endif
endif

.PHONY: mvn-version
mvn-version: java-home
	JAVA_HOME=$(JAVA_HOME) ./mvnw --version

.PHONY: clean
clean: java-home
	JAVA_HOME=$(JAVA_HOME) ./mvnw clean
	for d in $$(find . -name 'tokamak-*' -type d -maxdepth 1) ; do find "$$d" -name '*.class' -delete ; done

.PHONY: package
package: java-home
	JAVA_HOME=$(JAVA_HOME) ./mvnw package -DskipTests

.PHONY: test
test: java-home
	JAVA_HOME=$(JAVA_HOME) ./mvnw test

.PHONY: install
install: java-home
	JAVA_HOME=$(JAVA_HOME) ./mvnw install -DskipTests

.PHONY: uninstall
uninstall: java-home
	JAVA_HOME=$(JAVA_HOME) ./mvnw dependency:purge-local-repository -DmanualInclude="com.wrmsr.tokamak"

.PHONY: dependency-tree
dependency-tree: java-home
	JAVA_HOME=$(JAVA_HOME) ./mvnw dependency:tree

.PHONY: dependency-updates
dependency-updates: java-home
	JAVA_HOME=$(JAVA_HOME) ./mvnw versions:display-dependency-updates


# Python

.PHONY: clean-python
clean-python:
	rm -rf .venv
	rm -rf .pyenv

.PHONY: venv
venv:
ifndef PYENV_ROOT
ifeq ($(SYSTEM_PYENV),1)
	$(eval PYENV_ROOT=$(shell bash -c 'dirname $$(dirname $$(command -v pyenv))'))
else
	if [ ! -d ".pyenv" ]; then \
		git clone https://github.com/pyenv/pyenv .pyenv ; \
	fi
	$(eval PYENV_ROOT=$(shell pwd)/.pyenv)
endif
endif

	if [ ! -d ".venv" ]; then \
		PYENV_ROOT=$(PYENV_ROOT) "$(PYENV_ROOT)/bin/pyenv" install -s -v $(PYTHON_VERSION) && \
		"$(PYENV_ROOT)/versions/$(PYTHON_VERSION)/bin/python" -m venv .venv && \
		.venv/bin/python -m pip install $(PIP_ARGS) --upgrade pip && \
		.venv/bin/python -m pip install $(PIP_ARGS) -r python/requirements.txt ; \
	fi

	$(eval PYTHON=$(shell pwd)/.venv/bin/python)


# Docker

.PHONY: docker
docker:
	docker build -t wrmsr/tokamak .

.PHONY: docker_test
docker_test: docker
	docker run --rm wrmsr/tokamak make test

.PHONY: docker_bash
docker_bash:
	docker run --detach-keys 'ctrl-o,ctrl-d' -it wrmsr/tokamak bash

.PHONY: docker_invalidate
docker_invalidate:
	date +%s > .dockertimestamp


# Utilities

.PHONY: fix-copyright
fix-copyright: venv
	.venv/bin/python python/fix_copyright.py
