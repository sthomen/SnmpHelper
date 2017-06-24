import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.TreeEvent;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

/* 
 * This class is meant to convert a "rich" SNMP4J List<TreeEvent> into a flat Map<String,String> of
 * oid = value. It keeps a reference to the List so that it may be used if you really want to. This
 * operation requires you to import the snmp4j bits however, breaking the quarantine. This is probably
 * of limited usefulness.
 */

class SnmpHelperTree {
	private Map<String,String> contents = new HashMap<String,String>();
	private List<TreeEvent> tree = null;

	SnmpHelperTree(List<TreeEvent> tree) {
		this.tree = tree;
		loadTreeEventList();
	}

	public Map<String,String> getContents() {
		return contents;
	}

	public List<TreeEvent> getTree() {
		return tree;
	}

	public void loadTreeEventList() {
		contents.clear();
		Iterator iter = tree.iterator();

		do {
			TreeEvent te = (TreeEvent)iter.next();

			VariableBinding[] vbe = te.getVariableBindings();

			for (int i = 0;i<vbe.length;i++) {
				contents.put(vbe[i].getOid().toString(), vbe[i].getVariable().toString());
			}
		} while (iter.hasNext());
	}
}
