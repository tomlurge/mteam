
# How to install a single node Hadoop cluster
Our install is a bit untypical since we don't want a test installation on a 
local machine but OTOH don't want the typical multi machine Hadoop cluster 
either. We are going for **pseudo-distributed mode**, also called a **single 
node cluster** and therefor have to take care to not make false friend with all 
those tutorials about settingup Hadoop (clusters) out there. And we've been 
warned that setting up Hadoop isn't exactly trivial - in any case.

Help comes in the form of instructions on the Hadoop homepage [0](https://hadoop
.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html) and in two 
blogpost [1](http://www
 .bogotobogo.com/Hadoop/BigData_hadoop_Install_on_ubuntu_single_node_cluster
 .php) and [2](https://rstudio-pubs-static.s3.amazonaws
 .com/78508_abe89197267240dfb6f4facb361a20ed.html)


## (1) Users
Setup group and add members
 
	sudo addgroup hadoop
	sudo adduser --ingroup hadoop hduser
	sudo adduser --ingroup hadoop hdfs
	sudo adduser --ingroup hadoop yarn


## (2) Setup passphraseless ssh
configure passwordless ssh access for hadoop to localhost:

	ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
	cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
	export HADOOP\_PREFIX=/usr/local/ha

sshd must be running. check with the following command that you can see 
localhost without a passphrase:

	ssh localhost


## (3) Hadoop
- download http://www.apache.org/dist/hadoop/common/stable/
- verify   http://hadoop.apache.org/releases.html
- unpack and add it to the shell path

### configure Hadoop:
Let's first edit some configuration files.

#### ~/.bash.rc
*not sure about the first line - correct path? - and the last 2 lines, whih 
come from one of the blogposts*

	#Hadoop variables
	export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
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

	export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64

#### ensure proper progress
Run the following command

	hadoop

If it shows the usage documentation for the hadoop script everything is fine 
(so far)

#### /usr/local/hadoop/etc/hadoop/core-site.xml
https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/core-default.xml

	<property>
	  <name>fs.defaultFS</name>
	  <value>hdfs://localhost:9000</value>
	</property>
	
	<!-- TODO
	     see http://stackoverflow.com/questions/2354525 
	     i changed this a bit
	     have no idea where the $user.name should come from
	     maybe a simple /tmp/hadoop dir would be sufficient
	-->
	<property>
    <name>hadoop.tmp.dir</name>
    <value>/tmp/hadoop/user-${user.name}</value>
    <description>A base for other temp dirs</description>
	</property>

and, on the shell:

	sudo mkdir -p /tmp/hadoop
	sudo chown hduser:hadoop /tmp/hadoop

#### /usr/local/hadoop/etc/hadoop/hdfs-site.xml
https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/hdfs-default.xml

	<property>
	   <name>dfs.replication</name>
	   <value>1</value>
	</property>
	<property>
	   <name>dfs.namenode.name.dir</name>
	   <value>file://tmp/hadoop/namenode</value>
	</property>
	<property>
	   <name>dfs.datanode.data.dir</name>
	   <value>
	    file:/mnt/hdd1/datanode,
	    file:/mnt/hdd2/datanode,
	    [SS]file:/mnt/ssd2/datanode</value>
	</property>

#### /usr/local/hadoop/etc/hadoop/mapred-site.xml
https://hadoop.apache.org/docs/current/hadoop-mapreduce-client/hadoop-mapreduce-client-core/mapred-default.xml   
We want YARN to manage our map reduce jobs

	<configuration>
	  <property>
	    <name>mapreduce.framework.name</name>
	    <value>yarn</value>
	  </property>

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

	mkdir -p /tmp/hadoop/namenode

#### check permissions

	sudo chown hduser:hadoop -R /tmp/hadoop/namenode
	sudo chmod 777 -R /tmp/hadoop/namenode

#### Initialze namenode as user 'hdfs':  

	su hdfs
	hdfs namenode -format  
     
see [https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HDFSCommands.html#namenode](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HDFSCommands.html#namenode). 
This commad doesn't reformat the disk. It rather initializes some sort of 
file-database that grows on demand. It is possible to limit it's grow by e.g.
 setting a minimum of disk space that should remain untouched by HDFS.  
Note that hadoop namenode -format command should be executed once before we start using Hadoop.  
If this command is executed again after Hadoop has been used, it'll destroy all the data on the Hadoop file system.
	
#### start HDFS
We only start HDFS. So far we don't need YARN (start-yarn.sh). And start-all.sh
is deprecated.

	su hduser
	start-dfs.sh

Check the web interface for the NameNode if everything went well.

	http://localhost:50070

### datanode
Datanodes store the actual data.  

#### set users for HDFS partitions

	sudo chown hduser:hadoop -R /mnt/hdd1/
	sudo chown hduser:hadoop -R /mnt/hdd2/
	sudo chown hduser:hadoop -R /mnt/ssd2/
	sudo chmod 777 -R /mnt/hdd1/
	sudo chmod 777 -R /mnt/hdd2/
	sudo chmod 777 -R /mnt/ssd2/

#### create HDFS users
Now Hadoop users - as opposed to users of this unix box - need to be given 
access to HDFS by creating home dirs and setting permissions. The HDFS commands are similar to Unix commands though.   
Setting the space quota is optional. Here we set it to 200 GB. 
 
	dfs -mkdir /user
	dfs -mkdir /user/<username>
	dfs -chown <username>:<username> /user/<username>
	dfsadmin -setSpaceQuota 200g /user/<username> 


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

- namenode [http://136.243.78.39:50070](http://136.243.78.39:50070)   
- datanode [http://136.243.78.39:50075](http://136.243.78.39:50075)   
- resource manager [http://136.243.78.39:8088](http://136.243.78.39:8088)    
- logs [http://136.243.78.39:50090/logs/](http://136.243.78.39:50090/logs/)  
  (actually i'm not sure if logs are really accessible there. let's try! 
  otherwise maybe read http://hortonworks.com/blog/simplifying-user-logs-management-and-access-in-yarn/)


## (7) HBase
- download http://www.apache.org/dist/hbase/stable/








  
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




---

[0] https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html   
[1] http://www.bogotobogo.com/Hadoop/BigData_hadoop_Install_on_ubuntu_single_node_cluster.php  
[2] https://rstudio-pubs-static.s3.amazonaws.com/78508_abe89197267240dfb6f4facb361a20ed.html  