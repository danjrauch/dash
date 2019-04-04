(ns dash.core.cli
  (:require [clojure.string :as str]
            [dash.core.input :as input])
  (:gen-class)
  (:use clojure.java.shell))

(def ascii-right 67)
(def ascii-left 68)
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

(defn- move-cursor-to-pos
  [pos cursor_pos]
  (if (> pos cursor_pos)
    (dotimes [_ (- pos cursor_pos)]
      (print (char 27)) (print (char 91)) (print (char ascii-right)) (flush))
    (dotimes [_ (- cursor_pos pos)]
      (print (char 27)) (print (char 91)) (print (char ascii-left)) (flush))))

;;; Cleans the command line. Used in up and down arrow actions.
(defn- ^:dynamic clean-command-line
  "Cleans up the command line."
  [buffer cursor_pos]
  (when (> (count buffer) cursor_pos)
    (move-cursor-to-pos (count buffer) cursor_pos))
  (loop [new_pos (count buffer)]
    (when (> new_pos 0)
      (prints print "\b \b")
      (recur (dec new_pos)))))

;; Removes character before the cursor from the string.
(defn- delete-char
  [buffer cursor_pos]
  (str (subs buffer 0 (dec cursor_pos)) (when (< cursor_pos (count buffer)) (subs buffer cursor_pos))))

;; Add character after the cursor from the string.
(defn- insert-char
  [buffer cursor_pos c]
  (cond
    (= cursor_pos (count buffer)) (str buffer c)
    :else (str (subs buffer 0 cursor_pos) c (subs buffer cursor_pos))))

(defn- refresh-command-line
  [buffer new_buffer cursor_pos next_pos]
  (clean-command-line buffer cursor_pos)
  (prints print new_buffer)
  (move-cursor-to-pos next_pos (count new_buffer)))

;; Handles the backspace key stroke. It deletes the chars,
;; if there is, on the left hand side of the cursor.
(defmacro handle-backspace
  "Macro that handles backspace strokes"
  [buffer cursor_pos]
  `(if (and (not-empty ~buffer) (> ~cursor_pos 0))
     (do
       (refresh-command-line ~buffer (delete-char ~buffer ~cursor_pos) ~cursor_pos (dec ~cursor_pos))
       (recur (delete-char ~buffer ~cursor_pos) (dec ~cursor_pos)))
     (recur ~buffer 0)))

;; Handles left key stroke that moves the cursor to the left,
;; if it is not the case, that the cursor is located in
;; its most left position.
(defmacro handle-left
  "Macro that handles left arrow key stroke."
  [buffer cursor_pos]
  `(if (> ~cursor_pos 0)
     (do
       (print (char 27))
       (print (char 91))
       (print (char ascii-left))
       (flush)
       (recur ~buffer (dec ~cursor_pos)))
     (recur ~buffer ~cursor_pos)))

;; Handles right key stroke that moves the cursor to the right,
;; if it is not the case, that the cursor is located in its
;; most right position.
(defmacro handle-right
  "Macro that handles right arrow key stroke."
  [buffer cursor_pos]
  `(if (< ~cursor_pos (count ~buffer))
     (do
       (print (char 27))
       (print (char 91))
       (print (char ascii-right))
       (flush)
       (recur ~buffer (inc ~cursor_pos)))
     (recur ~buffer ~cursor_pos)))

(def history-cursor (atom 0))

;; REPL implementation. REPL is started by the main function
;; on start-up.
(defn repl
  "Read-Eval-Print-Loop implementation."
  []
  (print-prompt)
  (loop [buffer nil cursor_pos 0]
    (let [input_char (.read System/in)]
      (cond
        (= input_char ascii-escape)
        (do
          ;; by-pass the first char after escape-char.
          (.read System/in)
          (let [escape-char (.read System/in)]
            ;; Handle navigation keys, left and right key strokes.
            (cond
              (= escape-char ascii-right)
              (handle-right buffer cursor_pos)
              (= escape-char ascii-left)
              (handle-left buffer cursor_pos))))
        ;; On-enter pressed.
        (= input_char ascii-enter)
        (do
          (reset! history-cursor 0)
          (print " ")
          (cond
            (nil? buffer) ""
            (= "" (str/trim buffer)) ""
            (some #{(str/trim buffer)} '("quit" "exit")) (do (println) (System/exit 0))
            :else (input/handle-input (str/trim buffer))))
        ;; On-backspace entered.
        (= input_char ascii-backspace)
        (handle-backspace buffer cursor_pos)
        ;; default case
        :else
        (do
          (refresh-command-line buffer (insert-char buffer cursor_pos (char input_char)) cursor_pos (inc cursor_pos))
          (recur (insert-char buffer cursor_pos (char input_char)) (inc cursor_pos)))))))

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

(defn -main [& args]
  (start-repl))