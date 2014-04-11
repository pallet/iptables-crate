## Usage

The iptables crate is used to configure iptables.  It does not
currently install iptables.

The iptables rules are kept in each node's settings.  The crate
provides a `server-spec` function that can be extended to run the
correct `:settings`, `:install` and `:configure` phases.  The
`:install` phases installs boot time installation of the iptables
rules.  The configure phase is used to write the iptables rules, both
to the current firewall, and the configuration files.

To configure the firewall, run any of the following functions in the
`:settings` phase.

- accept-established
- accept-icmp
- accept-port
- redirect-port
- throttle-port
