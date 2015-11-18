package mteam;

/* Import standard Java classes. */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/* Import classes from metrics-lib. */
import org.torproject.descriptor.*;

/* Import classes from Google's Gson library. */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


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

public class ConvertToJson {


  static boolean verbose = false;
  static boolean archive = false;
  static String dir = "";


  /* Read all descriptors in the provided directory and
   * convert them to the appropriate JSON format. */
  public static void main(String[] args) throws IOException {

    /*  optional command line arguments
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
      ", \"starting at directory\" : \"data/in/" + dir + "\"},\n"
    );

    while (descriptorFiles.hasNext()) {
      DescriptorFile descriptorFile = descriptorFiles.next();
      for (Descriptor descriptor : descriptorFile.getDescriptors()) {
        String jsonDescriptor = null;
        //  relays & bridges descriptors
        if (descriptor instanceof ServerDescriptor) {
          jsonDescriptor = JsonServerDescriptor
                  .convert((ServerDescriptor) descriptor);
        }
        //  relays & bridges descriptors - extra info
        if (descriptor instanceof ExtraInfoDescriptor) {
          jsonDescriptor = JsonExtraInfoDescriptor
                  .convert((ExtraInfoDescriptor) descriptor);
        }
        if (descriptor instanceof RelayNetworkStatusConsensus) {
          jsonDescriptor = JsonRelayNetworkStatusConsensus
                  .convert((RelayNetworkStatusConsensus) descriptor);
        }
        if (descriptor instanceof RelayNetworkStatusVote) {
          jsonDescriptor = JsonRelayNetworkStatusVote
                  .convert((RelayNetworkStatusVote) descriptor);
        }
        if (descriptor instanceof BridgeNetworkStatus) {
          jsonDescriptor = JsonBridgeNetworkStatus
                  .convert((BridgeNetworkStatus) descriptor);
        }
        if (descriptor instanceof ExitList) { //tordnsel
          jsonDescriptor = JsonExitList
                  .convert((ExitList) descriptor);
        }
        /* there's a bug in Torperf...
        if (descriptor instanceof TorperfResult) {
          jsonDescriptor = JsonTorperfResult
                  .convert((TorperfResult) descriptor);
        }
        */

        if (jsonDescriptor != null) {
          // TODO        this comma -v- remove after testing
          bw.write((written++ > 0 ? ",\n" : "") + jsonDescriptor);
        }
      }
    }
    bw.close();
  }


  static class JDesc {

    /* generic key/value containers */
    static class StringInt {
      String key;
      int val;
      StringInt(String key, int val) {
        this.key = key;
        this.val = val;
      }
    }
    static class StringLong {
      String key;
      Long val;
      StringLong(String key, Long val) {
        this.key = key;
        this.val = val;
      }
    }

    /* Serialize "read-history" and "write-history" lines. */
    static class BandwidthHistory {
      String date; // format is YYYY-MM-DD HH:MM:SS
      long interval; // seconds
      Collection<Long> bytes;
    }

    /* Convert read or write history */
    static BandwidthHistory convertBandwidthHistory(org.torproject.descriptor.BandwidthHistory hist) {
      BandwidthHistory bandwidthHistory = new BandwidthHistory();
      bandwidthHistory.date = dateTimeFormat.format(hist.getHistoryEndMillis());
      bandwidthHistory.interval = hist.getIntervalLength();
      bandwidthHistory.bytes = hist.getBandwidthValues().values();
      return bandwidthHistory;
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

  }

  static class JsonServerDescriptor extends JDesc {

    /* bridge + relay server descriptor */
    /* mandatory */
    String descriptor_type;
    String published; // format YYYY-MM-DD HH:MM:SS
    String fingerprint; // always upper-case hex
    String nickname; // can be mixed-case
    String address; // changed to lower-case
    int or_port;
    int socks_port; // most likely 0 except for *very* old descriptors
    int dir_port;
    Integer bandwidth_avg;
    Integer bandwidth_burst;
    Boolean onion_key; // usually false b/c sanitization
    Boolean signing_key; // usually false b/c sanitization
    List<String> exit_policy;
    /* optional */
    Integer bandwidth_observed; //  missing in older descriptors!
    // List<AddressPort> or_addresses; // addresses sanitized!
    List<StringInt> or_addresses; // addresses sanitized!
    String platform; //  though usually set
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
    /*  relay only */
    //  Boolean identity_ed25519; // not supported in metrics-lib
    //  Boolean master_key_ed25519;  // not supported in metrics-lib
    //  Boolean onion_key_crosscert; // not supported in metrics-lib
    //  List<KeyAndSig> ntor_onion_key_crosscert; // not supported in metrics-lib
    //  Boolean router_sig_ed25519; // not supported in metrics-lib
    Boolean router_signature;

    /* Take a single server descriptor, test if it is a server-descriptor or a
     * bridge-server-descriptor and return a JSON string representation. */
    static String convert(ServerDescriptor desc) {
      JsonServerDescriptor server = new JsonServerDescriptor();
      for (String annotation : desc.getAnnotations()) {
        server.descriptor_type = annotation.substring("@type ".length());
        if (annotation.startsWith("@type server-descriptor")) {
          //relay specific attributes
          server.router_signature = desc.getRouterSignature() != null;
        }
      }
      server.nickname = desc.getNickname();
      server.address = desc.getAddress();
      server.or_port = desc.getOrPort();
      server.socks_port = desc.getSocksPort();
      server.dir_port = desc.getDirPort();
      server.bandwidth_avg = desc.getBandwidthRate();
      server.bandwidth_burst = desc.getBandwidthBurst();
      // test, if there is a key: return 'true' if yes, 'false' otherwise
      server.onion_key = desc.getOnionKey() != null;
      server.signing_key = desc.getSigningKey() != null;
      // verbose testing because of List type
      // first check that the list is not null, then if it's empty
      // (checking for emptiness right away could lead to null pointer exc)
      if (desc.getExitPolicyLines() != null && !desc.getExitPolicyLines().isEmpty()) {
        server.exit_policy = desc.getExitPolicyLines();
      }
      // can be '-1' if null. in that case we don't touch it here, leaving the
      // default from the class definition intact
      if (desc.getBandwidthObserved() >= 0) {
        server.bandwidth_observed = desc.getBandwidthObserved();
      }
      server.or_addresses = new ArrayList<StringInt>();
      if (desc.getOrAddresses() != null && !desc.getOrAddresses().isEmpty()) {
        for (String orAddress : desc.getOrAddresses()) {
          if (!orAddress.contains(":")) {
            continue;
          }
          int lastColon = orAddress.lastIndexOf(":");
          try {
            int val = Integer.parseInt(orAddress.substring(lastColon + 1));
            server.or_addresses.add(
                    new StringInt(orAddress.substring(0, lastColon), val)
            );
          } catch (NumberFormatException e) {
            continue;
          }
        }
      }
      server.platform = desc.getPlatform();
      server.published = dateTimeFormat.format(desc.getPublishedMillis());
      server.fingerprint = desc.getFingerprint().toUpperCase();
      // isHibernating can't return 'null' because it's of type 'boolean'
      // (with little 'b') but it's only present in the collecTor data if it's
      // true. therefor we check for it's existence and include it if it
      // exists. otherwise we leave it alone / to the default value from
      // the class definition above (which is null)
      if (desc.isHibernating()) {
        server.hibernating = desc.isHibernating();
      }
      server.uptime = desc.getUptime();
      server.ipv6_policy = desc.getIpv6DefaultPolicy();
      server.contact = desc.getContact();
      server.family = desc.getFamilyEntries();
      // check for 'null' first because we want to run a method on it
      // and not get a null pointer exception meanwhile
      if (desc.getReadHistory() != null) {
        server.read_history = convertBandwidthHistory(desc.getReadHistory());
      }
      if (desc.getWriteHistory() != null) {
        server.write_history = convertBandwidthHistory(desc.getWriteHistory());
      }
      server.eventdns = desc.getUsesEnhancedDnsLogic();
      server.caches_extra_info = desc.getCachesExtraInfo();
      if (desc.getExtraInfoDigest() != null) {
        server.extra_info_digest = desc.getExtraInfoDigest().toUpperCase();
      }
      server.hidden_service_dir_versions = desc.getHiddenServiceDirVersions();
      server.link_protocol_versions = desc.getLinkProtocolVersions();
      server.circuit_protocol_versions = desc.getCircuitProtocolVersions();
      server.allow_single_hop_exits = desc.getAllowSingleHopExits();
      server.ntor_onion_key = desc.getNtorOnionKey() != null;
      server.router_digest = desc.getServerDescriptorDigest().toUpperCase();

      return ToJson.serialize(server);
    }
  }

  static class JsonExtraInfoDescriptor extends JDesc {
    String descriptor_type;
    String nickname;
    String fingerprint;
    String published;
    String extra_info_digest;
    BandwidthHistory read_history;
    BandwidthHistory write_history;
    String geoip_db_digest;
    String geoip6_db_digest;
    String geoip_start_time;
    String dirreq_stats_end_date;
    Long dirreq_stats_end_interval;
    List<StringInt> dirreq_v2_ips;
    List<StringInt> dirreq_v3_ips;
    List<StringInt> dirreq_v2_reqs;
    List<StringInt> dirreq_v3_reqs;
    Double dirreq_v2_share;
    Double dirreq_v3_share;
    List<StringInt> dirreq_v2_resp;
    List<StringInt> dirreq_v3_resp;
    List<StringInt> dirreq_v2_direct_dl;
    List<StringInt> dirreq_v3_direct_dl;
    List<StringInt> dirreq_v2_tunneled_dl;
    List<StringInt> dirreq_v3_tunneled_dl;
    BandwidthHistory dirreq_read_history;
    BandwidthHistory dirreq_write_history;
    String entry_stats_end_date;
    Long entry_stats_end_interval;
    List<StringInt> entry_ips;
    String cell_stats_end_date;
    Long cell_stats_end_interval;
    List<Integer> cell_processed_cells;
    List<Double> cell_queued_cells;
    List<Integer> cell_time_in_queue;
    Integer cell_circuits_per_decile;
    ConnBiDirect conn_bi_direct;
    String exit_stats_end_date;
    Long exit_stats_end_interval;
    List<StringLong> exit_kibibytes_written;
    List<StringLong> exit_kibibytes_read;
    List<StringLong> exit_streams_opened;
    StringInt hidserv_stats_end;
    Object hidserv_rend_relayed_cells;
    Object hidserv_dir_onions_seen;
    List<String> transport;
    Boolean router_sig_ed25519;
    Boolean router_signature;
    /* only bridges */
    List<StringInt> geoip_client_origins;
    String bridge_stats_end_date;
    Long bridge_stats_end_interval;
    List<StringInt> bridge_ips;
    List<StringInt> bridge_ip_versions;
    List<StringInt> bridge_ip_transports;

    static class ConnBiDirect {
      String date;
      Long interval;
      Integer below;
      Integer read;
      Integer write;
      Integer both;
    }

    static String convert(ExtraInfoDescriptor desc) {
      JsonExtraInfoDescriptor extra = new JsonExtraInfoDescriptor();
      for (String annotation : desc.getAnnotations()) {
        extra.descriptor_type = annotation.substring("@type ".length());
        if (annotation.startsWith("@type bridge-extra-info")) {
          //bridge specific attributes
          if (desc.getGeoipClientOrigins() != null && !desc.getGeoipClientOrigins().isEmpty()) {
            extra.geoip_client_origins = new ArrayList<StringInt>();
            SortedMap<String, Integer> origins = desc.getGeoipClientOrigins();
            for (Map.Entry<String, Integer> kv : origins.entrySet()) {
              extra.geoip_client_origins.add(new StringInt(kv.getKey(), kv.getValue()));
            }
          }
          if (desc.getBridgeStatsEndMillis() >= 0) {
            extra.bridge_stats_end_date = dateTimeFormat.format(desc.getBridgeStatsEndMillis());
          }
          if (desc.getBridgeStatsIntervalLength() >= 0) {
            extra.bridge_stats_end_interval = desc.getBridgeStatsIntervalLength();
          }
          if (desc.getBridgeIps() != null && !desc.getBridgeIps().isEmpty()) {
            extra.bridge_ips = new ArrayList<StringInt>();
            SortedMap<String, Integer> b_ips = desc.getBridgeIps();
            for (Map.Entry<String, Integer> kv : b_ips.entrySet()) {
              extra.bridge_ips.add(new StringInt(kv.getKey(), kv.getValue()));
            }
          }
          if (desc.getBridgeIpVersions() != null && !desc.getBridgeIpVersions().isEmpty()) {
            extra.bridge_ip_versions = new ArrayList<StringInt>();
            SortedMap<String, Integer> b_ip_v = desc.getBridgeIpVersions();
            for (Map.Entry<String, Integer> kv : b_ip_v.entrySet()) {
              extra.bridge_ip_versions.add(new StringInt(kv.getKey(), kv.getValue()));
            }
          }
          if (desc.getBridgeIpTransports() != null && !desc.getBridgeIpTransports().isEmpty()) {
            extra.bridge_ip_transports = new ArrayList<StringInt>();
            SortedMap<String, Integer> b_ip_t = desc.getBridgeIpTransports();
            for (Map.Entry<String, Integer> kv : b_ip_t.entrySet()) {
              extra.bridge_ip_transports.add(new StringInt(kv.getKey(), kv.getValue()));
            }
          }
        } // end bridge specific attributes
      }
      extra.nickname = desc.getNickname();
      extra.fingerprint = desc.getFingerprint().toUpperCase();
      extra.published = dateTimeFormat.format(desc.getPublishedMillis());
      extra.extra_info_digest = desc.getExtraInfoDigest();
      if (desc.getReadHistory() != null) {
        extra.read_history = convertBandwidthHistory(desc.getReadHistory());
      }
      if (desc.getWriteHistory() != null) {
        extra.write_history = convertBandwidthHistory(desc.getWriteHistory());
      }
      extra.geoip_db_digest = desc.getGeoipDbDigest();
      extra.geoip6_db_digest = desc.getGeoip6DbDigest();
      if (desc.getGeoipStartTimeMillis() >= 0) {
        extra.geoip_start_time = dateTimeFormat.format(desc.getGeoipStartTimeMillis());
      }
      if (desc.getDirreqStatsEndMillis() >= 0) {
        extra.dirreq_stats_end_date = dateTimeFormat.format(desc.getDirreqStatsEndMillis());
      }
      if (desc.getDirreqStatsIntervalLength() >= 0) {
        extra.dirreq_stats_end_interval = desc.getDirreqStatsIntervalLength();
      }
      if (desc.getDirreqV2Ips() != null && !desc.getDirreqV2Ips().isEmpty()) {
        extra.dirreq_v2_ips = new ArrayList<StringInt>();
        SortedMap<String, Integer> v2_ips = desc.getDirreqV2Ips();
        for (Map.Entry<String, Integer> kv : v2_ips.entrySet()) {
          extra.dirreq_v2_ips.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getDirreqV3Ips() != null && !desc.getDirreqV3Ips().isEmpty()) {
        extra.dirreq_v3_ips = new ArrayList<StringInt>();
        SortedMap<String, Integer> v3_ips = desc.getDirreqV3Ips();
        for (Map.Entry<String, Integer> kv : v3_ips.entrySet()) {
          extra.dirreq_v3_ips.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getDirreqV2Reqs() != null && !desc.getDirreqV2Reqs().isEmpty()) {
        extra.dirreq_v2_reqs = new ArrayList<StringInt>();
        SortedMap<String, Integer> v2_reqs = desc.getDirreqV2Reqs();
        for (Map.Entry<String, Integer> kv : v2_reqs.entrySet()) {
          extra.dirreq_v2_reqs.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getDirreqV3Reqs() != null && !desc.getDirreqV3Reqs().isEmpty()) {
        extra.dirreq_v3_reqs = new ArrayList<StringInt>();
        SortedMap<String, Integer> v3_reqs = desc.getDirreqV3Reqs();
        for (Map.Entry<String, Integer> kv : v3_reqs.entrySet()) {
          extra.dirreq_v3_reqs.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getDirreqV2Share() >= 0) {
        extra.dirreq_v2_share = desc.getDirreqV2Share();
      }
      if (desc.getDirreqV3Share() >= 0) {
        extra.dirreq_v3_share = desc.getDirreqV3Share();
      }
      if (desc.getDirreqV2Resp() != null && !desc.getDirreqV2Resp().isEmpty()) {
        extra.dirreq_v2_resp = new ArrayList<StringInt>();
        SortedMap<String, Integer> v2_resp = desc.getDirreqV2Resp();
        for (Map.Entry<String, Integer> kv : v2_resp.entrySet()) {
          extra.dirreq_v2_resp.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getDirreqV3Resp() != null && !desc.getDirreqV3Resp().isEmpty()) {
        extra.dirreq_v3_resp = new ArrayList<StringInt>();
        SortedMap<String, Integer> v3_resp = desc.getDirreqV3Resp();
        for (Map.Entry<String, Integer> kv : v3_resp.entrySet()) {
          extra.dirreq_v3_resp.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getDirreqV2DirectDl() != null && !desc.getDirreqV2DirectDl().isEmpty()) {
        extra.dirreq_v2_direct_dl = new ArrayList<StringInt>();
        SortedMap<String, Integer> v2_direct = desc.getDirreqV2DirectDl();
        for (Map.Entry<String, Integer> kv : v2_direct.entrySet()) {
          extra.dirreq_v2_direct_dl.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getDirreqV3DirectDl() != null && !desc.getDirreqV3DirectDl().isEmpty()) {
        extra.dirreq_v3_direct_dl = new ArrayList<StringInt>();
        SortedMap<String, Integer> v3_direct = desc.getDirreqV3DirectDl();
        for (Map.Entry<String, Integer> kv : v3_direct.entrySet()) {
          extra.dirreq_v3_direct_dl.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getDirreqV2TunneledDl() != null && !desc.getDirreqV2TunneledDl().isEmpty()) {
        extra.dirreq_v2_tunneled_dl = new ArrayList<StringInt>();
        SortedMap<String, Integer> v2_tunneled = desc.getDirreqV2TunneledDl();
        for (Map.Entry<String, Integer> kv : v2_tunneled.entrySet()) {
          extra.dirreq_v2_tunneled_dl.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getDirreqV3TunneledDl() != null && !desc.getDirreqV3TunneledDl().isEmpty()) {
        extra.dirreq_v3_tunneled_dl = new ArrayList<StringInt>();
        SortedMap<String, Integer> v3_tunneled = desc.getDirreqV3TunneledDl();
        for (Map.Entry<String, Integer> kv : v3_tunneled.entrySet()) {
          extra.dirreq_v3_tunneled_dl.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getDirreqReadHistory() != null) {
        extra.dirreq_read_history =
                convertBandwidthHistory(desc.getDirreqReadHistory());
      }
      if (desc.getDirreqWriteHistory() != null) {
        extra.dirreq_write_history =
                convertBandwidthHistory(desc.getDirreqWriteHistory());
      }
      if (desc.getEntryStatsEndMillis() >= 0) {
        extra.entry_stats_end_date = dateTimeFormat.format(desc.getEntryStatsEndMillis());
      }
      if (desc.getEntryStatsIntervalLength() >= 0) {
        extra.entry_stats_end_interval = desc.getEntryStatsIntervalLength();
      }
      if (desc.getEntryIps() != null && !desc.getEntryIps().isEmpty()) {
        extra.entry_ips = new ArrayList<StringInt>();
        SortedMap<String, Integer> ips = desc.getEntryIps();
        for (Map.Entry<String, Integer> kv : ips.entrySet()) {
          extra.entry_ips.add(new StringInt(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getCellStatsEndMillis() >= 0) {
        extra.cell_stats_end_date = dateTimeFormat.format(desc.getCellStatsEndMillis());
      }
      if (desc.getCellStatsIntervalLength() >= 0) {
        extra.cell_stats_end_interval = desc.getCellStatsIntervalLength();
      }
      extra.cell_processed_cells = desc.getCellProcessedCells();
      extra.cell_queued_cells = desc.getCellQueuedCells();
      extra.cell_time_in_queue = desc.getCellTimeInQueue();
      if (desc.getCellCircuitsPerDecile() >= 0) {
        extra.cell_circuits_per_decile = desc.getCellCircuitsPerDecile();
      }
      if (desc.getConnBiDirectStatsEndMillis() >= 0) {
        extra.conn_bi_direct = new ConnBiDirect();
        extra.conn_bi_direct.date = dateTimeFormat.format(desc.getConnBiDirectStatsEndMillis());
        if (desc.getConnBiDirectStatsIntervalLength() >= 0) {
          extra.conn_bi_direct.interval = desc.getConnBiDirectStatsIntervalLength();
        }
        if (desc.getConnBiDirectBelow() >= 0) {
          extra.conn_bi_direct.below = desc.getConnBiDirectBelow();
        }
        if (desc.getConnBiDirectRead() >= 0) {
          extra.conn_bi_direct.read = desc.getConnBiDirectRead();
        }
        if (desc.getConnBiDirectWrite() >= 0) {
          extra.conn_bi_direct.write = desc.getConnBiDirectWrite();
        }
        if (desc.getConnBiDirectBoth() >= 0) {
          extra.conn_bi_direct.both = desc.getConnBiDirectBoth();
        }
      }
      if (desc.getExitStatsEndMillis() >= 0) {
        extra.exit_stats_end_date = dateTimeFormat.format(desc.getExitStatsEndMillis());
      }
      if (desc.getExitStatsIntervalLength() >= 0) {
        extra.exit_stats_end_interval = desc.getExitStatsIntervalLength();
      }
      if (desc.getExitKibibytesWritten() != null && !desc.getExitKibibytesWritten().isEmpty()) {
        extra.exit_kibibytes_written = new ArrayList<StringLong>();
        SortedMap<String, Long> written = desc.getExitKibibytesWritten();
        for (Map.Entry<String, Long> kv : written.entrySet()) {
          extra.exit_kibibytes_written.add(new StringLong(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getExitKibibytesRead() != null && !desc.getExitKibibytesRead().isEmpty()) {
        extra.exit_kibibytes_read = new ArrayList<StringLong>();
        SortedMap<String, Long> read = desc.getExitKibibytesRead();
        for (Map.Entry<String, Long> kv : read.entrySet()) {
          extra.exit_kibibytes_read.add(new StringLong(kv.getKey(), kv.getValue()));
        }
      }
      if (desc.getExitStreamsOpened() != null && !desc.getExitStreamsOpened().isEmpty()) {
        extra.exit_streams_opened = new ArrayList<StringLong>();
        SortedMap<String, Long> opened = desc.getExitStreamsOpened();
        for (Map.Entry<String, Long> kv : opened.entrySet()) {
          extra.exit_streams_opened.add(new StringLong(kv.getKey(), kv.getValue()));
        }
      }
      //  extra.hidserv_stats_end = new StringInt(); // no getter in metrics-lib
      //  extra.hidserv_rend_relayed_cells = new Object(); // no getter in metrics-lib
      //  extra.hidserv_dir_onions_seen = new Object(); // no getter in metrics-lib
      extra.transport = desc.getTransports();
      //  extra.router_sig_ed25519 = false; // no getter in metrics-lib
      //  extra.router_signature = false; // no getter in metrics-lib
      return ToJson.serialize(extra);
    }
  }

  static class JsonRelayNetworkStatusConsensus extends JDesc {
    String descriptor_type;
    //  String published;  TODO KL can we fake/"derive" a published date?
    //  NO String fingerprint;
    //  TODO

    static String convert(RelayNetworkStatusConsensus desc) {
      JsonRelayNetworkStatusConsensus consensus = new JsonRelayNetworkStatusConsensus();
      for (String annotation : desc.getAnnotations()) {
        consensus.descriptor_type = annotation.substring("@type ".length());
      }
      //  status.published = dateTimeFormat.format(desc.getPublishedMillis());
      //  TODO
      return ToJson.serialize(consensus);
    }
  }

  static class JsonRelayNetworkStatusVote extends JDesc {
    String descriptor_type;
    String published;
    //  String fingerprint;
    //  TODO  ¡¡¡procrastinate!!! this is the largest of them all...
    static String convert(RelayNetworkStatusVote desc) {
      JsonRelayNetworkStatusVote vote = new JsonRelayNetworkStatusVote();
      for (String annotation : desc.getAnnotations()) {
        vote.descriptor_type = annotation.substring("@type ".length());
      }
      vote.published = dateTimeFormat.format(desc.getPublishedMillis());
      //  vote.fingerprint = desc.getFingerprint().toUpperCase();
      //  TODO
      return ToJson.serialize(vote);
    }
  }

  static class JsonBridgeNetworkStatus extends JDesc {
    String descriptor_type;
    String published;
    //  NO String fingerprint;
    List<StringInt> flagTresholds; // no getter in metrics-lib
    SortedMap<String, NetworkStatusEntry> bridges;  // TODO
      /* Return status entries, one for each contained bridge. */
      // public SortedMap<String, NetworkStatusEntry> getStatusEntries();


    static class BridgeStatus {
      List<R> r; // bridge description
      List<String> s; // flags
      List<W> w; // bandwidths
      List<String> p; // policies
      String a; // additional IP adress and port
    }

    /* Helper to BridgeNetworkStatus */
    static class R {}

    /* Helper to BridgeNetworkStatus */
    static class W {}


    static String convert(BridgeNetworkStatus desc) {
      JsonBridgeNetworkStatus status = new JsonBridgeNetworkStatus();
      for (String annotation : desc.getAnnotations()) {
        status.descriptor_type = annotation.substring("@type ".length());
      }
      status.published = dateTimeFormat.format(desc.getPublishedMillis());
      //  TODO
      // status.bridges;
      return ToJson.serialize(status);
    }
  }

  static class JsonExitList extends JDesc {
    String descriptor_type;
    Long downloaded;
    List<Entry> relays;
    static class Entry {
      String fingerprint;
      String published;
      String last_status;
      Exit exit_adress;
    }
    static class Exit {
      String ip;
      String date;
    }

    static String convert(ExitList desc) {
      JsonExitList tordnsel = new JsonExitList();
      for (String annotation : desc.getAnnotations()) {
        tordnsel.descriptor_type = annotation.substring("@type ".length());
      }
      tordnsel.downloaded = desc.getDownloadedMillis();
      if (desc.getExitListEntries() != null && !desc.getExitListEntries().isEmpty()) {
        tordnsel.relays = new ArrayList<Entry>();
        for(ExitListEntry entry : desc.getExitListEntries() ) {
          Entry en = new Entry();
          en.fingerprint = entry.getFingerprint();
          en.published = dateTimeFormat.format(entry.getPublishedMillis());
          en.last_status = dateTimeFormat.format(entry.getLastStatusMillis());
          en.exit_adress = new Exit();
          en.exit_adress.ip = entry.getExitAddress();
          en.exit_adress.date = dateTimeFormat.format(entry.getScanMillis());
          tordnsel.relays.add(en);
        }
      }
      return ToJson.serialize(tordnsel);
    }
  }

  static class JsonTorperfResult extends JDesc {
    String descriptor_type;
    String source;
    Integer filesize;
    String start;
    String socket;
    String connect;
    String negotiate;
    String request;
    String response;
    String datarequest;
    String dataresponse;
    String datacomplete;
    Integer writebytes;
    Integer readbytes;
    Boolean didtimeout;
    Long dataperc10;
    Long dataperc20;
    Long dataperc30;
    Long dataperc40;
    Long dataperc50;
    Long dataperc60;
    Long dataperc70;
    Long dataperc80;
    Long dataperc90;
    String launch;
    String used_at;
    List<String> path;
    List<Long> buildtimes;
    String timeout;
    Double quantile;
    Integer circ_id;
    Integer used_by;

    static String convert(TorperfResult desc) {
      JsonTorperfResult torperf = new JsonTorperfResult();
      for (String annotation : desc.getAnnotations()) {
        torperf.descriptor_type = annotation.substring("@type ".length());
      }
      torperf.source = desc.getSource();
      torperf.filesize = desc.getFileSize();
      torperf.start = dateTimeFormat.format(desc.getStartMillis());
      torperf.socket = dateTimeFormat.format(desc.getSocketMillis());
      torperf.connect = dateTimeFormat.format(desc.getConnectMillis());
      torperf.negotiate = dateTimeFormat.format(desc.getNegotiateMillis());
      torperf.request = dateTimeFormat.format(desc.getRequestMillis());
      torperf.response = dateTimeFormat.format(desc.getResponseMillis());
      torperf.datarequest = dateTimeFormat.format(desc.getDataRequestMillis());
      torperf.dataresponse = dateTimeFormat.format(desc.getDataResponseMillis());
      torperf.datacomplete = dateTimeFormat.format(desc.getDataCompleteMillis());
      torperf.writebytes = desc.getWriteBytes();
      torperf.readbytes = desc.getReadBytes();
      torperf.didtimeout = desc.didTimeout();
      if (desc.getDataPercentiles() != null && !desc.getDataPercentiles().isEmpty()) {
        torperf.dataperc10 = desc.getDataPercentiles().get("10");
        torperf.dataperc20 = desc.getDataPercentiles().get("20");
        torperf.dataperc30 = desc.getDataPercentiles().get("30");
        torperf.dataperc40 = desc.getDataPercentiles().get("40");
        torperf.dataperc50 = desc.getDataPercentiles().get("50");
        torperf.dataperc60 = desc.getDataPercentiles().get("60");
        torperf.dataperc70 = desc.getDataPercentiles().get("70");
        torperf.dataperc80 = desc.getDataPercentiles().get("80");
        torperf.dataperc90 = desc.getDataPercentiles().get("90");
      }
      if (desc.getLaunchMillis() >= 0) {
        torperf.launch = dateTimeFormat.format(desc.getLaunchMillis());
      }
      if (desc.getUsedAtMillis() >= 0) {
        torperf.used_at = dateTimeFormat.format(desc.getUsedAtMillis());
      }
      if (desc.getPath() != null && !desc.getPath().isEmpty()) {
        torperf.path = desc.getPath();
      }
      if (desc.getBuildTimes() != null && !desc.getBuildTimes().isEmpty()) {
        torperf.buildtimes = desc.getBuildTimes();
      }
      if (desc.getTimeout() >= 0) {
        torperf.timeout = dateTimeFormat.format(desc.getTimeout());
      }
      if (desc.getQuantile() >= 0) {
        torperf.quantile = desc.getQuantile();
      }
      if (desc.getCircId() >= 0) {
        torperf.circ_id = desc.getCircId();
      }
      if (desc.getUsedBy() >= 0) {
        torperf.used_by = desc.getUsedBy();
      }
      return ToJson.serialize(torperf);
    }
  }


  /* Convert everything to a JSON string and return that.
   * If flag '-v' (for "verbose") is set serialize null-values too
   * If flag '-a' (for "archive") is set generate gzip compressed archive
   */
  static class ToJson {
    static String serialize(JDesc json) {
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
