# <img align="right" src="https://raw.githubusercontent.com/danjrauch/dash/master/logos/dashlogo_readme.png"> dash [![Build Status](https://travis-ci.org/danjrauch/dash.svg?branch=master)](https://travis-ci.org/danjrauch/dash)

A lightweight graph-based database system written with Clojure.

## Description

dash is being developed as a hobby project. It strives to allow for the storage and search of graphs. Development began on December 1 2018. 

### Source 

```sh
git clone https://github.com/danjrauch/dash dash
```

<!-- ### Development

Want to contribute?
Email me @ drauch@hawk.iit.edu

### Building

dash uses Leiningen. -->

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

Begin by creating a graph.
```sh
create name
```

Find and load a graph into memory.
```sh
load graph name
```

You can create nodes for your graph.
```sh
create (name:descriptor:... {attribute name:attribute value ...}) (...)
```

You can also create undirected and directed edges for your graph.
```sh
create (name)-[label]-(name)
```
or
```sh
create (name)<-[label]-(name)
```
or
```sh
create (name)-[label]->(name)
```

License
----
Copyright © 2019 Dan Rauch

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.