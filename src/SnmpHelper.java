package net.shangtai.snmphelper;

import org.snmp4j.*;
import org.snmp4j.smi.*;
import org.snmp4j.security.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.MPv3;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/* used for walking */
import org.snmp4j.util.TreeUtils;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.DefaultPDUFactory;

import java.util.List;

import java.io.IOException;

class SnmpHelper {
	private TransportMapping transport=null;
	private Snmp snmp=null;

	// all
	private Integer version=SnmpConstants.version2c;
	private Address address=null;

	// v1/v2c settings
	private OctetString community=new OctetString("public");
	private CommunityTarget v2ctarget=null;

	// v3 settings
	private USM usm=null;
	private UserTarget v3target=null;

	private OctetString username=null;

	private OID authhash=null;
	private OctetString auth_password=null;
	private OID privhash=null;
	private OctetString priv_password=null;

	SnmpHelper() throws IOException {
		transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);

		transport.listen();
	}

	/*************************************************************************
	 * common
	 ************************************************************************/

	public static Integer strToVersion(String str) {
		if (str == "1") {
			return SnmpConstants.version1;
		} else if (str == "2c") {
			return SnmpConstants.version2c;
		} else if (str == "3") {
			return SnmpConstants.version3;
		}

		throw new IllegalArgumentException("Couldn't match a version number for the given version string");
	}

	public SnmpHelper setVersion(String str) {
		return setVersion(SnmpHelper.strToVersion(str));
	}

	public SnmpHelper setVersion(int version) {
		this.version=version;
		return this;
	}

	public SnmpHelper setAddress(Address address) {
		this.address=address;
		return this;
	}

	public SnmpHelper setAddress(String address) {
		this.address=GenericAddress.parse(address);
		return this;
	}

	// closes the transport channel, for cleanup
	// endpoint
	public void close() throws IOException {
		transport.close();
	} 


	// endpoint
	public String get(String oidstr) throws IOException {
		return get(new OID(oidstr));
	}

	// endpoint
	public String get(OID oid) throws IOException {
		switch (version) {
			case SnmpConstants.version1:
			case SnmpConstants.version2c:
				return getV2c(oid);
			case SnmpConstants.version3:
				return getV3(oid);
		}

		return null;
	}

	private VariantVariable createVariable(String type, int value) {
		/* FIXME This should really just be vv.setValue(value) but somehow snmp4j breaks
		 * with a cryptic ClassCastException (cannot assign to 0) when value is an integer
		 */

		return createVariable(type, String.valueOf(value));
	}

	private VariantVariable createVariable(String type, String value) {
		Variable variable = AbstractVariable.createFromSyntax(SnmpHelperMap.getType(type));
		VariantVariable vv = new VariantVariable(variable);
		vv.setValue(value);

		return vv;
	}

	// endpoint
	public void set(String oidstr, String type, String value) throws IOException {
		set(new OID(oidstr), createVariable(type, value));
	}

	// endpoint
	public void set(OID oid, String type, String value) throws IOException {
		set(oid, createVariable(type, value));
	}

	// endpoint
	public void set(String oidstr, String type, int value) throws IOException {
		set(new OID(oidstr), createVariable(type, value));
	}

	// endpoint
	public void set(OID oid, String type, int value) throws IOException {
		set(oid, createVariable(type, value));
	}

	// endpoint
	public void set(OID oid, Variable value) throws IOException {
		switch (version) {
			case SnmpConstants.version1:
			case SnmpConstants.version2c:
				setV2c(oid, value);
				break;
			case SnmpConstants.version3:
				setV3(oid, value);
				break;
		}
	}

	// endpoint
	public SnmpHelperTree walk(String oidstr) throws IOException {
		return walk(new OID(oidstr));
	}

	// endpoint
	public SnmpHelperTree walk(OID root) throws IOException {
		switch (version) {
			case SnmpConstants.version1:
			case SnmpConstants.version2c:
				return walkV2c(root);
			case SnmpConstants.version3:
				return walkV3(root);
		}

		return null;
	}

	/*************************************************************************
	 * v2c
	 ************************************************************************/

	public SnmpHelper setCommunity(String community) {
		this.community.setValue(community);
		return this;
	}

	// Initializes a target (if required) for the get/set/walk methods
	public SnmpHelper initializeV2cTarget() {
		if (community != null && address != null) {
			v2ctarget = new CommunityTarget();
			v2ctarget.setCommunity(new OctetString(community));
			v2ctarget.setVersion(version);
			v2ctarget.setAddress(address);
		}

		return this;
	}

	// endpoint
	protected String getV2c(OID oid) throws IOException {
		String result=null;

		initializeV2cTarget();

		PDU pdu = new PDU();
		pdu.setType(PDU.GET);
		pdu.add(new VariableBinding(oid));

		ResponseEvent re = snmp.send(pdu, v2ctarget);
		PDU rpdu = re.getResponse();

		if (rpdu != null) {
			result=rpdu.getVariable(oid).toString();
		}

		return result;
	}

	// endpoint
	protected void setV2c(OID oid, Variable value) throws IOException {
		initializeV2cTarget();

		PDU pdu = new PDU();
		pdu.setType(PDU.SET);

		pdu.add(new VariableBinding(oid, value));
		snmp.send(pdu, v2ctarget);
	}

	// endpoint
	protected SnmpHelperTree walkV2c(OID root) throws IOException {
		initializeV2cTarget();

		TreeUtils tu=new TreeUtils(snmp, new DefaultPDUFactory());
		List<TreeEvent> tree=tu.getSubtree(v2ctarget, root);

		return new SnmpHelperTree(tree);
	}

	/*************************************************************************
	 * v3
	 ************************************************************************/

	// Initializes a target (if required) for the get/set/walk methods
	public SnmpHelper initializeV3Target() {
		if (usm == null) {
			usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
			SecurityModels.getInstance().addSecurityModel(usm);
		}

		if (username != null && address != null) {
			UsmUserEntry user = snmp.getUSM().getUser(snmp.getUSM().getLocalEngineID(), username);

			if (user == null) {
				usm.addUser(username,
					new UsmUser(username,
						authhash,
						auth_password,
						privhash,
						priv_password));
			}

			v3target = new UserTarget();

			v3target.setAddress(address);
			v3target.setVersion(SnmpConstants.version3);

			// XXX Why the hell isn't this derived from the UsmUser defined above!
			v3target.setSecurityName(username);

			if (authhash == null && privhash == null)
				v3target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);

			if (authhash != null && privhash == null)
				v3target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);

			if (authhash != null && privhash != null)
				v3target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
		}

		return this;
	}

	public SnmpHelper setUsername(String username) {
		if (username == null) {
			this.username = null;
		} else {
			if (this.username == null)
				this.username = new OctetString();

			this.username.setValue(username);
		}
		return this;
	}

	public SnmpHelper setAuthHash(String name) {
		setAuthHash(SnmpHelperMap.getAuthID(name));
		return this;
	}
	

	public SnmpHelper setAuthHash(OID id) {
		authhash = id;
		return this;
	}

	public SnmpHelper setAuthPassword(String password) {
		if (password == null) {
			auth_password = null;
		} else {
			if (auth_password == null)
				auth_password = new OctetString();

			auth_password.setValue(password);
		}
		return this;
	}

	public SnmpHelper setPrivHash(String name) {
		setPrivHash(SnmpHelperMap.getPrivID(name));
		return this;
	}

	public SnmpHelper setPrivHash(OID id) {
		authhash = id;
		return this;
	}

	public SnmpHelper setPrivPassword(String password) {
		if (password == null) {
			priv_password = null;
		} else {
			if (priv_password == null)
				priv_password = new OctetString();

			priv_password.setValue(password);
		}
		return this;
	}

	// endpoint
	protected String getV3(String oidstr) throws IOException {
		return getV3(new OID(oidstr));
	}

	// endpoint
	protected String getV3(OID oid) throws IOException {
		String result=null;

		initializeV3Target();

		PDU pdu = new ScopedPDU();
		pdu.setType(PDU.GET);
		pdu.add(new VariableBinding(oid));

		ResponseEvent re = snmp.send(pdu, v3target);
		PDU rpdu = re.getResponse();

		if (rpdu != null) {
			result=rpdu.getVariable(oid).toString();
		}

		return result;
	}

	// endpoint
	protected void setV3(OID oid, Variable value) throws IOException {
		initializeV3Target();

		PDU pdu = new ScopedPDU();
		pdu.setType(PDU.SET);

		pdu.add(new VariableBinding(oid, value));
		snmp.send(pdu, v3target);
	}

	protected SnmpHelperTree walkV3(OID root) throws IOException {
		initializeV3Target();

		TreeUtils tu=new TreeUtils(snmp, new SnmpHelperPDUFactory());
		List<TreeEvent> tree=tu.getSubtree(v3target, root);

		return new SnmpHelperTree(tree);
	}
}
