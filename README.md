mesh [![Build Status](https://travis-ci.org/danjrauch/mesh.svg?branch=master)](https://travis-ci.org/danjrauch/mesh)

A graph processing library for Clojure.

## Description

mesh is being developed as a hobby project. Its three namespaces offer several ways to process graph data. Development began on December 1 2018. 

[Rationale and Design Document](https://docs.google.com/document/d/1tJ0OM2-mhQ-G6V91LOSVeitK9jRDkzplbb81cemqD9c/edit?usp=sharing)

# CLI

mesh 

## Usage

### Example

<img align="center" src="https://raw.githubusercontent.com/danjrauch/mesh/master/images/example1.png">

### Insert

Begin by creating a graph.
```sh
mesh=> create ^name
```

Create nodes for your graph.
```sh
mesh=> create (^name:^descriptor:... {^attribute-name:^attribute-value ...}) (...)
```

Create undirected and directed edges for your graph.
```sh
mesh=> create (^name)-[^label]-(^name)
```
or
```sh
mesh=> create (^name)<[^label]-(^name)
```
or
```sh
mesh=> create (^name)-[^label]>(^name)
```
depending on if you want an undirected or directed edge.

### Load

Find and load a graph into memory.
```sh
mesh=> load ^name
```

### Display 

List the graphs in the database.
```sh
mesh=> list
```

Display nodes on the screen.
```sh
mesh=> show (^name)
```

### Save

Save your current graph to the graph.fress file.
```sh
mesh=> save
```

# Persistent Graphs



# In-memory Graphs

### Docker

mesh is very easy to install and deploy in a Docker container. In the directory with the Dockerfile run:
```sh
docker build .
docker run -ti --name mesh -v $(pwd):/mesh <dockerID from build output>
```
Then afterwards just run:
```sh
docker start -i mesh
```
to restart the container.

License
----
Copyright © 2018-2019 Dan Rauch

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.