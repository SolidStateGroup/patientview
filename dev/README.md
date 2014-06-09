# FHIRBase Developers Guide

## Installation

### Clone & Configure

```bash

git clone https://github.com/fhirbase/fhirbase.git`
cd dev

# copy configuration template
cp cfg/config.sh.template cfg/config.sh

# edit configuration
```

### Using vagrant

The simplest way to install development environment is using vagrant.
So you will get virtual sandbox with everything installed inside and not interfering
you local env.

Install vagrant - https://docs.vagrantup.com/v2/installation/


``` bash
  cd dev && vagrant up
```

If you are on linux, you can skip performance trade-offs using lxc containers
instead virtual box virtualization.

Install vagrant-lxc - https://github.com/fgrehm/vagrant-lxc

```bash
 cd dev && vagrant up --provider lxc
```

Vagrant will downloads box (virtual or lxc - depends on your system),
start it and provisions with bootstrap.sh script,
which will install everything you need.

By configuration vagrant is forwards 5433 port to your local host (you can change port in Vagrantfile).
So you can connect to `fhir` database inside vagrant vm with user `vagrant`:

`psql -h localhost -p 5433 -U vagrant fhirbase`


### Install locally & manually

Install deps:

* postgresql-9.3
* postgresql-contrib
* pgtap

`HINT: look at dev/bootstrap.sh`

Configure connection in config.sh and run `./runme install`

## Project structure description

```
  cfg/                   - folder with configuration for runme unit
  fhir/                  - FHIR metadata from official site
  sql/                   - sql generation code
  test/                  - pg_tap tests
  .vimrc                 - helpers for vim users
  Vagrantfile            - vagrant manifest
  apt.postgresql.org.sh* - copy of postgresql installation on ubuntu, should be moved to scripts
  bootstrap.sh           - bash script to install all deps, should be moved to scripts
  install.sql            - sql for installation, should be moved to sql
  pg_prove*              - pgtap test runner, should be moved to scripts
  runme*                 - bash util for development [build, tests, installation]
```

### Code Structure

```
  01_extensions.sql        - create extensions and schemas
  03_meta.sql              - create schema for FHIR metainformation
  04_load_meta.sql         - load meta data from FHIR xml
  05_functions.sql         - create helper functions
  06_datatypes.sql         - views for more convenient datatype's schema generation
  07_schema.sql            - views for schema generation
  08_generate_schema.sql   - generate schema script
  10__insert_helpers.sql   - helper functions for insert procedure generation
  10_insert.sql            - insert procedure generation
  11_delete.sql            - delete procedure
  12_update.sql            - update procedure
```

### Development workflow

Main util for developers is `./runme` bash script.

  Usage: ./runme [-c cfg/config_file.sh] [command] [args]

  Available commands:

  -h || help                this help text
  test [part_of_test_name]  run tests from dev/test dir. if test name is provided, run specified tests
  install dbname            generate and install fhirbase schema into specified DB
  build                     build fhirbase.sql file


We are addicted to TDD.
Tests written with pgtap is placed in tests/.
See more about [pgtap](http://pgtap.org/).

You can run tests with `./runme test [test_name_part]`
or in vagrant
`RUN_VAGRANT=true ./runme test [test_name_part]`

### Contribution

We are very interested in your collaboration & contribution.
You can contribute by creating [issues](https://github.com/fhirbase/fhirbase/issues?state=open)
and by [pull requests](https://help.github.com/articles/using-pull-requests).
