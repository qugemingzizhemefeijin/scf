package cg.zz.scf.server.core.communication.http;

public class Controler {
	
	private Action getAction;
	
	private Action postAction;
	
	private Action deleteAction;
	
	private Action putAction;
	
	private Action headAction;

	public Action getGetAction() {
		return getAction;
	}

	public void setGetAction(Action getAction) {
		this.getAction = getAction;
	}

	public Action getPostAction() {
		return postAction;
	}

	public void setPostAction(Action postAction) {
		this.postAction = postAction;
	}

	public Action getDeleteAction() {
		return deleteAction;
	}

	public void setDeleteAction(Action deleteAction) {
		this.deleteAction = deleteAction;
	}

	public Action getPutAction() {
		return putAction;
	}

	public void setPutAction(Action putAction) {
		this.putAction = putAction;
	}

	public Action getHeadAction() {
		return headAction;
	}

	public void setHeadAction(Action headAction) {
		this.headAction = headAction;
	}

}
