# data

The aim is to load as much raw data as possible into the analytics server. 
The data should be structured to be easily queryable and aggregateable. 
Pre-aggregated versions for common usecases may be added.

## size
- the server has to ingest all the raw data that collecTor aggregates
- maybe there's even more in other places?
- collecTor data for 09/2015 is 1 GB compressed, 24 GB uncompressed
- 24 GB x 12 months x 10 years = 2.880 GB of uncompressed.   
  This is exaggerated, since we only have data since 2007 (8 years) and earlier 
  years contain less data. So we are on the save side here.    
  2.800 GB do fit on our 2 x 2 TB HDDs = 4.000 GB.  
  Since either HBase or Parquet will compress data, the whole dataset should 
  fit snuggly on one HDD, leaving the second 2 TB HDD available for user 
  data. The sceond 240 GB SSD stays completely at the disposal of Hadoop to speed 
  up operations.


## structure



### types


The [collecTor website](https://collector.torproject.org) 
documents the different types of [formats](https://collector.torproject.org/formats.html).
- [Tor directory protocol, version 3 SPEC](https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt)
- although some are based on [version 2](https://gitweb.torproject.org/torspec.git/tree/attic/dir-spec-v2.txt)
See also the two parsers:
- [metrics-lib](https://gitweb.torproject.org/metrics-lib.git) (Java)
- [Stem](https://stem.torproject.org/) (Python)

Most of the documents have nested data structures. Some get quite large.

                                      
	name                                 json spec
	            
	server-descriptor                    x    v3 2.1.1. Sever description format
	extra-info                           x    v3 2.1.2. Extra-info document format  
	network-status-consensus             x    v3 3.4.1. 
	network-status-vote                  x    v3 3.4.1.
	dir-key-certificate                  x    v3 3.1.   Creating key certificates
	network-status-microdesc-consensus   *    v3 3.9.2. Microsescriptor consensus
	bridge-network-status                +
	bridge-server-descriptor             +
	bridge-extra-info                    +               
	bridge-pool-assignment               *
	tordnsel                             x 
	torperf                              x 
	
	x schema done
	+ schema almost done
	* needs a decision

common attributes (from which to construct a key in HBase, eventually)

	name                                published  fingerprint
	server-descriptor                   x          x                      
	extra-info                          x          x                          
	network-status-consensus            -          -                       
	network-status-vote                 x          x                       
	dir-key-certificate                 x          x                        
	network-status-microdesc-consensus                                    
	bridge-network-status               x          -         
	bridge-server-descriptor            x          x              
	bridge-extra-info                   x          x                        
	bridge-pool-assignment              x          x                       
	tordnsel                            x          x                        
	torperf                             -          -                        


JSON Serializations legend:

		"" : string
		# : number
		boolean : true/false
		[x,x,x...] : array of x
		if entry contains no value and no default is given in comment write: null
		if entry is absent write: null


#### collecTor examples

CollecTor provides 12 types of documents. The following examples each contain
- either the full document 
- or the header and one or entries plus the note "[[ and so forth ]]", separated by
 blank lines.    
 TODO Sometimes the footer is missing.   
There's no guarantee that these examples cover all possible cases. They should
rather be considered a cursory overview.

The following descriptive texts are copied from the collector [formats](https://collector.torproject.org/formats.html)
page. When it speaks about documents  "in archive" or "in recent" the original 
text links to directories containing those documents. Please check the [formats](https://collector.torproject.org/formats.html)
page if you want to retrieve them.


##### relay descriptors

Relays and directory authorities publish relay descriptors, so that clients can 
select relays for their paths through the Tor network. All these relay 
descriptors are specified in the Tor directory protocol, version 3 specification 
document (or in the earlier protocol versions 2 or 1).


######  server-descriptor 1.0

Server descriptors contain information that relays publish about themselves. 
Tor clients once downloaded this information, but now they use microdescriptors 
instead.

	router                        DimentoR 
	                              95.52.181.129 
	                              9001 
	                              0 
	                              9030
	platform                      Tor 0.2.4.23 on Windows 8
	protocols                     Link 1 2 Circuit 1
	published                     2015-09-26 01:01:29
	fingerprint                   CE67 6E21 2A1A 6F13 5E3E AE4F 6844 5ACA FC47 23F2
	uptime                        4
	bandwidth                     5242880 
	                              10485760 
	                              2721792
	extra-info-digest             BEEAACD7C30536202F24BAA140BEE792DE6F15BF
	onion-key                     RSA PUBLIC KEY
	signing-key                   RSA PUBLIC KEY
	hidden-service-dir
	contact                       <_taurus_ at mail dot ru>
	ntor-onion-key                5dZwYsrLkvdlrAqgBbFRMDHgTvFyitosFExQJGAFh3E=
	reject                        *:*
	router-signature              SIGNATURE

JSON SERIALIZATION
		
	"server-descriptor": {
		"version": "1.0",
		"router": {
			"nickname": "",
			"address": "",
			"ORPort": #,
			"Socksport": #,
			"DirPort": #
		},
		"identity-ed25519": boolean,
		"master-key-ed25519": boolean,
		"bandwidth": {
			"bandwidth-avg": #,
			"bandwidth-burst": #,
			"bandwidth-observed": #
		},
		"platform": "",
		"published": "",
		"fingerprint": "",
		"hibernating": boolean,
		"uptime": #,
		"onion-key": boolean,
		"onion-key-crosscert": boolean,
		"ntor-onion-key": "",
		"ntor-onion-key-crosscert": {
			"bit": boolean,
			"sig": boolean
		},
		"signing-key": boolean,
		"accept": ["","",""...],
		"reject": ["","",""...],
		"ipv6-policy": "",   // "reject 1-65535" if absent
		                     // TODO or as 2-line object instead of string?
		"router-sig-ed25519": boolean,
		"router-signature": boolean,
		"contact": "",
		"family": ["","",""...],
		"read-history": {
			"date": "",
			"interval": #,
			"bytes": [#,#,#...]
		},
		"write-history":  {
			"date": "",
			"interval": #,
			"bytes": [#,#,#...]
		},
		"eventdns": boolean,
		"caches-extra-info": "",
		"extra-info-digest": "",
		"hidden-service-dir": "",
		"protocols": "",
		"allow-single-hop-exits": boolean,   // false if absent
		"or-address": [
			{
				"adress": "",
				"port": #
			},
			...
		]
	}

###### extra-info 1.0

Extra-info descriptors contain relay information that Tor clients do not need in 
order to function. This is self-published, like server descriptors, but not 
downloaded by clients by default.

	extra-info                    moriarty 
	                              B34B40B3EFCC1F40EAB01CE3B22C13ADA694765E
	published                     2015-09-25 08:33:11
	write-history                 2015-09-25 07:34:36 (14400 s) 
	                              113746944,
	                              111004672,
	                              108476416,
	                              98019328,
	                              92381184,
	                              95155200
	read-history                  2015-09-25 07:34:36 (14400 s) 
	                              133614592,
	                              149231616,
	                              148308992,
	                              129772544,
	                              112099328,
	                              120804352
	dirreq-write-history          2015-09-25 07:34:36 (14400 s) 
	                              21904384,
	                              14827520,
	                              12581888,
	                              12393472,
	                              16252928,
	                              15477760
	dirreq-read-history           2015-09-25 07:34:36 (14400 s) 
	                              67584,
	                              58368,
	                              313344,
	                              111616,
	                              128000,
	                              87040
	geoip-db-digest               D095D62E8A1607C2C3AF61366929BCAD0E6D3184
	geoip6-db-digest              AC1BE3D0707D16AB04092FE00C9732658C926CD8
	dirreq-stats-end              2015-09-24 17:14:37 (86400 s)
	dirreq-v3-ips                 ar=8,
	                              ca=8,
	                              de=8,
	                              es=8,
	                              fr=8,
	                              it=8,
	                              jp=8,
	                              kz=8,
	                              nl=8,
	                              ru=8,
	                              tn=8,
	                              ua=8,
	                              us=8,
	                              vn=8
	dirreq-v3-reqs                de=32,
	                              us=24,
	                              nl=16,
	                              ar=8,
	                              ca=8,
	                              es=8,
	                              fr=8,
	                              it=8,
	                              jp=8,
	                              kz=8,
	                              ru=8,
	                              tn=8,
	                              ua=8,
	                              vn=8
	dirreq-v3-resp                ok=88,
	                              not-enough-sigs=0,
	                              unavailable=0,
	                              not-found=0,
	                              not-modified=0,
	                              busy=0
	dirreq-v3-direct-dl           complete=0,
	                              timeout=0,
	                              running=0
	dirreq-v3-tunneled-dl         complete=88,
	                              timeout=0,
	                              running=0,
	                              min=8986,
	                              d1=216688,
	                              d2=296420,
	                              q1=319770,
	                              d3=326776,
	                              d4=344067,
	                              md=352655,
	                              d6=371179,
	                              d7=395380,
	                              q3=490757,
	                              d8=561358,
	                              d9=1605219,
	                              max=3402475
	router-signature              SIGNATURE


JSON SERIALIZATION
		
	"extra-info": {
		"nickname": "",
		"fingerprint": "",
		"identity-ed25519": boolean,
		"published": "",
		"read-history": {
			"date": "",
			"interval": #,   // default: 86400
			"bytes": [#,#,#...]
		},
		"write-history":  {
			"date": "",
			"interval": #,   // default: 86400
			"bytes": [#,#,#...]
		},
		"geoip-db-digest": "",
		"geoip6-db-digest": "",
		"geoip-start-time": "",
		"geoip-client-origins": {
			"": #   //  country code : number
			...
		},
		"bridge-stats-end":  {
			"date": "",
			"interval": #   // default: 86400
		},
		"bridge-ips": {
			"": #   //  country code : number
			...
		},
		"bridge-ip-versions": {
			"": #   //  family : number
			...
		},
		"bridge-ip-transports": {
			"": #   //  pluggable transport : number
			...
		},
		"dirreq-stats-end":  {
			"date": "",
			"interval": #   // default: 86400
		},
		"dirreq-v2-ips": {
			"": #   //  country code : number
			...
		},
		"dirreq-v3-ips": {
			"": #   //  country code : number
			...
		},
		"dirreq-v2-reqs": {
			"": #   //  country code : number
			...
		},
		"dirreq-v3-reqs": {
			"": #   //  country code : number
			...
		},
		"dirreq-v2-share": "",
    "dirreq-v3-share": "",
		"dirreq-v2-resp": {
			"": #   //  status : number
			...
		},
		"dirreq-v3-resp": {
			"": #   //  status : number
			...
		},
		
		"dirreq-v2-direct-dl": {
			"": #   //  key : number
			...
		},
		"dirreq-v3-direct-dl": {
			"": #   //  key : number
			...
		},
		"dirreq-v2-tunneled-dl": {
			"": #   //  key : number
			...
		},
		"dirreq-v3-tunneled-dl": {
			"": #   //  key : number
			...
		},
		"dirreq-read-history": {
			"date": "",
			"interval": #,
			"bytes": [#,#,#...]
		},
		"dirreq-write-history": {
			"date": "",
			"interval": #,
			"bytes": [#,#,#...]
		},
		"entry-stats-end": {
			"date": "",
			"interval": #   // default: 86400
		},
		"entry-ips": {
			"": #   //  country code : number
			...
		},
		"cell-stats-end": {
			"date": "",
			"interval": #   // default: 86400
		},
		"cell-processed-cells": [#,#,#...],
		"cell-queued-cells": [#,#,#...],
		"cell-time-in-queue": [#,#,#...],
		"cell-circuits-per-decile": #,
		"conn-bi-direct"::  {
			"date": "",
			"interval": #,   // default: 86400
			"below": #,
			"read": #,
			"write": #,
			"both": #
		},
		"exit-stats-end"::  {
			"date": "",
			"interval": #   // default: 86400
		},
		"exit-kibibytes-written": {
			"": #    // port : number
			...
		},
		"exit-kibibytes-read": {
			"": #    // port : number
			...
		},
		"exit-streams-opened": {
			"": #    // port : number
			...
		},
		"hidserv-stats-end"::  {
			"date": "",
			"interval": #   // default: 86400
		},
		"hidserv-rend-relayed-cells": {
			"cells": #,
			"": #,    // key : number
			...       // more key:number pairs
		},
		"hidserv-dir-onions-seen": {
			"cells": #,
			"": #,    // key : number
			...       // more key:number pairs
		},
		"transport": {
			"": {    // transportname
				"adress": "",
				"port": #,
				"args": ""
			}
			...
		},
		"router-sig-ed25519": boolean
		"router-signature": boolean
	}
		

###### network-status-consensus-3 1.0

Though Tor relays are decentralized, the directories that track the overall 
network are not. These central points are called directory authorities, and 
every hour they publish a document called a consensus, or network status 
document. The consensus in turn is made up of router status entries containing 
flags, heuristics used for relay selection, etc. 
 
	network-status-version        3
	vote-status                   consensus
	consensus-method              20
	valid-after                   2015-09-01 00:00:00
	fresh-until                   2015-09-01 01:00:00
	valid-until                   2015-09-01 03:00:00
	voting-delay                  300 300
	client-versions               0.2.4.23,
	                              0.2.4.24,
	                              0.2.4.25,
	                              0.2.4.26,
	                              0.2.4.27,
	                              0.2.5.8-rc,
	                              0.2.5.9-rc,
	                              0.2.5.10,
	                              0.2.5.11,
	                              0.2.5.12,
	                              0.2.6.5-rc,
	                              0.2.6.6,
	                              0.2.6.7,
	                              0.2.6.8,
	                              0.2.6.9,
	                              0.2.6.10,
	                              0.2.7.1-alpha,
	                              0.2.7.2-alpha
	server-versions               0.2.4.23,
	                              0.2.4.24,
	                              0.2.4.25,
	                              0.2.4.26,
	                              0.2.4.27,
	                              0.2.5.8-rc,
	                              0.2.5.9-rc,
	                              0.2.5.10,
	                              0.2.5.11,
	                              0.2.5.12,
	                              0.2.6.5-rc,
	                              0.2.6.6,
	                              0.2.6.7,
	                              0.2.6.8,
	                              0.2.6.9,
	                              0.2.6.10,
	                              0.2.7.1-alpha,
	                              0.2.7.2-alpha
	known-flags                   Authority 
	                              BadExit 
	                              Exit 
	                              Fast 
	                              Guard 
	                              HSDir 
	                              Running 
	                              Stable 
	                              V2Dir 
	                              Valid
	params                        CircuitPriorityHalflifeMsec=30000 
	                              NumDirectoryGuards=3 
	                              NumEntryGuards=1 
	                              NumNTorsPerTAP=100 
	                              Support022HiddenServices=0 
	                              UseNTorHandshake=1 
	                              UseOptimisticData=1 
	                              bwauthpid=1 
	                              cbttestfreq=1000 
	                              pb_disablepct=0 
	                              usecreatefast=0
	                              
	dir-source                    tor26 
	                              14C131DFC5C6F93646BE72FA1401C02A8DF2E8B4 
	                              86.59.21.38 
	                              86.59.21.38 
	                              80 
	                              443
	contact                       Peter Palfrader
	vote-digest                   27CCBB171EE50155A74C12352BD1DF1109E3C7E6
	
	dir-source                    longclaw 
	                              23D15D965BC35114467363C165C4F724B64B4F66 
	                              longclaw.riseup.net 
	                              199.254.238.52 
	                              80 
	                              443
	contact                       Riseup Networks <collective at riseup dot net> - 1nNzekuHGGzBYRzyjfjFEfeisNvxkn4RT
	vote-digest                   09818950D27BBB6CC4D25D3287A6D17584A25808
	
	[[ and so forth ]]
	
	r PDrelay1 AAFJ5u9xAqrKlpDW6N0pMhJLlKs 0f0NYCcVKbQkJCK2ZdZRfxFXtVU 2015-08-31 10:53:35 95.215.44.189 8080 0
	a [2a02:7aa0:1619::9847:f57c]:8080
	s Fast Running Stable Valid
	v Tor 0.2.7.2-alpha-dev
	w Bandwidth=480
	p reject 1-65535
	r seele AAoQ1DAR6kkoo19hBAX5K0QztNw B19hzXUIOTzrKQHU33e9ZL76wZo 2015-08-31 12:27:15 73.15.150.172 9001 0
	s Running Stable Valid
	v Tor 0.2.6.10
	w Bandwidth=27
	p reject 1-65535
	r TorNinurtaName AA8YrCza5McQugiY3J4h5y4BF9g WYdCSTrgy1lV9UlMjguwOXTiB9E 2015-08-31 14:32:39 151.236.6.198 443 80
	a [2a03:f80:ed15:ca7:ea75:b12d:7d0:1110]:443
	s Fast Running Stable V2Dir Valid
	v Tor 0.2.6.10
	w Bandwidth=1530
	p reject 1-65535

	[[ and so forth ]]

	directory-footer
	
	[[ and so forth ]]

JSON SERIALIZATION

	"network-status-consensus": {
		"version": "",
		"vote-status": "",
    "consensus-method": #,
		"valid-after": "",
		"fresh-until": "",
		"valid-until": "",
		"voting-delay": {
			"VoteSeconds": #,
			"DistSeconds": #
		},
		"client-versions": ["","",""...],
		"server-versions": ["","",""...],
		"package": [
			{
				"packagename": "",
				"version": "",
				"url": "",
				"digests": {
					"": ""    // digest-type : digest-value
					...
				}
			}
			...
		],
		"known-flags": ["","",""...],
		"params": {
			"": #     // paramter : number
			...
		},
		"authority": [
			{
				"dir-source": {
					"nickname": "",
					"identity": "",
					"adress": "",
					"ip": "",
					"dirport": #,
					"orport": #
				},
				"contact": "",
				"vote-digest": ""
			}
			...
		],
		"router-status": [
			{
				"r": {
					"nickname": "",
					"identity": "",
					"digest": "",
					"publication": "",
					"ip": "",
					"QRPort": #,
					"DirPort": #
				},
				"a": ["","",""...],
				"s": ["","",""...],
				"v": "",
				"w": {
					"bandwidth": #,
					"unmeasured": #
				},
				"p": {
					"accept": boolean,
					"portlist": [#,#,#...]
				}
			}
			...
		],
		"directory-footer": {
			"bandwidth-weights": {
				"": #    //  weight-keyword : number
				...
			},
			"directory-signature": [
				{
					"algorithm": "",
					"identity": "",
					"signing-key-digest": "",
					"signature": boolean
				}
				...
			]
		}
	}
	
	

######  network-status-vote-3 1.0

The directory authorities exchange votes every hour to come up with a common 
consensus. Vote documents are by far the largest documents provided here. 

	network-status-version        3
	vote-status                   vote
	consensus-methods             13 14 15 16 17 18 19 20
	published                     2015-08-31 23:50:01
	valid-after                   2015-09-01 00:00:00
	fresh-until                   2015-09-01 01:00:00
	valid-until                   2015-09-01 03:00:00
	voting-delay                  300 300
	client-versions               0.2.4.23,
	                              0.2.4.24,
	                              0.2.4.25,
	                              0.2.4.26,
	                              0.2.4.27,
	                              0.2.5.8-rc,
	                              0.2.5.9-rc,
	                              0.2.5.10,
	                              0.2.5.11,
	                              0.2.5.12,
	                              0.2.6.5-rc,
	                              0.2.6.6,
	                              0.2.6.7,
	                              0.2.6.8,
	                              0.2.6.9,
	                              0.2.6.10,
	                              0.2.7.1-alpha,
	                              0.2.7.2-alpha
	server-versions               0.2.4.23,
	                              0.2.4.24,
	                              0.2.4.25,
	                              0.2.4.26,
	                              0.2.4.27,
	                              0.2.5.8-rc,
	                              0.2.5.9-rc,
	                              0.2.5.10,
	                              0.2.5.11,
	                              0.2.5.12,
	                              0.2.6.5-rc,
	                              0.2.6.6,
	                              0.2.6.7,
	                              0.2.6.8,
	                              0.2.6.9,
	                              0.2.6.10,
	                              0.2.7.1-alpha,
	                              0.2.7.2-alpha
	known-flags                   Authority 
	                              BadExit 
	                              Exit 
	                              Fast 
	                              Guard 
	                              HSDir 
	                              Running 
	                              Stable 
	                              V2Dir 
	                              Valid
	flag-thresholds               stable-uptime=1219114 
	                              stable-mtbf=2098570 
	                              fast-speed=83000 
	                              guard-wfu=98.000% 
	                              guard-tk=691200 
	                              guard-bw-inc-exits=3072000 
	                              guard-bw-exc-exits=2457000 
	                              enough-mtbf=1 
	                              ignoring-advertised-bws=0
	params                        CircuitPriorityHalflifeMsec=30000 
	                              NumDirectoryGuards=3
	                              NumEntryGuards=1 
	                              NumNTorsPerTAP=100 
	                              Support022HiddenServices=0 
	                              UseNTorHandshake=1 
	                              bwauthpid=1 
	                              cbttestfreq=1000 
	                              pb_disablepct=0 
	                              usecreatefast=0
	dir-source                    tor26 
	                              14C131DFC5C6F93646BE72FA1401C02A8DF2E8B4 
	                              86.59.21.38 86.59.21.38 
	                              80 
	                              443
	contact                       Peter Palfrader
	dir-key-certificate-version   3
	fingerprint                   14C131DFC5C6F93646BE72FA1401C02A8DF2E8B4
	dir-key-published             2015-06-01 00:00:00
	dir-key-expires               2015-11-01 00:00:00
	dir-identity-key              RSA PUBLIC KEY
	dir-signing-key               RSA PUBLIC KEY
	dir-key-crosscert             SIGNATURE
	dir-key-certification         SIGNATURE
	
	r                             PDrelay1 
	                              AAFJ5u9xAqrKlpDW6N0pMhJLlKs 
	                              0f0NYCcVKbQkJCK2ZdZRfxFXtVU 
	                              2015-08-31 
	                              10:53:35 
	                              95.215.44.189 
	                              8080 
	                              0
	a                             [2a02:7aa0:1619::9847:f57c]:8080
	s                             Fast 
	                              Running 
	                              Stable 
	                              Valid
	v                             Tor 0.2.7.2-alpha-dev
	w                             Bandwidth=627
	p                             reject 1-65535
	m                             13 
	                              sha256=YcMZGgMs9RzjoY7AZlPXzwojYnbENCUKZhedWXZoETs
	m                             14,
	                              15 
	                              sha256=lS5Yg/12NS/Y9bowa1fQ1aCp47S98X/7vnky/MduYFw
	m                             16,
	                              17 
	                              sha256=pZaYWEakwk4GfYzcGvATuhxLAyHQAEf71tZZoD+U2ZA
	m                             18,
	                              19,
	                              20 
	                              sha256=vXOfevUd3V8WbiM0Svk6wvnavdJRvJ3pxWvRfDrGkjI
	
	[[ and so forth ]]


JSON SERIALIZATION

	"network-status-vote" {
		"version": "",
		"vote-status": "",
		"consensus-methods": [#,#,#...],
		"published": "",
		"valid-after": "",
		"flag-thresholds": {
			"": #,    // treshold : number  // TODO make it string because can be '%'? 
			...       // more treshold:number pairs
		},
		"fresh-until": "",
		"valid-until": "",
		"voting-delay": {
			"VoteSeconds": #,
			"DistSeconds": #
		},
		"client-versions": ["","",""...],
		"server-versions": ["","",""...],
		"package": [
			{
				"packagename": "",
				"version": "",
				"url": "",
				"digests": {
					"": ""    // digest-type : digest-value
					...
				}
			}
			...
		],
		"known-flags": ["","",""...],
		"params": {
			"": #     // paramter : number
			...
		},
		authority: [
			{
				"dir-source": {
					"nickname": "",
					"identity": "",
					"adress": "",
					"ip": "",
					"dirport": #,
					"orport": #
				},
				"contact": "",
				"legacy-dir-key": {
					"dir-source": "",
					"nickname": "",
					"identity": "",
					"adress": "",
					"ip": "",
					"dirport": #,
					"orport": #
				},
				"key-certificate": {
					"version": #,
					"fingerprint": "",
					"dir-key-published": "",   
					"dir-key-expires": "",
					"dir-identity-key": boolean,
					"dir-signing-key": boolean,
					"dir-key-crosscert": boolean,
					"dir-key-certification": boolean
				}
			}
			...
		],
		"router-status": [
			{
				"r": {
					"nickname": "",
					"identity": "",
					"digest": "",
					"publication": "",
					"ip": "",
					"ORPort": #,
					"DirPort": #
				},
				"a": ["","",""...],
				"s": ["","",""...],
				"v": "",
				"w": {
					"bandwidth": #,
					"measured": #,
				},
				"p": {
					"accept": boolean,
					"portlist": [#,#,#...]
				},
				"m": [
					{
						"methods": [#,#,#...],
						"algorithm": "",
						"digest": ""
					}
					...
				]
			}
			...
		],
		"id": {
			"ed25519": ""    // TODO I'm not sure I understand the spec correctly
		},
		"directory-footer": {
			"bandwidth-weights": {
				"": #    // weight-keyword : number
			},
			"directory-signature": {
				"algorithm": "",
				"identity": "",
				"signing-key-digest": "",
				"signature": boolean
			}
		}
	}
	
	
	
#### dir-key-certificate-3 1.0
     
The directory authorities sign their votes and the consensus with their key that 
they publish in a key certificate. These key certificates change once every few 
months, so they are only available in the archive. 

	dir-key-certificate-version   3
	fingerprint                   0D95B91896E6089AB9A3C6CB56E724CAF898C43F
	dir-key-published             2007-12-02 21:24:31
	dir-key-expires               2008-12-02 21:24:31
	dir-identity-key              RSA PUBLIC KEY
	dir-signing-key               RSA PUBLIC KEY
	dir-key-certification         SIGNATURE


JSON SERIALIZATION

	"dir-key-certificate": {
		"version": #,
		"dir-address": "",
		"fingerprint": "",
		"dir-identity-key": boolean,
		"dir-key-published": "",
		"dir-key-expires": "",
		"dir-signing-key": boolean,
		"dir-key-crosscert": boolean,
		"dir-key-certification": boolean
	}



######  network-status-microdesc-consensus-3 1.0

Tor clients used to download all server descriptors of active relays, but now 
they only download the smaller microdescriptors which are derived from server 
descriptors. The microdescriptor consensus lists all active relays and 
references their currently used microdescriptor. The tarballs in archive contain 
both microdescriptor consensuses and referenced microdescriptors together. 

They should not be included in the analytics server.   
Or only the header.  
TODO what to do?
  
	network-status-version        3 microdesc
	vote-status                   consensus
	consensus-method              20
	valid-after                   2015-09-01 00:00:00
	fresh-until                   2015-09-01 01:00:00
	valid-until                   2015-09-01 03:00:00
	voting-delay                  300 300
	client-versions               0.2.4.23,0.2.4.24,0.2.4.25,0.2.4.26,0.2.4.27,0.2.5.8-rc,0.2.5.9-rc,0.2.5.10,0.2.5.11,0.2.5.12,0.2.6.5-rc,0.2.6.6,0.2.6.7,0.2.6.8,0.2.6.9,0.2.6.10,0.2.7.1-alpha,0.2.7.2-alpha
	server-versions               0.2.4.23,0.2.4.24,0.2.4.25,0.2.4.26,0.2.4.27,0.2.5.8-rc,0.2.5.9-rc,0.2.5.10,0.2.5.11,0.2.5.12,0.2.6.5-rc,0.2.6.6,0.2.6.7,0.2.6.8,0.2.6.9,0.2.6.10,0.2.7.1-alpha,0.2.7.2-alpha
	known-flags                   Authority BadExit Exit Fast Guard HSDir Running Stable V2Dir Valid
	params                        CircuitPriorityHalflifeMsec=30000 
	                              NumDirectoryGuards=3 
	                              NumEntryGuards=1 
	                              NumNTorsPerTAP=100 
	                              Support022HiddenServices=0 
	                              UseNTorHandshake=1 
	                              UseOptimisticData=1 
	                              bwauthpid=1 
	                              cbttestfreq=1000 
	                              pb_disablepct=0 
	                              usecreatefast=0
	
	dir-source                    tor26 
	                              14C131DFC5C6F93646BE72FA1401C02A8DF2E8B4 
	                              86.59.21.38 
	                              86.59.21.38 
	                              80 
	                              443
	contact                       Peter Palfrader
	vote-digest                   27CCBB171EE50155A74C12352BD1DF1109E3C7E6
	
	dir-source                    longclaw 
	                              23D15D965BC35114467363C165C4F724B64B4F66 
	                              longclaw.riseup.net 
	                              199.254.238.52 
	                              80 
	                              443
	contact                       Riseup Networks <collective at riseup dot net> - 1nNzekuHGGzBYRzyjfjFEfeisNvxkn4RT
	vote-digest                   09818950D27BBB6CC4D25D3287A6D17584A25808
	
	[[ and so forth ]]

	
##### microdescriptor 1.0
  
Microdescriptors are minimalistic documents that just includes the information 
necessary for Tor clients to work. The tarballs in archive contain both 
microdescriptor consensuses and referenced microdescriptors together.
The microdescriptors in archive contain one descriptor per file, whereas the 
files in recent contain all descriptors collected in an hour concatenated into 
a single file. 
	
They should not be included in the analytics server. 
TODO okay?

	
##### network-status-2 1.0

Version 2 network statuses have been published by the directory authorities 
before consensuses have been introduced. In contrast to consensuses, each 
directory authority published their own authoritative view on the network, and 
clients combined these documents locally. We stopped archiving version 2 network 
statuses in 2012.

TODO should we include these?


##### directory 1.0

The first directory protocol version combined the list of active relays with 
server descriptors in a single directory document. We stopped archiving 
version 1 directories in 2007. 
	
TODO should we include these?


##### bridge descriptors

Bridges and the bridge authority publish bridge descriptors that are used by 
censored clients to connect to the Tor network. We cannot, however, make bridge 
descriptors available as we do with relay descriptors, because that would defeat 
the purpose of making bridges hard to enumerate for censors. We therefore 
sanitize bridge descriptors by removing all potentially identifying information 
and publish sanitized versions here.   
For a thorough description of the sanitizing steps see the section on 
[sanitizing](https://collector.torproject.org/formats.html#bridge-descriptors)
in the collector formats document mentioned above.
	
	
###### bridge-network-status 1.0

Sanitized bridge network statuses are similar to version 2 relay network 
statuses, but with only a published line in the header and without any lines in 
the footer. The tarballs in archive contain all bridge descriptors of a given 
month, not just network statuses. 
	
	published                     2015-09-01 00:02:08
	flag-thresholds               stable-uptime=1813852 
	                              stable-mtbf=2008701 
	                              fast-speed=55000 
	                              guard-wfu=98.000% 
	                              guard-tk=691200 
	                              guard-bw-inc-exits=332000 
	                              guard-bw-exc-exits=336000 
	                              enough-mtbf=1 
	                              ignoring-advertised-bws=0
	                              
	r                             Unnamed 
	                              ABk0wg4j6BLCdZKleVtmNrfzJGI
	                              UMbJcOARWHvYAdhn590RvN5CCMw 
	                              2015-08-31 23:47:06 
	                              10.63.118.188 
	                              443 
	                              0
	s                             Fast 
	                              Running 
	                              Valid
	w                             Bandwidth=114
	p                             reject 
	                              1-65535
	
	[[ and so forth ]]



JSON SERIALIZATION

	"bridge-network-status": {
		"published": "",
		"flag-tresholds": {
			"": #,     // flag : treshold
			...
		},
		"bridge": [
			{
				"r": {
					"nickname": "",,
		 			"identity-key": "",
		 			"descriptor": "",
		 			"date": "",
		 			"adress": "",
		 			"ORPort": #,
		 			"DirPort": #
				},
				"s": ["","",""...],
				"w": {
					"bandwidth": #
				},
				"p": ""
			}
			...
		]
	}
	
TODO 
https://collector.torproject.org/index2.html says that bridges network 
status is modeled after https://gitweb.torproject.org/torspec
.git/tree/attic/dir-spec-v2.txt, but the example above and the network 
status format as defined in spec_v2 chapter 3. don't match. the spec v2 
chapter 3.0 doesn't contain a field "flag-treshold" nor the fields "w" and 
"p". it does however include a field "v".


######  bridge-server-descriptor 1.1

Bridge server descriptors follow the same format as relay server descriptors, 
except for the sanitizing steps mentioned above.   
The format has changed over time to accomodate changes to the sanitizing 
process, with earlier versions being:

- @type bridge-server-descriptor 1.0 was the first version.
- There was supposed to be a newer version indicating added ntor-onion-key 
lines, but due to a mistake only the version number of sanitized bridge 
extra-info descriptors was raised. As a result, there may be sanitized bridge 
server descriptors with version @type bridge-server-descriptor 1.0 with and 
without those lines.
- @type bridge-server-descriptor 1.1 added master-key-ed25519 lines and 
router-digest-sha256 to server descriptors published by bridges using an Ed25519 
master key.


	router                        Unnamed 
	                              10.143.227.19 
	                              9001 
	                              0 
	                              0
	platform                      Tor 0.2.6.2-alpha on Windows Server 2003 [server]
	protocols                     Link 1 2 Circuit 1
	published                     2015-09-29 21:36:18
	fingerprint                   5E59 0229 A5CB 92A1 C40A A2D8 2021 AFA4 C860 69D5
	uptime                        236
	bandwidth                     1073741824 
	                              1073741824 
	                              70549
	extra-info-digest             C457007438B350FABBA6AD75B08E0674A944E30D
	hidden-service-dir
	ntor-onion-key                cjj99BMgt2DdqglDgQgZyM0HSW4ZUzvYeL3s0IG+tzE=
	reject                        *:*
	router-digest                 00A0A2F7AA65DBDE7CE7A3FEF659368792FAAB2B


JSON SERIALIZATION

	"bridge-server-descriptor": {
		"router": {
			"nickname": "",
			"adress": "",
			"ORPort": #,
			"SocksPort": #,
			"DirPort": #
		},
		"protocols": "",
		"bandwidth": {
			"bandwidth-avg": #,
			"bandwidth-burst": #,
			"bandwidth-observed": #
		},
		"platform": "",
		"published": "",
		"fingerprint": "",
		"hibernating": boolean, 
		"uptime": #,
		"ntor-onion-key": "",
		"accept": "",
		"reject": "",
		"router-digest": "",
		"extra-info-digest": "",
		"hidden-service-dir": ""
	}
	
TODO this needs to be checked!
	
	
###### bridge-extra-info 1.3

Bridge extra-info descriptors follow the same format as relay extra-info 
descriptors, except for the sanitizing steps mentioned above. The format has 
changed over time to accomodate changes to the sanitizing process, with earlier 
versions being:

- @type bridge-extra-info 1.0 was the first version.
- @type bridge-extra-info 1.1 added sanitized transport lines.
- @type bridge-extra-info 1.2 was supposed to indicate added ntor-onion-key 
lines, but those changes only affect bridge server descriptors, not extra-info 
descriptors. So, nothing has changed as compared to version 1.1.
- @type bridge-extra-info 1.3 added master-key-ed25519 lines and 
router-digest-sha256 to extra-info descriptors published by bridges using an 
Ed25519 master key.


	extra-info                    Unnamed 
	                              D38A157492E3F4CDAAFD6BDAFC5908CFF7255806
	published                     2015-09-27 23:04:01
	read-history                  2015-09-27 19:16:46 
	                              (14400 s) 
	                              50480128,
	                              4490240,
	                              11934720,
	                              6256640,
	                              89082880,
	                              66740224
	write-history                 2015-09-27 19:16:46 
	                              (14400 s) 
	                              46851072,
	                              1771520,
	                              9085952,
	                              2136064,
	                              85985280,
	                              64917504
	geoip-db-digest               D095D62E8A1607C2C3AF61366929BCAD0E6D3184
	geoip6-db-digest              AC1BE3D0707D16AB04092FE00C9732658C926CD8
	bridge-stats-end              2015-09-27 08:41:48 
	                              (86400 s)
	bridge-ips                    us=24,
	                              cz=8,
	                              fr=8,
	                              gb=8,
	                              ir=8,
	                              ru=8,
	                              sv=8
	bridge-ip-versions            v4=32,
	                              v6=0
	bridge-ip-transports          <OR>=8,
	                              obfs3=8,
	                              obfs4=32
	dirreq-write-history          2015-09-27 19:16:46 
	                              (14400 s) 
	                              1113088,
	                              1165312,
	                              1201152,
	                              625664,
	                              1170432,
	                              3538944
	dirreq-read-history           2015-09-27 19:16:46 
	                              (14400 s) 
	                              100352,
	                              5120,
	                              17408,
	                              8192,
	                              7168,
	                              135168
	dirreq-stats-end              2015-09-27 04:47:05 
	                              (86400 s)
	dirreq-v3-ips                 cz=8,
	                              ir=8,
	                              ru=8,
	                              sv=8,
	                              us=8
	dirreq-v3-reqs                cz=8,ir=8,ru=8,sv=8,us=8
	dirreq-v3-resp                ok=16,
	                              not-enough-sigs=0,
	                              unavailable=0,
	                              not-found=0,
	                              not-modified=0,
	                              busy=0
	dirreq-v3-direct-dl           complete=0,
	                              timeout=0,
	                              running=0
	dirreq-v3-tunneled-dl         complete=16,
	                              timeout=0,
	                              running=0,
	                              min=11874,
	                              d1=11874,
	                              d2=12283,
	                              q1=24858,
	                              d3=195862,
	                              d4=312620,
	                              md=387808,
	                              d6=446494,
	                              d7=1730520,
	                              q3=5403163,
	                              d8=5586742,
	                              d9=5687171,
	                              max=5795541
	transport                     scramblesuit
	transport                     obfs3
	transport                     obfs4
	transport                     fte
	router-digest                 00A0BC18393D1CC4162998A06CE5B4FD606F268E


JSON SERIALIZATION
		
	"extra-info": {
		"nickname": "",
		"fingerprint": "",
		"published": "",
		"read-history": {
			"date": "",
			"interval": #,   // default: 86400
			"bytes": [#,#,#...]
		},
		"write-history":  {
			"date": "",
			"interval": #,   // default: 86400
			"bytes": [#,#,#...]
		},
		"geoip-db-digest": "",
		"geoip6-db-digest": "",
		"geoip-start-time": "",                      // TODO not sure about this one
		"geoip-client-origins": {                    // TODO not sure about this one
			"": #   //  country code : number
			...
		},
		"bridge-stats-end":  {
			"date": "",
			"interval": #   // default: 86400
		},
		"bridge-ips": {
			"": #   //  country code : number
			...
		},
		"bridge-ip-versions": {
			"": #   //  family : number
			...
		},
		"bridge-ip-transports": {
			"": #   //  pluggable transport : number
			...
		},
		"dirreq-stats-end":  {
			"date": "",
			"interval": #   // default: 86400
		},
		"dirreq-v2-ips": {                           // TODO not sure about this one
			"": #   //  country code : number
			...
		},
		"dirreq-v3-ips": {
			"": #   //  country code : number
			...
		},
		"dirreq-v2-reqs": {                          // TODO not sure about this one
			"": #   //  country code : number
			...
		},
		"dirreq-v3-reqs": {
			"": #   //  country code : number
			...
		},
		"dirreq-v2-share": "",                      // TODO not sure about this one
    "dirreq-v3-share": "",                      // TODO not sure about this one
		"dirreq-v2-resp": {                         // TODO not sure about this one
			"": #   //  status : number
			...
		},
		"dirreq-v3-resp": {
			"": #   //  status : number
			...
		},
		
		"dirreq-v2-direct-dl": {                    // TODO not sure about this one 
			"": #   //  key : number
			...
		},
		"dirreq-v3-direct-dl": {
			"": #   //  key : number
			...
		},
		"dirreq-v2-tunneled-dl": {                   // TODO not sure about this one
			"": #   //  key : number
			...
		},
		"dirreq-v3-tunneled-dl": {
			"": #   //  key : number
			...
		},
		"dirreq-read-history": {
			"date": "",
			"interval": #,
			"bytes": [#,#,#...]
		},
		"dirreq-write-history": {
			"date": "",
			"interval": #,
			"bytes": [#,#,#...]
		},
		"entry-stats-end": {                         // TODO not sure about this one
			"date": "",
			"interval": #   // default: 86400
		},
		"entry-ips": {                               // TODO not sure about this one
			"": #   //  country code : number
			...
		},
		"cell-stats-end": {                          // TODO not sure about this one
			"date": "",
			"interval": #   // default: 86400
		},
		"cell-processed-cells": [#,#,#...],          // TODO not sure about this one
		"cell-queued-cells": [#,#,#...],             // TODO not sure about this one
		"cell-time-in-queue": [#,#,#...],            // TODO not sure about this one
		"cell-circuits-per-decile": #,               // TODO not sure about this one
		"conn-bi-direct"::  {                        // TODO not sure about this one
			"date": "",
			"interval": #,   // default: 86400
			"below": #,
			"read": #,
			"write": #,
			"both": #
		},
		"exit-stats-end"::  {                        // TODO not sure about this one
			"date": "",
			"interval": #   // default: 86400
		},
		"exit-kibibytes-written": {                  // TODO not sure about this one
			"": #    // port : number
			...
		},
		"exit-kibibytes-read": {                     // TODO not sure about this one
			"": #    // port : number
			...
		},
		"exit-streams-opened": {                     // TODO not sure about this one
			"": #    // port : number
			...
		},
		"hidserv-stats-end"::  {                     // TODO not sure about this one
			"date": "",
			"interval": #   // default: 86400
		},
		"hidserv-rend-relayed-cells": {              // TODO not sure about this one
			"cells": #,
			"": #,    // key : number
			...       // more key:number pairs
		},
		"hidserv-dir-onions-seen": {                 // TODO not sure about this one
			"cells": #,
			"": #,    // key : number
			...       // more key:number pairs
		},
		"transport": ["","",""...],
		"router-digest": ""
	}



##### other 

###### hidden-service-descriptor 1.0

Tor hidden services make it possible for users to hide their locations while 
offering various kinds of services. A hidden service assembles a hidden service 
descriptor to make its service available in the network. This descriptor gets 
stored on hidden service directories and can be retrieved by hidden service 
clients. Hidden service descriptors are not formally archived, but some libraries 
support parsing these descriptors when obtaining them from a locally running Tor 
instance. 


TODO what about them?

###### bridge-pool-assignment 1.0

The document below shows a BridgeDB pool assignment file from March 13, 2011. 
Every such file begins with a line containing the timestamp when BridgeDB wrote 
this file. Subsequent lines start with the SHA-1 hash of a bridge fingerprint, 
followed by ring, subring, and/or file bucket information. There are currently 
three distributor ring types in BridgeDB:

  1. unallocated: These bridges are not distributed by BridgeDB, but are either
   reserved for manual distribution or are written to file buckets for 
   distribution via an external tool. If a bridge in the unallocated ring is 
   assigned to a file bucket, this is noted by bucket=$bucketname.
  2. email: These bridges are distributed via an e-mail autoresponder. Bridges 
  can be assigned to subrings by their OR port or relay flag which is defined by 
  port=$port and/or flag=$flag.
  3. https: These bridges are distributed via https server. There are multiple 
  https rings to further distribute bridges by IP address ranges, which is 
  denoted by ring=$ring. Bridges in the https ring can also be assigned to 
  subrings by OR port or relay flag which is defined by port=$port and/or 
  flag=$flag.


	bridge-pool-assignment 2011-03-13 14:38:03
	00b834117566035736fc6bd4ece950eace8e057a unallocated
	00e923e7a8d87d28954fee7503e480f3a03ce4ee email port=443 flag=stable
	0103bb5b00ad3102b2dbafe9ce709a0a7c1060e4 https ring=2 port=443 flag=stable
	[...]

As of December 8, 2014, bridge pool assignment files are no longer archived. 

	002941....8b0de74e97          email 
	                              ip=4 
	                              flag=stable 
	                              transport=obfs3,
	                                        obfs2,
	                                        scramblesuit
	0126c6....fa7f658257          https 
	                              ip=4 
	                              ring=3 
	                              flag=stable 
	                              transport=obfs3,
	                                        obfs2,
	                                        scramblesuit
	0241c8....d81ccda97c          https 
	                              ip=4 
	                              ring=2 
	                              flag=stable 
	                              transport=fte,
	                                        obfs3,
	                                        scramblesuit


TODO what about them?                            
                            
                            
###### tordnsel 1.0

The exit list service TorDNSEL publishes exit lists containing the IP addresses 
of relays that it found when exiting through them. 

Tor Check makes the list of known exits and corresponding exit IP addresses 
available in a specific format. 

	ExitNode 63BA28370F543D175173E414D5450590D73E22DC
	Published 2010-12-28 07:35:55
	LastStatus 2010-12-28 08:10:11
	ExitAddress 91.102.152.236 2010-12-28 07:10:30
	ExitAddress 91.102.152.227 2010-12-28 10:35:30

The document above shows an entry of the exit 
list written on December 28, 2010 at 15:21:44 UTC. This entry means that the 
relay with fingerprint 63BA.. which published a descriptor at 07:35:55 and was 
contained in a version 2 network status from 08:10:11 uses two different 
IP addresses for exiting. The first address 91.102.152.236 was found in a test 
performed at 07:10:30. When looking at the corresponding server descriptor, one 
finds that this is also the IP address on which the relay accepts connections 
from inside the Tor network. A second test performed at 10:35:30 reveals that 
the relay also uses IP address 91.102.152.227 for exiting.


	Downloaded                    2015-09-01 00:02:02
	
	ExitNode                      0011BD2485AD45D984EC4159C88FC066E5E3300E
	Published                     2015-08-31 16:17:30
	LastStatus                    2015-08-31 17:03:18
	ExitAddress                   162.247.72.201 2015-08-31 17:09:23
	
	ExitNode                      0098C475875ABC4AA864738B1D1079F711C38287
	Published                     2015-08-31 13:59:24
	LastStatus                    2015-08-31 15:03:20
	ExitAddress                   162.248.160.151 2015-08-31 15:07:27
	
	[[ and so forth ]]


JSON SERIALIZATION

	"tordnsel": {
		"Downloaded": "",
		"relays": [
			{
				"ExitNode": "",
				"Published": "",
				"LastStatus": "",
				"ExitAddress": {
				"adress": "",
				"date": ""
			}
			...
		]
	}



	
######  torperf 1.0

The performance measurement service Torperf publishes performance data from 
making simple HTTP requests over the Tor network. Torperf uses a trivial SOCKS 
client to download files of various sizes over the Tor network and notes how 
long substeps take. 

	SOURCE=moria 
	FILESIZE=51200 
	START=1441065601.86 
	SOCKET=1441065601.86 
	CONNECT=1441065601.86 
	NEGOTIATE=1441065601.86
	REQUEST=1441065601.86
	RESPONSE=1441065602.38 
	DATAREQUEST=1441065602.38 
	DATARESPONSE=1441065602.84 
	DATACOMPLETE=1441065603.39 
	WRITEBYTES=75
	READBYTES=51416  
	DIDTIMEOUT=0 
	DATAPERC10=1441065602.98 
	DATAPERC20=1441065603.03 
	DATAPERC30=1441065603.09 
	DATAPERC40=1441065603.12 
	DATAPERC50=1441065603.23 
	DATAPERC60=1441065603.25 
	DATAPERC70=1441065603.31 
	DATAPERC80=1441065603.37 
	DATAPERC90=1441065603.37 
	LAUNCH=1441065361.30 
	USED_AT=1441065603.40 
	PATH=$C4C9C332D25B3546BEF4E1250CF410E97EF996E6,
	     $C43FA6474A9F071E9120DF63ED6EB8FDBA105234,
	     $7C0AA4E3B73E407E9F5FEB1912F8BE26D8AA124D 
	BUILDTIMES=0.872834920883,
	           1.09103679657,
	           1.49180984497 
	TIMEOUT=1500 
	QUANTILE=0.800000 
	CIRC_ID=1228  
	USED_BY=2475 


JSON SERIALIZATION

	"torperf": {
		"configuration": {
			"SOURCE": "",
			"FILESIZE": #
		},
		"measurements": {
			"START": #,
			"SOCKET": #,
			"CONNECT": #,
			"NEGOTIATE": #,
			"REQUEST": #,
			"RESPONSE": #,
			"DATAREQUEST": #,
			"DATARESPONSE": #,
			"DATACOMPLETE": #,
			"WRITEBYTES": #,
			"READBYTES": #,
			"DIDTIMEOUT": #,
			"DATAPERC10": #,
			"DATAPERC20": #,
			"DATAPERC30": #,
			"DATAPERC40": #,
			"DATAPERC50": #,
			"DATAPERC60": #,
			"DATAPERC70": #,
			"DATAPERC80": #,
			"DATAPERC90": #
		},
		"additional": {
			"LAUNCH": #,
			"USED_AT": #,
			"PATH": ["","",""...],
			"BUILDTIMES": [#,#,#...],
			"TIMEOUT": #,
			"QUANTILE": #,
			"CIRC_ID": #,
			"USED_BY": #
		}
	}
