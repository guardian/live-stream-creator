# Packer

A Packer definition for a Wowza Media Server AMI with Guardian configuration backed in to the box.

The `config` directory will be `rsync` to `/usr/local/WowzaStreamingEngine`.

## Usage

```sh
./packer-build.sh
```

The ami that is generated will be used in the cloudformation definition of this stack.
