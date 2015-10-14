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
 - MongoDB 2.4.10
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

### Open tasks:
  
  - karsten: about users
  
  
	https://rstudio-pubs-static.s3.amazonaws.com/78508_abe89197267240dfb6f4facb361a20ed.html

	  To avoid security issues, it’s a good practice to setup new Hadoop user 
	  group and user account to deal with all Hadoop related activities. We will 
	  create hadoop as system group and hduser as system user.
	
	    $ sudo addgroup hadoop
	    $ sudo adduser --ingroup hadoop hduser
	    $ sudo adduser hduser sudo
      
	oreilly hadoop definitive guide
	
		Creating Unix User Accounts
    It’s good practice to create dedicated Unix user accounts to separate the 
    Hadoop processes from each other, and from other services running on the 
    same machine. The HDFS, MapReduce, and YARN services are usually run as 
    separate users, named hdfs, mapred, and yarn, respectively. They all belong 
    to the same hadoop group.
    
    Installing Hadoop
    Download Hadoop from the Apache Hadoop releases page, and unpack the 
    contents of the distribution in a sensible location, such as /usr/local 
	    % cd /usr/local
	    % sudo tar xzf hadoop-x.y.z.tar.gz
    You also need to change the owner of the Hadoop files to be the hadoop user 
    and group: 
      % sudo chown -R hadoop:hadoop hadoop-x.y.z

  
  - karsten: change Java max heap size -Xmx to something sensible 
  - karsten: find out how to permit user t to sudo into hadoop user
  - karsten: mv /usr/local/hadoop/etc/hadoop/mapred-site.xml.template 
  /usr/local/hadoop/etc/hadoop/mapred-site.xml
  - karsten: firewall?
  - karsten: install hbase, 
  - karsten: install spark
  - karsten: install drill
  - karsten: install avro 
  - karsten: install thrift
  - karsten: rename mteam to ...
  - t: prepare data ingestion
  
    - schemata
    - scripts
    - execution plan







