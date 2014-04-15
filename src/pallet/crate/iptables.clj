(ns pallet.crate.iptables
  "Crate for managing iptables"
  (:require
   [clojure.string :as string]
   [clojure.tools.logging :refer [debugf]]
   [pallet.action :as action]
   [pallet.actions :refer [directory exec-checked-script package remote-file]]
   [pallet.api :as api :refer [plan-fn]]
   [pallet.crate :as crate :refer [assoc-settings get-settings update-settings]]
   [pallet.crate.iptables.config :as config
    :refer [if-script iptables network-manager-script Rule]]
   [pallet.crate.iptables.kb :as kb :refer [os-settings package-names]]
   [pallet.crate-install :refer [install-from]]
   [pallet.script.lib :refer [dirname]]
   [pallet.stevedore :refer [fragment]]
   [pallet.utils :refer [apply-map deep-merge]]
   [schema.core :as schema :refer [validate]]))

;;; # Settings
(def facility
  "Identifier used to access settings."
  ::iptables)

(defn package-install
  "Settings for a package install strategy."
  []
  {:install-strategy :packages
   :packages (package-names)})

(defn persistent-rules-settings
  "Settings for different ways to make the iptables rules persist.  The
  return value should be put in the settings on the :persist-rules key."
  [kw]
  {:pre [(keyword? kw)]}
  (let [m (kw kb/persistent-rules)]
    (when-not m
      (throw
       (ex-info (str "Unknown iptables persistent rule strategy: " kw))))
    {kw m}))

(defn default-persistence-settings
  "Return the default settings for making iptables persistent."
  []
  (if-let [kw (kb/default-persistence)]
    {:persist-rules (persistent-rules-settings kw)}))

(defn default-settings
  "Default settings for iptables."
  []
  (deep-merge
   (os-settings)
   (package-install)
   (default-persistence-settings)))

(defn settings
  "Define initial settings."
  ([{:keys [iptables-file iptables-restore instance-id rules] :as settings}]
     (debugf "iptables settings %s" settings )
     (assoc-settings facility settings {:instance-id instance-id}))
  ([] (settings (default-settings))))

(defn iptables-rule
  "Define a rule for the iptables. The argument should be a string
  containing an iptables configuration line (cf. arguments to an
  iptables invocation)"
  [[table config-line :as rule] {:keys [instance-id] :as options}]
  {:pre [(validate Rule rule)]}
  (update-settings facility options update-in [:rules]
                   (fnil conj []) [table config-line]))

;;; # Install
(defmulti install-persistence
  "Install an iptables persistence strategy."
  (fn [k v settings] k))

(defmethod install-persistence :network-manager
  [_ {:keys [file]} {:keys [iptables-file iptables-restore]}]
  {:pre [file iptables-file iptables-restore]}
  (remote-file
   file
   :mode "0755"
   :content (network-manager-script iptables-file iptables-restore)
   :owner "root"))

(defmethod install-persistence :if
  [_ {:keys [file]} {:keys [iptables-file iptables-restore]}]
  {:pre [file iptables-file iptables-restore]}
  (remote-file
   file
   :mode "0755"
   :content (if-script iptables-file iptables-restore)
   :owner "root"))

(defmethod install-persistence :persistent-package
  [_ {:keys [packages package-options]} _]
  {:pre [packages]}
  (doseq [p packages]
    (apply-map package p package-options)))

(defn install
  "Installs iptables boot time configuration."
  [{:keys [instance-id] :as options}]
  (let [{:keys [install-strategy iptables-file iptables-restore
                persist-rules]
         :as settings}
        (get-settings facility {:instance-id instance-id})]
    (directory (fragment @(dirname ~iptables-file)) :owner "root")
    (when install-strategy
      (debugf "Install iptables via %s" install-strategy)
      (install-from settings))
    (doseq [[k m :as s] persist-rules]
      (debugf "Install iptables boot persistence %s" k)
      (install-persistence k m settings))))

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
  "Return a server-spec map for installing and configuring iptables.
  `settings` is a map, which defaults to to the result of calling
  `default-settings`."
  ([] (server-spec nil))
  ([settings & {:keys [instance-id] :as options}]
  (api/server-spec
   :phases {:settings (plan-fn
                          (pallet.crate.iptables/settings
                           (merge settings options)))
            :install (plan-fn
                         (install options))
            :configure (plan-fn
                        (configure options))}
   :default-phases [:install :configure])))
