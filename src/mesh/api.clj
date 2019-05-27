(ns mesh.api
  "Asynchronous compojure-api application."
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [environ.core :as environ]
            [manifold.deferred :as d]
            [schema.core :as s]
            [clojure.core.async :as async]
            [clojure.data.json :as json]
            [mesh.graph :as graph]
            [mesh.persist :as persist]
            compojure.api.async))

(def data_dir
  (if (= (environ/env :clj-env) "test")
    "test/test_data/"
    "data/"))

(s/defschema Node
  {:name s/Str
   :descriptors [s/Str]
   :attributes {s/Keyword s/Str}
   :adjacency_map {s/Keyword [ {:w s/Num :label s/Str} ]}
   }
  )

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "Mesh API"
                   :description "REST API for mesh"}
            :tags [{:name "meshapi", :description ""}]
            :consumes ["application/json"]
            :produces ["application/json"]}}}

   (context "/api" []
     :tags ["api"]

     (GET "/node" []
       :return {:result Node}
       :query-params [graph :- String, node :- String]
       :summary "Returns a node"
       (let [n (graph/return-node (persist/read-graph graph data_dir) node)
             jnode {:name (:name n)
                    :descriptors (vec (:descriptor_set n))
                    :attributes (:attribute_map n)
                    :adjacency_map (into {} (map (fn [[key val]] [key (vec val)]) (:adjacency_map node)))
                    }]
         (println jnode)
         (ok {:result jnode})))

    ;  (GET "/minus" []
    ;    :return {:result Long}
    ;    :query-params [x :- Long, y :- Long]
    ;    :summary "subtract two numbers from each other"
    ;    (fn [_ respond _]
    ;      (future
    ;        (respond (ok {:result (- x y)})))
    ;      nil))

    ;  (GET "/times" []
    ;    :return {:result Long}
    ;    :query-params [x :- Long, y :- Long]
    ;    :summary "multiply two numbers together"
    ;    (d/success-deferred
    ;     (ok {:result (* x y)})))

    ;  (GET "/divide" []
    ;    :return {:result Float}
    ;    :query-params [x :- Long, y :- Long]
    ;    :summary "divide two numbers together"
    ;    (async/go (ok {:result (float (/ x y))})))
     )

   (context "/resource" []
     (resource
      {:responses {200 {:schema {:total Long}}}
       :handler (fn [_ respond _]
                  (future
                    (respond (ok {:total 42})))
                  nil)}))))