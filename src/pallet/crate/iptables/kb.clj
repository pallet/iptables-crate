(ns pallet.crate.iptables.kb
  "Knowledge base for iptables"
  (:require
   [pallet.version-dispatch :refer [os-map os-map-lookup]]))

;; https://help.ubuntu.com/community/IptablesHowTo
;; http://stackoverflow.com/questions/9330694/how-to-permanently-update-iptables

;;; # Default file locations, etc.
(def os-settings-map
  "Defaults per os.  This map can be extended to customise it."
  (os-map
   {{:os :debian-base} {:iptables-restore "/sbin/iptables-restore"
                        ;; filename should match that used for
                        ;; iptables-persistent package
                        :iptables-file "/etc/iptables/rules.v4"}
    {:os :rh-base} {:iptables-file "/etc/sysconfig/iptables"
                    :iptables-restore "/sbin/iptables-restore"}}))

(defn os-settings
  "Return the default os settings."
  []
  (os-map-lookup os-settings-map))

;;; # Default package names
(def package-names-map
  "Defaults for packages.  This map can be extended to customise it."
  (os-map
   {{:os :linux} ["iptables"]}))

(defn package-names
  "Return the default packages names."
  []
  (os-map-lookup package-names-map))

;;; # Different ways of making the iprules persistent
(def persistent-rules
  {:network-manager {:file "/etc/NetworkManager/dispatcher.d/01firewall"}
   :if {:file "/etc/network/if-pre-up.d/iptablesload"}
   :persistent-package {:packages ["iptables-persistent"]}})

(def default-persistence-map
  "Defaults per os.  This map can be extended to customise it."
  (os-map
   ;; we dont use iptables-persistent by default, as it saves automatically
   {{:os :debian-base} :if}))

(defn default-persistence
  "Return a keyword for the default persistence to use."
  []
  (os-map-lookup default-persistence-map))
