(defproject uk.co.hughpowell/railway-oriented-clj "0.3.11"
  :description "Stop throwing exceptions, discard nil and explicitly deal with your failures"
  :url "https://github.com/HughPowell/railway-oriented-clj"
  :license {:name "Mozilla Public License v2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clojure-future-spec "1.9.0-beta4"]]
  :plugins [[update-readme "0.1.1"]]
  :repositories [["releases" {:url "https://clojars.org/repo/"
                              :username :env
                              :password :env
                              :sign-releases false}]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["update-readme"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["update-readme"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
