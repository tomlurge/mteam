# TODO

* flattened -> array of objects
  jagged -> array of simple objects OR objects with key/value pairs?
  
* jagginess
  - make sure that all arrays of key/value pairs have a jagged version (default)
    and a flattened version (for Drill)

* null's etc
  * check for correct interpretation of return values
    if the value we are looking for doesn't exist or can't be found
    some methods return -1, some null, some just return nothing
    it was not always perfectly clear to me what they do.
    all this has to be checked again (yes, tedious...)
  - make all objects and attributes appear regardless if they are empty or not
  - return values of "-1" should be treated as null
  - initialize all arrays, even if they are empty

* streamline attribute names and structures (at least check for it)
  - documentation in data.md or in excel or in avro schema?
  
* test
  - convert one tarball per type 
      to see if there is one suspiciously big JSON result
  - error handling
  - task-17872
    - server descriptors 
      - brdges too ?
        x Ed25519 certificates 
        x Ed25519 master keys
        x Ed25519 signatures
        x SHA-256 digests
        x onion-key cross certificates
        x ntor-onion-key cross certificates
    - extra-info descriptors
      - brdges too ?
        x Ed25519 certificates 
        x Ed25519 master keys 
        x Ed25519 signatures
        x SHA-256 digests
        x hidden-service statistics
      micro
        RSA-1024 signatures of SHA-1 digests
      votes
        x Ed25519 master keys
  - review verbose branch line 275 & 416
  - add verbosity on new attributes
    
* output wird komplett in eine datei geschrieben
  + verzeichnisstruktur wird igoriert und geht verloren
  >> Dann Bau doch deine Methoden wie
  >> JsonRelayServerDescriptor.convert() um in
  >> convertAndAppendToFile(), wobei jede Json*-Klasse selbst weiß
  >> wohin sie schreiben soll.
  >  Mach doch pro Dokumenttyp eine Map<String, Writer>, wobei String der
  >  Monat im Format YYYY-MM ist und Writer der ensprechend offene Writer
  >  für die Datei.

* think through the process of mass ingestion  
* and periodic ingestion  

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
* check sizes again
  + a bunch of archives, XZ, one of each type            ->  1 GB
  + the same as JSON, GZ, with nulls and chatty arrays   ->  5.7 GB
  + the same as JSON, GZ, with chatty arrays             ->  5.63 GB

* check all things identity/digest/certificate
  - the documentation was often not helpful in finding the right mapping
    between methods and attributes.
    befor the converter goes into production this has to be checked specifically
    by someone who knows and understands the details
  
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

