[Repository](https://github.com/pallet/iptables-crate) &#xb7;
[Issues](https://github.com/pallet/iptables-crate/issues) &#xb7;
[API docs](http://palletops.com/iptables-crate/0.8/api) &#xb7;
[Annotated source]() &#xb7;
[Release Notes](https://github.com/pallet/iptables-crate/blob/develop/ReleaseNotes.md)

This crate configures iptables

### Dependency Information

```clj
:dependencies [[com.palletops/iptables-crate "0.8.0-SNAPSHOT"]]
```

### Releases

<table>
<thead>
  <tr><th>Pallet</th><th>Crate Version</th><th>Repo</th><th>GroupId</th></tr>
</thead>
<tbody>
  <tr>
    <th>0.8.0-RC.9</th>
    <td>0.8.0-SNAPSHOT</td>
    <td>clojars</td>
    <td>com.palletops</td>
    <td><a href='https://github.com/pallet/iptables-crate/blob/0.8.0-SNAPSHOT/ReleaseNotes.md'>Release Notes</a></td>
    <td><a href='https://github.com/pallet/iptables-crate/blob/0.8.0-SNAPSHOT/'>Source</a></td>
  </tr>
</tbody>
</table>

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

## Support

[On the group](http://groups.google.com/group/pallet-clj), or
[#pallet](http://webchat.freenode.net/?channels=#pallet) on freenode irc.

## License

Licensed under [EPL](http://www.eclipse.org/legal/epl-v10.html)

Copyright 2013 Hugo Duncan and Antoni Batchelli.
