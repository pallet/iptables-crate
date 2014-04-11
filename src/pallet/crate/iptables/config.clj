(ns pallet.crate.iptables.config
  "iptables configuration file format"
  (:require
   [clojure.string :refer [join]]
   [pallet.script.lib :refer [exit]]
   [pallet.stevedore :as stevedore :refer [script]]
   [schema.core :as schema :refer [validate]]))

(def Rule [(schema/one schema/Keyword "table") (schema/one String "rule")])

;;; # Config File
(def prefix
  "Initial fixed content for iptables."
  {:filter ":INPUT ACCEPT
:FORWARD ACCEPT
:OUTPUT ACCEPT
:FWR -
-A INPUT -j FWR
-A FWR -i lo -j ACCEPT"})

(def suffix
  "Final fixed content for iptables"
  {"filter" "# Rejects all remaining connections with port-unreachable errors.
-A FWR -p tcp -m tcp --tcp-flags SYN,RST,ACK SYN -j REJECT --reject-with icmp-port-unreachable
-A FWR -p udp -j REJECT --reject-with icmp-port-unreachable
COMMIT
"})


(defn format-iptables
  [tables]
  (join \newline (map second tables)))

(defn iptables
  "Return an iptables configuration string for args"
  [args]
  (->>
   args ;; [table config-line]
   (group-by first)
   (map
    #(vector
      (name (first %))
      (str
       "*" (name (first %)) \newline
       (join
        \newline (filter
                  identity
                  [(prefix (first %))
                   (join
                    \newline
                    (map second (second %)))
                   (suffix (first %) "COMMIT\n")])))))
   (into {})
   format-iptables))



(defn accept-established
  "Accept established connections"
  []
  {:post [(validate Rule %)]}
  [:filter "-A FWR -m state --state RELATED,ESTABLISHED -j ACCEPT"])

(defn accept-icmp
  "Accept ICMP"
  []
  {:post [(validate Rule %)]}
  [:filter "-A FWR -p icmp -j ACCEPT"])

(defonce accept-option-strings
  {:source " -s %s" :source-range " -src-range %s"})

(defn accept-port
  "Accept specific port, by default for tcp."
  [port {:keys [protocol source source-range]
         :or {protocol "tcp"}
         :as options}]
  {:post [(validate Rule %)]}
  [:filter
   (format
    "-A FWR -p %s%s --dport %s -j ACCEPT"
    protocol
    (reduce
     #(str %1 (format
               ((first %2) accept-option-strings)
               (second %2)))
     "" options)
    port)])

(defn redirect-port
  "Redirect a port."
  [from-port to-port {:keys [protocol] :or {protocol "tcp"}}]
  {:post [(validate Rule %)]}
  [:nat
   (format "-I PREROUTING -p %s --dport %s -j REDIRECT --to-port %s"
           protocol from-port to-port)])

(defn throttle-port
  "Throttle repeated connection attempts.
  Disallow more than hitcount connects in time-period seconds.
  http://hostingfu.com/article/ssh-dictionary-attack-prevention-with-iptables"
  [name port {:keys [protocol time-period hitcount]
              :or {protocol "tcp"
                   time-period 60
                   hitcount 4}}]
  {:post [(validate Rule %)]}
  [:filter
   (format
    "-N %s
-A FWR -p %s --dport %s -m state --state NEW -j %s
-A %s -m recent --set --name %s
-A %s -m recent --update --seconds %s --hitcount %s --name %s -j DROP"
    name protocol port name name name name time-period hitcount name)])


;;; # Config on Boot

(defn network-manager-script
  "A script to configure iptables on network manager up"
  [iptables-file iptables-restore]
  (script
   (set! LOGGER (if ("[ -x /usr/bin/logger]")
                  (quoted
                   ("/usr/bin/logger" -s -p daemon.info -t FirewallHandler))
                  ("echo")))
   (if (= (quoted "$2") (quoted "up"))
     (if (not (file-exists? ~iptables-file))
       ((@LOGGER) (quoted "No iptables rules exist to restore."))
       (do
         ((@LOGGER) (quoted "Restoring iptables rules"))
         (iptables-restore "<" ~iptables-file))))))

(defn if-script
  "A script to configure if network up"
  [iptables-file iptables-restore]
  (script
   (iptables-restore < ~iptables-file)
   (exit 0)))
