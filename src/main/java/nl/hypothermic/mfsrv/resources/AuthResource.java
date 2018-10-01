package nl.hypothermic.mfsrv.resources;

import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.Javalin;

public class AuthResource implements IResource {

	@Override public void registerResource(Javalin instance) {
		instance.get("/auth/isAuthenticated", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("token") != null) {
					
				}
				ctx.result("0");
			}
		});
	}
}
