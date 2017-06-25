package net.shangtai.snmphelper;

import org.snmp4j.Target;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.util.PDUFactory;

class SnmpHelperPDUFactory implements PDUFactory {
	public PDU createPDU(Target target) {
		PDU request;
		if (target.getVersion() == SnmpConstants.version3) {
			request = new ScopedPDU();
			request.setType(PDU.GETBULK);
		} else {
			request = new PDU();
			request.setType(PDU.GETNEXT);
		}
		return request;
	}

	public PDU createPDU(MessageProcessingModel messageProcessingModel) {
		return createPDU((Target)null);
	}
}
