SHELL := /bin/bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:

SRC_DIR := src

OUT_DIR := out
SRCS := $(wildcard *.(java|rs|py|qnt))

CLS := $(wildcard *.(class|jar|rlib))

JCOMPILER := mvn
JPACKAGE := package -U
JVERSIONS := versions:use-latest-releases

RSCOMPILER := cargo
RSCLEAN := clean
RSPACKAGE := build --release
RSPACKAGEFAST := build
RSVERSIONS := update

QNTCOMPILER := quint
QNTARGS := verify --verbosity 5
QNTSPEC := ring_txns.qnt --invariant invariant_ring_agrees

.SUFFIXES: .java .py .rs .qnt

##
# targets that do not produce output files
##
.PHONY: all clean

##
# default target(s)
##
all: build-rust build-java

update-java-versions:
	@cd src/java-src; $(JCOMPILER) $(JVERSIONS)

update-rust-versions:
	@cd src/rust-src; $(RSCOMPILER) $(RSVERSIONS)

build-rust-fast:
	@echo 'building rust'
	@cd src/rust-src; $(RSCOMPILER) $(RSPACKAGEFAST)

build-rust:
	@echo 'building rust'
	@cd src/rust-src; $(RSCOMPILER) $(RSPACKAGE)

clean-rust:
	@echo 'cleaning rust'
	@cd src/rust-src; $(RSCOMPILER) $(RSCLEAN)

build-java:
	@echo 'building java'
	@cd src/java-src; $(JCOMPILER) $(JPACKAGE)

verify-quint:
	@echo 'verifying quint'
	@cd src/quint-src; $(QNTCOMPILER) $(QNTARGS) $(QNTSPEC)

# clean up any output files
##
clean:
	@echo 'cleaning output'
	@cd src/java-src; rm -rf target
	@cd src/rust-src; rm -rf target
	@cd src/quint-src; rm -rf _apalache-out