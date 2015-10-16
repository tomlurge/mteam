# Measurement Team Server

IP: 136.243.78.39  
System: Debian-82-jessie-64-minimal  
Name: mteam  

### Spec:
 - RAM: 32 GB DDR3
 - SSD: 2 x 240 GB
 - HDD: 2 x 2 TB
 - PROC: i7-4770 Quad-Core

### Mounted disks/partitions:
 - /: SSD for the system (/dev/sda3)
 - /mnt/ssd2: Hadoop Distributed File System HDFS (/dev/sdb1)
 - /mnt/hdd1: Hadoop Distributed File System HDFS (/dev/sdc1)
 - /mnt/hdd2: Hadoop Distributed File System HDFS (/dev/sdd1)

### Users:
 - root: for server administration only
 - t: tomlurge
 - karsten: karsten

### Installed packages:
 - openjdk-7-jdk
 - git
 - python3.4
 - screen
 - r-base (3.1.1-1)
 - mc (Midnight Commander)
 - ipython3
 - mongodb (MongoDB 2.4.10)
 - sudo

### Manually installed software:
 - Scala 2.10.4 and Scala build tool 0.13.6  
   sudo apt-get remove scala-library scala    
   sudo wget www.scala-lang.org/files/archive/scala-2.10.4.deb  
   sudo dpkg -i scala-2.10.4.deb  
   sudo wget https://bintray.com/artifact/download/sbt/debian/sbt-0.13.6.deb   
   sudo dpkg -i sbt-0.13.6.deb    

### Sharing a screen created by root with user t:

	# chmod u+s $(which screen)
	# chmod 755 /var/run/screen
	# rm -fr /var/run/screen/*
	# exit
	# screen -d -m -S shared
	Ctrl-a :multiuser on
	Ctrl-a :acladd t
	$ screen -r root/shared
	
### logging into hadoop
	
	log in    t@mteam:~$ sudo -s -H -u hadoop
	log out   hadoop@mteam:/home/t$ exit
	          t@mteam:~$ 

### Open tasks:
  
  - karsten: install avro 
  - karsten: install thrift

	  
	- t: check how to set max RAM for java on debian
	     
	     t@mteam:~$ free -m
	                  total       used       free     shared    buffers     cached
	     Mem:         32105       5260      26844          8        139       3172
	     -/+ buffers/cache:       1947      30157
	     Swap:        16383          0      16383
       
	     export _JAVA_OPTIONS="-Xmx8g"
	     
	- t: check if hadoop websites are only informational or make the installation 
	vulnerable
	
		seems like they provide no admin functionality, only read-only information
	  
		to secure them with user-name (but no password) do edit core-site.xml
				<property>
        	<name>hadoop.http.filter.initializers</name>
        	<value>org.apache.hadoop.security.AuthenticationFilterInitializer</value>
        </property>
				<property>
        	<name>hadoop.http.authentication.type</name>
        	<value>simple</value>
        </property>
				<property>
        	<name>hadoop.http.authentication.token.validity</name>
        	<value>36000</value>
        </property>
				<property>
        	<name>hadoop.http.authentication.signature.secret.file</name>
        	<value>$user.home/hadoop-http-auth-signature-secret</value>
        </property>
        	<!-- IMPORTANT: This file should be readable only by the Unix user running the daemons. -->
				<property>
        	<name>hadoop.http.authentication.simple.anonymous.allowed</name>
        	<value>false</value>
        	<!-- # defaults to true! why? -->
        </property>
				
		more on hadoop security
			http://hadoop.apache.org/docs/r2.7.1/hadoop-project-dist/hadoop-common/ClusterSetup.html
			http://hadoop.apache.org/docs/r2.7.1/hadoop-project-dist/hadoop-common/SecureMode.html
			https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/HttpAuthentication.html
	  
	- t: format interoperability matrix
	
	              json  hbase avro  parquet          
		hadoop mr                      
		spark             x (1)             
		drill       x     x     x     x        (2)
	
		(1) http://www.vidyasource.com/blog/Programming/Scala/Java/Data/Hadoop/Analytics/2014/01/25/lighting-a-spark-with-hbase
		(2) https://drill.apache.org/docs/drill-default-input-format/
	  
	  
	- t: check if spark_1.5.1-hadoop_2.6 can run with hadoop 2.7.1
	     
		we will see. 
		IF NOT http://spark.apache.org/docs/latest/hadoop-provided.html 
	     
	- t: check why hbase has no dir in hdfs
	
	- t: how2hadoop (start/stop commands, admin URLs, shell specifics etc)
	
  - t: prepare data ingestion
  
    - schemata
    - scripts
    - execution plan:
      xz-dateien -> 
      pachted metrics-lib -> 
      export as (hopefully not hadoop-specific) JSON -> 
      shell-script/hadoop command line tool ->
      hadoop

- t: do all the different datasets that collecTor offers have the same key?
     e.g. timestamp?

