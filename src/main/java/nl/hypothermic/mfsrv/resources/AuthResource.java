package nl.hypothermic.mfsrv.resources;

import java.net.URLDecoder;

import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.Javalin;
import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.obj.auth.TelephoneNum;

public class AuthResource implements IResource {

	private MFServer server;

	public AuthResource(MFServer instance) {
		this.server = instance;
	}

	@Override public void registerResource(Javalin instance) {
		instance.get("/auth/login", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("country") == null || ctx.queryParam("num") == null || ctx.queryParam("passwd") == null) {
					ctx.result("-1");
				} else {
					try {
						ctx.result(server.database.userLogin(new TelephoneNum(Integer.valueOf(ctx.queryParam("country")),
								                                              Integer.valueOf(ctx.queryParam("num"))),
								                             URLDecoder.decode(ctx.queryParam("passwd"), "UTF-8")) + "");
					} catch (NumberFormatException nfx) {
						ctx.result("-2");
					}
				}
			}
		});
		instance.get("/auth/register", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("country") == null || ctx.queryParam("num") == null || ctx.queryParam("passwd") == null) {
					ctx.result("-1");
				} else {
					try {
						ctx.result(server.database.userRegister(new TelephoneNum(Integer.valueOf(ctx.queryParam("country")),
								                                                 Integer.valueOf(ctx.queryParam("num"))),
								                                URLDecoder.decode(ctx.queryParam("passwd"), "UTF-8")) + "");
					} catch (NumberFormatException nfx) {
						ctx.result("-2");
					}
				}
			}
		});
		instance.get("/auth/verify", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("country") == null || ctx.queryParam("num") == null || ctx.queryParam("verifytoken") == null) {
					ctx.result("-1");
				} else {
					try {
						ctx.result(server.database.userVerify(new TelephoneNum(Integer.valueOf(ctx.queryParam("country")),
								                                                 Integer.valueOf(ctx.queryParam("num"))),
								                                Integer.valueOf((ctx.queryParam("verifytoken")))) + "");
					} catch (NumberFormatException nfx) {
						ctx.result("-2");
					}
				}
			}
		});
		instance.get("/auth/sync", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("country") == null || ctx.queryParam("num") == null || ctx.queryParam("token") == null) {
					ctx.result("-1");
				} else {
					try {
						ctx.result(server.database.isSessionTokenValid(new TelephoneNum(Integer.valueOf(ctx.queryParam("country")),
								                                                        Integer.valueOf(ctx.queryParam("num"))),
								                                       Integer.valueOf(ctx.queryParam("token")))
								   ? "1" : "0");
					} catch (NumberFormatException nfx) {
						ctx.result("-2");
					}
				}
			}
		});
	}
}
