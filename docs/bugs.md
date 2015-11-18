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

# Torperf
something is wrong with the 'torperf' detection algorithm    
it doesnt find the type declaration but spits out   
hundreds of false positives: {"descriptor_type":null}


# JsonServerDescriptor 
never calls getIpv6PortList()
on purpose?