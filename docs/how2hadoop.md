# get started

## log into user hadoop
	
	log in    t@mteam:~$ sudo -s -H -u hadoop
	log out   hadoop@mteam:/home/t$ exit
	          t@mteam:~$ 


## check that hadoop is running
	hadoop@mteam:~$ hadoop
should print out usage documentation


## start HDFS
	hadoop@mteam:~$ start-dfs.sh
Check `http://136.243.78.39:50070` if everything went well.   
To stop HDFS invoke `stop-dfs.sh`

## start YARN
	hadoop@mteam:~$ start-yarn.sh
Check `http://136.243.78.39:8088` for the ResourceManager.   
To stop YARN invoke `stop-yarn.sh`

## start HBase
	hadoop@mteam:~$ start-hbase.sh 
Check `http://136.243.78.39:8020/hbase` 
To stop HBase invoke `stop-hbase.sh`

## 'jps' to check if everything runs as expected
	hadoop@mteam:~$ jps
 

## summary of webinterfaces

- HDFS namenode [http://136.243.78.39:50070](http://136.243.78.39:50070)   
- HDFS datanode [http://136.243.78.39:50075](http://136.243.78.39:50075) 
- HDFS secondary namenode [http://136.243.78.39:50090](http://136.243.78.39:50090) 
- MR jobtracker [http://136.243.78.39:50030](http://136.243.78.39:50030) 
- MR tasktracker [http://136.243.78.39:50060](http://136.243.78.39:50060) 
- MR jobhistory [http://136.243.78.39:19888](http://136.243.78.39:19888) 
- resource manager [http://136.243.78.39:8088](http://136.243.78.39:8088)    
- logs [http://136.243.78.39:50090/logs/](http://136.243.78.39:50090/logs/)  
- HBase [http://136.243.78.39:8020/hbase](http://136.243.78.39:8020/hbase)
- Drill [http://http://136.243.78.39:8047/](http://http://136.243.78.39:8047/)

## fun on the HDFS shell

    hadoop@mteam:/home$ hadoop fs -mkdir JSON_descriptors
    hadoop@mteam:/home$ hadoop fs -ls
    Found 1 items
    drwxr-xr-x   - hadoop hadoop          0 2015-10-28 00:26 JSON_descriptors
    hadoop@mteam:/home$ hadoop fs -put /home/hadoop/bridge-server-descriptors-2015-10.json.xz JSON_descriptors
    hadoop@mteam:/home$ hadoop fs -ls /JSON_descriptors
    ls: `/JSON_descriptors': No such file or directory
    hadoop@mteam:/home$ hadoop fs -ls JSON_descriptors
    Found 1 items
    -rw-r--r--   1 hadoop hadoop   39902072 2015-10-28 00:28 JSON_descriptors/bridge-server-descriptors-2015-10.json.xz
  
# command and control

## drill

    cp references data in a java class
    convert input json to output parquet without further ado
      create table dfs.tmp.`filename.parquet` as select * from dfs.`/tmp/filename.json` t
      http://stackoverflow.com/questions/21690992/convert-file-of-json-objects-to-parquet-file
    
    json
      https://drill.apache.org/docs/json-data-model
      short-ish introduction into querying json
      json documents are not allowed to have semicolons between toplevel entries
      and some more little caveats that have to be taken into acount
      
    query gz compressed files
      https://drill.apache.org/docs/querying-plain-text-files/#querying-compressed-files
    
    query directories
      You can store multiple files in a directory and query them as if they were
      a single entity. You do not have to explicitly join the files. 
        https://drill.apache.org/docs/querying-directories/