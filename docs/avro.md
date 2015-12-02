spec: https://avro.apache.org/docs/1.7.7/spec.html  
intro: https://avro.apache.org/docs/current/gettingstartedjava.html    
good example of a complex schema: https://stackoverflow.com/questions/28163225/how-to-define-avro-schema-for-complex-json-document    


good to know:

 a schema file can only contain a single schema definition   
 eg {"name": "favorite_number",  "type": ["int", "null"]}
   favorite_number can either be an int or null, essentially making it an optional field. 