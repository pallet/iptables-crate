(ns pallet.crate.iptables-test
  (:use pallet.crate.iptables)
  (:require
   [pallet.action :as action]
   [pallet.actions :as actions]
   [pallet.build-actions :as build-actions]
   [pallet.script.lib :refer [make-temp-file rm]]
   [pallet.stevedore :as stevedore])
  (:use clojure.test
        pallet.test-utils))

;; (use-fixtures :once with-ubuntu-script-template with-bash-script-language)

;; (deftest iptables-test
;;   []
;;   (testing "debian"
;;     (is (= (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :ubuntu}}}
;;              (stevedore/script (var tmp @(make-temp-file iptablesXXXX)))
;;              (actions/remote-file
;;               "$tmp"
;;               :content
;;               "*filter\n:INPUT ACCEPT\n:FORWARD ACCEPT\n:OUTPUT ACCEPT\n:FWR -\n-A INPUT -j FWR\n-A FWR -i lo -j ACCEPT\nf1\nf2\n# Rejects all remaining connections with port-unreachable errors.\n-A FWR -p tcp -m tcp --tcp-flags SYN,RST,ACK SYN -j REJECT --reject-with icmp-port-unreachable\n-A FWR -p udp -j REJECT --reject-with icmp-port-unreachable\nCOMMIT\n")
;;              (stevedore/checked-script
;;               "Restore IPtables"
;;               ("/sbin/iptables-restore" < @tmp))
;;              (stevedore/script (rm @tmp))))
;;            (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :ubuntu}}}
;;              (iptables-rule "filter" "f1")
;;              (iptables-rule "filter" "f2"))))))
;;   (testing "redhat"
;;     (is (= (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :centos}}}
;;              (actions/remote-file
;;               "/etc/sysconfig/iptables"
;;               :content
;;               "*filter\n:INPUT ACCEPT\n:FORWARD ACCEPT\n:OUTPUT ACCEPT\n:FWR -\n-A INPUT -j FWR\n-A FWR -i lo -j ACCEPT\n\n# Rejects all remaining connections with port-unreachable errors.\n-A FWR -p tcp -m tcp --tcp-flags SYN,RST,ACK SYN -j REJECT --reject-with icmp-port-unreachable\n-A FWR -p udp -j REJECT --reject-with icmp-port-unreachable\nCOMMIT\n"
;;               :mode "0755")
;;              (actions/exec-script
;;               ("/sbin/iptables-restore" < "/etc/sysconfig/iptables"))))
;;            (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :centos}}}
;;              (iptables-rule "filter" "")))))))


;; (deftest redirect-port-test
;;   (testing "redirect with default protocol"
;;     (is (= (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :centos}}}
;;              (iptables-rule
;;               "nat"
;;               "-I PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8081")))
;;            (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :centos}}}
;;              (redirect-port 80 8081)))))))

;; (deftest iptables-accept-port-test
;;   (testing "accept with default protocol"
;;     (is (= (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :centos}}}
;;              (iptables-rule
;;               "filter"
;;               "-A FWR -p tcp --dport 80 -j ACCEPT")))
;;          (first
;;           (build-actions/build-actions
;;            {:server {:group-name :n :image {:os-family :centos}}}
;;            (accept-port 80))))))
;;   (testing "accept with source"
;;     (is (= (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :centos}}}
;;              (iptables-rule
;;               "filter"
;;               "-A FWR -p tcp -s 1.2.3.4 --dport 80 -j ACCEPT")))
;;            (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :centos}}}
;;              (accept-port 80 "tcp" :source "1.2.3.4"))))))
;;   (testing "accept with source range"
;;     (is (= (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :centos}}}
;;              (iptables-rule
;;               "filter"
;;               (str "-A FWR -p tcp -src-range 11.22.33.10-11.22.33.50"
;;                    " --dport 80 -j ACCEPT"))))
;;            (first
;;             (build-actions/build-actions
;;              {:server {:group-name :n :image {:os-family :centos}}}
;;              (accept-port
;;               80 "tcp" :source-range "11.22.33.10-11.22.33.50")
;;              ()))))))

(deftest invocation-test
  (is (build-actions/build-actions
       {:server {:image {:os-family :ubuntu}}}
       (accept-established)
       (accept-icmp)
       (accept-port 80)
       (redirect-port 80 81)
       (throttle-port "a" 80))))
