FROM clojure
MAINTAINER Dan Rauch <drauch@hawk.iit.edu>
WORKDIR /dashdb
ENTRYPOINT ["/bin/bash"]

CMD ["lein", "run"]