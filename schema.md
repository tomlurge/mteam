# schema

we will need several schemata

- collecTor/JSON   
CollecTor/JSON is a JSON schema to which to transform the data provided by collecTor. All collecTor data will be transformed to JSON data according to this schema, as an intermediate step to ingestion into both Hadoop applications (HBase, Drill, Spark) and other solutions like MongoDB and PostgreSQL.  
This schema will follow the collecTor schema very closely.  
It would probably be best to start with the same collecTor/JSON schema in Spark 
as well as in Drill and then monitor usage rather than prematurely optimize 
the schema before ingestion. After all both tools are designed precisely to 
develop those optimizations.

	- Drill   
Drill will initially provide the data in the same schema as collecTor/JSON. 
From there other views/aggregations can be materialized as needed.

	- Spark   
	We have the following choices. Taking the detour over Drill looks attractive as long as data import in Spark directly hasn't been explored. Maybe that's a non-issue soon.  
		- starting from collecTor/JSON, same way as with Drill
		- export Drill data 1:1 to Parquet files and ingest those into Spark
		- pre-aggregate views in Drill and ingest those (via Parquet) into Spark    

- HBase   
HBase will be our solution for looking up single documents. You can do map reduce on HBase too but other storage formats like Parquet are better suited to 
that task. The HBase schema will be optimized for access to records of individual servers. Since HBase has to substitute SQL joins with data denormalization and uses some special techniques to that end this schema will look very different
 to Collector/JSON.


- aggregation schemata   
tbd

## collecTor/JSON schema



## HBase schema


:man könnte zum beispiel consensus-einträge und bridge-status-einträge in eine tabelle packen.
und dazu noch version-2-relay-network-status-einträge und version-1-relay-network-status-einträge.
das wäre vielleicht auch sinnvoll, um vergleiche über die letzten 10+ jahre anzustellen.
aber andere dinge passen da nicht wirklich gut rein.



:dann müssten wir uns etwas einfallen lassen wie wir consensuses und server-descriptors verbinden.
:allerdings riecht das auch wieder nach version 2.
:ein consensus-eintrag enthält einen server-descriptor-digest.
:und im server-descriptor stehen dann auch wieder interessante dinge.
:die infos sind quasi aufgeteilt.
:gleiches gilt für extra-info-descriptors, die jeweils zu genau einem server-descriptor gehören.
:ja, problem ist, dass sie aus drei quellen zusammengesetzt werden müsste.

:ok. mein vorschlag wäre mit consensuses anzufangen.
:oder!
:mit bridge-network-statuses.
:die sind nämlich chronisch unteranalysiert.
:und dann machen wir uns gedanken über bridge-server-descriptors und bridge-extra-infos.
:merk dir nur: schau nach bridges, nicht relays. :)




## aggregation schemata