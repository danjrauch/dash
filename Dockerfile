FROM clojure
# from alpine:latest
MAINTAINER Dan Rauch <drauch@hawk.iit.edu>
WORKDIR /dashdb
RUN apt-get -y update && apt-get install -y \
	autotools-dev \
	autoconf \
	git \
	libtool \
	man-db \
	valgrind \
	&& rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["/bin/bash"]

# WORKDIR /usr/src/dashdb
# CMD ["lein", "run"]

# RUN mkdir -p /usr/src/dashdb
# WORKDIR /usr/src/dashdb
# COPY project.clj /usr/src/dashdb/
# RUN lein deps
# COPY . /usr/src/dashdb
# RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar
# CMD ["java", "-jar", "app-standalone.jar"]