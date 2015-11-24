# RelayNetworkStatusConsensus

not a bug, but ... there is no "published" field. can we fake/derive one from 
another attribute? having a published attribute would come in very handy because
it's the only attribute that nearly all other datasets have in common



## DirSourceEntry

is missing getter for 'adress', only has 'ip'  
spec specifies fields for 'adress' and 'ip'

## NetworkStatusEntry

mapping is unclear:  
   
    identity <-> getFingerprint,   
    digest <-> getDescriptor ?

    
    
# RelayNetworkStatusVote

missing 

    package
    authority
      adress (only has ip)
      legacy dir key details
      key certificate
        fingerprint
        dir idendity key
        dir key crosscert
        dir key certification
    directory-footer/bandwidth-weights



# JsonServerDescriptor 
never calls getIpv6PortList()
on purpose?