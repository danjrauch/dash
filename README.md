<p align="center">
  <img src="./logos/dash.png">
</p>

# dashdb [![Build Status](https://travis-ci.org/danjrauch/dashDB.svg?branch=master)](https://travis-ci.org/danjrauch/dashDB)

dashdb is *(will be)* a lightweight graph-based database system written with Clojure. It's also the predecessor to rubik (a scalable future version of dashdb).

## Story

dashdb is being developed as a hobby project. It strives to allow for the storage and search of entities and relationships through a tableless architecture. 
In other words entities and relationships are first class types. It supports a cypher-like, derivative query language. Development began on December 1 2018. 

### Source 

```sh
git clone https://github.com/danjrauch/dashDB dashdb
```

### Development

Want to contribute? Great!
Email me @ drauch@hawk.iit.edu

### Building

dashdb uses Leiningen.

### Docker

dashdb is very easy to install and deploy in a Docker container. In the directory with the Dockerfile run:
```sh
docker build .
docker run -ti --name dashdb -v $(pwd):/dashdb <dockerID from build output>
```
Then afterwards just run:
```sh
docker start -i dashdb
```
to restart the container.

License
----
Copyright © 2018 Dan Rauch

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
