(defproject match-block "0.1.0-SNAPSHOT"
  :description "Pattern matching blocks as values"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.match "0.2.0"]
                 [n01se/seqex "2.0.0"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}})
