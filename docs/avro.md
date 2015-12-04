spec: https://avro.apache.org/docs/1.7.7/spec.html  
intro: https://avro.apache.org/docs/current/gettingstartedjava.html    
good example of a complex schema: https://stackoverflow.com/questions/28163225/how-to-define-avro-schema-for-complex-json-document    


good to know:

 a schema file can only contain a single schema definition   
 eg {"name": "favorite_number",  "type": ["int", "null"]}
   favorite_number can either be an int or null, essentially making it an optional field. 
   

MAPPINGS
  generic
    a dynamic mapping, can be used even when the schema is not known ahead of runtime
  specific
    when you have a copy of the schema before you read or write data
    generated classes also provide a more domain-oriented API for user code than Generic ones.
  reflective
    maps Avro types onto preexisting Java types using reflection
    
    
TYPES

    null
    boolean
    int
    long
    float
    double
    bytes
    string
    array     items
    map       values
    record    fields
    enum      symbols
    fixed
    union


## WORKING WITH iT  (from hadoop def guide p350 ff)

sample SCHEMA

    {
      "type": "record",
      "name": "StringPair", 
      "doc": "A pair of strings.", 
      "fields": [
        {"name": "left", "type": "string"},
        {"name": "right", "type": "string"} 
      ]
    }


### using the _generic_ api

load the schema 
   
    Schema.Parser parser = new Schema.Parser(); 
    Schema mySchema = parser.parse(getClass().getResourceAsStream("mySchema.avsc"));
    
create an instance of the avro record  

    GenericRecord myDatum = new GenericData.Record(mySchema); 
    myDatum.put("left", "L");
    myDatum.put("right", "R");

serialize record   
.to output stream

    ByteArrayOutputStream out = new ByteArrayOutputStream(); 
    DatumWriter<GenericRecord> myWriter = 
            new GenericDatumWriter<GenericRecord>(mySchema);
    Encoder myEncoder = EncoderFactory.get().binaryEncoder(out, null); 
    myWriter.write(myDatum, myEncoder);
    encoder.flush();
    out.close();
    
.to file

    File myFile = new File("data.avro"); 
    DatumWriter<GenericRecord> myWriter =
            new GenericDatumWriter<GenericRecord>(mySchema); 
    DataFileWriter<GenericRecord> dataFileWriter = 
            new DataFileWriter<GenericRecord>(myWriter);
    dataFileWriter.create(mySchema, myFile);
    dataFileWriter.append(myDatum);
    dataFileWriter.close();

To write a file to HDFS, we get an Output Stream by calling create() on 
FileSystem (see “Writing Data” on page 61).


### using the _specific_ api 

load the schema 

    create class 'StringPair' by using one of 3 options
      - maven (see hadoop def guide p351)
      - ant (see there)
      - avro command line tools      

create an instance of the avro record  

    StringPair myDatum = new StringPair();
    myDatum.setLeft("L");
    myDatum.setRight("R");

serialize record to output stream

    ByteArrayOutputStream out = new ByteArrayOutputStream(); 
    DatumWriter<StringPair> myWriter = 
            new SpecificDatumWriter<StringPair>(StringPair.class); 
    Encoder myEncoder = EncoderFactory.get().binaryEncoder(out, null); 
    myWriter.write(myDatum, myEncoder);
    myEncoder.flush();
    out.close();
    

## Avro TOOLS commands

    convert .avro to JSON
    % java -jar $AVRO_HOME/avro-tools-*.jar tojson myFile.avro
    
    compile a schema
    % java -jar avro-tools-1.7.7.jar compile schema ~/desc.avsc /desc


## java 

### import

    http://www.hascode.com/2014/03/using-apache-avro-with-java-and-maven/
    
    import org.apache.avro.file.DataFileReader;
    import org.apache.avro.file.DataFileWriter;
    import org.apache.avro.io.DatumReader;
    import org.apache.avro.io.DatumWriter;
    import org.apache.avro.specific.SpecificDatumReader;
    import org.apache.avro.specific.SpecificDatumWriter;
 
    
### maven

    <dependencies>
    	<dependency>
    		<groupId>org.apache.avro</groupId>
    		<artifactId>avro</artifactId>
    		<version>1.7.7</version>
    	</dependency>
    </dependencies>
     
    <build>
    	<plugins>
    		<plugin>
    			<groupId>org.apache.avro</groupId>
    			<artifactId>avro-maven-plugin</artifactId>
    			<version>1.7.7</version>
    			<executions>
    				<execution>
    					<phase>generate-sources</phase>
    					<goals>
    						<goal>schema</goal>
    					</goals>
    					<configuration>
    						<sourceDirectory>${project.basedir}/src/main/avro/</sourceDirectory>
    						<outputDirectory>${project.basedir}/src/main/java/</outputDirectory>
    					</configuration>
    				</execution>
    			</executions>
    		</plugin>
    	</plugins>
    </build>
    
You may run the following command to create the Book class needed from the schema file:

    mvn generate-sources



## PARQUET - AvroParquetWriter

The Avro schema is:

    {
    "type": "record",
    "name": "StringPair", "doc": "A pair of strings.", "fields": [
    {"name": "left", "type": "string"},
    {"name": "right", "type": "string"} ]
    }

We create a schema instance and a generic record with:

    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse(getClass().getResourceAsStream("StringPair.avsc"));
    
    GenericRecord datum = new GenericData.Record(schema); 
    datum.put("left", "L");
    datum.put("right", "R");
    
Then we can write a Parquet file:

    Path path = new Path("data.parquet"); 
    AvroParquetWriter<GenericRecord> writer =
            new AvroParquetWriter<GenericRecord>(path, schema); 
    writer.write(datum);
    writer.close();

AvroParquetWriter converts the Avro schema into a Parquet schema, and also 
translates each Avro GenericRecord instance into the corresponding Parquet types 
to write to the Parquet file. The file is a regular Parquet file, with an extra 
piece of metadata to store the Avro schema. 
To see the Parquet schema that was generated from the Avro schema, we can use 
the following:

    % parquet-tools schema data.parquet 
    message StringPair {
      required binary left (UTF8);
      required binary right (UTF8);
    }


## CONSTRUCTOR or BUILDER

After completing code generation, let's create some Users and set their fields.
    
    // Constructor
    User user1 = new User();
    user1.setName("Alyssa");
    user1.setFavoriteNumber(256);
    // Leave favorite color null
    
    // Alternate constructor
    User user2 = new User("Ben", 7, "red");
    
    // Construct via builder
    User user3 = User.newBuilder()
                 .setName("Charlie")
                 .setFavoriteColor("blue")
                 .setFavoriteNumber(null)
                 .build();
            
As shown in this example, Avro objects can be created either by invoking a 
constructor directly or by using a builder. Unlike constructors, builders will 
automatically set any default values specified in the schema. Additionally, 
builders validate the data as it set, whereas objects constructed directly will 
not cause an error until the object is serialized. However, using constructors 
directly generally offers better performance, as builders create a copy of the 
datastructure before it is written. 

Builder
  
  - automatically set any default values specified in the schema
  - validate the data as it's set
  - however less performant
    
    
    