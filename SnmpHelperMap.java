/* Auth and Priv maps return OIDs */
import org.snmp4j.smi.OID;

/* Type map maps to SMIConstant integers */
import org.snmp4j.smi.SMIConstants;

/* some common authentication mechanisms */
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.AuthSHA2;

/* some common privacy mechanisms */
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.PrivAES;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import java.util.Map;
import java.util.HashMap;

class SnmpHelperMap {
	private final static Map<String,OID> authmap=createAuthMap();
	private final static Map<String,OID> privmap=createPrivMap();
	private final static Map<String,Integer> typemap=createTypeMap();

	private static Map<String,OID> createAuthMap() {
		Map<String,OID> map=new HashMap<String,OID>();

		map.put("md5", AuthMD5.ID);
		map.put("sha", AuthSHA.ID);
// XXX AuthSHA2 does not have an ID field, so static access is impossible
//		map.put("sha2", AuthSHA2.ID);

		return map;
	}

	public static String[] getAvailableAuthMethods() {
		return (String[])SnmpHelperMap.authmap.keySet().toArray();
	}

	public static OID getAuthID(String id) {
		return SnmpHelperMap.authmap.get(id);
	}


	private static Map<String,OID> createPrivMap() {
		Map<String,OID> map=new HashMap<String,OID>();

		map.put("des", PrivDES.ID);
		map.put("3des", Priv3DES.ID);
// XXX PrivAES does not have an ID field, so static access is impossible
//		map.put("aes", PrivAES.ID);
		map.put("aes128", PrivAES128.ID);
		map.put("aes192", PrivAES192.ID);
		map.put("aes256", PrivAES256.ID);

		return map;
	}

	public static String[] getAvailablePrivMethods() {
		return (String[])SnmpHelperMap.privmap.keySet().toArray();
	}

	public static OID getPrivID(String id) {
		return SnmpHelperMap.privmap.get(id);
	}

	private static Map<String,Integer> createTypeMap() {
		Map<String,Integer> map=new HashMap<String,Integer>();

		map.put("bits", SMIConstants.SYNTAX_BITS);
		map.put("counter32", SMIConstants.SYNTAX_COUNTER32);
		map.put("counter64", SMIConstants.SYNTAX_COUNTER64);
		map.put("gauge32", SMIConstants.SYNTAX_GAUGE32);
		map.put("integer", SMIConstants.SYNTAX_INTEGER);
		map.put("integer32", SMIConstants.SYNTAX_INTEGER32);
		map.put("ipaddress", SMIConstants.SYNTAX_IPADDRESS);
		map.put("null", SMIConstants.SYNTAX_NULL);
		map.put("oid", SMIConstants.SYNTAX_OBJECT_IDENTIFIER);
		map.put("octet string", SMIConstants.SYNTAX_OCTET_STRING);
		map.put("opaque", SMIConstants.SYNTAX_OPAQUE);
		map.put("timeticks", SMIConstants.SYNTAX_TIMETICKS);
		map.put("unsigned integer32", SMIConstants.SYNTAX_UNSIGNED_INTEGER32);

		return map;
	}

	public static Integer getType(String name) {
		return SnmpHelperMap.typemap.get(name);
	}
}
