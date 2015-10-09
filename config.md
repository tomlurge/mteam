# Measurement Team Server

IP: 136.243.78.39  
Name: mteam  
System: Debian-82-jessie-64-minimal  

### Spec:
 - RAM: 32 GB DDR3
 - SSD: 2 x 240 GB
 - HDD: 2 x 2 TB
 - PROC: i7-4770 Quad-Core

### Mounted disks/partitions:
 - /: SSD for the system (/dev/sda3)
 - /mnt/hdd-ext4: HDD for downloading and extracting data (/dev/sdc1)
 - /mnt/hdd-hdfs: HDD for storing raw data (/dev/sdd1)
 - /mnt/ssd-hdfs: SSD for aggregating data (/dev/sdb1)

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

### Manually installed software:
 - Scala 2.10.4 and Scala build tool 0.13.6  
   sudo apt-get remove scala-library scala    
   sudo wget www.scala-lang.org/files/archive/scala-2.10.4.deb  
   sudo dpkg -i scala-2.10.4.deb  
   sudo wget https://bintray.com/artifact/download/sbt/debian/sbt-0.13.6.deb   
   sudo dpkg -i sbt-0.13.6.deb    

### Open tasks:
  - karsten: install MongoDB 2.4.x https://packages.debian.org/jessie/mongodb (jessie stable)
  - t: figure out how to install the analytics stack
 
### How to install a single node Hadoop cluster

  (1) users
      setup group 
        sudo addgroup hadoop
      and members
        sudo adduser --ingroup hadoop hduser
        sudo adduser --ingroup hadoop hdfs
        sudo adduser --ingroup hadoop yarn

  (2) SSH for hadoop
      configure passwordless ssh access for hadoop to localhost
      sshd should be running (?)



  (3) hadoop
      download http://www.apache.org/dist/hadoop/common/stable/
      verify   http://hadoop.apache.org/releases.html
      unpack and add it to the shell path
      now configure hadoop: follow the instructions in 
          http://www.bogotobogo.com/Hadoop/BigData_hadoop_Install_on_ubuntu_single_node_cluster.php
          and
          https://rstudio-pubs-static.s3.amazonaws.com/78508_abe89197267240dfb6f4facb361a20ed.html
      to modify 
          ~/.bash.rc, 
              #Hadoop variables
              export JAVA_HOME=/usr/lib/jvm/jdk/
              export HADOOP_INSTALL=/usr/local/hadoop
              export PATH=$PATH:$HADOOP_INSTALL/bin
              export PATH=$PATH:$HADOOP_INSTALL/sbin
              export HADOOP_MAPRED_HOME=$HADOOP_INSTALL
              export HADOOP_COMMON_HOME=$HADOOP_INSTALL
              export HADOOP_HDFS_HOME=$HADOOP_INSTALL
              export YARN_HOME=$HADOOP_INSTALL
              export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_INSTALL/lib/native
              export HADOOP_OPTS="-Djava.library.path=$HADOOP_INSTALL/lib"
          /usr/local/hadoop/etc/hadoop/hadoop-env.sh,
              export JAVA_HOME=/usr/bin/java   (nehme ich mal an)
          /usr/local/hadoop/etc/hadoop/core-site.xml, 
              <property>
                  <name>hadoop.tmp.dir</name>
                  <value>/wo/auch/immer/</value>
                  <description>A base for other temp dirs</description>
              </property>
              <property>
                <name>fs.default.name</name>
                <value>hdfs://localhost:9000</value>
              </property>
          /usr/local/hadoop/etc/hadoop/yarn-site.xml,
              <configuration>
                <property>
                  <name>yarn.nodemanager.aux-services</name>
                  <value>mapreduce_shuffle</value>
                </property>
                <property>
                  <name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>
                  <value>org.apache.hadoop.mapred.ShuffleHandler</value>
                </property>
              </configuration>
          /usr/local/hadoop/etc/hadoop/mapred-site.xml,
              <configuration>
                <property>
                  <name>mapreduce.framework.name</name>
                  <value>yarn</value>
                </property>
                <property>
                  <name>mapred.job.tracker</name>
                  <value>localhost:9001</value>
                  <description>where the MapReduce job tracker runs</description>
                </property>
              </configuration>
        /usr/local/hadoop/etc/hadoop/hdfs-site.xml
            (first make 2 directories
            mkdir -p HADOOP_INSTALL/namenode
            mkdir -p /mnt/hdd-hdfsl/datanode
            )
            <property>
               <name>dfs.replication</name>
               <value>1</value>
            </property>
            <property>
               <name>dfs.namenode.name.dir</name>
               <value>file:HADOOP_INSTALL/namenode</value>
            </property>
            <property>
               <name>dfs.datanode.data.dir</name>
               <value>file:/mnt/hdd-hdfs/datanode</value>
            </property>
            (now check permissions
            sudo chown hduser:hadoop -R HADOOP_INSTALL/namenode
            sudo chmod 777 -R HADOOP_INSTALL/namenode
            sudo chown hduser:hadoop -R /mnt/hdd-hdfs/
            sudo chmod 777 -R /mnt/hdd-hdfs/
            )
            (i'm not sure how to handle /mnt/ssd-hdfs 
            - maybe a second entry in hdfs-site.xml?
            )

  (4) HDFS
      in the appropriate directories (/mnt/hdd-hdfs and /mnt/ssd-hdfs) issue the command 
      hdfs@mteam:~$  hdfs namenode -format        // TODO needs checking - does namenode refer to the directory?
          (as user hdfs) 
      (this commad doesn't reformat the disk. it rather initializes a sort of file-database that grows on demand)
      now users need to be given access to hdfs, creating home dirs and setting permissions
        % hadoop fs -mkdir /user/username
        % hadoop fs -chown username:username /user/username
        % hdfs dfsadmin -setSpaceQuota 1t /user/username 

  (5) now (pray and) start hadoop
      su hduser
      cd /usr/local/hadoop/
      sbin/start-dfs.sh
  
  (6) HBase
      download http://www.apache.org/dist/hbase/stable/
  
  (7) Spark
      download spark-1.5.1-bin-hadoop2.6.tgz from http://www.apache.org/dist/spark/spark-1.5.1/
      see also http://spark.apache.org/downloads.html
      and here http://spark.apache.org/docs/latest/
      but basically it's just download and verification
  
  (8) Drill
      download http://www.apache.org/dist/drill/drill-1.1.0/
      see also https://drill.apache.org/docs/installing-drill-on-linux-and-mac-os-x/








