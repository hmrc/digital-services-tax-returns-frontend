#!/bin/bash

sbt clean scalafmt test:scalafmtAll coverage test it/test coverageReport