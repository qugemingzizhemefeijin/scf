package cg.zz.scf.client.configuration.secure;

import org.w3c.dom.Node;

/**
 * 授权文件属性类
 *
 */
public class KeyProfile {
	
	/**
	 * 授权文件
	 */
	private String info;
	
	public KeyProfile(Node node) {
		if (node != null) {
			Node infoNode = node.getAttributes().getNamedItem("info");
			if (infoNode != null)
				this.info = infoNode.getNodeValue();
		}
	}

	public String getInfo() {
		return this.info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

}
