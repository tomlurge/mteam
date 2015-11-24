* think through the process of mass ingestion  
* and periodic ingestion  
* add more options to reduce verbosity  
  . make key-value pairs less obese  
* check the correctnes of singles in one file  
* and streamlien attribute names and structures (at least check for it)




/* CONSTRUCTION MATERIALS


  SORTEDMAP TEMPLATE

      SortedMap<String,Integer> MAP = desc.GETTER();
      for(Map.Entry<String,Integer> kv : MAP.entrySet()) {
        extra.ATTRIBUTE.add(new StringInt(kv.getKey(), kv.getValue()));
      }

  USEFUL CHECKS TO DEFEND AGAINST NULL POINTER EXCEPTIONS

      can return -1
        if (desc.XXX() >= 0) {

      can't be null or false, only true'
        if (desc.XXX()) {

      if a method is called on the desc property always check for null
        if (desc.XXX() != null) {

      for keys: test, if there is one and return 'true' if yes, 'false' otherwise
          server.onion_key = desc.getOnionKey() != null;

      List: first check that the list is not null, then if it's empty
          if (desc.XXX() != null && !desc.XXX().isEmpty()) {


 */