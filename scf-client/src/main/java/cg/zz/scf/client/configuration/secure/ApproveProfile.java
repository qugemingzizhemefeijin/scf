package cg.zz.scf.client.configuration.secure;

import org.w3c.dom.Node;

public class ApproveProfile {
	
	private String info;
	
	public ApproveProfile(Node node) {
		if (node != null) {
			Node infoNode = node.getAttributes().getNamedItem("info");
			if (infoNode != null) this.info = infoNode.getNodeValue();
		}
	}

	public String getInfo() {
		return this.info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

}
