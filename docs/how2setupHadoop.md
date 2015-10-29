
# How to install a single node Hadoop cluster
Our install is a bit untypical since we want neither a test installation on a 
local machine nor the typical multi machine Hadoop cluster. 
We are going for **pseudo-distributed mode**, also called a **single 
node cluster** and therefor have to take care to not make false friend with all 
those tutorials about settingup Hadoop (clusters) out there. And we've been 
warned that setting up Hadoop isn't exactly trivial - in any case.

Help comes in the form of instructions on the Hadoop homepage [0](https://hadoop
.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html) 
and in two blogpost [1](http://www.bogotobogo.com/Hadoop/BigData_hadoop_Install_on_ubuntu_single_node_cluster.php) 
and [2](https://rstudio-pubs-static.s3.amazonaws.com/78508_abe89197267240dfb6f4facb361a20ed.html)


## (1) Users

	adduser hadoop

## (2) Setup passphraseless ssh
If you cannot ssh to localhost without a passphrase, execute the following 
commands to configure passwordless ssh access for hadoop to localhost:

	ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
	cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
	export HADOOP\_PREFIX=/usr/local/hadoop

sshd must be running. check with the following command that you can see 
localhost without a passphrase:

	ssh localhost


## (3) Hadoop
- download http://www.apache.org/dist/hadoop/common/stable/
- verify   http://hadoop.apache.org/releases.html
- unpack and add it to the shell path

### configure Hadoop:
Let's first edit some configuration files.

#### /home/hadoop/.bashrc

	export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
	export HADOOP_INSTALL=/usr/local/hadoop
	export PATH=$PATH:$HADOOP_INSTALL/bin
	export PATH=$PATH:$HADOOP_INSTALL/sbin
	export HADOOP_MAPRED_HOME=$HADOOP_INSTALL
	export HADOOP_COMMON_HOME=$HADOOP_INSTALL
	export HADOOP_HDFS_HOME=$HADOOP_INSTALL
	export YARN_HOME=$HADOOP_INSTALL

#### /usr/local/hadoop/etc/hadoop/hadoop-env.sh

	export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64

#### ensure proper progress
Run the following command

	hadoop

If it shows the usage documentation for the hadoop script everything is fine 
(so far).

#### /usr/local/hadoop/etc/hadoop/core-site.xml
https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/core-default.xml   

	<configuration>
		<property>
		  <name>fs.defaultFS</name>
		  <value>hdfs://localhost:9000</value>
		</property>
		<property>
	    <name>hadoop.tmp.dir</name>
	    <value>/tmp/hadoop</value>
	    <description>A base for other temp dirs</description>
		</property>
	</configuration>

and, on the shell:

	sudo mkdir -p /tmp/hadoop
	sudo chown hadoop:hadoop /tmp/hadoop

#### /usr/local/hadoop/etc/hadoop/hdfs-site.xml
https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/hdfs-default.xml

	<configuration>
		<property>
		   <name>dfs.replication</name>
		   <value>1</value>
		</property>
		<property>
		   <name>dfs.namenode.name.dir</name>
		   <value>file:///home/hadoop/namenode</value>
		</property>
		<property>
		   <name>dfs.datanode.data.dir</name>
		   <value>
		    file:/mnt/hdd1/hadoop/datanode,
		    file:/mnt/hdd2/hadoop/datanode,
		    [SSD]file:/mnt/ssd2/hadoop/datanode</value>
		</property>
	</configuration>

#### /usr/local/hadoop/etc/hadoop/mapred-site.xml
https://hadoop.apache.org/docs/current/hadoop-mapreduce-client/hadoop-mapreduce-client-core/mapred-default.xml   
We want YARN to manage our map reduce jobs

	<configuration>
	  <property>
	    <name>mapreduce.framework.name</name>
	    <value>yarn</value>
	  </property>
	</configuration>

#### /usr/local/hadoop/etc/hadoop/yarn-site.xml
https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-common/yarn-default.xml

	<configuration>
	  <property>
	    <name>yarn.nodemanager.aux-services</name>
	    <value>mapreduce_shuffle</value>
	  </property>
	</configuration>


## (4) HDFS

### namenode
The namenode manages the filesystem metadata in the HDFS

#### create namenode
Let's try to keep 'namenode' on /. It keeps track of the files in 'datanode' 
and needs about 150 bytes per file. That should fit on /.

	mkdir -p /home/hadoop/namenode

#### check permissions

	sudo chown hadoop:hadoop -R /home/hadoop/namenode
	sudo chmod 755 -R /home/hadoop/namenode

#### Initialze namenode 

	/usr/local/hadoop/bin/hdfs namenode -format  
     
see [https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HDFSCommands.html#namenode](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HDFSCommands.html#namenode). 
This commad doesn't reformat the disk. It rather initializes some sort of 
file-database that grows on demand. It is possible to limit it's grow by e.g.
 setting a minimum of disk space that should remain untouched by HDFS.  
Note that hdfs namenode -format command should be executed once before we start 
using Hadoop. If this command is executed again after Hadoop has been used, 
it'll destroy all the data on the Hadoop file system.
	
#### start HDFS
We only start HDFS. So far we don't need YARN (start-yarn.sh). And start-all.sh
is deprecated.

	start-dfs.sh

Check the web interface for the NameNode if everything went well.

	http://localhost:50070

### datanode
Datanodes store the actual data.  

#### set users for HDFS partitions

	sudo chown hadoop:hadoop -R /mnt/hdd1/hadoop/datanode
	sudo chown hadoop:hadoop -R /mnt/hdd2/hadoop/datanode
	sudo chown hadoop:hadoop -R /mnt/ssd2/hadoop/datanode
	sudo chmod 700 -R /mnt/hdd1/hadoop/datanode
	sudo chmod 700 -R /mnt/hdd2/hadoop/datanode
	sudo chmod 700 -R /mnt/ssd2/hadoop/datanode

#### create HDFS users
Now Hadoop users - as opposed to users of this unix box - need to be given 
access to HDFS by creating home dirs and setting permissions. The HDFS commands 
are similar to Unix commands though.   
Setting the space quota is optional. Here we do not set it to 200 GB. 
 
	hdfs dfs -mkdir /user
	hdfs dfs -mkdir /user/hadoop
	hdfs dfs -chown hadoop:hadoop /user/hadoop
	# hdfs dfsadmin -setSpaceQuota 200g /user/<username> 


## (5) YARN
Configuration has been done above (mapred-site.xml and yarn-site.xml). Now 
start ResourceManager daemon and NodeManager daemon:

	start-yarn.sh

Check the web interface for the ResourceManager at 

	http://localhost:8088


## (6) cluster up and running
After issuing 'start-dfs.sh' and 'start-yarn.sh' commands run 'jps' to see if
 everything works:
 
	hduser@mteam:/usr/local/hadoop/sbin$ jps
	9026 NodeManager
	7348 NameNode
	9766 Jps
	8887 ResourceManager
	7507 DataNode

### stopping hadoop

	stop-dfs.sh
	stop-yarn.sh

### summary of webinterfaces

- HDFS namenode [http://136.243.78.39:50070](http://136.243.78.39:50070)   
- HDFS datanode [http://136.243.78.39:50075](http://136.243.78.39:50075) 
- HDFS secondary namenode [http://136.243.78.39:50090](http://136.243.78.39:50090) 
- MR jobtracker [http://136.243.78.39:50030](http://136.243.78.39:50030) 
- MR tasktracker [http://136.243.78.39:50060](http://136.243.78.39:50060) 
- MR jobhistory [http://136.243.78.39:19888](http://136.243.78.39:19888) 
- resource manager [http://136.243.78.39:8088](http://136.243.78.39:8088)    
- logs [http://136.243.78.39:50090/logs/](http://136.243.78.39:50090/logs/)  
 

## (7) HBase
- download http://www.apache.org/dist/hbase/stable
- docs http://hbase.apache.org/book.html
- installation http://hbase.apache.org/book.html#quickstart
- install to /usr/local/hbase


#### create user and tmp dir
	
	adduser hbase
	mkdir -p /home/hbase/tmp
	sudo chown hadoop:hadoop -R /home/hbase
	sudo chmod 755 -R /home/hbase
	
#### conf/hbase-env.sh

	JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
	
#### ~.bashrc

	export HBASE_HOME=/usr/local/hbase
	export PATH=$PATH:$HBASE_HOME/bin
	# export HBASE_HEAPSIZE=4096 # hmmm

#### conf/hbase-site.xml

	<configuration>
	  <property>
	    <name>hbase.rootdir</name>
	    <value>hdfs://localhost:8020/hbase</value>
	  </property>
	  <property>
	    <name>hbase.tmp.dir</name>
	    <value>/home/hbase/tmp</value>
	  </property>
	  <property>
	    <name>hbase.zookeeper.property.dataDir</name>
	    <value>/home/hadoop/zookeeper</value>
	  </property>
		<property>
		  <name>hbase.cluster.distributed</name>
		  <value>true</value>
		</property>
	</configuration>
	
hbase will create the hbase dir in hdfs for you. do not interfer.   
the following command will list the directory

	hdfs dfs -ls /hbase


#### start and stop HBase

	start-hbase.sh 
	stop-hbase.sh

  
## (8) Spark 
Basically it's just download and verification
- download spark-1.5.1-bin-hadoop2.6.tgz from http://www.apache
 .org/dist/spark/spark-1.5.1/
- see also http://spark.apache.org/downloads.html
- and here http://spark.apache.org/docs/latest/   
  
## (9) Drill
Basically it's just download and verification
- download http://www.apache.org/dist/drill/drill-1.1.0/
- see also https://drill.apache.org/docs/installing-drill-on-linux-and-mac-os-x/
- edit /usr/local/drill/conf/drill-override.conf to disable web console which 
  otherwise provides a sql interface to the world

      http: {
        enabled: false,       // default: true
        ssl_enabled: false,
        port: 8047
      }


## (10) Avro
- python: https://avro.apache.org/docs/1.7.6/gettingstartedpython.html
- for 'political' reasons java would be better, but it seems a lot more hassle:
  java: https://avro.apache.org/docs/1.7.6/gettingstartedjava.html   
  needs also jackson http://wiki.fasterxml.com/JacksonDownload

## (11) Thrift
- basic requirements https://thrift.apache.org/docs/install/
- thrft itself https://thrift.apache.org/docs/install/debian
- python support python-all python-all-dev python-all-dbg




---

[0] https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html   
[1] http://www.bogotobogo.com/Hadoop/BigData_hadoop_Install_on_ubuntu_single_node_cluster.php  
[2] https://rstudio-pubs-static.s3.amazonaws.com/78508_abe89197267240dfb6f4facb361a20ed.html  