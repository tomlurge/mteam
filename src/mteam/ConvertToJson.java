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

    // TL did this to start generalization
    String currentDescriptorCollection ="bridge-server-descriptors-2015-10";

    DescriptorReader descriptorReader = DescriptorSourceFactory.createDescriptorReader();
    descriptorReader.addTarball(new File("in/" + currentDescriptorCollection + ".tar"));
    Iterator<DescriptorFile> descriptorFiles = descriptorReader.readDescriptors();

    int written = 0;
    BufferedWriter bw = new BufferedWriter(new FileWriter(currentDescriptorCollection + ".json"));
    // TL Drill expects one doc per line, without semicolon or surrounding object
    // bw.write("{\"descriptors\": [\n");
    while (descriptorFiles.hasNext()) {
      DescriptorFile descriptorFile = descriptorFiles.next();
      for (Descriptor descriptor : descriptorFile.getDescriptors()) {
        String jsonDescriptor = null;

        if (descriptor instanceof ServerDescriptor) { // TL what else could it be and where is that defined?
          jsonDescriptor = convertServerDescriptor((ServerDescriptor) descriptor);
        }
        /* Could add more else-if statements here. */ // TL for example?
        if (jsonDescriptor != null) {
          // TL see above
          // bw.write((written++ > 0 ? ",\n" : "") + jsonDescriptor);
          bw.write((written++ > 0 ? "\n" : "") + jsonDescriptor);
        }
      }
    }
    // TL see above
    // bw.write("\n]\n}\n");
    bw.close();
  }

  /* Inner class to serialize address/port combinations in "or_address"
   * lines or others.  In theory, we could also include those as
   * strings. */
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
    String date; // required, format is YYYY-MM-DD HH:MM:SS
    long interval; // required, seconds
    Collection<Long> bytes; // required
  }

  /* Inner class to serialize bridge server descriptors. */
  static class JsonBridgeServerDescriptor {
    String descriptor_type; // set to bridge-server-descriptor $VERSION
    String nickname; // required, can be mixed-case
    String address; // required, changed to lower-case
    int or_port; // required
    int socks_port; // required, most likely 0 except for *very* old descs
    int dir_port; // required
    Integer bandwidth_avg; // required
    Integer bandwidth_burst; // required
    Integer bandwidth_observed; // optional, missing in older descriptors!
    List<AddressAndPort> or_addresses; // addresses sanitized!
    String platform; // optional, though usually set
    String published; // format YYYY-MM-DD HH:MM:SS
    String fingerprint; // always upper-case hex
    Boolean hibernating; // optional
    Long uptime; // optional, though usually set
    Boolean onion_key; // required; usually false b/c sanitization
    Boolean signing_key; // required; usually false b/c sanitization
    List<String> exit_policy; // required
    String ipv6_policy; // optional
    String contact; // optional
    List<String> family; // optional, apparently not used at all
    BandwidthHistory read_history; // optional
    BandwidthHistory write_history; // optional
    Boolean eventdns;
    Boolean caches_extra_info;
    String extra_info_digest; // optional!, upper-case hex
    List<Integer> hidden_service_dir_versions;
    List<Integer> link_protocol_versions;
    List<Integer> circuit_protocol_versions;
    Boolean allow_single_hop_exits;
    Boolean ntor_onion_key;
    String router_digest; // upper-case hex
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

  /* Take a single server descriptor, assume it's a *bridge* server
   * descriptor, and return a JSON string representation for it. */
  static String convertServerDescriptor(ServerDescriptor desc) {                // DESC
    JsonBridgeServerDescriptor json = new JsonBridgeServerDescriptor();         // JSON

    /* Find the @type annotation and include its content. */
    for (String annotation : desc.getAnnotations()) {
      if (annotation.startsWith("@type ")) {
        json.descriptor_type = annotation.substring("@type ".length());
      }
    }

    json.nickname = desc.getNickname();
    json.address = desc.getAddress();
    json.or_port = desc.getOrPort();
    json.socks_port = desc.getSocksPort();
    json.dir_port = desc.getDirPort();

    json.or_addresses = new ArrayList<AddressAndPort>();
    /* If there are any or-addresses, include them in a list. */
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

    /* Include a bandwidth object with average, burst, and possibly
     * observed bandwidth. */
    json.bandwidth_avg = desc.getBandwidthRate();  // TL why no check for null here?
    json.bandwidth_burst = desc.getBandwidthBurst();
    if (desc.getBandwidthObserved() >= 0) {
      json.bandwidth_observed = desc.getBandwidthObserved();
    }
    else { json.bandwidth_observed =  null; } // TL added null

    /* Include a few more fields, some of them only if they're not
     * null. */  // TL why not just omit the !null check? will that lead to null-pointer-exceptions?
    if (desc.getExtraInfoDigest() != null) {
      json.extra_info_digest = desc.getExtraInfoDigest().toUpperCase();
    }
    if (desc.getPlatform() != null) {
      json.platform = desc.getPlatform();
    }
    json.published = dateTimeFormat.format(desc.getPublishedMillis()); // TL why no null check here?
    json.fingerprint = desc.getFingerprint().toUpperCase();
    if (desc.isHibernating()) {  // TL why no explicit null check here?
      json.hibernating = desc.isHibernating();
    }
    if (desc.getUptime() != null) {
      json.uptime = desc.getUptime();
    }
    json.onion_key = desc.getOnionKey() != null;
    json.signing_key = desc.getSigningKey() != null;
    if (desc.getExitPolicyLines() != null &&
        !desc.getExitPolicyLines().isEmpty()) {
      json.exit_policy = desc.getExitPolicyLines();
    }

    if (desc.getIpv6DefaultPolicy() != null &&
            !desc.getIpv6DefaultPolicy().isEmpty()) {
        json.ipv6_policy = desc.getIpv6DefaultPolicy();
    }

    /* Include bandwidth histories using their own helper method. */
    if (desc.getWriteHistory() != null) {
      json.write_history = convertBandwidthHistory(
          desc.getWriteHistory());
    }
    if (desc.getReadHistory() != null) {
      json.read_history = convertBandwidthHistory(
          desc.getReadHistory());
    }

    /* Include more fields. */
    json.contact = desc.getContact();  // TL why no null checks here?
    json.family = desc.getFamilyEntries();
    json.eventdns = desc.getUsesEnhancedDnsLogic();
    json.caches_extra_info = desc.getCachesExtraInfo();
    json.hidden_service_dir_versions = desc.getHiddenServiceDirVersions();
    json.link_protocol_versions = desc.getLinkProtocolVersions();
    json.circuit_protocol_versions = desc.getCircuitProtocolVersions();
    json.allow_single_hop_exits = desc.getAllowSingleHopExits();
    json.ntor_onion_key = desc.getNtorOnionKey() != null;
    json.router_digest = desc.getServerDescriptorDigest().toUpperCase();

    /* Convert everything to a JSON string and return that. */
    Gson gson = new GsonBuilder().serializeNulls().create();
    return gson.toJson(json);
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
}
