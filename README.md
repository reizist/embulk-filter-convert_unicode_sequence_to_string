# Convert Unicode Sequence To String filter plugin for Embulk

TODO: Write short description here and build.gradle file.

## Overview

* **Plugin type**: filter

## Configuration

- **option1**: description (integer, required)
- **option2**: description (string, default: `"myvalue"`)
- **option3**: description (string, default: `null`)

## Example

```yaml
filters:
  - type: convert_unicode_sequence_to_string
    option1: example1
    option2: example2
```


## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```
