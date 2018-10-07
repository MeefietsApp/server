package nl.hypothermic.mfsrv;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import io.javalin.Javalin;
import nl.hypothermic.api.NexmoHooks;
import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.database.IDatabaseHandler;
import nl.hypothermic.mfsrv.database.TempDatabase;
import nl.hypothermic.mfsrv.resources.AccountResource;
import nl.hypothermic.mfsrv.resources.AuthResource;
import nl.hypothermic.mfsrv.resources.IResource;

public class MFServer {

	public static final double SERVER_VERSION = 1.01;
	public static final long SESSION_TIMEOUT = 300000; // ms

	private final Javalin instance;
	public final NexmoHooks nexmo;
	private final IResource[] resources = {
			(IResource) new AuthResource(this),
			(IResource) new AccountResource(this),
	};

	public final IDatabaseHandler database = new TempDatabase(this);
	private final ConfigHandler cfg;

	private static final AtomicInteger counter = new AtomicInteger();
	public static final ExecutorService threadpool = Executors.newCachedThreadPool(new ThreadFactory() {
		public Thread newThread(Runnable r) {
			return new Thread(r, "MFSRV-" + counter.incrementAndGet());
		}
	});

	public static void main(String[] args) throws Exception {
		new MFServer(args).start();
	}

	public MFServer(String[] args) throws IOException {
		cfg = new ConfigHandler();
		instance = Javalin.create().server(new Supplier<Server>() {
			@Override public Server get() {
			    Server server = new Server();
			    ServerConnector serverConnector = new ServerConnector(server);
			    serverConnector.setHost(ConfigHandler.instance.getStringOrCrash("srv_address"));
			    serverConnector.setPort(Integer.valueOf(ConfigHandler.instance.getStringOrCrash("srv_port")));
			    server.setConnectors(new Connector[]{
			    		serverConnector
			    });
			    return server;
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					MFLogger.log(this, "Database ontkoppelen...");
					database.eventServletStop();
				} catch (Exception x) {
					MFLogger.err(this, "Fout bij ontkoppelen database:");
					x.printStackTrace();
				}
				MFLogger.log(this, "REST-server stoppen...");
				instance.stop();
				MFLogger.log(this, "MF-server is gestopt.");
			}
		});
		nexmo = new NexmoHooks(ConfigHandler.instance.getStringOrCrash("nexmo_key"),
							   ConfigHandler.instance.getStringOrCrash("nexmo_secret"));
	}

	public void start() throws Exception {
		MFLogger.log(this, "Database aan het laden...");
		database.eventServletStart();
		MFLogger.log(this, "REST-server starten...");
		instance.start(7000);
		for (IResource iter : resources) {
			iter.registerResource(instance);
		}
		MFLogger.log(this, "Gereed voor gebruik.");
	}
}
