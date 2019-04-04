FROM clojure
MAINTAINER Dan Rauch <drauch@hawk.iit.edu>
WORKDIR /mesh
ENTRYPOINT ["/bin/bash"]

CMD ["lein", "run"]