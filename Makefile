SHELL=/bin/bash
UNAME:=$(shell uname)

.PHONY: _java_home
_java_home:
	@true
ifndef JAVA_HOME
ifeq ($(UNAME), Darwin)
	export JAVA_HOME=$(shell find /Library/Java/JavaVirtualMachines -name 'jdk1.8.0_*' -type d -maxdepth 1 | sort | tail -n1)
endif
endif

.PHONY: fix-copyright
fix-copyright:
	find tokamak-* -name '*.java' | xargs -P8 -n1 perl -i -p0e 's/\*\/\n\npackage com\./\*\/\npackage com./s'

.PHONY: clean
clean: _java_home
	./mvnw clean

.PHONY: package
package: _java_home
	./mvnw package -DskipTests

.PHONY: test
test: _java_home
	./mvnw test

.PHONY: dependency-tree
dep-tree: _java_home
	./mvnw dependency:tree

.PHONY: dependency-updates
dependency-updates: _java_home
	./mvnw versions:display-dependency-updates
