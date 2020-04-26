![Test CI](https://github.com/reizist/embulk-filter-convert_unicode_sequence_to_string/workflows/Test%20CI/badge.svg)

[![Coverage Status](https://coveralls.io/repos/github/reizist/embulk-filter-convert_unicode_sequence_to_string/badge.svg)](https://coveralls.io/github/reizist/embulk-filter-convert_unicode_sequence_to_string)

# Convert Unicode Sequence To String filter plugin for Embulk

Convert unicode sequence to string filter plugin.

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

### Run Example

```
$ ./gradlew gem
$ embulk run ./embulk-example/seed.yml -I lib
```

docker environment:

```
docker-compose run embulk bash
embulk run ./embulk-example/seed.yml -I lib
```

## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```

## Development

```
./gradlew classpath # build
./gradlew checkstyle # check style
```

