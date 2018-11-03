package nl.hypothermic.mfsrv.resources;

import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.Javalin;
import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.obj.auth.TelephoneNum;
import nl.hypothermic.mfsrv.obj.event.Event;
import nl.hypothermic.mfsrv.obj.event.EventType;
import nl.hypothermic.mfsrv.obj.event.InvalidEventTypeException;

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
							Event event = server.database.getEvent(Integer.valueOf(ctx.queryParam("id"))).getSanitized();
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
		instance.get("/event/adduser", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("token") == null || ctx.queryParam("id") == null 
					|| ctx.queryParam("targetcountry") == null || ctx.queryParam("targetnum") == null) {
					ctx.result("-1");
				} else {
					try {
						if (server.database.isSessionTokenValid(null, Integer.valueOf(ctx.queryParam("token")))) {
							ctx.result(server.database.addUserEvent(Integer.valueOf(ctx.queryParam("id")),
									                                new TelephoneNum(Integer.valueOf(ctx.queryParam("targetcountry")),
		                                                            Integer.valueOf(ctx.queryParam("targetnum"))))
									   + "");
						} else {
							ctx.result("-9");
						}
					} catch (NumberFormatException | NullPointerException x) {
						ctx.result("-2");
					}
				}
			}
		});
		instance.get("/event/deluser", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("token") == null || ctx.queryParam("id") == null
					// Gebruikers kunnen alleen hun eigen user-event-entry verwijderen (dus geen target* params)
					|| ctx.queryParam("country") == null || ctx.queryParam("num") == null) {
					ctx.result("-1");
				} else {
					try {
						TelephoneNum user = new TelephoneNum(Integer.valueOf(ctx.queryParam("country")),
	                                                         Integer.valueOf(ctx.queryParam("num")));
						if (server.database.isSessionTokenValid(user, Integer.valueOf(ctx.queryParam("token")))) {
							ctx.result(server.database.deleteUserEvent(Integer.valueOf(ctx.queryParam("id")), 
									                                   user)
									   + "");
						} else {
							ctx.result("-9");
						}
					} catch (NumberFormatException | NullPointerException x) {
						ctx.result("-2");
					}
				}
			}
		});
	}
}
