(defproject io.factorhouse/slipway-jetty9 "1.0.8"

  :description "A Clojure Companion for Embedded Jetty"

  :url "https://github.com/factorhouse/slipway"

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :profiles {:dev   {:dependencies   [[clj-kondo "2022.08.03"]
                                      [clj-http "3.12.3" :exclusions [commons-io]]
                                      [ch.qos.logback/logback-classic "1.2.11"]
                                      [hiccup "1.0.5"]
                                      [metosin/reitit-ring "0.5.18"]]
                     :resource-paths ["dev-resources" "common/dev-resources"]
                     :plugins        [[lein-cljfmt "0.8.2"]]}
             :smoke {:pedantic? :abort}}

  :aliases {"check"  ["with-profile" "+smoke" "check"]
            "kondo"  ["with-profile" "+smoke" "run" "-m" "clj-kondo.main" "--lint" "src:test" "--parallel"]
            "fmt"    ["with-profile" "+smoke" "cljfmt" "check"]
            "fmtfix" ["with-profile" "+smoke" "cljfmt" "fix"]}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.logging "1.2.4"]
                 [ring/ring-servlet "1.9.5"]
                 [org.eclipse.jetty/jetty-server "9.4.48.v20220622"]
                 [org.eclipse.jetty.websocket/websocket-server "9.4.48.v20220622"]
                 [org.eclipse.jetty.websocket/websocket-servlet "9.4.48.v20220622"]
                 [org.eclipse.jetty/jetty-jaas "9.4.48.v20220622"]
                 [org.slf4j/slf4j-api "1.7.36"]]

  :source-paths ["src" "common/src" "common-javax/src"]
  :java-source-paths ["common-javax/src-java"]
  :test-paths ["test" "common/test"]

  :javac-options ["-target" "8" "-source" "8"])