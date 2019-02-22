FROM clojure
MAINTAINER Dan Rauch <drauch@hawk.iit.edu>
WORKDIR /dash
ENTRYPOINT ["/bin/bash"]

CMD ["lein", "run"]