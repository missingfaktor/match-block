(ns match-block.util)

(defn raise-error [message]
  (throw (RuntimeException. message)))
