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
import org.torproject.descriptor.*;

/* Import classes from Google's Gson library. */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



public class ConvertToJson {

  static boolean verbose = false;
  static boolean archive = false;
  static String dir = "";


  /* Read all descriptors in the provided directory and
   * convert them to the appropriate JSON format. */
  public static void main(String[] args) throws IOException {

    /*  optional arguments
     *    -v                force creation of attributes with null values
     *    -a                generate .gz archive
     *    <directory name>  scan only a given subdirectory of data/in
     */
    for(String arg : args) {
      if (arg.equals("-v")) verbose = true;
      else if (arg.equals("-a")) archive = true;
      else dir = arg;
    }

    DescriptorReader descriptorReader = DescriptorSourceFactory.createDescriptorReader();
    descriptorReader.addDirectory(new File("data/in/" + dir));
    Iterator<DescriptorFile> descriptorFiles = descriptorReader.readDescriptors();

    int written = 0;
    BufferedWriter bw = new BufferedWriter(new FileWriter("data/out/test.json"));

    // TODO remove after testing
    bw.write(
      "{\"verbose\": " + verbose +
      ", \"archive\": " + archive  +
      ", \"starting with directory\" : \"data/in/" + dir + "\"},\n"
    );

    while (descriptorFiles.hasNext()) {
      DescriptorFile descriptorFile = descriptorFiles.next();
      for (Descriptor descriptor : descriptorFile.getDescriptors()) {
        String jsonDescriptor = null;
        /*
         *    descriptor formats   +    classes
         *
         *    server-descriptor         ServerDescriptor
         *    extra-info                ExtraInfoDescriptor
         *    network-status-consensus  RelayNetworkStatusConsensus
         *    network-status-vote       RelayNetworkStatusVote
         *    bridge-network-status     BridgeNetworkStatus
         *    bridge-server-descriptor  ServerDescriptor
         *    bridge-extra-info         ExtraInfoDescriptor
         *    tordnsel                  ExitList
         *    torperf                   TorperfResult
         */
        if (descriptor instanceof ServerDescriptor) {
          jsonDescriptor = convertCollectorDescriptor((ServerDescriptor) descriptor);
        }
        //  if (descriptor instanceof ExtraInfoDescriptor) {
        //    jsonDescriptor = convertExtraInfoDescriptor((ExtraInfoDescriptor) descriptor);
        //  }
        //  if (descriptor instanceof RelayNetworkStatusConsensus) {
        //    jsonDescriptor = convertRelayNetworkStatusConsensus((RelayNetworkStatusConsensus) descriptor);
        //  }
        //  if (descriptor instanceof RelayNetworkStatusVote) {
        //    jsonDescriptor = convertRelayNetworkStatusVote((RelayNetworkStatusVote) descriptor);
        //  }
        //  if (descriptor instanceof BridgeNetworkStatus) {
        //    jsonDescriptor = convertBridgeNetworkStatus((BridgeNetworkStatus) descriptor);
        //  }
        //  if (descriptor instanceof ExtraInfoDescriptor) {
        //    jsonDescriptor = convertExtraInfoDescriptor((ExtraInfoDescriptor) descriptor);
        //  }
        //  if (descriptor instanceof ExitList) {
        //    jsonDescriptor = convertExitList((ExitList) descriptor);
        //  }
        //  if (descriptor instanceof TorperfResult) {
        //    jsonDescriptor = convertTorperfResult((TorperfResult) descriptor);
        //  }

        if (jsonDescriptor != null) {
          // TODO       remove this -v- comma after testing
          bw.write((written++ > 0 ? ",\n" : "") + jsonDescriptor);
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



  static class JsonDescriptor {}

  static class JsonServerDescriptor extends JsonDescriptor {}
  static class JsonExtraInfo extends JsonDescriptor {}
  static class JsonNetworkStatusConsensus extends JsonDescriptor {}
  static class JsonNetworkStatusVote extends JsonDescriptor {}

  static class JsonBridgeNetworkStatus extends JsonDescriptor {
    String descriptor_type;
    String published; // format YYYY-MM-DD HH:MM:SS
    List<FlagsAndTresholds> flagTresholds;
    List<BridgeStatus> bridge;
  }

  static class JsonBridgeServerDescriptor extends JsonDescriptor {
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

  static class JsonBridgeExtraInfo extends JsonDescriptor {}
  static class JsonTordnsel extends JsonDescriptor {}
  static class JsonTorperf extends JsonDescriptor {}



  /* Take a single CollecTor server descriptor, test which type it is,
   * and return a JSON string representation for it. */
  static String convertCollectorDescriptor(ServerDescriptor desc) {

    String jDesc = null;

    /* Find the @type annotation switch to appropriate JSONdescriptor */
    for (String annotation : desc.getAnnotations()) {

      /*
       *        server-descriptor
       *        bridge-server-descriptor
       */

      if (annotation.startsWith("@type bridge-server-descriptor")) {
        JsonBridgeServerDescriptor json = new JsonBridgeServerDescriptor();
        /* mandatory */
        json.descriptor_type = annotation.substring("@type ".length());
        json.nickname = desc.getNickname();
        json.address = desc.getAddress();
        json.or_port = desc.getOrPort();
        json.socks_port = desc.getSocksPort();
        json.dir_port = desc.getDirPort();
        json.bandwidth_avg = desc.getBandwidthRate();
        json.bandwidth_burst = desc.getBandwidthBurst();
        // test, if there is a key: return 'true' if yes, 'false' otherwise
        json.onion_key = desc.getOnionKey() != null;
        json.signing_key = desc.getSigningKey() != null;
        // verbose testing because of List type
        // first check that the list is not null, then if it's empty
        // (checking for emptiness right away could lead to null pointer exc)
        if (desc.getExitPolicyLines() != null && !desc.getExitPolicyLines().isEmpty()) {
          json.exit_policy = desc.getExitPolicyLines();
        }
        /* optional */
        // can be '-1' if null. in taht case we don't touch it here, leaving the
        // default from the class definition intact
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
        json.platform = desc.getPlatform();
        json.published = dateTimeFormat.format(desc.getPublishedMillis());
        json.fingerprint = desc.getFingerprint().toUpperCase();
        // isHibernating can't return 'null' because it's of type 'boolean'
        // (with little 'b') but it's only present in the collecTor data if it's
        // true. therefor we check for it's existence and include it if it
        // exists. otherwise we leave it alone / to the default value from
        // the class definition above (which is null)
        if (desc.isHibernating()) {
          json.hibernating = desc.isHibernating();
        }
        json.uptime = desc.getUptime();
        json.ipv6_policy = desc.getIpv6DefaultPolicy();
        json.contact = desc.getContact();
        json.family = desc.getFamilyEntries();
        // check for 'null' first because we want to run a method on it
        // and not get a null pointer exception meanwhile
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

        jDesc = ToJson.serialize(json);
      }
    }


    return jDesc;
  }


  /* Convert everything to a JSON string and return that.
   * If flag '-v' (for "verbose") is set serialize null-values too
   * If flag '-a' (for "archive") is set generate .gz archive
   */
  static class ToJson {
    static String serialize(JsonDescriptor json) {
      Gson gson;
      String output;
      if (verbose) {
        gson = new GsonBuilder().serializeNulls().create();
      }
      else {
        gson = new GsonBuilder().create();
      }
      output = gson.toJson(json);
      if (archive) {
        // TODO
      }
      return output;
    }
  }


}
