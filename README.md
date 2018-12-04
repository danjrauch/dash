<p align="center">
  <img src="./logos/dash.png">
</p>

# dashdb

dashdb is *(will be)* a lightweight graph-based database system written with Clojure. It's also the predecessor to rubik (a scalable future version of dashdb).

## Story

dashdb is being developed as a hobby project. Development began on December 1 2018.

### Source 

```sh
git clone https://github.com/danjrauch/dashDB dashdb
```

### Development

Want to contribute? Great!
Email me @ drauch@hawk.iit.edu

### Building

I currently use lein, but may expand on this eventually.

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

Distributed under the Eclipse Public License either version 1.0 or (atyour option) any later version.