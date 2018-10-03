package nl.hypothermic.mfsrv.resources;

import java.net.URLDecoder;

import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.Javalin;
import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.obj.TelephoneNum;

public class AccountResource implements IResource {

	private MFServer server;

	public AccountResource(MFServer instance) {
		this.server = instance;
	}

	@Override public void registerResource(Javalin instance) {
		instance.get("/account/getname", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("country") == null || ctx.queryParam("num") == null || ctx.queryParam("token") == null) {
					ctx.result("-1");
				} else {
					try {
						// TODO
					} catch (NumberFormatException nfx) {
						ctx.result("-2");
					}
				}
			}
		});
	}
}
