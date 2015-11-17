# RelayNetworkStatusConsensus

is missing "package" getters  


## DirSourceEntry

is missing getter for 'adress', only has 'ip'  
spec specifies fields for 'adress' and 'ip'

## NetworkStatusEntry

is missing getters for router IPv6 adress/port   
OTOH what is getOrAddresses for? is that it?

mapping is unclear:  
   
    identity <-> getFingerprint,   
    digest <-> getDescriptor ?

serious: router-status attribute 'm' is missing
    
    
# RelayNetworkStatusVote

missing 

    package
    flag-tresholds/ignoring-advertised-bws
    authority
      adress (only has ip)
      legacy dir key details
      key certificate
        fingerprint
        dir idendity key
        dir key crosscert
        dir key certification
        