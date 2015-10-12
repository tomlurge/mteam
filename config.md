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

### Manually installed software:
 - Scala 2.10.4 and Scala build tool 0.13.6  
   sudo apt-get remove scala-library scala    
   sudo wget www.scala-lang.org/files/archive/scala-2.10.4.deb  
   sudo dpkg -i scala-2.10.4.deb  
   sudo wget https://bintray.com/artifact/download/sbt/debian/sbt-0.13.6.deb   
   sudo dpkg -i sbt-0.13.6.deb    

### Open tasks:
  
  - karsten: install hadoop
  - t: prepare data ingestion
  
    - schemata
    - scripts
    - execution plan







