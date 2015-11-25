# TODO

* think through the process of mass ingestion  
* and periodic ingestion  
* and streamline attribute names and structures (at least check for it)
* convert to Parquet 
* check sizes
  + a bunch of Collector descriptors (say: 1 month)
  + converted to verbose and gzipped JSON
  + converted to non-verbose and gzipped JSON
  + converted to - the same - in Parquet


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

EXIT-LIST / TORDNSEL
Unrecognized lines in /Users/tl/tor/analyticsServer/mteam/data/in/exit-list/2015-09-01-09-02-03:
[
  @type tordnsel 1.0
]
IN ALLEN EXIT-LIST DATEIEN


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


## SORTEDMAP TEMPLATE

      SortedMap<String,Integer> MAP = desc.GETTER();
      for(Map.Entry<String,Integer> kv : MAP.entrySet()) {
        extra.ATTRIBUTE.add(new StringInt(kv.getKey(), kv.getValue()));
      }

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

## ADD VERBOSITY TO SORTED MAPS
  

            if (verbose) {
              ArrayList<StringInt> verboseXXX = new ArrayList<StringInt>();

                verboseXXX.add(new StringInt(kv.getKey(), kv.getValue()));
              }
              cons.ZZZ = verboseXXX;
            } else {
              cons.ZZZ = desc.AAA();
            }

