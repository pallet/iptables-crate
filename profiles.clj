{:provided {:dependencies [[org.clojure/clojure "1.5.1"]
                           [com.palletops/pallet "0.8.0-RC.9"
                            :exclusions [org.clojure/clojure]]]}
 :dev {:dependencies [[com.palletops/pallet "0.8.0-RC.9" :classifier "tests"]
                      [com.palletops/pallet-test-env "RELEASE"]
                      [ch.qos.logback/logback-classic "1.0.9"]]
       :plugins [[com.palletops/pallet-test-env "RELEASE"]
                 [lein-pallet-release "RELEASE"]
                 [com.palletops/lein-pallet-crate "RELEASE"]]
       :pallet-release
       {:url "https://pbors:${GH_TOKEN}@github.com/pallet/iptables-crate.git"
        :branch "master"}}}
