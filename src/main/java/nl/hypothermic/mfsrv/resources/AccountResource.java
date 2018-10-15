package nl.hypothermic.mfsrv.resources;

import java.net.URLDecoder;

import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.Javalin;
import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.obj.account.Account;
import nl.hypothermic.mfsrv.obj.auth.TelephoneNum;
import nl.hypothermic.mfsrv.util.UserInputCleaner;

public class AccountResource implements IResource {

	private MFServer server;

	public AccountResource(MFServer instance) {
		this.server = instance;
	}

	@Override public void registerResource(Javalin instance) {
		instance.get("/account/get", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("token") == null || ctx.queryParam("targetcountry") == null || ctx.queryParam("targetnum") == null) {
					ctx.result("-1");
				} else {
					try {
						if (server.database.isSessionTokenValid(null, Integer.valueOf(ctx.queryParam("token")))) {
							ctx.result("1" + server.database.getAccount(new TelephoneNum(Integer.valueOf(ctx.queryParam("targetcountry")),
                                                                                         Integer.valueOf(ctx.queryParam("targetnum")))).toSerializedString());
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
		instance.get("/account/contacts/get", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("country") == null || ctx.queryParam("num") == null || ctx.queryParam("token") == null) {
					ctx.result("-1");
				} else {
					try {
						TelephoneNum num = new TelephoneNum(Integer.valueOf(ctx.queryParam("country")),
	                                                        Integer.valueOf(ctx.queryParam("num")));
						if (server.database.isSessionTokenValid(num, Integer.valueOf(ctx.queryParam("token")))) {
							ctx.result("1" + server.database.getContacts(num).toSerializedString());
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
		instance.get("/account/contacts/add", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("country") == null || ctx.queryParam("num") == null || ctx.queryParam("token") == null
				    || ctx.queryParam("targetcountry") == null || ctx.queryParam("targetnum") == null) {
					ctx.result("-1");
				} else {
					try {
						TelephoneNum num = new TelephoneNum(Integer.valueOf(ctx.queryParam("country")),
	                                                        Integer.valueOf(ctx.queryParam("num")));
						TelephoneNum target = new TelephoneNum(Integer.valueOf(ctx.queryParam("targetcountry")),
                                							   Integer.valueOf(ctx.queryParam("targetnum")));
						if (server.database.isSessionTokenValid(num, Integer.valueOf(ctx.queryParam("token")))) {
							ctx.result(server.database.addContact(num, target) + "");
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
		instance.get("/account/contacts/delete", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				if (ctx.queryParam("country") == null || ctx.queryParam("num") == null || ctx.queryParam("token") == null
				    || ctx.queryParam("targetcountry") == null || ctx.queryParam("targetnum") == null) {
					ctx.result("-1");
				} else {
					try {
						TelephoneNum num = new TelephoneNum(Integer.valueOf(ctx.queryParam("country")),
	                                                        Integer.valueOf(ctx.queryParam("num")));
						TelephoneNum target = new TelephoneNum(Integer.valueOf(ctx.queryParam("targetcountry")),
                                							   Integer.valueOf(ctx.queryParam("targetnum")));
						if (server.database.isSessionTokenValid(num, Integer.valueOf(ctx.queryParam("token")))) {
							ctx.result(server.database.deleteContact(num, target) + "");
						} else {
							ctx.result("-9");
						}
					} catch (NumberFormatException nfx) {
						ctx.result("-2");
					} catch (NullPointerException npe) {
						ctx.result("-3");
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
			}
		});
		instance.get("/account/manage/setname", new Handler() {
			@Override public void handle(Context ctx) throws Exception {
				// NOTE: controleer het telnummer omdat isSessionTokenValid met alleen de token param
				//       niet kan controleren of dit de gebruiker is die de username verandert.
				if (ctx.queryParam("country") == null || ctx.queryParam("num") == null || ctx.queryParam("token") == null || ctx.queryParam("value") == null) {
					ctx.result("-1");
				} else {
					try {
						TelephoneNum num = new TelephoneNum(Integer.valueOf(ctx.queryParam("country")),
								                            Integer.valueOf(ctx.queryParam("num")));
						if (server.database.isSessionTokenValid(num, Integer.valueOf(ctx.queryParam("token")))) {
							Account modifiable = server.database.getAccount(num);
							modifiable.userName = UserInputCleaner.clean(ctx.queryParam("value"));
							modifiable.toFile();
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
