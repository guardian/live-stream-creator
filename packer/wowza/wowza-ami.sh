#!/usr/bin/env bash

rsync -a /home/ec2-user/config/ /usr/local/WowzaStreamingEngine/
rm -rf /home/ec2-user/config

service WowzaStreamingEngineManager restart
service WowzaStreamingEngine restart

# jq is needed for the scripts under bin/
yum install jq -y

# localhost:8087 needs to be available, wait for wowza to be ready before continuing
sleep 5m

/home/ec2-user/bin/enable-stream-targets
/home/ec2-user/bin/enable-transcoder
