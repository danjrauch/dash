(ns dashdb.core.cli
  (:require [dashdb.query.parse :as parse]
            [clojure.string :as str])
  (:gen-class)
  (:use clojure.java.shell))

(def ascii-right 68)
(def ascii-left 67)
(def ascii-enter 10)
(def ascii-escape 27)
(def ascii-backspace 127)

(defmulti prints
  (fn [f, & arg] (class arg)))

(defmethod prints :default [f, & arg]
  (doseq [item arg] (f item))
  (flush))

(defmethod prints String [f, & arg]
  (f arg)
  (flush))

(defmethod prints Character [f, & arg]
  (f arg)
  (flush))

;; Prints the prompt in the CLI.
(defn- print-prompt
  "Prints the command prompt."
  []
  (print "dash=> ") (flush))

;; Removes last character from the string.
(defmacro remove-last
  [txt]
  `(subs ~txt 0 (- (count ~txt) 1)))

;; Handles the backspace key stroke. It deletes the chars,
;; if there is, on the left hand side of the cursor.
(defmacro handle-backspace
  "Macro that handles backspace strokes"
  [command-buffer vertical-cursor-pos]
  `(if (not-empty ~command-buffer)
     (do
       (prints print "\b \b")
       (recur (remove-last ~command-buffer) (dec ~vertical-cursor-pos)))
     (recur ~command-buffer 0)))

;; Handles left key stroke that moves the cursor to the left,
;; if it is not the case, that the cursor is located in
;; its most link position.
(defmacro handle-left
  "Macro that handles left arrow key stroke."
  [command-buffer vertical-cursor-pos]
  `(if (and (< ~vertical-cursor-pos (count ~command-buffer)))
     (do
       (print (char 27))
       (print (char 91))
       (print (char 67))
       (flush)
       (recur ~command-buffer (inc ~vertical-cursor-pos)))
     (recur ~command-buffer ~vertical-cursor-pos)))
 
;; Handles right key stroke that moves the cursor to the right,
;; if it is not the case, that the cursor is located in its
;; most right position.
(defmacro handle-right
  "Macro that handles right arrow key stroke."
  [command-buffer vertical-cursor-pos]
  `(if (> ~vertical-cursor-pos 0)
     (do
      (print (char 27))
      (print (char 91))
      (print (char 68))
      (flush)
      (recur ~command-buffer (dec ~vertical-cursor-pos)))
    (recur ~command-buffer ~vertical-cursor-pos)))

(def history-cursor (atom 0))

;; REPL implementation. REPL is started by the main function
;; on start-up.
(defn repl
  "Read-Eval-Print-Loop implementation."
  []
  (print-prompt)
  (loop [command-buffer nil vertical-cursor-pos 0]
    (let [input-char (.read System/in)]
      (cond
        (= input-char ascii-escape)
        (do
          ;; by-pass the first char after escape-char.
          (.read System/in)
          (let [escape-char (.read System/in)]
            ;; Handle navigation keys, left and right key strokes.
            (cond
              (= escape-char ascii-right)
              (handle-right command-buffer vertical-cursor-pos)
              (= escape-char ascii-left)
              (handle-left command-buffer vertical-cursor-pos))))
        ;; On-enter pressed.
        (= input-char ascii-enter)
        (do
          (reset! history-cursor 0)
          (println "")
          (if (not (= (str/trim command-buffer) "exit"))
            (parse/parse-input (str/trim command-buffer))
            (System/exit 0)
            )
          )
        ;; On-backspace entered.
        (= input-char ascii-backspace)
        (handle-backspace command-buffer vertical-cursor-pos)
        ;; default case
        :else
        (do
          (prints print (char input-char))
          (recur (str command-buffer (char input-char)) (inc vertical-cursor-pos))
          )))))

(defn addShutdownHook
  "Add a function as shutdown hook on JVM exit."
  [func]
  (.addShutdownHook (Runtime/getRuntime) (Thread. func)))

(defn turn-char-buffering-on
  []
  (sh "sh" "-c" "stty -g < /dev/tty")
  (sh "sh" "-c" "stty -icanon min 1 < /dev/tty")
  (sh "sh" "-c" "stty -echo </dev/tty"))

(defn turn-char-buffering-off
  []
  (flush)
  (sh "sh" "-c" "stty echo </dev/tty"))

(defn start-repl
  "Starts the repl session"
  []
  (addShutdownHook (fn [] (turn-char-buffering-off)))
  (turn-char-buffering-on)
  (while true (repl))
  (System/exit 0))

(defn -main [ & args ]
  (start-repl))