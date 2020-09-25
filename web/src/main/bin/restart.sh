#!/bin/bash

args=$@
sh stop.sh $args
sh start.sh $args
