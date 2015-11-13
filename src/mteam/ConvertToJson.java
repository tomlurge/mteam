package mteam;

/* Import standard Java classes. */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/* Import classes from metrics-lib. */
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorFile;
import org.torproject.descriptor.DescriptorReader;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ServerDescriptor;

/* Import classes from Google's Gson library. */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;




public class ConvertToJson {

  /* Read all descriptors in the provided (decompressed) tarball and
   * convert all server descriptors to the JSON format. */
  public static void main(String[] args) throws IOException {

    /*  optional arguments
     *    -v                force creation of attributes with null values
     *    <directory name>  scan only a given subdirectory of data/in
     */
    boolean verbose = false;
    if (args.length > 0 && args[0].equals("-v")) {
      verbose = true;
    }
    // verbose=false; // testing

    String dir = "";
    if (args.length == 1 && !args[0].equals("-v") ) {
      dir = args[0];
    }
    else if (args.length == 2) {
      dir = args[1];
    }

    DescriptorReader descriptorReader = DescriptorSourceFactory.createDescriptorReader();
    descriptorReader.addDirectory(new File("data/in/" + dir));
    Iterator<DescriptorFile> descriptorFiles = descriptorReader.readDescriptors();

    int written = 0;
    BufferedWriter bw = new BufferedWriter(new FileWriter("data/out/test.json"));
    bw.write("{\"to start with\" : \"some remark\"}\n");
    while (descriptorFiles.hasNext()) {
      DescriptorFile descriptorFile = descriptorFiles.next();
      for (Descriptor descriptor : descriptorFile.getDescriptors()) {
        String jsonDescriptor = null;

        if (descriptor instanceof ServerDescriptor) {
          jsonDescriptor = convertServerDescriptor((ServerDescriptor) descriptor, verbose);
        }
        /* Could add more else-if statements here. */
        if (jsonDescriptor != null) {
          bw.write((written++ > 0 ? "\n" : "") + jsonDescriptor);
        }
      }
    }
    bw.close();
  }

  /* BRIDGES SERVER DESCRIPTORS */

  /* Inner class to serialize address/port combinations in "or_address"
   * lines or others.  In theory, we could also include those as strings. */
  static class AddressAndPort {
    String address; // always lower-case
    int port;
    AddressAndPort(String address, int port) {
      this.address = address;
      this.port = port;
    }
  }

  /* Inner class to serialize "read-history" and "write-history" lines. */
  static class BandwidthHistory {
    String date; // format is YYYY-MM-DD HH:MM:SS
    long interval; // seconds
    Collection<Long> bytes;
  }

  /* Date/time formatter. */
  static final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
  static final Locale dateTimeLocale = Locale.US;
  static final TimeZone dateTimezone = TimeZone.getTimeZone("UTC");
  static DateFormat dateTimeFormat;
  static {
    dateTimeFormat = new SimpleDateFormat(dateTimePattern, dateTimeLocale);
    dateTimeFormat.setLenient(false);
    dateTimeFormat.setTimeZone(dateTimezone);
  }

  /* Convert a read or write history to an inner class that can later be
   * serialized to JSON. */
  static BandwidthHistory convertBandwidthHistory(org.torproject.descriptor.BandwidthHistory hist) {
    BandwidthHistory bandwidthHistory = new BandwidthHistory();
    bandwidthHistory.date = dateTimeFormat.format(hist.getHistoryEndMillis());
    bandwidthHistory.interval = hist.getIntervalLength();
    bandwidthHistory.bytes = hist.getBandwidthValues().values();
    return bandwidthHistory;
  }


  /* BRIDGE NETWORK STATUS */

  /* Inner class to serialize flag/treshold combinations. */
  static class FlagsAndTresholds {
    String flag; // always lower-case
    int treshold;
    FlagsAndTresholds(String flag, int treshold) {
      this.flag = flag;
      this.treshold = treshold;
    }
  }

  static class BridgeStatus {
    List<R> r; // bridge description
    List<String> s; // flags
    List<W> w; // bandwidths
    List<String> p; // policies
    String a; // additional IP adress and port
  }

  static class R {}

  static class W {}

  /*  TODO  modularize
   *
   *        move JSON descriptor definition to sperate classes
   */

  /* Inner class to serialize bridge server descriptors. */
  static class JsonBridgeServerDescriptor {
    /* mandatory */
    String descriptor_type; // set to bridge-server-descriptor $VERSION
    String nickname; // can be mixed-case
    String address; // changed to lower-case
    int or_port; 
    int socks_port; // most likely 0 except for *very* old descs
    int dir_port; 
    Integer bandwidth_avg; 
    Integer bandwidth_burst; 
    Boolean onion_key; // usually false b/c sanitization
    Boolean signing_key; // usually false b/c sanitization
    List<String> exit_policy;
    /* optional */
    Integer bandwidth_observed; //  missing in older descriptors!
    List<AddressAndPort> or_addresses; // addresses sanitized!
    String platform; //  though usually set
    String published; // format YYYY-MM-DD HH:MM:SS
    String fingerprint; // always upper-case hex
    Boolean hibernating;
    Long uptime; // though usually set
    String ipv6_policy;
    String contact;
    List<String> family; // apparently not used at all
    BandwidthHistory read_history;
    BandwidthHistory write_history;
    Boolean eventdns;
    Boolean caches_extra_info;
    String extra_info_digest;  // upper-case hex
    List<Integer> hidden_service_dir_versions;
    List<Integer> link_protocol_versions;
    List<Integer> circuit_protocol_versions;
    Boolean allow_single_hop_exits;
    Boolean ntor_onion_key;
    String router_digest; // upper-case hex
  }

  static class JsonBridgeNetworkStatus {
    String descriptor_type;
    String published; // format YYYY-MM-DD HH:MM:SS
    List<FlagsAndTresholds> flagTresholds;
    List<BridgeStatus> bridge;
  }


  /* Take a single server descriptor, assume it's a *bridge* server
   * descriptor, and return a JSON string representation for it. */
  static String convertServerDescriptor(ServerDescriptor desc, boolean verbose) { // DESC

  /*  TODO  switch for different types of descriptors
   *
   *        server-descriptor
   *        extra-info
   *        network-status-consensus
   *        network-status-vote
   *        bridge-network-status
   *        bridge-server-descriptor
   *        bridge-extra-info
   *        tordnsel
   *        torperf
   */

    //  TODO so geht das natürlich nicht
    //        da müsste ich ja erst alle descriptoren initialisieren
    JsonBridgeServerDescriptor json = null;
    //  TODO  also muss ich wohl ganz am ende die GSON serialisierung
    //        in eine eigene klasse auslagern
    //        und aus der schleife heraus aufrufen

    /* Find the @type annotation switch to appropriate JSONdescriptor */
    for (String annotation : desc.getAnnotations()) {
      if (annotation.startsWith("@type bridge-server-descriptor")) {
        json = new JsonBridgeServerDescriptor(); // JSON


        /* mandatory */
        json.descriptor_type = annotation.substring("@type ".length());
        json.nickname = desc.getNickname();
        json.address = desc.getAddress();
        json.or_port = desc.getOrPort();
        json.socks_port = desc.getSocksPort();
        json.dir_port = desc.getDirPort();


    /* Include a bandwidth object with average, burst, and possibly
     * observed bandwidth. */
        json.bandwidth_avg = desc.getBandwidthRate();
        json.bandwidth_burst = desc.getBandwidthBurst();
        json.onion_key = desc.getOnionKey() != null;
        json.signing_key = desc.getSigningKey() != null;
        if (desc.getExitPolicyLines() != null && !desc.getExitPolicyLines().isEmpty()) {
          json.exit_policy = desc.getExitPolicyLines();
        }

    /* optional */

        if (desc.getBandwidthObserved() >= 0) {
          json.bandwidth_observed = desc.getBandwidthObserved();
        }
        json.or_addresses = new ArrayList<AddressAndPort>();
        if (desc.getOrAddresses() != null && !desc.getOrAddresses().isEmpty()) {
          for (String orAddress : desc.getOrAddresses()) {
            if (!orAddress.contains(":")) {
              continue;
            }
            int lastColon = orAddress.lastIndexOf(":");
            try {
              int port = Integer.parseInt(orAddress.substring(
                      lastColon + 1));
              json.or_addresses.add(
                      new AddressAndPort(orAddress.substring(0,
                              lastColon), port));
            } catch (NumberFormatException e) {
              continue;
            }
          }
        }
        //if (desc.getPlatform() != null) {
        json.platform = desc.getPlatform();
        //}
        json.published = dateTimeFormat.format(desc.getPublishedMillis());
        json.fingerprint = desc.getFingerprint().toUpperCase();
        if (desc.isHibernating()) {
          json.hibernating = desc.isHibernating();
        }

        if (desc.getUptime() != null) {
          json.uptime = desc.getUptime();
        }

        if (desc.getIpv6DefaultPolicy() != null && !desc.getIpv6DefaultPolicy().isEmpty()) {
          json.ipv6_policy = desc.getIpv6DefaultPolicy();
        }

        json.contact = desc.getContact();
        json.family = desc.getFamilyEntries();
    /* Include bandwidth histories using their own helper method. */
        if (desc.getReadHistory() != null) {
          json.read_history = convertBandwidthHistory(desc.getReadHistory());
        }
        if (desc.getWriteHistory() != null) {
          json.write_history = convertBandwidthHistory(desc.getWriteHistory());
        }

        json.eventdns = desc.getUsesEnhancedDnsLogic();
        json.caches_extra_info = desc.getCachesExtraInfo();
        if (desc.getExtraInfoDigest() != null) {
          json.extra_info_digest = desc.getExtraInfoDigest().toUpperCase();
        }
        json.hidden_service_dir_versions = desc.getHiddenServiceDirVersions();
        json.link_protocol_versions = desc.getLinkProtocolVersions();
        json.circuit_protocol_versions = desc.getCircuitProtocolVersions();
        json.allow_single_hop_exits = desc.getAllowSingleHopExits();
        json.ntor_onion_key = desc.getNtorOnionKey() != null;
        json.router_digest = desc.getServerDescriptorDigest().toUpperCase();










      }
    }

   // JsonBridgeServerDescriptor json = new JsonBridgeServerDescriptor(); // JSON



    /* Convert everything to a JSON string and return that.
     * If flag '-v' (for "verbose") is set serialize null-values too
     */

    if (verbose) {
      Gson gson = new GsonBuilder().serializeNulls().create();
      return gson.toJson(json);
    }
    else {
      Gson gson = new GsonBuilder().create();
      return gson.toJson(json);
    }

  }

  static class ToJson {
    generateJson(JsonBridgeServerDescriptor json)
  }


}
