(set-env!
 :source-paths #{"src/clj" "src/cljs" "test/clj"}
 :resource-paths #{"html"}
 :dependencies '[[adzerk/boot-cljs      "0.0-3308-0"]
                 [adzerk/boot-cljs-repl "0.1.10-SNAPSHOT"]
                 [adzerk/boot-reload    "0.3.1"]
                 [adzerk/boot-test "1.0.4"]
                 [cljsjs/hammer "2.0.4-4"]
                 [cljsjs/media-stream-recorder "1.2.6-0"]
                 [compojure "1.1.6"]
                 [datascript "0.11.5"]
                 [hiccup "1.0.5"]
                 [org.clojure/clojure "1.7.0-RC1"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/test.check "0.7.0"]
                 [org.omcljs/om "0.8.8"]
                 [pandeiro/boot-http "0.6.3-SNAPSHOT"]
                 [ring/ring-devel "1.4.0-RC1"]
                 [http-kit "2.1.18"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[adzerk.boot-test      :refer [test]]
 '[pandeiro.boot-http    :refer [serve]])

(task-options!
 cljs {:source-map true
       :optimizations :none
       :pretty-print true})

(require '[clojure.java.shell :only [sh]])

(deftask appengine-dev
  "Starts a python development server"
  []
  (fn [next-task]
    (let [f (sh "dev_appserver.py" "target" "--port" "7070" "--dev_appserver_log_level" "warning")]
      (fn [fileset]
        (let [result (next-task fileset)]
          (cleanup (future-cancel f))
          result)))))

(deftask appengine-build
  "Starts a python development server"
  []
  (fn [next-task]
    (fn [fileset]
      (let [result (next-task fileset)]
        (dosh "rm" "-r" "target/out")
        (dosh "rm" "target/main.js.map")
        (dosh "appcfg.py" "update" "target")
        result))))

(deftask build
  "Build an uberjar of this project that can be run with java -jar"
  []
  (comp
   (cljs :optimizations :advanced)
   (appengine-build)))

(deftask dev
  "Run project in development (start server manually)"
  []
  (comp (appengine-dev) (watch) (speak) (reload) (cljs-repl) (cljs)))
