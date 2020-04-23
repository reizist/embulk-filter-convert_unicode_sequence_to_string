# Convert Unicode Sequence To String filter plugin for Embulk

TODO: Write short description here and build.gradle file.

## Overview

* **Plugin type**: filter

## Configuration

- **target_columns**: columns to convert (array of string)

## Example

Say input.csv is as follows:
```csv
id,name
0,hoge\u0000
1,fuga\u0041
2,normal_string
```

```yaml
filters:
  - type: convert_unicode_sequence_to_string
    target_columns:
      - name
```

converts unicode escape sequence like below:

```csv
id,name
0,hoge
1,fugaA
2,normal_string
```


## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```
