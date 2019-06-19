.PHONY: fix-copyright
fix-copyright:
	find tokamak-* -name '*.java' | xargs -P8 -n1 perl -i -p0e 's/\*\/\n\npackage com\./\*\/\npackage com./s'
