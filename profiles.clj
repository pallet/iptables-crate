{:provided {:dependencies [[org.clojure/clojure "1.5.1"]
                           [com.palletops/pallet "0.8.0-RC.9"
                            :exclusions [org.clojure/clojure]]]}
 :dev {:dependencies [[com.palletops/pallet "0.8.0-RC.9"
                       :classifier "tests"
                       :exclusions [org.clojure/tools.logging]]
                      [com.palletops/crates "0.1.2-SNAPSHOT"]
                      [com.palletops/pallet-test-env "RELEASE"]
                      [ch.qos.logback/logback-classic "1.1.1"]]
       :plugins [[com.palletops/lein-test-env "RELEASE"]
                 [lein-pallet-release "RELEASE"]
                 [com.palletops/lein-pallet-crate "RELEASE"]]
       :pallet-release
       {:url "https://pbors:${GH_TOKEN}@github.com/pallet/iptables-crate.git"
        :branch "master"}}
 :aws {:pallet/test-env {:test-specs
                         [ ;; {:selector :ubuntu-13-10}
                          ;; {:selector :ubuntu-13-04
                          ;;  :expected [{:feature ["oracle-java-8"]
                          ;;              :expected? :not-supported}]}
                          ;; {:selector :ubuntu-12-04}
                          {:selector :amzn-linux-2013-092}
                          ;; {:selector :centos-6-5}
                          ;; {:selector :debian-7-4}
                          ;; {:selector :debian-6-0}
                          ]}}
 :vmfest {:pallet/test-env {:test-specs
                            [{:selector :ubuntu-13-04}]}}}
