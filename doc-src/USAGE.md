## Usage

The iptables crate is used to install and configure iptables.

The iptables rules are kept in each node's settings.  The crate
provides a `server-spec` function that can be extended to run the
correct `:settings`, `:install` and `:configure` phases.  The
`:install` phase can install iptables and boot time setting of the
iptables rules.  The configure phase is used to write the iptables
rules, both to the current firewall, and the configuration files.

The `settings` function, run in the `:settings` phase, is used to
configure the crate.  When called with no arguments, it will use the
`default-settings` function to compute a default settings map that
will install iptables using the package manager and, on debian
derivatives, set things so the configuration is read on network
startup.

To customise the settings, you can pass your own settings map to the
`settings` function or the settings argument of the `server-spec`
function.  Apart from the `default-settings` function, you can also
build you settings map using the `package-install` and
`persistent-rules-settings` functions.

## Firewall Configuration

To configure the firewall, run any of the following functions in the
`:settings` phase.

- accept-established
- accept-icmp
- accept-port
- redirect-port
- throttle-port

You will almost certainly want to call at least `accept-established`
and probably `accept-icmp`.  Calling `accept-port` on the SSH port is
also recommended so you don't lock yourself out of a machine.
