(ns pallet.crate.iptables.support-test
  (:require
   [clojure.test :refer :all]
   [clojure.tools.logging :refer [infof]]
   [pallet.actions :refer [exec-checked-script exec-script* minimal-packages
                           package-manager]]
   [pallet.api :refer [converge group-spec lift plan-fn]]
   [pallet.build-actions :refer [build-actions build-session]]
   [pallet.core.api :refer [phase-errors]]
   [pallet.core.session :refer [with-session]]
   [pallet.crate :refer [is-64bit?]]
   [pallet.crate.automated-admin-user :refer [automated-admin-user]]
   [pallet.crate.iptables :as iptables]
   [pallet.crates.test-nodes :as test-nodes]
   [pallet.repl :refer [explain-session]]
   [pallet.script :refer [with-script-context]]
   [pallet.script-test :refer [is-true testing-script]]
   [pallet.script.lib :refer [package-manager-non-interactive]]
   [pallet.test-env
    :refer [*compute-service* *node-spec-meta*
            with-group-spec test-env unique-name]]
   [pallet.test-env.project :as project]))

(test-env test-nodes/node-specs project/project)

(deftest ^:support default-settings
  (let [spec (group-spec (unique-name)
               :node-spec (:node-spec *node-spec-meta*)
               :phases
               {:bootstrap (plan-fn
                             (minimal-packages)
                             (package-manager :update)
                             (automated-admin-user))
                :settings (plan-fn
                            (iptables/settings {})
                            (iptables/accept-established)
                            (iptables/accept-icmp)
                            (iptables/accept-port 22)
                            (iptables/throttle-port "SSH" 22))
                :install (plan-fn
                             (iptables/install {}))
                :configure (plan-fn
                               (iptables/configure {}))
                :verify (plan-fn
                            (exec-script*
                             (testing-script
                              "Iptables"
                              (is-true ("iptables save")
                                       "Verify iptables is configured"))))})]
    (with-group-spec spec
      (let [session (lift spec
                          :phase [:settings :install :configure :verify]
                          :compute *compute-service*)]
        (testing "configure iptables"
          (is session)
          (is (not (phase-errors session)))
          (when (phase-errors session)
            (explain-session session)))))))
