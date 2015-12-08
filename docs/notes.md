# TODO

* a real problem: 

ich habe gerade mal versucht, den converter über ein paar tar.xz archive von consensus und server daten laufen zu lassen. ich bekomme aber folgenden fehler:

    /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java -Didea.launcher.port=7540 "-Didea.launcher.bin.path=/Applications/IntelliJ IDEA 15.app/Contents/bin" -Dfile.encoding=UTF-8 -classpath "/Users/tl/tor/analyticsServer/mteam/bin:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/deploy.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/dt.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/javaws.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/jce.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/jconsole.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/management-agent.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/plugin.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/sa-jdi.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/charsets.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/jsse.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/ui.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/ext/apple_provider.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/ext/dnsns.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/ext/localedata.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/ext/sunjce_provider.jar:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/ext/sunpkcs11.jar:/Users/tl/tor/analyticsServer/junit-4.12.jar:/Users/tl/tor/analyticsServer/mteam/lib/gson-2.3.1.jar:/Users/tl/tor/analyticsServer/commons-compress-1.10/commons-compress-1.10.jar:/Users/tl/tor/analyticsServer/commons-compress-1.10/commons-compress-1.10-tests.jar:/Users/tl/tor/analyticsServer/commons-compress-1.10/commons-compress-1.10-javadoc.jar:/Users/tl/tor/analyticsServer/commons-compress-1.10/commons-compress-1.10-sources.jar:/Users/tl/tor/analyticsServer/commons-compress-1.10/commons-compress-1.10-test-sources.jar:/Applications/IntelliJ IDEA 15.app/Contents/lib/idea_rt.jar" com.intellij.rt.execution.application.AppMain mteam.ConvertToJson
    Bug: uncaught exception or error while reading descriptors:
    java.lang.OutOfMemoryError: Java heap space
      at java.util.Arrays.copyOf(Arrays.java:2786)
      at java.io.ByteArrayOutputStream.write(ByteArrayOutputStream.java:94)
      at org.torproject.descriptor.impl.DescriptorReaderImpl$DescriptorReaderRunnable.readFile(DescriptorReaderImpl.java:341)
      at org.torproject.descriptor.impl.DescriptorReaderImpl$DescriptorReaderRunnable.readDescriptors(DescriptorReaderImpl.java:253)
      at org.torproject.descriptor.impl.DescriptorReaderImpl$DescriptorReaderRunnable.run(DescriptorReaderImpl.java:155)
      at java.lang.Thread.run(Thread.java:695)
    
    Process finished with exit code 0

reicht mein heap space nicht oder ist da ein fehler im programm aufgetreten?


-Xmx8g 


* compression as GZ !!! (not XZ, as was previously asked)
* output wird komplett in eine datei geschrieben
  + verzeichnisstruktur wird igoriert und geht verloren
  + ist das schlimm? wollen wir das?

* think through the process of mass ingestion  
* and periodic ingestion  
* streamline attribute names and structures (at least check for it)
* convert to Parquet 
* in Avro schema
  + make optional fields optional
  + add documentation
  + unify formatting (either automatic or handmade)
* check sizes
  + a bunch of Collector descriptors (say: 1 month)
  + converted to verbose and gzipped JSON
  + converted to non-verbose and gzipped JSON
  + converted to - the same - in Parquet
  + check: 
    - all descriptor samples uncompressed in directories -> 57 MB
    - the same as JSON, non-verbose                      -> 67 MB
                                                 compressed 15 MB
    - the same as JSON, but verbose, uncompressed        -> 72 MB
                                                 compressed 15,1 MB
   
* modularize
* write tests
  + that would involve writing test descriptors too i guess
  + which would mean learning how to write collector data
  + well well well
  
* usecase
  [c] https://blog.torproject.org/blog/did-fbi-pay-university-attack-tor-users     
  [c] f. hat mich um 2 uhr morgens geweckt, kurz nachdem er den angriff entdeckt hat.
  [c] war gerade dev meeting in paris.
  [c] und er hat einen der relays beim angriff entdeckt.
  [c] jetzt ging es darum möglichst alles über diesen relay und ähnliche relays im netzwerk herauszufinden.
  [c] seit wann sind die da,
  [c] wie viele sind es,
  [c] welche ip-präfixe,
  [c] wie viele andere gibt es in diesen ip-präfixen.
  [c] wie schnell sind die, sind es exits,
  [c] contacts?
  [c] platforms?
  [c] versions?
  [c] alles.
  [c] angenommen f. hätte so eine datenbank gehabt,
  [c] was hätte er machen können?
  
# BUGS

## DirSourceEntry

is missing getter for 'adress', only has 'ip'  
spec specifies fields for 'adress' and 'ip'

## NetworkStatusEntry

mapping is unclear:  
   
    identity <-> getFingerprint,   
    digest <-> getDescriptor ?
  

## METRICS-LIB COVERAGE
  
  ServerDescriptor
    never called: String getIpv6PortList()
    
  ExtraInfiDescriptor
    okay
  
  RelayNetworkStatusConsensus
    never called: NetworkStatusEntry getStatusEntry(String fingerprint)
    okay
   
  RelayNetworkStatusVote
    never called: NetworkStatusEntry getStatusEntry(String fingerprint)
                  boolean containsStatusEntry(String fingerprint)
    okay
        
  DirSourceEntry
    never called: byte[] getDirSourceEntryBytes() // Return the raw dir-source bytes
    okay
    
  NetworkStatusEntry
    never called: byte[] getStatusEntryBytes()
                  Set<String> getMicrodescriptorDigests()

  BridgeNetworkStatus
    okay
    
  ExitList
    okay
    
  ExitListEntry
    okay
    
  TorperfResult
    okay

## UNRECOGNIZED LINES  

BRIDGE EXTRA INFO
Unrecognized lines in /Users/tl/tor/analyticsServer/mteam/data/in/bridge-extra-infos/00a1c16516d4624cf467fceaa4198bbe76eb8cf8:
[
  master-key-ed25519 0KqMQEq2jSzzG6N/pNc13lJJL25e8XmiVVBQFC5aIJ0, 
  router-digest-sha256 pBWv7LVdJzOjZUxw+qcXc5hNMgrAPewGX3hR3IGuH1I
]


VOTES
Unrecognized lines in /Users/tl/tor/analyticsServer/mteam/data/in/votes/2015-09-01-00-00-00-vote-EFCBE720AB3A82B99F9E953CD5BF50F7EEFC7B97-E2341F06E70659B9A5BB9E131AC24F91DAE7ED0E:
[
  id ed25519 8RH34kO07Pp+XYwzdoATVyCibIvmbslUjRkAm7J4IA8, 
  id ed25519 none, 
  ... id ed25519 none, id ed25519 none, id ed25519 none, id ed25519 none, iid ed25519 none, id ed25519 none, id ed25519 none, id ed25519 none, id ed25519 none, id ed25519 none, id ed25519 none, id ed25519 JGn65DiDJLfj0tDtKQwP8lkVBg0pRb1sXgg9/sMHGNQ, id ed25519 none, ... id ed25519 none]




# CONSTRUCTION MATERIALS


## USEFUL CHECKS TO DEFEND AGAINST NULL POINTER EXCEPTIONS

      can return -1
        if (desc.XXX() >= 0) {

      can't be null or false, only true'
        if (desc.XXX()) {

      if a method is called on the desc property always check for null
        if (desc.XXX() != null) {

      for keys: test, if there is one and return 'true' if yes, 'false' otherwise
          server.onion_key = desc.getOnionKey() != null;

      List: first check that the list is not null, then if it's empty
          if (desc.XXX() != null && !desc.XXX().isEmpty()) {

