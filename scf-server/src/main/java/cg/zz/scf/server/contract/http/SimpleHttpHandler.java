package cg.zz.scf.server.contract.http;

/**
 * 简单的http处理类
 * @author chengang
 *
 */
public abstract class SimpleHttpHandler implements IHttpHandler {

	@Override
	public void get(HttpContext context) {
		context.getResponse().setCode(404);
	}

	@Override
	public void post(HttpContext context) {
		context.getResponse().setCode(404);
	}

	@Override
	public void put(HttpContext context) {
		context.getResponse().setCode(404);
	}

	@Override
	public void delete(HttpContext context) {
		context.getResponse().setCode(404);
	}

	@Override
	public void head(HttpContext context) {
		context.getResponse().setCode(404);
	}

}
