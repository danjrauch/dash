# <img align="right" src="https://raw.githubusercontent.com/danjrauch/dash/master/logos/dashlogo_readme.png"> dash [![Build Status](https://travis-ci.org/danjrauch/dash.svg?branch=master)](https://travis-ci.org/danjrauch/dash)

A lightweight graph-based database system written with Clojure.

## Description

dash is being developed as a hobby project. It strives to allow for the storage and search of entities and relationships through a tableless architecture. 
In other words entities and relationships are first class types. It supports a cypher-like, derivative query language. Development began on December 1 2018. 

### Source 

```sh
git clone https://github.com/danjrauch/dash dash
```

### Development

Want to contribute?
Email me @ drauch@hawk.iit.edu

### Building

dash uses Leiningen.

### Docker

dash is very easy to install and deploy in a Docker container. In the directory with the Dockerfile run:
```sh
docker build .
docker run -ti --name dash -v $(pwd):/dash <dockerID from build output>
```
Then afterwards just run:
```sh
docker start -i dash
```
to restart the container.

## Usage



License
----
Copyright © 2018 Dan Rauch

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
