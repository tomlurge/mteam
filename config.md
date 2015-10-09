# Measurement Team Server

IP: 136.243.78.39
Name: mteam
System: Debian-82-jessie-64-minimal

Spec:
 - RAM: 32 GB DDR3
 - SSD: 2 x 240 GB
 - HDD: 2 x 2 TB
 - PROC: i7-4770 Quad-Core

Mounted disks/partitions:
 - /: SSD for the system (/dev/sda3)
 - /mnt/hdd-ext4: HDD for downloading and extracting data (/dev/sdc1)
 - /mnt/hdd-hdfs: HDD for storing raw data (/dev/sdd1)
 - /mnt/ssd-hdfs: SSD for aggregating data (/dev/sdb1)

Users:
 - root: for server administration only
 - t: tomlurge
 - karsten: karsten

Installed packages:
 - openjdk-7-jdk
 - git
 - python3.4
 - screen
 - r-base (3.1.1-1)
 - mc (Midnight Commander)
 - ipython3

Manually installed software:
 - Scala 2.10.4 and Scala build tool 0.13.6
   sudo apt-get remove scala-library scala
   sudo wget www.scala-lang.org/files/archive/scala-2.10.4.deb
   sudo dpkg -i scala-2.10.4.deb
   sudo wget https://bintray.com/artifact/download/sbt/debian/sbt-0.13.6.deb
   sudo dpkg -i sbt-0.13.6.deb

Open tasks:
  - karsten: install MongoDB 2.4.x https://packages.debian.org/jessie/mongodb (jessie stable)
  - t: figure out how to install the analytics stack
