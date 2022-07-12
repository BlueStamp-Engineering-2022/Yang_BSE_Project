#!/bin/sh
gpio -g mode $1 out
gpio -g write $1 $2
