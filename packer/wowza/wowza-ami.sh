#!/usr/bin/env bash

rsync -a /home/ec2-user/config/ /usr/local/WowzaStreamingEngine/
rm -rf /home/ec2-user/config
