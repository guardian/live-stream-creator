#!/usr/bin/env bash

# always use the multimedia account
export AWS_PROFILE=multimedia

packer build provisioning.json

# clean up after ourselves
unset AWS_PROFILE
