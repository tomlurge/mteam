
# How to install a single node Hadoop cluster
Our install is a bit untypical since we don't want a test installation on a 
local machine but OTOH don't want the typical multi machine Hadoop cluster 
either. We are going for a **single node cluster** and therefor have to take 
care to not make false friend with all those tutorials about settingup Hadoop 
(clusters) out there. And we've been warned that setting up Hadoop isn't 
exactly trivial - in any case.

## TODO
- do we need YARN?
- do we need Parquet?
- how to integrate the 4 disks


## (1) Users
 - setup group 
 
        sudo addgroup hadoop
 
 - and members
 
        sudo adduser --ingroup hadoop hduser
        sudo adduser --ingroup hadoop hdfs
        sudo adduser --ingroup hadoop yarn

## (2) SSH for hadoop
 - configure passwordless ssh access for hadoop to localhost
 - sshd should be running (according to [1]. really?)



## (3) Hadoop
- download http://www.apache.org/dist/hadoop/common/stable/
- verify   http://hadoop.apache.org/releases.html
- unpack and add it to the shell path

Configure Hadoop with help from instructions in blogpost [1](http://www
 .bogotobogo.com/Hadoop/BigData_hadoop_Install_on_ubuntu_single_node_cluster
 .php) and [2](https://rstudio-pubs-static.s3.amazonaws
 .com/78508_abe89197267240dfb6f4facb361a20ed.html)
 
#### ~/.bash.rc
*not sure about the last 2 lines*

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

#### /usr/local/hadoop/etc/hadoop/hadoop-env.sh
*assuming, that's the right path*

	export JAVA_HOME=/usr/bin/java

#### /usr/local/hadoop/etc/hadoop/core-site.xml, 

	<property>
	    <name>hadoop.tmp.dir</name>
	    <value>/maybe/the/second/ssd</value>
	    <description>A base for other temp dirs</description>
	</property>
	<property>
	  <name>fs.default.name</name>
	  <value>hdfs://localhost:9000</value>
	</property>

#### /usr/local/hadoop/etc/hadoop/yarn-site.xml,

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

#### /usr/local/hadoop/etc/hadoop/mapred-site.xml,

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

#### create hadoop directories
Create 2 directories, 'namenode' and 'datanode'. Let's try to keep 'namenode'
on /. It keeps track of the files in 'datanode' and needs about 150 bytes per 
file. That should fit on /.

	mkdir -p HADOOP_INSTALL/namenode
	mkdir -p /mnt/hdd-hdfsl/datanode


#### /usr/local/hadoop/etc/hadoop/hdfs-site.xml

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

#### Now check permissions

	sudo chown hduser:hadoop -R HADOOP_INSTALL/namenode
	sudo chmod 777 -R HADOOP_INSTALL/namenode
	sudo chown hduser:hadoop -R /mnt/hdd-hdfs/
	sudo chmod 777 -R /mnt/hdd-hdfs/

#### second SSD as HDFS
i'm not sure how to handle /mnt/ssd-hdfs - maybe a second entry in hdfs-site.xml?

## (4) HDFS
In the appropriate directories (/mnt/hdd-hdfs and /mnt/ssd-hdfs) issue the 
following command as user 'hdfs':  

	hdfs@mteam:~$  hdfs namenode -format  
     
see [https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs
/HDFSCommands.html#namenode](https://hadoop.apache
.org/docs/current/hadoop-project-dist/hadoop-hdfs/HDFSCommands.html#namenode). 
This commad doesn't reformat the disk. It rather initializes some sort of 
file-database that grows on demand.

Now users need to be given access to hdfs, creating home dirs and setting 
permissions:
 
	hadoop fs -mkdir /user/username
	hadoop fs -chown username:username /user/username
	hdfs dfsadmin -setSpaceQuota 1t /user/username 


## (5) now (pray and) start Hadoop
We only start DFS. So far we don't need YARN (start-yarn.sh). And start-all.sh
is deprecated

	su hduser
	cd /usr/local/hadoop/
	sbin/start-dfs.sh


## (6) HBase
- download http://www.apache.org/dist/hbase/stable/
  
## (7) Spark 
Basically it's just download and verification
- download spark-1.5.1-bin-hadoop2.6.tgz from http://www.apache
 .org/dist/spark/spark-1.5.1/
- see also http://spark.apache.org/downloads.html
- and here http://spark.apache.org/docs/latest/   
  
## (8) Drill
Basically it's just download and verification
- download http://www.apache.org/dist/drill/drill-1.1.0/
- see also https://drill.apache.org/docs/installing-drill-on-linux-and-mac-os-x/

## (9) Parquet
What about it? Do we need it?



[1] http://www
 .bogotobogo.com/Hadoop/BigData_hadoop_Install_on_ubuntu_single_node_cluster
 .php  
[2] https://rstudio-pubs-static.s3.amazonaws
.com/78508_abe89197267240dfb6f4facb361a20ed.html  