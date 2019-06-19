SHELL=/bin/bash
UNAME:=$(shell uname)

PYTHON_VERSION=3.7.3

SYSTEM_PYENV:=$(shell if command -v pyenv ; then echo 1 ; else echo 0 ; fi)
PIP_ARGS:=$(shell if ! [ -z "$$CIRCLECI" ]; then echo '--quiet --progress-bar=off' ; fi)

ALL: clean package test

.PHONY: java_home
java_home:
	@true

ifndef JAVA_HOME
ifeq ($(UNAME), Darwin)
	export JAVA_HOME=$(shell find /Library/Java/JavaVirtualMachines -name 'jdk1.8.0_*' -type d -maxdepth 1 | sort | tail -n1)
endif
endif

.PHONY: clean
clean: java_home
	./mvnw clean

.PHONY: fix-copyright
fix-copyright:
	find tokamak-* -name '*.java' | xargs -P8 -n1 perl -i -p0e 's/\*\/\n\npackage com\./\*\/\npackage com./s'

.PHONY: package
package: java_home
	./mvnw package -DskipTests

.PHONY: test
test: java_home
	./mvnw test

.PHONY: dependency-tree
dep-tree: java_home
	./mvnw dependency:tree

.PHONY: dependency-updates
dependency-updates: java_home
	./mvnw versions:display-dependency-updates

.PHONY: clean-python
clean-python:
	rm -rf .pyenv
	rm -rf .venv

.PHONY: venv
venv:
ifeq ($(SYSTEM_PYENV),0)
	if [ ! -d ".pyenv" ]; then \
		git clone https://github.com/pyenv/pyenv .pyenv ; \
	fi

	export PATH="$(shell pwd)/.pyenv/bin:$(PATH)"
	pyenv install -s $(PYTHON_VERSION)
endif

	if [ ! -d ".venv" ]; then \
		".pyenv/versions/$(PYTHON_VERSION)/bin/python" -m venv .venv ; \
	fi

	source .venv/bin/activate

	pip install $(PIP_ARGS) \
		sqlalchemy \
