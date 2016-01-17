

      IntelliJ doesn't know how to handle long classpaths,
      messes them up when building the MANIFEST.MF
      therefor, after building artifacts
      - decompress the jar
      - edit the MANIFEST.MF to correct IntelliJs garbage
      - run "jar cmf META-INF/MANIFEST.MF convertToJson.jar lib mteam"
      see https://docs.oracle.com/javase/tutorial/deployment/jar/build.html 
      and https://youtrack.jetbrains.com/issue/IDEA-148005