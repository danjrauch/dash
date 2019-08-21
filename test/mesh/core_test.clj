(ns mesh.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as javaio]
            [test.util :refer :all]))

(println "☔️ Running tests on Clojure" (clojure-version) "| JVM" (System/getProperty "java.version") (str "(" (System/getProperty "java.vm.name") " v" (System/getProperty "java.vm.version") ")"))