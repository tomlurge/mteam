MUMBLINGS...

#i know shit 'bout git


    [7:56PM] karsten: `git branch master-with-task-17617`
    [7:56PM] karsten: `git checkout master-with-task-17617`
    [7:56PM] karsten: `git merge task-17617`
    [7:57PM] karsten: `git log`
    [7:58PM] thms: ash:metrics-lib t$ git merge task-17617
    [7:58PM] thms: merge: task-17617 - not something we can merge
    [7:59PM] karsten: mach mal `git remote`
    [7:58PM] karsten: dann `git remote add karsten https://git.torproject.org/user/karsten/metrics-lib`
    [8:00PM] karsten: `git fetch karsten`
    [8:00PM] karsten: git merge karsten/task-17617
    [8:01PM] thms: so, okay, hat was geholt
    [8:00PM] thms: ich glaube, ich erkenne das
    [8:01PM] karsten: git merge karsten/task-17617
    [8:01PM] thms: er hat vi aufgemacht. ich bin verloren
    [8:01PM] thms hilfe!
    [8:02PM] karsten: :x
    [8:02PM] thms: das war knapp!
    [8:02PM] thms: okay, 5 files changed
    [8:02PM] karsten: was sagt git log?
    [8:03PM] thms: Merge remote-tracking branch 'karsten/task-17617' into master-with-task-17617
    [8:03PM] karsten: dann hast du was du brauchst
    [8:03PM] karsten: toll toll!
    [8:03PM] thms: wie komm ich aus less raus?
    [8:03PM] karsten: q

    Last login: Fri Dec  4 19:57:23 on ttys000
    ash:~ t$ cd /Users/t/Projects/Tor/metrics-lib
    ash:metrics-lib t$ git remote
    origin
    ash:metrics-lib t$ git remote add karsten https://git.torproject.org/user/karsten/metrics-lib
    ash:metrics-lib t$ git fetch karsten
    remote: Counting objects: 349, done.
    remote: Compressing objects: 100% (149/149), done.
    remote: Total 266 (delta 117), reused 54 (delta 23)
    Receiving objects: 100% (266/266), 57.17 KiB | 32.00 KiB/s, done.
    Resolving deltas: 100% (117/117), completed with 23 local objects.
    From https://git.torproject.org/user/karsten/metrics-lib
     * [new branch]      maven      -> karsten/maven
     * [new branch]      relay-bridge-descs -> karsten/relay-bridge-descs
     * [new branch]      task-13166 -> karsten/task-13166
     * [new branch]      task-14071 -> karsten/task-14071
     * [new branch]      task-16151 -> karsten/task-16151
     * [new branch]      task-16424 -> karsten/task-16424
     * [new branch]      task-17033 -> karsten/task-17033
     * [new branch]      task-17617 -> karsten/task-17617
     * [new branch]      task-17696 -> karsten/task-17696
    ash:metrics-lib t$ git merge karsten/task-17617
    Merge made by the 'recursive' strategy.
     .../torproject/descriptor/BridgeNetworkStatus.java |  47 +++++++
     .../descriptor/RelayNetworkStatusVote.java         |   6 +
     .../descriptor/impl/BridgeNetworkStatusImpl.java   | 102 ++++++++++++++
     .../impl/RelayNetworkStatusVoteImpl.java           |   8 ++
     .../descriptor/impl/BridgeNetworkStatusTest.java   | 151 +++++++++++++++++++++
     5 files changed, 314 insertions(+)
     create mode 100644 test/org/torproject/descriptor/impl/BridgeNetworkStatusTest.java
    ash:metrics-lib t$ git log
    commit 07b101217e7a8f4ba3b052286d771578b3a34a44
    Merge: 1e942ab 5250d71
    Author: tomlurge <t@ash.local>
    Date:   Fri Dec 4 20:01:18 2015 +0100

        Merge remote-tracking branch 'karsten/task-17617' into master-with-task-17617

    commit 1e942ab15d636ca3307fe5327896afff807b52f8
    Author: iwakeh <iwakeh@users.ourproject.org>
    Date:   Thu Nov 26 10:00:00 2015 +0000

        Prevent disappearance of torperf annotation.

    commit 5250d71fb06368fa9b7eb2e088ee4cba40f6c8a4
    Author: Karsten Loesing <karsten.loesing@gmx.net>
    Date:   Wed Dec 2 15:21:35 2015 +0100

        squash! Parse flag thresholds in bridge network statuses.

        Forgot to add unit test class earlier.

    commit 125d5cb92ef9ad86ccae10d6924e9ba951e76d7d
    Author: Karsten Loesing <karsten.loesing@gmx.net>
    ash:metrics-lib t$
