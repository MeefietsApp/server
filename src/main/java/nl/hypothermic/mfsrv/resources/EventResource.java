package nl.hypothermic.mfsrv.resources;

import com.auth0.jwt.internal.com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.Javalin;
import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.obj.event.Event;
import nl.hypothermic.mfsrv.obj.event.EventType;
import nl.hypothermic.mfsrv.obj.event.InvalidEventTypeException;
import nl.hypothermic.mfsrv.obj.event.MeefietsEvent;

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
							Event event = server.database.getEvent(Integer.valueOf(ctx.queryParam("id")));
							if (event != null) {
								ctx.result("1" + event.toSerializedString());
							} else {
								ctx.result("0");
							}
						} else {
							ctx.result("-9");
						}
					} catch (NumberFormatException | NullPointerException x) {
						ctx.result("-2");
					}
				}
			}
		});
		instance.get("/event/create", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("token") == null || ctx.queryParam("type") == null) {
					ctx.result("-1");
				} else {
					try {
						if (server.database.isSessionTokenValid(null, Integer.valueOf(ctx.queryParam("token")))) {
							//legacy//ctx.result(server.database.createEvent(EventType.fromInt(Integer.valueOf(ctx.queryParam("type")))) + "");
							ctx.result(server.database.registerEvent(Event.fromType(EventType.fromInt(Integer.valueOf(ctx.queryParam("type"))))
									                                      .fromJavalinCtx(ctx))
									   + "");
						} else {
							ctx.result("-9");
						}
					} catch (NumberFormatException | NullPointerException x) {
						ctx.result("-2");
					} catch (InvalidEventTypeException iete) {
						ctx.result("-6");
					}
				}
			}
		});
	}
}
