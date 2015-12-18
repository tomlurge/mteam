package mteam;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/*  metrics-lib  */
import org.torproject.descriptor.*;


public class ConvertToJson {

  /*  argument defaults  */
  static boolean verbose = false; // defaults to 'false'
  static boolean compress = true; // defaults to 'true'
  static String dir = "";

  /*  Read all descriptors in the provided directory and
   *  convert them to the appropriate JSON format.  */
  public static void main(String[] args) throws IOException {

    /*  optional command line arguments
     *    -v                verbose: emit attributes with null values
     *    -u                uncompressed: do not generate .gz archive
     *    -t                testing: verbose and uncompressed
     *    <directory name>  scan only a given subdirectory of data/in
     *
     */
    for (String arg : args) {
      if (arg.equals("-v")) verbose = true;
      else if (arg.equals("-u")) compress = false;
      else if (arg.equals("-t")) {
        verbose = true;
        compress = false;
      }
      else dir = arg;
    }

    DescriptorReader descriptorReader = DescriptorSourceFactory.createDescriptorReader();
    descriptorReader.addDirectory(new File("data/in/" + dir));
    descriptorReader.setMaxDescriptorFilesInQueue(5);
    Iterator<DescriptorFile> descriptorFiles = descriptorReader.readDescriptors();

    int written = 0;
    String outputPath = "data/out/";
    String outputName = "result.json";
    Writer JsonWriter;
    if (compress) {
      JsonWriter = new OutputStreamWriter(new GZIPOutputStream(
              new FileOutputStream(outputPath + outputName + ".gz")));
    }
    else {
      JsonWriter = new FileWriter(outputPath + outputName);
    }
    BufferedWriter bw = new BufferedWriter(JsonWriter);

    /*  TODO remove after testing */
      bw.write(
        "{\"verbose\": " + verbose +
        ", \"compress\": " + compress +
        ", \"starting at directory\" : \"data/in/" + dir + "\"},\n"
      );

    while (descriptorFiles.hasNext()) {
      DescriptorFile descriptorFile = descriptorFiles.next();
      if(null != descriptorFile.getException()){
        System.err.print(descriptorFile.getException()
                + "\n    in " + descriptorFile.getFileName() + "\n");
      }

      for (Descriptor descriptor : descriptorFile.getDescriptors()) {
        String jsonDescriptor = null;

        //  relay descriptors
        if (descriptor instanceof RelayServerDescriptor) {
          jsonDescriptor = JsonRelayServerDescriptor
                  .convert((RelayServerDescriptor) descriptor);
        }
        //  bridge descriptors
        if (descriptor instanceof BridgeServerDescriptor) {
          jsonDescriptor = JsonBridgeServerDescriptor
                  .convert((BridgeServerDescriptor) descriptor);
        }
        //  relays extra info descriptors
        if (descriptor instanceof RelayExtraInfoDescriptor) {
          jsonDescriptor = JsonRelayExtraInfoDescriptor
                  .convert((RelayExtraInfoDescriptor) descriptor);
        }
        //  bridge extra info descriptors
        if (descriptor instanceof BridgeExtraInfoDescriptor) {
          jsonDescriptor = JsonBridgeExtraInfoDescriptor
                  .convert((BridgeExtraInfoDescriptor) descriptor);
        }
        //  network status consensus
        if (descriptor instanceof RelayNetworkStatusConsensus) {
          jsonDescriptor = JsonRelayNetworkStatusConsensus
                  .convert((RelayNetworkStatusConsensus) descriptor);
        }
        //  network status vote
        if (descriptor instanceof RelayNetworkStatusVote) {
          jsonDescriptor = JsonRelayNetworkStatusVote
                  .convert((RelayNetworkStatusVote) descriptor);
        }
        //  bridge network status
        if (descriptor instanceof BridgeNetworkStatus) {
          jsonDescriptor = JsonBridgeNetworkStatus
                  .convert((BridgeNetworkStatus) descriptor);
        }
        //  tordnsel
        if (descriptor instanceof ExitList) {
          jsonDescriptor = JsonExitList
                  .convert((ExitList) descriptor);
        }
        //  torperf
        if (descriptor instanceof TorperfResult) {
          jsonDescriptor = JsonTorperfResult
                  .convert((TorperfResult) descriptor);
        }

        if (!descriptor.getUnrecognizedLines().isEmpty()) {
          System.err.println("Unrecognized lines in "
                  + descriptorFile.getFileName() + ":");
          System.err.println(descriptor.getUnrecognizedLines());
          continue;
        }
        if (jsonDescriptor != null) {
          // TODO remove this comma -v- after testing
          bw.write((written++ > 0 ? ",\n" : "") + jsonDescriptor);
        }
      }
    }
    bw.close();
  }

  //  all descriptors
  static class JsonDescriptor {

    /*  generic key/value objects for verbose output  */
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
    static class StringDouble {
      String key;
      Double val;
      StringDouble(String key, Double val) {
        this.key = key;
        this.val = val;
      }
    }

    /*  Serialize "read-history" and "write-history" lines  */
    static class BandwidthHistory {
      String date; // format is YYYY-MM-DD HH:MM:SS
      long interval; // seconds
      Collection<Long> bytes;
    }
    /*  Convert read or write history  */
    static BandwidthHistory convertBandwidthHistory(org.torproject.descriptor.BandwidthHistory hist) {
      BandwidthHistory bandwidthHistory = new BandwidthHistory();
      bandwidthHistory.date = dateTimeFormat.format(hist.getHistoryEndMillis());
      bandwidthHistory.interval = hist.getIntervalLength();
      bandwidthHistory.bytes = hist.getBandwidthValues().values();
      return bandwidthHistory;
    }
    /*  Date/time formatter  */
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

  //  relay descriptors
  static class JsonRelayServerDescriptor extends JsonDescriptor {
    String descriptor_type;
    Boolean router_signature;
    Boolean identity_ed25519;                       // getIdentityEd25519
    Boolean master_key_ed25519;                     // getMasterKeyEd25519
    Boolean onion_key_crosscert;                    // getOnionKeyCrosscert
    CrossCert ntor_onion_key_crosscert;
    static class CrossCert {
      Boolean cert;                                 // getNtorOnionKeyCrosscert
      Integer bit;                                  // getNtorOnionKeyCrosscertSign
    }
    String published;   // format YYYY-MM-DD HH:MM:SS
    Boolean router_sig_ed25519;                     // getRouterSignatureEd25519
    String fingerprint;  // always upper-case hex
    String nickname;  // can be mixed-case
    String address;  // changed to lower-case
    int or_port;
    int socks_port;  // most likely 0 except for *very* old descriptors
    int dir_port;
    Integer bandwidth_avg;
    Integer bandwidth_burst;
    Boolean onion_key;  // usually false b/c sanitization
    Boolean signing_key;  // usually false b/c sanitization
    List<String> exit_policy;
    Integer bandwidth_observed;  // missing in older descriptors!
    List<StringInt> or_addresses;  // addresses sanitized!
    String platform;  // though usually set
    Boolean hibernating;
    Long uptime;  // though usually set
    String ipv6_policy;
    String contact;
    List<String> family;  // apparently not used at all
    BandwidthHistory read_history;
    BandwidthHistory write_history;
    Boolean eventdns;
    Boolean caches_extra_info;
    String extra_info_digest;  // upper-case hex
    Boolean extra_info_digest_sha256;               // getExtraInfoDigestSha256
    List<Integer> hidden_service_dir_versions;
    List<Integer> link_protocol_versions;
    List<Integer> circuit_protocol_versions;
    Boolean allow_single_hop_exits;
    Boolean ntor_onion_key;
    String router_digest;  // upper-case hex
    Boolean router_digest_sha256;                    // getServerDescriptorDigestSha256

    static String convert(ServerDescriptor desc) {
      JsonRelayServerDescriptor relay = new JsonRelayServerDescriptor();
      for (String annotation : desc.getAnnotations()) {
        relay.descriptor_type = annotation.substring("@type ".length());
      }
      relay.router_signature = desc.getRouterSignature() != null;
      relay.identity_ed25519 = desc.getIdentityEd25519() != null;
      relay.master_key_ed25519 = desc.getMasterKeyEd25519() != null;
      relay.onion_key_crosscert = desc.getOnionKeyCrosscert() != null;
      relay.ntor_onion_key_crosscert = new CrossCert();
      relay.ntor_onion_key_crosscert.cert = desc.getNtorOnionKeyCrosscert() != null;
      relay.ntor_onion_key_crosscert.bit = desc.getNtorOnionKeyCrosscertSign();
      relay.router_sig_ed25519 = desc.getRouterSignatureEd25519() != null;
      relay.nickname = desc.getNickname();
      relay.address = desc.getAddress();
      relay.or_port = desc.getOrPort();
      relay.socks_port = desc.getSocksPort();
      relay.dir_port = desc.getDirPort();
      relay.bandwidth_avg = desc.getBandwidthRate();
      relay.bandwidth_burst = desc.getBandwidthBurst();
      //  test, if there is a key: return 'true' if yes, 'false' otherwise
      relay.onion_key = desc.getOnionKey() != null;
      relay.signing_key = desc.getSigningKey() != null;
      //  verbose testing because of List type
      //  first check that the list is not null, then if it's empty
      //  (checking for emptiness right away could lead to null pointer exc)
      if (desc.getExitPolicyLines() != null && !desc.getExitPolicyLines().isEmpty()) {
        relay.exit_policy = desc.getExitPolicyLines();
      }
      //  can be '-1' if null. in that case we don't touch it here, leaving the
      //  default from the class definition intact
      if (desc.getBandwidthObserved() >= 0) {
        relay.bandwidth_observed = desc.getBandwidthObserved();
      }
      if (desc.getOrAddresses() != null && !desc.getOrAddresses().isEmpty()) {
        relay.or_addresses = new ArrayList<>();
        for (String orAddress : desc.getOrAddresses()) {
          if (!orAddress.contains(":")) {
            continue;
          }
          int lastColon = orAddress.lastIndexOf(":");
          try {
            int val = Integer.parseInt(orAddress.substring(lastColon + 1));
            relay.or_addresses.add(
                    new StringInt(orAddress.substring(0, lastColon), val)
            );
          } catch (NumberFormatException e) {
            continue;
          }
        }
        /*  TODO the above solution always returns verbose results
                 but the non-verbose 'else' clause below needs review & testing

        if(verbose) {
          ArrayList<StringInt> verboseOR = new ArrayList<StringInt>();
          server.or_addresses = new ArrayList<StringInt>();
          for (String orAddress : desc.getOrAddresses()) {
            if (!orAddress.contains(":")) {
              continue;
            }
            int lastColon = orAddress.lastIndexOf(":");
            try {
              int val = Integer.parseInt(orAddress.substring(lastColon + 1));
              verboseOR.add(
                      new StringInt(orAddress.substring(0, lastColon), val)
              );
            } catch (NumberFormatException e) {
              continue;
            }
          }
          server.or_addresses = verboseOR;
        } else {
          server.or_addresses = desc.getOrAddresses();
        }

        // don't forget to define 'or_addresses' as type 'Object' above

        */
      }
      relay.platform = desc.getPlatform();
      relay.published = dateTimeFormat.format(desc.getPublishedMillis());
      relay.fingerprint = desc.getFingerprint().toUpperCase();
      //  isHibernating can't return 'null' because it's of type 'boolean'
      //  (with little 'b') but it's only present in the collecTor data if it's
      //  true. therefor we check for it's existence and include it if it
      //  exists. otherwise we leave it alone / to the default value from
      //  the class definition above (which is null)
      if (desc.isHibernating()) {
        relay.hibernating = desc.isHibernating();
      }
      relay.uptime = desc.getUptime();
      relay.ipv6_policy = desc.getIpv6DefaultPolicy();
      relay.contact = desc.getContact();
      relay.family = desc.getFamilyEntries();
      //  check for 'null' first because we want to run a method on it
      //  and not get a null pointer exception meanwhile
      if (desc.getReadHistory() != null) {
        relay.read_history = convertBandwidthHistory(desc.getReadHistory());
      }
      if (desc.getWriteHistory() != null) {
        relay.write_history = convertBandwidthHistory(desc.getWriteHistory());
      }
      relay.eventdns = desc.getUsesEnhancedDnsLogic();
      relay.caches_extra_info = desc.getCachesExtraInfo();
      if (desc.getExtraInfoDigest() != null) {
        relay.extra_info_digest = desc.getExtraInfoDigest().toUpperCase();
      }
      relay.extra_info_digest_sha256 = desc.getExtraInfoDigestSha256() != null;
      relay.hidden_service_dir_versions = desc.getHiddenServiceDirVersions();
      relay.link_protocol_versions = desc.getLinkProtocolVersions();
      relay.circuit_protocol_versions = desc.getCircuitProtocolVersions();
      relay.allow_single_hop_exits = desc.getAllowSingleHopExits();
      relay.ntor_onion_key = desc.getNtorOnionKey() != null;
      relay.router_digest = desc.getServerDescriptorDigest().toUpperCase();
      relay.router_digest_sha256 = desc.getServerDescriptorDigest() != null;

      return ToJson.serialize(relay);
    }
  }

  //  bridge descriptors
  static class JsonBridgeServerDescriptor extends JsonDescriptor {
    String descriptor_type;
    String published;   // format YYYY-MM-DD HH:MM:SS
    String fingerprint;  // always upper-case hex
    String nickname;  // can be mixed-case
    String address;  // changed to lower-case
    int or_port;
    int socks_port;  // most likely 0 except for *very* old descriptors
    int dir_port;
    Integer bandwidth_avg;
    Integer bandwidth_burst;
    Boolean onion_key;  // usually false b/c sanitization
    Boolean signing_key;  // usually false b/c sanitization
    List<String> exit_policy;
    Integer bandwidth_observed;  // missing in older descriptors!
    List<StringInt> or_addresses;  // addresses sanitized!
    String platform;  // though usually set
    Boolean hibernating;
    Long uptime;  // though usually set
    String ipv6_policy;
    String contact;
    List<String> family;  // apparently not used at all
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
    String router_digest;  // upper-case hex

    static String convert(ServerDescriptor desc) {
      JsonBridgeServerDescriptor bridge = new JsonBridgeServerDescriptor();
      for (String annotation : desc.getAnnotations()) {
        bridge.descriptor_type = annotation.substring("@type ".length());
      }
      bridge.nickname = desc.getNickname();
      bridge.address = desc.getAddress();
      bridge.or_port = desc.getOrPort();
      bridge.socks_port = desc.getSocksPort();
      bridge.dir_port = desc.getDirPort();
      bridge.bandwidth_avg = desc.getBandwidthRate();
      bridge.bandwidth_burst = desc.getBandwidthBurst();
      //  test, if there is a key: return 'true' if yes, 'false' otherwise
      bridge.onion_key = desc.getOnionKey() != null;
      bridge.signing_key = desc.getSigningKey() != null;
      //  verbose testing because of List type
      //  first check that the list is not null, then if it's empty
      //  (checking for emptiness right away could lead to null pointer exc)
      if (desc.getExitPolicyLines() != null && !desc.getExitPolicyLines().isEmpty()) {
        bridge.exit_policy = desc.getExitPolicyLines();
      }
      //  can be '-1' if null. in that case we don't touch it here, leaving the
      //  default from the class definition intact
      if (desc.getBandwidthObserved() >= 0) {
        bridge.bandwidth_observed = desc.getBandwidthObserved();
      }
      if (desc.getOrAddresses() != null && !desc.getOrAddresses().isEmpty()) {
        bridge.or_addresses = new ArrayList<>();
        for (String orAddress : desc.getOrAddresses()) {
          if (!orAddress.contains(":")) {
            continue;
          }
          int lastColon = orAddress.lastIndexOf(":");
          try {
            int val = Integer.parseInt(orAddress.substring(lastColon + 1));
            bridge.or_addresses.add(
                    new StringInt(orAddress.substring(0, lastColon), val)
            );
          } catch (NumberFormatException e) {
            continue;
          }
        }
        /*  TODO the above solution always returns verbose results
                 but the non-verbose 'else' clause below needs review & testing

        if(verbose) {
          ArrayList<StringInt> verboseOR = new ArrayList<StringInt>();
          server.or_addresses = new ArrayList<StringInt>();
          for (String orAddress : desc.getOrAddresses()) {
            if (!orAddress.contains(":")) {
              continue;
            }
            int lastColon = orAddress.lastIndexOf(":");
            try {
              int val = Integer.parseInt(orAddress.substring(lastColon + 1));
              verboseOR.add(
                      new StringInt(orAddress.substring(0, lastColon), val)
              );
            } catch (NumberFormatException e) {
              continue;
            }
          }
          server.or_addresses = verboseOR;
        } else {
          server.or_addresses = desc.getOrAddresses();
        }

        // don't forget to define 'or_addresses' as type 'Object' above

        */
      }
      bridge.platform = desc.getPlatform();
      bridge.published = dateTimeFormat.format(desc.getPublishedMillis());
      bridge.fingerprint = desc.getFingerprint().toUpperCase();
      //  isHibernating can't return 'null' because it's of type 'boolean'
      //  (with little 'b') but it's only present in the collecTor data if it's
      //  true. therefor we check for it's existence and include it if it
      //  exists. otherwise we leave it alone / to the default value from
      //  the class definition above (which is null)
      if (desc.isHibernating()) {
        bridge.hibernating = desc.isHibernating();
      }
      bridge.uptime = desc.getUptime();
      bridge.ipv6_policy = desc.getIpv6DefaultPolicy();
      bridge.contact = desc.getContact();
      bridge.family = desc.getFamilyEntries();
      //  check for 'null' first because we want to run a method on it
      //  and not get a null pointer exception meanwhile
      if (desc.getReadHistory() != null) {
        bridge.read_history = convertBandwidthHistory(desc.getReadHistory());
      }
      if (desc.getWriteHistory() != null) {
        bridge.write_history = convertBandwidthHistory(desc.getWriteHistory());
      }
      bridge.eventdns = desc.getUsesEnhancedDnsLogic();
      bridge.caches_extra_info = desc.getCachesExtraInfo();
      if (desc.getExtraInfoDigest() != null) {
        bridge.extra_info_digest = desc.getExtraInfoDigest().toUpperCase();
      }
      bridge.hidden_service_dir_versions = desc.getHiddenServiceDirVersions();
      bridge.link_protocol_versions = desc.getLinkProtocolVersions();
      bridge.circuit_protocol_versions = desc.getCircuitProtocolVersions();
      bridge.allow_single_hop_exits = desc.getAllowSingleHopExits();
      bridge.ntor_onion_key = desc.getNtorOnionKey() != null;
      bridge.router_digest = desc.getServerDescriptorDigest().toUpperCase();

      return ToJson.serialize(bridge);
    }
  }

  //  relay extra info descriptors
  static class JsonRelayExtraInfoDescriptor extends JsonDescriptor {
    String descriptor_type;
    String nickname;
    String fingerprint;
    String published;
    Boolean identity_ed25519;                         // getIdentityEd25519
    Boolean master_key_ed25519;                       // getMasterKeyEd25519
    String extra_info_digest;
    Boolean extra_info_digest_sha256;                 // getExtraInfoDigestSha256
    BandwidthHistory read_history;
    BandwidthHistory write_history;
    String geoip_db_digest;
    String geoip6_db_digest;
    String geoip_start_time;
    String dirreq_stats_end_date;
    Long dirreq_stats_end_interval;
    Object dirreq_v2_ips;
    Object dirreq_v3_ips;
    Object dirreq_v2_reqs;
    Object dirreq_v3_reqs;
    Double dirreq_v2_share;
    Double dirreq_v3_share;
    Object dirreq_v2_resp;
    Object dirreq_v3_resp;
    Object dirreq_v2_direct_dl;
    Object dirreq_v3_direct_dl;
    Object dirreq_v2_tunneled_dl;
    Object dirreq_v3_tunneled_dl;
    BandwidthHistory dirreq_read_history;
    BandwidthHistory dirreq_write_history;
    String entry_stats_end_date;
    Long entry_stats_end_interval;
    Object entry_ips;
    String cell_stats_end_date;
    Long cell_stats_end_interval;
    List<Integer> cell_processed_cells;
    List<Double> cell_queued_cells;
    List<Integer> cell_time_in_queue;
    Integer cell_circuits_per_decile;
    ConnBiDirect conn_bi_direct;
    static class ConnBiDirect {
      String date;
      Long interval;
      Integer below;
      Integer read;
      Integer write;
      Integer both;
    }
    String exit_stats_end_date;
    Long exit_stats_end_interval;
    Object exit_kibibytes_written;
    Object exit_kibibytes_read;
    Object exit_streams_opened;

    HidStats hidserv_stats_end;
    static class HidStats {
      String date;                                  // long getHidservStatsEndMillis
      Long interval;                                // long getHidservStatsIntervalLength();
    }
    HidRend hidserv_rend_relayed_cells;
    static class HidRend {
      Double cells;                                 // Double getHidservRendRelayedCells();
      List<StringDouble> obfuscation;               // Map<String, Double> getHidservRendRelayedCellsParameters()
    }
    HidDir hidserv_dir_onions_seen;
    static class HidDir {
      Double onions;                                // Double getHidservDirOnionsSeen();
      List<StringDouble> obfuscation;               // Map<String, Double> getHidservDirOnionsSeenParameters();
    }
    List<String> transport;
    Boolean router_sig_ed25519;                     // getRouterSignatureEd25519
    Boolean router_signature;                       // getRouterSignature

    static String convert(RelayExtraInfoDescriptor desc) {
      JsonRelayExtraInfoDescriptor relayExtra = new JsonRelayExtraInfoDescriptor();
      for (String annotation : desc.getAnnotations()) {
        relayExtra.descriptor_type = annotation.substring("@type ".length());
      }
      relayExtra.nickname = desc.getNickname();
      relayExtra.fingerprint = desc.getFingerprint().toUpperCase();
      relayExtra.published = dateTimeFormat.format(desc.getPublishedMillis());
      relayExtra.identity_ed25519 = desc.getIdentityEd25519() != null;
      relayExtra.master_key_ed25519 = desc.getMasterKeyEd25519() != null;
      relayExtra.extra_info_digest = desc.getExtraInfoDigest();
      relayExtra.extra_info_digest_sha256 = desc.getExtraInfoDigestSha256() != null;
      if (desc.getReadHistory() != null) {
        relayExtra.read_history = convertBandwidthHistory(desc.getReadHistory());
      }
      if (desc.getWriteHistory() != null) {
        relayExtra.write_history = convertBandwidthHistory(desc.getWriteHistory());
      }
      relayExtra.geoip_db_digest = desc.getGeoipDbDigest();
      relayExtra.geoip6_db_digest = desc.getGeoip6DbDigest();
      if (desc.getGeoipStartTimeMillis() >= 0) {
        relayExtra.geoip_start_time = dateTimeFormat.format(desc.getGeoipStartTimeMillis());
      }
      if (desc.getDirreqStatsEndMillis() >= 0) {
        relayExtra.dirreq_stats_end_date = dateTimeFormat.format(desc.getDirreqStatsEndMillis());
      }
      if (desc.getDirreqStatsIntervalLength() >= 0) {
        relayExtra.dirreq_stats_end_interval = desc.getDirreqStatsIntervalLength();
      }
      if (desc.getDirreqV2Ips() != null && !desc.getDirreqV2Ips().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV2Ips = new ArrayList<>();
          relayExtra.dirreq_v2_ips = new ArrayList<>();
          SortedMap<String, Integer> v2_ips = desc.getDirreqV2Ips();
          for (Map.Entry<String, Integer> v2_ip : v2_ips.entrySet()) {
            verboseV2Ips.add(new StringInt(v2_ip.getKey(), v2_ip.getValue()));
          }
          relayExtra.dirreq_v2_ips = verboseV2Ips;
        } else {
          relayExtra.dirreq_v2_ips = desc.getDirreqV2Ips();
        }
      }
      if (desc.getDirreqV3Ips() != null && !desc.getDirreqV3Ips().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV3Ips = new ArrayList<>();
          relayExtra.dirreq_v3_ips = new ArrayList<>();
          SortedMap<String, Integer> v3_ips = desc.getDirreqV3Ips();
          for (Map.Entry<String, Integer> v3_ip : v3_ips.entrySet()) {
            verboseV3Ips.add(new StringInt(v3_ip.getKey(), v3_ip.getValue()));
          }
          relayExtra.dirreq_v3_ips = verboseV3Ips;
        } else {
          relayExtra.dirreq_v3_ips = desc.getDirreqV3Ips();
        }
      }
      if (desc.getDirreqV2Reqs() != null && !desc.getDirreqV2Reqs().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV2Reqs = new ArrayList<>();
          relayExtra.dirreq_v2_reqs = new ArrayList<>();
          SortedMap<String, Integer> v2_reqs = desc.getDirreqV2Reqs();
          for (Map.Entry<String, Integer> v2_req : v2_reqs.entrySet()) {
            verboseV2Reqs.add(new StringInt(v2_req.getKey(), v2_req.getValue()));
          }
          relayExtra.dirreq_v2_reqs = verboseV2Reqs;
        } else {
          relayExtra.dirreq_v2_reqs = desc.getDirreqV2Reqs();
        }
      }
      if (desc.getDirreqV3Reqs() != null && !desc.getDirreqV3Reqs().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV3Reqs = new ArrayList<>();
          relayExtra.dirreq_v3_reqs = new ArrayList<>();
          SortedMap<String, Integer> v3_reqs = desc.getDirreqV3Reqs();
          for (Map.Entry<String, Integer> v3_req : v3_reqs.entrySet()) {
            verboseV3Reqs.add(new StringInt(v3_req.getKey(), v3_req.getValue()));
          }
          relayExtra.dirreq_v3_reqs = verboseV3Reqs;
        } else {
          relayExtra.dirreq_v3_reqs = desc.getDirreqV3Reqs();
        }
      }
      if (desc.getDirreqV2Share() >= 0) {
        relayExtra.dirreq_v2_share = desc.getDirreqV2Share();
      }
      if (desc.getDirreqV3Share() >= 0) {
        relayExtra.dirreq_v3_share = desc.getDirreqV3Share();
      }
      if (desc.getDirreqV2Resp() != null && !desc.getDirreqV2Resp().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV2Resp = new ArrayList<>();
          relayExtra.dirreq_v2_resp = new ArrayList<>();
          SortedMap<String, Integer> v2_resps = desc.getDirreqV2Resp();
          for (Map.Entry<String, Integer> v2_resp : v2_resps.entrySet()) {
            verboseV2Resp.add(new StringInt(v2_resp.getKey(), v2_resp.getValue()));
          }
          relayExtra.dirreq_v2_resp = verboseV2Resp;
        } else {
          relayExtra.dirreq_v2_resp = desc.getDirreqV2Resp();
        }
      }
      if (desc.getDirreqV3Resp() != null && !desc.getDirreqV3Resp().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV3Resp = new ArrayList<>();
          relayExtra.dirreq_v3_resp = new ArrayList<>();
          SortedMap<String, Integer> v3_resps = desc.getDirreqV3Resp();
          for (Map.Entry<String, Integer> v3_resp : v3_resps.entrySet()) {
            verboseV3Resp.add(new StringInt(v3_resp.getKey(), v3_resp.getValue()));
          }
          relayExtra.dirreq_v3_resp = verboseV3Resp;
        } else {
          relayExtra.dirreq_v3_resp = desc.getDirreqV3Resp();
        }
      }
      if (desc.getDirreqV2DirectDl() != null && !desc.getDirreqV2DirectDl().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV2DirectDl = new ArrayList<>();
          relayExtra.dirreq_v2_direct_dl = new ArrayList<>();
          SortedMap<String, Integer> v2_direct = desc.getDirreqV2DirectDl();
          for (Map.Entry<String, Integer> v2_dir : v2_direct.entrySet()) {
            verboseV2DirectDl.add(new StringInt(v2_dir.getKey(), v2_dir.getValue()));
          }
          relayExtra.dirreq_v2_direct_dl = verboseV2DirectDl;
        } else {
          relayExtra.dirreq_v2_direct_dl = desc.getDirreqV2DirectDl();
        }
      }
      if (desc.getDirreqV3DirectDl() != null && !desc.getDirreqV3DirectDl().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV3DirectDl = new ArrayList<>();
          relayExtra.dirreq_v3_direct_dl = new ArrayList<>();
          SortedMap<String, Integer> v3_direct = desc.getDirreqV3DirectDl();
          for (Map.Entry<String, Integer> v3_dir : v3_direct.entrySet()) {
            verboseV3DirectDl.add(new StringInt(v3_dir.getKey(), v3_dir.getValue()));
          }
          relayExtra.dirreq_v3_direct_dl = verboseV3DirectDl;
        } else {
          relayExtra.dirreq_v3_direct_dl = desc.getDirreqV3DirectDl();
        }
      }
      if (desc.getDirreqV2TunneledDl() != null && !desc.getDirreqV2TunneledDl().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV2Tun = new ArrayList<>();
          relayExtra.dirreq_v2_tunneled_dl = new ArrayList<>();
          SortedMap<String, Integer> v2_tunneled = desc.getDirreqV2TunneledDl();
          for (Map.Entry<String, Integer> v2_tun : v2_tunneled.entrySet()) {
            verboseV2Tun.add(new StringInt(v2_tun.getKey(), v2_tun.getValue()));
          }
          relayExtra.dirreq_v2_tunneled_dl = verboseV2Tun;
        } else {
          relayExtra.dirreq_v2_tunneled_dl = desc.getDirreqV2TunneledDl();
        }
      }
      if (desc.getDirreqV3TunneledDl() != null && !desc.getDirreqV3TunneledDl().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV3Tun = new ArrayList<>();
          relayExtra.dirreq_v3_tunneled_dl = new ArrayList<>();
          SortedMap<String, Integer> v3_tunneled = desc.getDirreqV3TunneledDl();
          for (Map.Entry<String, Integer> v3_tun : v3_tunneled.entrySet()) {
            verboseV3Tun.add(new StringInt(v3_tun.getKey(), v3_tun.getValue()));
          }
          relayExtra.dirreq_v3_tunneled_dl = verboseV3Tun;
        } else {
          relayExtra.dirreq_v3_tunneled_dl = desc.getDirreqV3TunneledDl();
        }
      }
      if (desc.getDirreqReadHistory() != null) {
        relayExtra.dirreq_read_history =
                convertBandwidthHistory(desc.getDirreqReadHistory());
      }
      if (desc.getDirreqWriteHistory() != null) {
        relayExtra.dirreq_write_history =
                convertBandwidthHistory(desc.getDirreqWriteHistory());
      }
      if (desc.getEntryStatsEndMillis() >= 0) {
        relayExtra.entry_stats_end_date = dateTimeFormat.format(desc.getEntryStatsEndMillis());
      }
      if (desc.getEntryStatsIntervalLength() >= 0) {
        relayExtra.entry_stats_end_interval = desc.getEntryStatsIntervalLength();
      }
      if (desc.getEntryIps() != null && !desc.getEntryIps().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseIps = new ArrayList<>();
          relayExtra.entry_ips = new ArrayList<>();
          SortedMap<String, Integer> ips = desc.getEntryIps();
          for (Map.Entry<String, Integer> ip : ips.entrySet()) {
            verboseIps.add(new StringInt(ip.getKey(), ip.getValue()));
          }
          relayExtra.entry_ips = verboseIps;
        } else {
          relayExtra.entry_ips = desc.getEntryIps();
        }
      }
      if (desc.getCellStatsEndMillis() >= 0) {
        relayExtra.cell_stats_end_date = dateTimeFormat.format(desc.getCellStatsEndMillis());
      }
      if (desc.getCellStatsIntervalLength() >= 0) {
        relayExtra.cell_stats_end_interval = desc.getCellStatsIntervalLength();
      }
      relayExtra.cell_processed_cells = desc.getCellProcessedCells();
      relayExtra.cell_queued_cells = desc.getCellQueuedCells();
      relayExtra.cell_time_in_queue = desc.getCellTimeInQueue();
      if (desc.getCellCircuitsPerDecile() >= 0) {
        relayExtra.cell_circuits_per_decile = desc.getCellCircuitsPerDecile();
      }
      if (desc.getConnBiDirectStatsEndMillis() >= 0) {
        relayExtra.conn_bi_direct = new ConnBiDirect();
        relayExtra.conn_bi_direct.date = dateTimeFormat.format(desc.getConnBiDirectStatsEndMillis());
        if (desc.getConnBiDirectStatsIntervalLength() >= 0) {
          relayExtra.conn_bi_direct.interval = desc.getConnBiDirectStatsIntervalLength();
        }
        if (desc.getConnBiDirectBelow() >= 0) {
          relayExtra.conn_bi_direct.below = desc.getConnBiDirectBelow();
        }
        if (desc.getConnBiDirectRead() >= 0) {
          relayExtra.conn_bi_direct.read = desc.getConnBiDirectRead();
        }
        if (desc.getConnBiDirectWrite() >= 0) {
          relayExtra.conn_bi_direct.write = desc.getConnBiDirectWrite();
        }
        if (desc.getConnBiDirectBoth() >= 0) {
          relayExtra.conn_bi_direct.both = desc.getConnBiDirectBoth();
        }
      }
      if (desc.getExitStatsEndMillis() >= 0) {
        relayExtra.exit_stats_end_date = dateTimeFormat.format(desc.getExitStatsEndMillis());
      }
      if (desc.getExitStatsIntervalLength() >= 0) {
        relayExtra.exit_stats_end_interval = desc.getExitStatsIntervalLength();
      }
      if (desc.getExitKibibytesWritten() != null && !desc.getExitKibibytesWritten().isEmpty()) {
        if (verbose) {
          ArrayList<StringLong> verboseWritten = new ArrayList<>();
          relayExtra.exit_kibibytes_written = new ArrayList<>();
          SortedMap<String, Long> written = desc.getExitKibibytesWritten();
          for (Map.Entry<String, Long> writ : written.entrySet()) {
            verboseWritten.add(new StringLong(writ.getKey(), writ.getValue()));
          }
          relayExtra.exit_kibibytes_written = verboseWritten;
        } else {
          relayExtra.exit_kibibytes_written = desc.getExitKibibytesWritten();
        }
      }
      if (desc.getExitKibibytesRead() != null && !desc.getExitKibibytesRead().isEmpty()) {
        if (verbose) {
          ArrayList<StringLong> verboseRead = new ArrayList<>();
          relayExtra.exit_kibibytes_read = new ArrayList<>();
          SortedMap<String, Long> reads = desc.getExitKibibytesRead();
          for (Map.Entry<String, Long> read : reads.entrySet()) {
            verboseRead.add(new StringLong(read.getKey(), read.getValue()));
          }
          relayExtra.exit_kibibytes_read = verboseRead;
        } else {
          relayExtra.exit_kibibytes_read = desc.getExitKibibytesRead();
        }
      }
      if (desc.getExitStreamsOpened() != null && !desc.getExitStreamsOpened().isEmpty()) {
        if (verbose) {
          ArrayList<StringLong> verboseOpened = new ArrayList<>();
          relayExtra.exit_streams_opened = new ArrayList<>();
          SortedMap<String, Long> opened = desc.getExitStreamsOpened();
          for (Map.Entry<String, Long> open : opened.entrySet()) {
            verboseOpened.add(new StringLong(open.getKey(), open.getValue()));
          }
          relayExtra.exit_streams_opened = verboseOpened;
        } else {
          relayExtra.exit_streams_opened = desc.getExitStreamsOpened();
        }
      }
      relayExtra.hidserv_stats_end = new HidStats();
      relayExtra.hidserv_stats_end.date = dateTimeFormat.format(desc.getHidservStatsEndMillis());
      relayExtra.hidserv_stats_end.interval = desc.getHidservStatsIntervalLength();
      relayExtra.hidserv_rend_relayed_cells = new HidRend();
      relayExtra.hidserv_rend_relayed_cells.cells = desc.getHidservRendRelayedCells();
      relayExtra.hidserv_rend_relayed_cells.obfuscation = new ArrayList<>();
      if (desc.getHidservRendRelayedCellsParameters() != null &&
              !desc.getHidservRendRelayedCellsParameters().isEmpty()) {
        //  TODO non-verbose version + switch
        for (Map.Entry<String,Double> mapEntry : desc.getHidservRendRelayedCellsParameters().entrySet()) {
          relayExtra.hidserv_rend_relayed_cells.obfuscation.add(
                  new StringDouble(mapEntry.getKey(), mapEntry.getValue())
          );
        }
      }
      relayExtra.hidserv_dir_onions_seen = new HidDir();
      relayExtra.hidserv_dir_onions_seen.onions = desc.getHidservDirOnionsSeen();
      relayExtra.hidserv_dir_onions_seen.obfuscation = new ArrayList<>();
      if (desc.getHidservDirOnionsSeenParameters() != null &&
              !desc.getHidservDirOnionsSeenParameters().isEmpty()) {
        //  TODO non-verbose version + switch
        for (Map.Entry<String,Double> mapEntry : desc.getHidservDirOnionsSeenParameters().entrySet()) {
          relayExtra.hidserv_dir_onions_seen.obfuscation.add(
                  new StringDouble(mapEntry.getKey(), mapEntry.getValue())
          );
        }
      }
      relayExtra.transport = desc.getTransports();
      relayExtra.router_sig_ed25519 = desc.getRouterSignatureEd25519() != null;
      relayExtra.router_signature = desc.getRouterSignature() != null;
      return ToJson.serialize(relayExtra);
    }
  }

  //  bridge extra info descriptors
  static class JsonBridgeExtraInfoDescriptor extends JsonDescriptor {
    String descriptor_type;
    Object geoip_client_origins;
    String bridge_stats_end_date;
    Long bridge_stats_end_interval;
    Object bridge_ips;
    Object bridge_ip_versions;
    Object bridge_ip_transports;
    String nickname;
    String fingerprint;
    String published;
    String extra_info_digest;
    Boolean extra_info_digest_sha256; // getExtraInfoDigestSha256
    BandwidthHistory read_history;
    BandwidthHistory write_history;
    String geoip_db_digest;
    String geoip6_db_digest;
    String geoip_start_time;
    String dirreq_stats_end_date;
    Long dirreq_stats_end_interval;
    Object dirreq_v2_ips;
    Object dirreq_v3_ips;
    Object dirreq_v2_reqs;
    Object dirreq_v3_reqs;
    Double dirreq_v2_share;
    Double dirreq_v3_share;
    Object dirreq_v2_resp;
    Object dirreq_v3_resp;
    Object dirreq_v2_direct_dl;
    Object dirreq_v3_direct_dl;
    Object dirreq_v2_tunneled_dl;
    Object dirreq_v3_tunneled_dl;
    BandwidthHistory dirreq_read_history;
    BandwidthHistory dirreq_write_history;
    String entry_stats_end_date;
    Long entry_stats_end_interval;
    Object entry_ips;
    String cell_stats_end_date;
    Long cell_stats_end_interval;
    List<Integer> cell_processed_cells;
    List<Double> cell_queued_cells;
    List<Integer> cell_time_in_queue;
    Integer cell_circuits_per_decile;
    ConnBiDirect conn_bi_direct;
    static class ConnBiDirect {
      String date;
      Long interval;
      Integer below;
      Integer read;
      Integer write;
      Integer both;
    }
    String exit_stats_end_date;
    Long exit_stats_end_interval;
    Object exit_kibibytes_written;
    Object exit_kibibytes_read;
    Object exit_streams_opened;

/*
    Object hidserv_stats_end;
    String date; // long getHidservStatsEndMillis
    Integer interval; // long getHidservStatsIntervalLength();

    Object hidserv_rend_relayed_cells;
    Double cells; // Double getHidservRendRelayedCells();
    List<StringInt> obfuscation; // Map<String, Double> getHidservRendRelayedCellsParameters()

    Object hidserv_dir_onions_seen;
    Double onions; // Double getHidservDirOnionsSeen();
    List<StringInt> obfuscation; // Map<String, Double> getHidservDirOnionsSeenParameters();
*/

    List<String> transport;
    Boolean router_sig_ed25519; // getRouterSignatureEd25519
    Boolean router_signature; // getRouterSignature

    static String convert(BridgeExtraInfoDescriptor desc) {
      JsonBridgeExtraInfoDescriptor bridgeExtra = new JsonBridgeExtraInfoDescriptor();
      for (String annotation : desc.getAnnotations()) {
        bridgeExtra.descriptor_type = annotation.substring("@type ".length());
        if (annotation.startsWith("@type bridge-extra-info")) {
          if (desc.getGeoipClientOrigins() != null && !desc.getGeoipClientOrigins().isEmpty()) {
            if(verbose) {
              ArrayList<StringInt> verboseGeo = new ArrayList<>();
              bridgeExtra.geoip_client_origins = new ArrayList<>();
              SortedMap<String, Integer> origins = desc.getGeoipClientOrigins();
              for (Map.Entry<String, Integer> geo : origins.entrySet()) {
                verboseGeo.add(new StringInt(geo.getKey(), geo.getValue()));
              }
              bridgeExtra.geoip_client_origins = verboseGeo;
            } else {
              bridgeExtra.geoip_client_origins = desc.getGeoipClientOrigins();
            }
          }
          if (desc.getBridgeStatsEndMillis() >= 0) {
            bridgeExtra.bridge_stats_end_date = dateTimeFormat.format(desc.getBridgeStatsEndMillis());
          }
          if (desc.getBridgeStatsIntervalLength() >= 0) {
            bridgeExtra.bridge_stats_end_interval = desc.getBridgeStatsIntervalLength();
          }
          if (desc.getBridgeIps() != null && !desc.getBridgeIps().isEmpty()) {
            if (verbose) {
              ArrayList<StringInt> verboseIP = new ArrayList<>();
              bridgeExtra.bridge_ips = new ArrayList<>();
              SortedMap<String, Integer> b_ips = desc.getBridgeIps();
              for (Map.Entry<String, Integer> b_ip : b_ips.entrySet()) {
                verboseIP.add(new StringInt(b_ip.getKey(), b_ip.getValue()));
              }
              bridgeExtra.bridge_ips = verboseIP;
            } else {
              bridgeExtra.bridge_ips = desc.getBridgeIps();
            }
          }
          if (desc.getBridgeIpVersions() != null && !desc.getBridgeIpVersions().isEmpty()) {
            if (verbose) {
              ArrayList<StringInt> verboseIPversions = new ArrayList<>();
              bridgeExtra.bridge_ip_versions = new ArrayList<>();
              SortedMap<String, Integer> b_ips_v = desc.getBridgeIpVersions();
              for (Map.Entry<String, Integer> b_ip_v : b_ips_v.entrySet()) {
                verboseIPversions.add(new StringInt(b_ip_v.getKey(), b_ip_v.getValue()));
              }
              bridgeExtra.bridge_ip_versions = verboseIPversions;
            } else {
              bridgeExtra.bridge_ip_versions = desc.getBridgeIpVersions();
            }
          }
          if (desc.getBridgeIpTransports() != null && !desc.getBridgeIpTransports().isEmpty()) {
            if (verbose) {
              ArrayList<StringInt> verboseIPtrans = new ArrayList<>();
              bridgeExtra.bridge_ip_transports = new ArrayList<>();
              SortedMap<String, Integer> b_ips_t = desc.getBridgeIpTransports();
              for (Map.Entry<String, Integer> b_ip_t : b_ips_t.entrySet()) {
                verboseIPtrans.add(new StringInt(b_ip_t.getKey(), b_ip_t.getValue()));
              }
              bridgeExtra.bridge_ip_transports = verboseIPtrans;
            } else {
              bridgeExtra.bridge_ip_transports = desc.getBridgeIps();
            }
          }
        }
      }
      bridgeExtra.nickname = desc.getNickname();
      bridgeExtra.fingerprint = desc.getFingerprint().toUpperCase();
      bridgeExtra.published = dateTimeFormat.format(desc.getPublishedMillis());
      bridgeExtra.extra_info_digest = desc.getExtraInfoDigest();
      if (desc.getReadHistory() != null) {
        bridgeExtra.read_history = convertBandwidthHistory(desc.getReadHistory());
      }
      if (desc.getWriteHistory() != null) {
        bridgeExtra.write_history = convertBandwidthHistory(desc.getWriteHistory());
      }
      bridgeExtra.geoip_db_digest = desc.getGeoipDbDigest();
      bridgeExtra.geoip6_db_digest = desc.getGeoip6DbDigest();
      if (desc.getGeoipStartTimeMillis() >= 0) {
        bridgeExtra.geoip_start_time = dateTimeFormat.format(desc.getGeoipStartTimeMillis());
      }
      if (desc.getDirreqStatsEndMillis() >= 0) {
        bridgeExtra.dirreq_stats_end_date = dateTimeFormat.format(desc.getDirreqStatsEndMillis());
      }
      if (desc.getDirreqStatsIntervalLength() >= 0) {
        bridgeExtra.dirreq_stats_end_interval = desc.getDirreqStatsIntervalLength();
      }
      if (desc.getDirreqV2Ips() != null && !desc.getDirreqV2Ips().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV2Ips = new ArrayList<>();
          bridgeExtra.dirreq_v2_ips = new ArrayList<>();
          SortedMap<String, Integer> v2_ips = desc.getDirreqV2Ips();
          for (Map.Entry<String, Integer> v2_ip : v2_ips.entrySet()) {
            verboseV2Ips.add(new StringInt(v2_ip.getKey(), v2_ip.getValue()));
          }
          bridgeExtra.dirreq_v2_ips = verboseV2Ips;
        } else {
          bridgeExtra.dirreq_v2_ips = desc.getDirreqV2Ips();
        }
      }
      if (desc.getDirreqV3Ips() != null && !desc.getDirreqV3Ips().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV3Ips = new ArrayList<>();
          bridgeExtra.dirreq_v3_ips = new ArrayList<>();
          SortedMap<String, Integer> v3_ips = desc.getDirreqV3Ips();
          for (Map.Entry<String, Integer> v3_ip : v3_ips.entrySet()) {
            verboseV3Ips.add(new StringInt(v3_ip.getKey(), v3_ip.getValue()));
          }
          bridgeExtra.dirreq_v3_ips = verboseV3Ips;
        } else {
          bridgeExtra.dirreq_v3_ips = desc.getDirreqV3Ips();
        }
      }
      if (desc.getDirreqV2Reqs() != null && !desc.getDirreqV2Reqs().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV2Reqs = new ArrayList<>();
          bridgeExtra.dirreq_v2_reqs = new ArrayList<>();
          SortedMap<String, Integer> v2_reqs = desc.getDirreqV2Reqs();
          for (Map.Entry<String, Integer> v2_req : v2_reqs.entrySet()) {
            verboseV2Reqs.add(new StringInt(v2_req.getKey(), v2_req.getValue()));
          }
          bridgeExtra.dirreq_v2_reqs = verboseV2Reqs;
        } else {
          bridgeExtra.dirreq_v2_reqs = desc.getDirreqV2Reqs();
        }
      }
      if (desc.getDirreqV3Reqs() != null && !desc.getDirreqV3Reqs().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV3Reqs = new ArrayList<>();
          bridgeExtra.dirreq_v3_reqs = new ArrayList<>();
          SortedMap<String, Integer> v3_reqs = desc.getDirreqV3Reqs();
          for (Map.Entry<String, Integer> v3_req : v3_reqs.entrySet()) {
            verboseV3Reqs.add(new StringInt(v3_req.getKey(), v3_req.getValue()));
          }
          bridgeExtra.dirreq_v3_reqs = verboseV3Reqs;
        } else {
          bridgeExtra.dirreq_v3_reqs = desc.getDirreqV3Reqs();
        }
      }
      if (desc.getDirreqV2Share() >= 0) {
        bridgeExtra.dirreq_v2_share = desc.getDirreqV2Share();
      }
      if (desc.getDirreqV3Share() >= 0) {
        bridgeExtra.dirreq_v3_share = desc.getDirreqV3Share();
      }
      if (desc.getDirreqV2Resp() != null && !desc.getDirreqV2Resp().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV2Resp = new ArrayList<>();
          bridgeExtra.dirreq_v2_resp = new ArrayList<>();
          SortedMap<String, Integer> v2_resps = desc.getDirreqV2Resp();
          for (Map.Entry<String, Integer> v2_resp : v2_resps.entrySet()) {
            verboseV2Resp.add(new StringInt(v2_resp.getKey(), v2_resp.getValue()));
          }
          bridgeExtra.dirreq_v2_resp = verboseV2Resp;
        } else {
          bridgeExtra.dirreq_v2_resp = desc.getDirreqV2Resp();
        }
      }
      if (desc.getDirreqV3Resp() != null && !desc.getDirreqV3Resp().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV3Resp = new ArrayList<>();
          bridgeExtra.dirreq_v3_resp = new ArrayList<>();
          SortedMap<String, Integer> v3_resps = desc.getDirreqV3Resp();
          for (Map.Entry<String, Integer> v3_resp : v3_resps.entrySet()) {
            verboseV3Resp.add(new StringInt(v3_resp.getKey(), v3_resp.getValue()));
          }
          bridgeExtra.dirreq_v3_resp = verboseV3Resp;
        } else {
          bridgeExtra.dirreq_v3_resp = desc.getDirreqV3Resp();
        }
      }
      if (desc.getDirreqV2DirectDl() != null && !desc.getDirreqV2DirectDl().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV2DirectDl = new ArrayList<>();
          bridgeExtra.dirreq_v2_direct_dl = new ArrayList<>();
          SortedMap<String, Integer> v2_direct = desc.getDirreqV2DirectDl();
          for (Map.Entry<String, Integer> v2_dir : v2_direct.entrySet()) {
            verboseV2DirectDl.add(new StringInt(v2_dir.getKey(), v2_dir.getValue()));
          }
          bridgeExtra.dirreq_v2_direct_dl = verboseV2DirectDl;
        } else {
          bridgeExtra.dirreq_v2_direct_dl = desc.getDirreqV2DirectDl();
        }
      }
      if (desc.getDirreqV3DirectDl() != null && !desc.getDirreqV3DirectDl().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV3DirectDl = new ArrayList<>();
          bridgeExtra.dirreq_v3_direct_dl = new ArrayList<>();
          SortedMap<String, Integer> v3_direct = desc.getDirreqV3DirectDl();
          for (Map.Entry<String, Integer> v3_dir : v3_direct.entrySet()) {
            verboseV3DirectDl.add(new StringInt(v3_dir.getKey(), v3_dir.getValue()));
          }
          bridgeExtra.dirreq_v3_direct_dl = verboseV3DirectDl;
        } else {
          bridgeExtra.dirreq_v3_direct_dl = desc.getDirreqV3DirectDl();
        }
      }
      if (desc.getDirreqV2TunneledDl() != null && !desc.getDirreqV2TunneledDl().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV2Tun = new ArrayList<>();
          bridgeExtra.dirreq_v2_tunneled_dl = new ArrayList<>();
          SortedMap<String, Integer> v2_tunneled = desc.getDirreqV2TunneledDl();
          for (Map.Entry<String, Integer> v2_tun : v2_tunneled.entrySet()) {
            verboseV2Tun.add(new StringInt(v2_tun.getKey(), v2_tun.getValue()));
          }
          bridgeExtra.dirreq_v2_tunneled_dl = verboseV2Tun;
        } else {
          bridgeExtra.dirreq_v2_tunneled_dl = desc.getDirreqV2TunneledDl();
        }
      }
      if (desc.getDirreqV3TunneledDl() != null && !desc.getDirreqV3TunneledDl().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseV3Tun = new ArrayList<>();
          bridgeExtra.dirreq_v3_tunneled_dl = new ArrayList<>();
          SortedMap<String, Integer> v3_tunneled = desc.getDirreqV3TunneledDl();
          for (Map.Entry<String, Integer> v3_tun : v3_tunneled.entrySet()) {
            verboseV3Tun.add(new StringInt(v3_tun.getKey(), v3_tun.getValue()));
          }
          bridgeExtra.dirreq_v3_tunneled_dl = verboseV3Tun;
        } else {
          bridgeExtra.dirreq_v3_tunneled_dl = desc.getDirreqV3TunneledDl();
        }
      }
      if (desc.getDirreqReadHistory() != null) {
        bridgeExtra.dirreq_read_history =
                convertBandwidthHistory(desc.getDirreqReadHistory());
      }
      if (desc.getDirreqWriteHistory() != null) {
        bridgeExtra.dirreq_write_history =
                convertBandwidthHistory(desc.getDirreqWriteHistory());
      }
      if (desc.getEntryStatsEndMillis() >= 0) {
        bridgeExtra.entry_stats_end_date = dateTimeFormat.format(desc.getEntryStatsEndMillis());
      }
      if (desc.getEntryStatsIntervalLength() >= 0) {
        bridgeExtra.entry_stats_end_interval = desc.getEntryStatsIntervalLength();
      }
      if (desc.getEntryIps() != null && !desc.getEntryIps().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseIps = new ArrayList<>();
          bridgeExtra.entry_ips = new ArrayList<>();
          SortedMap<String, Integer> ips = desc.getEntryIps();
          for (Map.Entry<String, Integer> ip : ips.entrySet()) {
            verboseIps.add(new StringInt(ip.getKey(), ip.getValue()));
          }
          bridgeExtra.entry_ips = verboseIps;
        } else {
          bridgeExtra.entry_ips = desc.getEntryIps();
        }
      }
      if (desc.getCellStatsEndMillis() >= 0) {
        bridgeExtra.cell_stats_end_date = dateTimeFormat.format(desc.getCellStatsEndMillis());
      }
      if (desc.getCellStatsIntervalLength() >= 0) {
        bridgeExtra.cell_stats_end_interval = desc.getCellStatsIntervalLength();
      }
      bridgeExtra.cell_processed_cells = desc.getCellProcessedCells();
      bridgeExtra.cell_queued_cells = desc.getCellQueuedCells();
      bridgeExtra.cell_time_in_queue = desc.getCellTimeInQueue();
      if (desc.getCellCircuitsPerDecile() >= 0) {
        bridgeExtra.cell_circuits_per_decile = desc.getCellCircuitsPerDecile();
      }
      if (desc.getConnBiDirectStatsEndMillis() >= 0) {
        bridgeExtra.conn_bi_direct = new ConnBiDirect();
        bridgeExtra.conn_bi_direct.date = dateTimeFormat.format(desc.getConnBiDirectStatsEndMillis());
        if (desc.getConnBiDirectStatsIntervalLength() >= 0) {
          bridgeExtra.conn_bi_direct.interval = desc.getConnBiDirectStatsIntervalLength();
        }
        if (desc.getConnBiDirectBelow() >= 0) {
          bridgeExtra.conn_bi_direct.below = desc.getConnBiDirectBelow();
        }
        if (desc.getConnBiDirectRead() >= 0) {
          bridgeExtra.conn_bi_direct.read = desc.getConnBiDirectRead();
        }
        if (desc.getConnBiDirectWrite() >= 0) {
          bridgeExtra.conn_bi_direct.write = desc.getConnBiDirectWrite();
        }
        if (desc.getConnBiDirectBoth() >= 0) {
          bridgeExtra.conn_bi_direct.both = desc.getConnBiDirectBoth();
        }
      }
      if (desc.getExitStatsEndMillis() >= 0) {
        bridgeExtra.exit_stats_end_date = dateTimeFormat.format(desc.getExitStatsEndMillis());
      }
      if (desc.getExitStatsIntervalLength() >= 0) {
        bridgeExtra.exit_stats_end_interval = desc.getExitStatsIntervalLength();
      }
      if (desc.getExitKibibytesWritten() != null && !desc.getExitKibibytesWritten().isEmpty()) {
        if (verbose) {
          ArrayList<StringLong> verboseWritten = new ArrayList<>();
          bridgeExtra.exit_kibibytes_written = new ArrayList<>();
          SortedMap<String, Long> written = desc.getExitKibibytesWritten();
          for (Map.Entry<String, Long> writ : written.entrySet()) {
            verboseWritten.add(new StringLong(writ.getKey(), writ.getValue()));
          }
          bridgeExtra.exit_kibibytes_written = verboseWritten;
        } else {
          bridgeExtra.exit_kibibytes_written = desc.getExitKibibytesWritten();
        }
      }
      if (desc.getExitKibibytesRead() != null && !desc.getExitKibibytesRead().isEmpty()) {
        if (verbose) {
          ArrayList<StringLong> verboseRead = new ArrayList<>();
          bridgeExtra.exit_kibibytes_read = new ArrayList<>();
          SortedMap<String, Long> reads = desc.getExitKibibytesRead();
          for (Map.Entry<String, Long> read : reads.entrySet()) {
            verboseRead.add(new StringLong(read.getKey(), read.getValue()));
          }
          bridgeExtra.exit_kibibytes_read = verboseRead;
        } else {
          bridgeExtra.exit_kibibytes_read = desc.getExitKibibytesRead();
        }
      }
      if (desc.getExitStreamsOpened() != null && !desc.getExitStreamsOpened().isEmpty()) {
        if (verbose) {
          ArrayList<StringLong> verboseOpened = new ArrayList<>();
          bridgeExtra.exit_streams_opened = new ArrayList<>();
          SortedMap<String, Long> opened = desc.getExitStreamsOpened();
          for (Map.Entry<String, Long> open : opened.entrySet()) {
            verboseOpened.add(new StringLong(open.getKey(), open.getValue()));
          }
          bridgeExtra.exit_streams_opened = verboseOpened;
        } else {
          bridgeExtra.exit_streams_opened = desc.getExitStreamsOpened();
        }
      }
      //  extra.hidserv_stats_end = new StringInt(); // no getter in metrics-lib
      //  extra.hidserv_rend_relayed_cells = new Object(); // no getter in metrics-lib
      //  extra.hidserv_dir_onions_seen = new Object(); // no getter in metrics-lib
      bridgeExtra.transport = desc.getTransports();
      //  extra.router_sig_ed25519 = false; // no getter in metrics-lib
      //  extra.router_signature = false; // no getter in metrics-lib
      return ToJson.serialize(bridgeExtra);
    }
  }

  //  network status consensus
  static class JsonRelayNetworkStatusConsensus extends JsonDescriptor {
    String descriptor_type;
    String published;
    Integer vote_status;
    Integer consensus_method;
    String consensus_flavor;
    String valid_after;
    String fresh_until;
    String valid_until;
    Vote voting_delay;
    static class Vote {
      Long vote_seconds;
      Long dist_seconds;
    }
    List<String> client_version;
    List<String> server_versions;
    SortedSet<String> known_flags;
    Object params;
    List<Authority> dir_source;
    static class Authority {
      String nickname;
      String identity;
      String adress;
      Integer dir_port;
      Integer or_port;
      String contact;
      String vote_digest;
      Boolean legacy;
    }
    List<Router> router_status;
    static class Router {
      R r;  // router description
      List<String> a;  // additinal OR adresses and ports
      List<String> s;  // flags
      String v;  // version
      W w;  // bandwidths
      Policy p;  // policies
    }
    static class R {
      String nickname;
      String identity;
      String digest;
      String publication;
      String ip;
      Integer or_port;
      Integer dir_port;
    }
    static class W {
      Long bandwidth;
      Long measured_bw;
      Boolean unmeasured_bw;
    }
    static class Policy {
      String default_policy;
      String port_summary;
    }
    DirFooter directory_footer;
    static class DirFooter {
      Object bandwidth_weights;
      String consensus_digest;
      List<DirSig> directory_signature;
    }
    static class DirSig {
      String algorithm;
      String identity;
      String signing_key_digest;
      Boolean signature;
    }

    static String convert(RelayNetworkStatusConsensus desc) {
      JsonRelayNetworkStatusConsensus cons = new JsonRelayNetworkStatusConsensus();
      for (String annotation : desc.getAnnotations()) {
        cons.descriptor_type = annotation.substring("@type ".length());
      }
      cons.published = dateTimeFormat.format(desc.getValidAfterMillis());
      cons.vote_status = desc.getNetworkStatusVersion();
      cons.consensus_method = desc.getConsensusMethod();
      cons.consensus_flavor = desc.getConsensusFlavor();
      cons.valid_after = dateTimeFormat.format(desc.getValidAfterMillis());
      cons.fresh_until = dateTimeFormat.format(desc.getFreshUntilMillis());
      cons.valid_until = dateTimeFormat.format(desc.getValidUntilMillis());
      cons.voting_delay = new Vote();
      cons.voting_delay.vote_seconds = desc.getVoteSeconds();
      cons.voting_delay.dist_seconds = desc.getDistSeconds();
      if (desc.getRecommendedClientVersions() != null &&
              !desc.getRecommendedClientVersions().isEmpty()) {
        cons.client_version = desc.getRecommendedClientVersions();
      }
      if (desc.getRecommendedServerVersions() != null &&
              !desc.getRecommendedServerVersions().isEmpty()) {
        cons.server_versions = desc.getRecommendedServerVersions();
      }
      if (desc.getKnownFlags() != null && !desc.getKnownFlags().isEmpty()) {
        cons.known_flags = desc.getKnownFlags();
      }
      if (desc.getConsensusParams() != null && !desc.getConsensusParams().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseParams = new ArrayList<>();
          cons.params = new ArrayList<>();
          SortedMap<String,Integer> paramsC = desc.getConsensusParams();
          for(Map.Entry<String,Integer> paraC : paramsC.entrySet()) {
            verboseParams.add(new StringInt(paraC.getKey(), paraC.getValue()));
          }
          cons.params = verboseParams;
        } else {
          cons.params = desc.getConsensusParams();
        }
      }
      if (desc.getDirSourceEntries() != null && !desc.getDirSourceEntries().isEmpty()) {
        cons.dir_source = new ArrayList<>();
        SortedMap<String, DirSourceEntry> AuthorityMap = desc.getDirSourceEntries();
        for (Map.Entry<String, DirSourceEntry> mAuth : AuthorityMap.entrySet()) {
          Authority auth = new Authority();
          auth.nickname = mAuth.getValue().getNickname();
          auth.identity = mAuth.getValue().getIdentity();
          auth.adress = mAuth.getValue().getIp();
          auth.dir_port = mAuth.getValue().getDirPort();
          auth.or_port = mAuth.getValue().getOrPort();
          auth.contact = mAuth.getValue().getContactLine();
          auth.vote_digest = mAuth.getValue().getVoteDigest();
          auth.legacy = mAuth.getValue().isLegacy();
          cons.dir_source.add(auth);
        }
      }
      if (desc.getStatusEntries() != null && !desc.getStatusEntries().isEmpty()) {
        cons.router_status = new ArrayList();
        SortedMap<String,NetworkStatusEntry> statusMap = desc.getStatusEntries();
        for(Map.Entry<String,NetworkStatusEntry> status : statusMap.entrySet()) {
          Router router = new Router();
          router.r = new R();
          router.r.nickname = status.getValue().getNickname();
          router.r.identity = status.getValue().getFingerprint();
          router.r.digest = status.getValue().getDescriptor();
          router.r.publication = dateTimeFormat.format(status.getValue().getPublishedMillis());
          router.r.ip = status.getValue().getAddress();
          router.r.or_port = status.getValue().getOrPort();
          router.r.dir_port = status.getValue().getDirPort();
          if (status.getValue().getOrAddresses() != null &&
                  !status.getValue().getOrAddresses().isEmpty()) {
            router.a = status.getValue().getOrAddresses();
          }
          if (status.getValue().getFlags() != null && !status.getValue().getFlags().isEmpty()) {
            router.s = new ArrayList<>();
            for (String flag : status.getValue().getFlags()) router.s.add(flag);
          }
          router.v = status.getValue().getVersion();
          router.w = new W();
          if (status.getValue().getBandwidth() >= 0) {
            router.w.bandwidth = status.getValue().getBandwidth();
          }
          if (status.getValue().getMeasured() >= 0) {
            router.w.measured_bw = status.getValue().getMeasured();
          }
          router.w.unmeasured_bw = status.getValue().getUnmeasured();
          router.p = new Policy();
          router.p.default_policy = status.getValue().getDefaultPolicy();
          router.p.port_summary = status.getValue().getPortList();
          cons.router_status.add(router);
        }
      }
      if (desc.getDirectorySignatures() != null && !desc.getDirectorySignatures().isEmpty()) {
        cons.directory_footer = new DirFooter();
        if (desc.getBandwidthWeights() != null && !desc.getBandwidthWeights().isEmpty()) {
          if (verbose) {
              ArrayList<StringInt> verboseBwWeights = new ArrayList<>();
            cons.directory_footer.bandwidth_weights = new ArrayList<> ();
            SortedMap<String,Integer> bwWeights = desc.getBandwidthWeights();
            for(Map.Entry<String,Integer> bw : bwWeights.entrySet()) {
              verboseBwWeights.add(new StringInt(bw.getKey(), bw.getValue()));
            }
            cons.directory_footer.bandwidth_weights = verboseBwWeights;
          } else {
            cons.directory_footer.bandwidth_weights = desc.getBandwidthWeights();
          }
        }
        cons.directory_footer.consensus_digest = desc.getConsensusDigest();
        if (desc.getDirectorySignatures() != null && !desc.getDirectorySignatures().isEmpty()) {
          cons.directory_footer.directory_signature = new ArrayList<>();
          SortedMap<String,DirectorySignature> dirSigMap = desc.getDirectorySignatures();
          for(Map.Entry<String,DirectorySignature> dirSigEntry : dirSigMap.entrySet()) {
            DirSig dirSig = new DirSig();
            dirSig.algorithm = dirSigEntry.getValue().getAlgorithm();
            dirSig.identity = dirSigEntry.getValue().getIdentity();
            dirSig.signing_key_digest = dirSigEntry.getValue().getSigningKeyDigest();
            dirSig.signature = dirSigEntry.getValue().getSignature() != null;
            cons.directory_footer.directory_signature.add(dirSig);
          }
        }
      }
      return ToJson.serialize(cons);
    }
  }

  //  network status vote
  static class JsonRelayNetworkStatusVote extends JsonDescriptor {
    String descriptor_type;
    String published;
    Integer vote_status;
    List<Integer> consensus_method;
    String valid_after;
    String fresh_until;
    String valid_until;
    Vote voting_delay;
    static class Vote {
      Long vote_seconds;
      Long dist_seconds;
    }
    List<String> client_version;
    List<String> server_versions;
    FlagTreshold flagTreshold;
    static class FlagTreshold {
      Long stable_uptime;
      Long stable_mtbf;
      Integer enough_mtbf;
      Long fast_speed;
      Double guard_wfu;
      Long guard_tk;
      Long guard_bw_inc_exits;
      Long guard_bw_exc_exits;
      Integer ignoring_advertised;
    }
    SortedSet<String> known_flags;
    Object params;
    Authority authority;
    static class Authority {
      String nickname;
      String identity;
      String adress;
      Integer dir_port;
      Integer or_port;
      String contact;
      String legacy_dir_key;
      Cert key_certificate;
    }
    static class Cert {
      Integer version;
      String dir_key_published;
      String dir_key_expires;
      Boolean dir_signing_key;
    }
    List<Router> router_status;
    static class Router {
      R r;  // router description
      List<String> a;  // additinal OR adresses and ports
      List<String> s;  // flags
      String v;  // version
      W w;  // bandwidths
      Policy p;  // policies
      Boolean id;                                     // getMasterKeyEd25519
    }
    static class R {
      String nickname;
      String identity;
      String digest;
      String publication;
      String ip;
      Integer or_port;
      Integer dir_port;
    }
    static class W {
      Long bandwidth;
      Long measured_bw;
      Boolean unmeasured_bw;
    }
    static class Policy {
      String default_policy;
      String port_summary;
    }
    DirFooter directory_footer;
    static class DirFooter {
      DirSig directory_signature;
    }
    static class DirSig {
      String algorithm;
      String identity;
      String signing_key_digest;
      Boolean signature;
    }

    static String convert(RelayNetworkStatusVote desc) {
      JsonRelayNetworkStatusVote vote = new JsonRelayNetworkStatusVote();
      for (String annotation : desc.getAnnotations()) {
        vote.descriptor_type = annotation.substring("@type ".length());
      }
      vote.published = dateTimeFormat.format(desc.getPublishedMillis());
      vote.vote_status = desc.getNetworkStatusVersion();
      if (desc.getConsensusMethods() != null && !desc.getConsensusMethods().isEmpty()) {
        vote.consensus_method = desc.getConsensusMethods();
      }
      vote.valid_after = dateTimeFormat.format(desc.getValidAfterMillis());
      vote.fresh_until = dateTimeFormat.format(desc.getFreshUntilMillis());
      vote.valid_until = dateTimeFormat.format(desc.getValidUntilMillis());
      vote.voting_delay = new Vote();
      vote.voting_delay.vote_seconds = desc.getVoteSeconds();
      vote.voting_delay.dist_seconds = desc.getDistSeconds();
      if (desc.getRecommendedClientVersions() != null &&
              !desc.getRecommendedClientVersions().isEmpty()) {
        vote.client_version = desc.getRecommendedClientVersions();
      }
      if (desc.getRecommendedServerVersions() != null &&
              !desc.getRecommendedServerVersions().isEmpty()) {
        vote.server_versions = desc.getRecommendedServerVersions();
      }
      vote.flagTreshold = new FlagTreshold();
      if (desc.getStableUptime() >= 0) {
        vote.flagTreshold.stable_uptime = desc.getStableUptime();
      }
      if (desc.getStableMtbf() >= 0) {
        vote.flagTreshold.stable_mtbf = desc.getStableMtbf();
      }
      if (desc.getEnoughMtbfInfo() >= 0) {
        vote.flagTreshold.enough_mtbf = desc.getEnoughMtbfInfo();
      }
      if (desc.getFastBandwidth() >= 0) {
        vote.flagTreshold.fast_speed = desc.getFastBandwidth();
      }
      if (desc.getGuardWfu() >= 0) {
        vote.flagTreshold.guard_wfu = desc.getGuardWfu();
      }
      if (desc.getGuardTk() >= 0) {
        vote.flagTreshold.guard_tk = desc.getGuardTk();
      }
      if (desc.getGuardBandwidthIncludingExits() >= 0) {
        vote.flagTreshold.guard_bw_inc_exits = desc.getGuardBandwidthIncludingExits();
      }
      if (desc.getGuardBandwidthExcludingExits() >= 0) {
        vote.flagTreshold.guard_bw_exc_exits = desc.getGuardBandwidthExcludingExits();
      }
      if (desc.getIgnoringAdvertisedBws() >= 0) {
        vote.flagTreshold.ignoring_advertised = desc.getIgnoringAdvertisedBws();
      }
      if (desc.getKnownFlags() != null && !desc.getKnownFlags().isEmpty()) {
        vote.known_flags = desc.getKnownFlags();
      }
      if (desc.getConsensusParams() != null && !desc.getConsensusParams().isEmpty()) {
        if (verbose) {
          ArrayList<StringInt> verboseConsParams = new ArrayList<>();
          vote.params = new ArrayList<> ();
          SortedMap<String,Integer> params = desc.getConsensusParams();
          for(Map.Entry<String,Integer> para : params.entrySet()) {
            verboseConsParams.add(new StringInt(para.getKey(), para.getValue()));
          }
          vote.params = verboseConsParams;
        } else {
          vote.params = desc.getConsensusParams();
        }
      }
      vote.authority = new Authority();
      vote.authority.nickname = desc.getNickname();
      vote.authority.identity = desc.getIdentity();
      vote.authority.adress = desc.getAddress();
      vote.authority.dir_port = desc.getDirport();
      vote.authority.or_port = desc.getOrport();
      vote.authority.contact = desc.getContactLine();
      vote.authority.legacy_dir_key = desc.getLegacyDirKey();
      vote.authority.key_certificate = new Cert();
      vote.authority.key_certificate.version = desc.getDirKeyCertificateVersion();
      vote.authority.key_certificate.dir_key_published =
              dateTimeFormat.format(desc.getDirKeyPublishedMillis());
      vote.authority.key_certificate.dir_key_expires =
              dateTimeFormat.format(desc.getDirKeyExpiresMillis());
      vote.authority.key_certificate.dir_signing_key = desc.getSigningKeyDigest() != null;
      if (desc.getStatusEntries() != null && !desc.getStatusEntries().isEmpty()) {
        vote.router_status = new ArrayList();
        SortedMap<String,NetworkStatusEntry> statusMap = desc.getStatusEntries();
        for(Map.Entry<String,NetworkStatusEntry> status : statusMap.entrySet()) {
          Router router = new Router();
          router.r = new R();
          router.r.nickname = status.getValue().getNickname();
          router.r.identity = status.getValue().getFingerprint();
          router.r.digest = status.getValue().getDescriptor();
          router.r.publication = dateTimeFormat.format(status.getValue().getPublishedMillis());
          router.r.ip = status.getValue().getAddress();
          router.r.or_port = status.getValue().getOrPort();
          router.r.dir_port = status.getValue().getDirPort();
          if (status.getValue().getOrAddresses() != null &&
                  !status.getValue().getOrAddresses().isEmpty()) {
            router.a = status.getValue().getOrAddresses();
          }
          if (status.getValue().getFlags() != null && !status.getValue().getFlags().isEmpty()) {
            router.s = new ArrayList<>();
            for (String flag : status.getValue().getFlags()) router.s.add(flag);
          }
          router.v = status.getValue().getVersion();
          router.w = new W();
          if (status.getValue().getBandwidth() >= 0) {
            router.w.bandwidth = status.getValue().getBandwidth();
          }
          if (status.getValue().getMeasured() >= 0) {
            router.w.measured_bw = status.getValue().getMeasured();
          }
          router.w.unmeasured_bw = status.getValue().getUnmeasured();
          router.p = new Policy();
          router.p.default_policy = status.getValue().getDefaultPolicy();
          router.p.port_summary = status.getValue().getPortList();
          router.id = status.getValue().getMasterKeyEd25519() != null;
          vote.router_status.add(router);
        }
      }
      if (desc.getDirectorySignatures() != null && !desc.getDirectorySignatures().isEmpty()) {
        vote.directory_footer = new DirFooter();
        vote.directory_footer.directory_signature = new DirSig();
        SortedMap<String,DirectorySignature> dirSigs = desc.getDirectorySignatures();
        for(Map.Entry<String,DirectorySignature> dirSig : dirSigs.entrySet()) {
          vote.directory_footer.directory_signature.algorithm =
                  dirSig.getValue().getAlgorithm();
          vote.directory_footer.directory_signature.identity =
                  dirSig.getValue().getIdentity();
          vote.directory_footer.directory_signature.signing_key_digest =
                  dirSig.getValue().getSigningKeyDigest();
          vote.directory_footer.directory_signature.signature =
                  dirSig.getValue().getSignature() != null;
        }
      }
      return ToJson.serialize(vote);
    }
  }

  //  bridge network status
  static class JsonBridgeNetworkStatus extends JsonDescriptor {
    String descriptor_type;
    String published;
    FlagTreshold flagTreshold;
    static class FlagTreshold {
      Long stable_uptime;
      Long stable_mtbf;
      Integer enough_mtbf;
      Long fast_speed;
      Double guard_wfu;
      Long guard_tk;
      Long guard_bw_inc_exits;
      Long guard_bw_exc_exits;
      Integer ignoring_advertised;
    }
    List<Bridge> bridges;
    static class Bridge {
      R r; // bridge description
      SortedSet<String> s; // flags
      W w; // bandwidths
      String p; // policy
      String a; // port summary
      String v;
    }
    static class R {
      String nickname;
      String identity;
      String digest;
      String date;
      String ip;
      Integer or_port;
      Integer dir_port;
    }
    static class W {
      Long bandwidth;
      Long measured_bw;
      Boolean unmeasured_bw;
    }

    static String convert(BridgeNetworkStatus desc) {
      JsonBridgeNetworkStatus status = new JsonBridgeNetworkStatus();
      for (String annotation : desc.getAnnotations()) {
        status.descriptor_type = annotation.substring("@type ".length());
      }
      status.published = dateTimeFormat.format(desc.getPublishedMillis());
      status.flagTreshold = new FlagTreshold();
      if (desc.getStableUptime() >= 0) {
        status.flagTreshold.stable_uptime = desc.getStableUptime();
      }
      if (desc.getStableMtbf() >= 0) {
        status.flagTreshold.stable_mtbf = desc.getStableMtbf();
      }
      if (desc.getEnoughMtbfInfo() >= 0) {
        status.flagTreshold.enough_mtbf = desc.getEnoughMtbfInfo();
      }
      if (desc.getFastBandwidth() >= 0) {
        status.flagTreshold.fast_speed = desc.getFastBandwidth();
      }
      if (desc.getGuardWfu() >= 0) {
        status.flagTreshold.guard_wfu = desc.getGuardWfu();
      }
      if (desc.getGuardTk() >= 0) {
        status.flagTreshold.guard_tk = desc.getGuardTk();
      }
      if (desc.getGuardBandwidthIncludingExits() >= 0) {
        status.flagTreshold.guard_bw_inc_exits = desc.getGuardBandwidthIncludingExits();
      }
      if (desc.getGuardBandwidthExcludingExits() >= 0) {
        status.flagTreshold.guard_bw_exc_exits = desc.getGuardBandwidthExcludingExits();
      }
      if (desc.getIgnoringAdvertisedBws() >= 0) {
        status.flagTreshold.ignoring_advertised = desc.getIgnoringAdvertisedBws();
      }
      if (desc.getStatusEntries() != null && !desc.getStatusEntries().isEmpty()) {
        status.bridges = new ArrayList<>();
        for (Map.Entry<String, NetworkStatusEntry> entry : desc.getStatusEntries().entrySet()) {
          Bridge b = new Bridge();
          b.r = new R();
          b.r.nickname = entry.getValue().getNickname();
          b.r.identity = entry.getValue().getDescriptor();
          b.r.digest = entry.getValue().getFingerprint();
          b.r.date = dateTimeFormat.format(entry.getValue().getPublishedMillis());
          b.r.ip = entry.getValue().getAddress();
          b.r.or_port = entry.getValue().getOrPort();
          b.r.dir_port = entry.getValue().getDirPort();
          if (entry.getValue().getFlags() != null && !entry.getValue().getFlags().isEmpty()) {
            b.s = entry.getValue().getFlags();
          }
          b.w = new W();
          if (entry.getValue().getBandwidth() >= 0) {
            b.w.bandwidth = entry.getValue().getBandwidth();
          }
          if (entry.getValue().getMeasured() >= 0) {
            b.w.measured_bw = entry.getValue().getMeasured();
          }
          if (entry.getValue().getUnmeasured()) {
            b.w.unmeasured_bw = entry.getValue().getUnmeasured();
          }
          b.p = entry.getValue().getDefaultPolicy();
          b.a = entry.getValue().getPortList();
          b.v = entry.getValue().getVersion();
          status.bridges.add(b);
        }
      }
      return ToJson.serialize(status);
    }
  }

  //  tordnsel
  static class JsonExitList extends JsonDescriptor {
    String descriptor_type;
    Long downloaded;
    List<Entry> relays;
    static class Entry {
      String fingerprint;
      String published;
      String last_status;
      List<Exit> exit_list;
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
      if (desc.getEntries() != null && !desc.getEntries().isEmpty()) {
        tordnsel.relays = new ArrayList<>();
        for(ExitList.Entry exitEntry : desc.getEntries()) {
          Entry entry = new Entry();
          entry.fingerprint = exitEntry.getFingerprint();
          entry.published = dateTimeFormat.format(exitEntry.getPublishedMillis());
          entry.last_status = dateTimeFormat.format(exitEntry.getLastStatusMillis());
          entry.exit_list = new ArrayList<>();
          for ( Map.Entry<String,Long> exitAdress : exitEntry.getExitAddresses().entrySet() ) {

            Exit exit = new Exit();
            exit.ip = exitAdress.getKey();
            exit.date = dateTimeFormat.format(exitAdress.getValue());
            entry.exit_list.add(exit);

          }
          tordnsel.relays.add(entry);
        }
      }
      return ToJson.serialize(tordnsel);
    }
  }

  //  torperf
  static class JsonTorperfResult extends JsonDescriptor {
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
        torperf.dataperc10 = desc.getDataPercentiles().get(10);
        torperf.dataperc20 = desc.getDataPercentiles().get(20);
        torperf.dataperc30 = desc.getDataPercentiles().get(30);
        torperf.dataperc40 = desc.getDataPercentiles().get(40);
        torperf.dataperc50 = desc.getDataPercentiles().get(50);
        torperf.dataperc60 = desc.getDataPercentiles().get(60);
        torperf.dataperc70 = desc.getDataPercentiles().get(70);
        torperf.dataperc80 = desc.getDataPercentiles().get(80);
        torperf.dataperc90 = desc.getDataPercentiles().get(90);
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


  /*  Convert everything to a JSON string and return that.
   *  If flag 'verbose' is set also serialize attributes evaluating to null.
   *  Gson docs: https://google-gson.googlecode.com/svn/trunk/gson/docs/
   *  javadocs/com/google/gson/GsonBuilder.html
   */
  static class ToJson {
    static String serialize(JsonDescriptor jsonDescriptor) {
      Gson gsonBuilder;
      String output;
      if (verbose) {
        gsonBuilder = new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create();
      }
      else {
        gsonBuilder = new GsonBuilder()
                .disableHtmlEscaping()
                .create();
      }
      output = gsonBuilder.toJson(jsonDescriptor);
      return output;
    }
  }

}