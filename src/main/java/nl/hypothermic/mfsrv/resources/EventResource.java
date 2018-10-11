package nl.hypothermic.mfsrv.resources;

import java.net.URLDecoder;

import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.Javalin;
import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.obj.account.Account;
import nl.hypothermic.mfsrv.obj.auth.TelephoneNum;
import nl.hypothermic.mfsrv.util.UserInputCleaner;

public class EventResource implements IResource {

	private MFServer server;

	public EventResource(MFServer instance) {
		this.server = instance;
	}

	@Override public void registerResource(Javalin instance) {
		instance.get("/event/get", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("token") == null || ctx.queryParam("id") == null) {
					ctx.result("-1");
				} else {
					try {
						if (server.database.isSessionTokenValid(null, Integer.valueOf(ctx.queryParam("token")))) {
							ctx.result("1");
						} else {
							ctx.result("-9");
						}
					} catch (NumberFormatException nfx) {
						ctx.result("-2");
					} catch (NullPointerException npe) {
						ctx.result("0");
					}
				}
			}
		});
	}
}
