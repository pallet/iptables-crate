(ns pallet.crate.iptables.kb
  "Knowledge base for iptables"
  (:require
   [pallet.version-dispatch :refer [os-map os-map-lookup]]))

(def os-settings-map
  "Defaults per os.  This map can be extended to customise it."
  (os-map
   {{:os :debian-base} {:iptables-restore "/sbin/iptables-restore"
                        :iptables-file "/etc/iptables.rules"
                        ;; :network-manager
                        ;; {:file "/etc/NetworkManager/dispatcher.d/01firewall"}
                        :if
                        {:file "/etc/network/if-pre-up.d/iptablesload"}}
    {:os :rh-base} {:iptables-file "/etc/sysconfig/iptables"
                    :iptables-restore "/sbin/iptables-restore"}}))

(defn os-settings
  "Return the default os settings."
  []
  (os-map-lookup os-settings-map))
