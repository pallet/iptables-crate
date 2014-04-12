(ns pallet.crate.iptables
  "Crate for managing iptables"
  (:require
   [clojure.string :as string]
   [clojure.tools.logging :refer [debugf]]
   [pallet.action :as action]
   [pallet.actions :refer [exec-checked-script remote-file]]
   [pallet.api :as api :refer [plan-fn]]
   [pallet.crate :as crate :refer [assoc-settings get-settings update-settings]]
   [pallet.crate.iptables.config :as config
    :refer [if-script iptables network-manager-script Rule]]
   [pallet.crate.iptables.kb :refer [os-settings]]
   [pallet.utils :refer [deep-merge]]
   [schema.core :as schema :refer [validate]]))

;; https://help.ubuntu.com/community/IptablesHowTo

(def facility
  "Identifier used to access settings."
  ::iptables)

(defn settings
  "Define initial settings."
  [{:keys [iptables-file iptables-restore instance-id rules] :as settings}]
  (let [default (os-settings)
        effective-settings (deep-merge default settings)]
    (debugf "iptables settings %s" effective-settings )
    (assoc-settings facility effective-settings {:instance-id instance-id})))

(defn iptables-rule
  "Define a rule for the iptables. The argument should be a string
  containing an iptables configuration line (cf. arguments to an
  iptables invocation)"
  [[table config-line :as rule] {:keys [instance-id] :as options}]
  {:pre [(validate Rule rule)]}
  (update-settings facility options update-in [:rules]
                   (fnil conj []) [table config-line]))

(defn install-network-manager-config
  [iptables-file iptables-restore {:keys [file]}]
  (remote-file
   file
   :mode "0755"
   :content (network-manager-script iptables-file iptables-restore)
   :owner "root"))

(defn install-if-config
  [iptables-file iptables-restore {:keys [file]}]
  (remote-file
   file
   :mode "0755"
   :content (if-script iptables-file iptables-restore)
   :owner "root"))

(defn install
  "Installs iptables boot time configuration."
  [{:keys [instance-id] :as options}]
  (let [{:keys [iptables-file iptables-restore network-manager if]}
        (get-settings facility {:instance-id instance-id})]
    (debugf "Install iptables boot config network-manager %s if %s"
            (boolean network-manager) (boolean if))
    (debugf "Install iptables boot config file %s, loading with %s"
            iptables-file iptables-restore)
    (when network-manager
      (install-network-manager-config
       iptables-file iptables-restore network-manager))
    (when if
      (install-if-config iptables-file iptables-restore if))))

(defn configure
  "Configure iptables.  This changes the running iptables, and writes the
  configuration for setting iptables on boot."
  [{:keys [instance-id] :as options}]
  (debugf "plan-state %s" (pr-str (:plan-state (pallet.core.session/session))))
  (let [{:keys [iptables-file iptables-restore rules] :as settings}
        (get-settings facility {:instance-id instance-id})]
    (debugf "Writing iptables configuration to %s and loading with %s"
            iptables-file iptables-restore)
    (remote-file
     iptables-file
     :mode "0644"
     :owner "root"
     :content (iptables rules))
    (exec-checked-script
     "Restore iptables configuration"
     (iptables-restore < ~iptables-file))))

(defn accept-established
  "Accept established connections"
  ([] (accept-established {}))
  ([{:keys [instance-id] :as options}]
     (iptables-rule
      (config/accept-established)
      (select-keys options [:instance-id]))))

(defn accept-icmp
  "Accept ICMP"
  ([] (accept-icmp {}))
  ([{:keys [instance-id] :as options}]
     (iptables-rule
      (config/accept-icmp)
      (select-keys options [:instance-id]))))

(defn accept-port
  "Accept specific port, by default for tcp."
  ([port] (accept-port port {}))
  ([port {:keys [protocol source source-range instance-id] :as options}]
     (iptables-rule
      (config/accept-port port (dissoc options :instance-id))
      (select-keys options [:instance-id]))))

(defn redirect-port
  "Redirect a specific port, by default for tcp."
  ([from-port to-port]
     (redirect-port from-port to-port {}))
  ([from-port to-port {:keys [protocol instance-id] :as options}]
     (iptables-rule
      (config/redirect-port from-port to-port (dissoc options :instance-id))
      (select-keys options [:instance-id]))))

(defn throttle-port
  "Throttle repeated connection attempts.
  Disallow more than hitcount connects in time-period seconds.
  http://hostingfu.com/article/ssh-dictionary-attack-prevention-with-iptables"
  ([name port] (throttle-port name port {}))
  ([name port {:keys [protocol time-period hitcount instance-id] :as options}]
     (iptables-rule
      (config/throttle-port name port (dissoc options :instance-id))
      (select-keys options [:instance-id]))))

(defn server-spec
  [settings & {:keys [instance-id] :as options}]
  (api/server-spec
   :phases {:settings (plan-fn
                          (pallet.crate.iptables/settings
                           (merge settings options)))
            :install (plan-fn
                         (install options))
            :configure (plan-fn
                        (configure options))}
   :default-phases [:install :configure]))
