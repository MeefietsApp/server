package nl.hypothermic.mfsrv;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.apache.commons.codec.DecoderException;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import io.javalin.Javalin;
import nl.hypothermic.api.NexmoHooks;
import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.database.IDatabaseHandler;
import nl.hypothermic.mfsrv.database.TempDatabase;
import nl.hypothermic.mfsrv.obj.NetArrayList;
import nl.hypothermic.mfsrv.obj.account.Account;
import nl.hypothermic.mfsrv.obj.auth.TelephoneNum;
import nl.hypothermic.mfsrv.resources.AccountResource;
import nl.hypothermic.mfsrv.resources.AuthResource;
import nl.hypothermic.mfsrv.resources.EventResource;
import nl.hypothermic.mfsrv.resources.IResource;

public class MFServer {

	public static final double SERVER_VERSION = 1.04;
	public static final long SESSION_TIMEOUT = 300000; // ms

	private final Javalin instance;
	public final NexmoHooks nexmo;
	private final IResource[] resources = {
			(IResource) new AuthResource(this),
			(IResource) new AccountResource(this),
			(IResource) new EventResource(this),
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
			    
			    if (Boolean.valueOf(ConfigHandler.instance.getStringOrCrash("http_enable"))) {
			    	ServerConnector httpConnector = new ServerConnector(server);
			    	httpConnector.setHost(ConfigHandler.instance.getStringOrCrash("srv_address"));
			    	httpConnector.setPort(ConfigHandler.instance.getIntegerOrCrash("http_port"));
			    	
			    	server.addConnector(httpConnector);
			    	MFLogger.log(this, "HTTP ingeschakeld op " + httpConnector.getPort());
			    }
			    
			    if (Boolean.valueOf(ConfigHandler.instance.getStringOrCrash("https_enable"))) {
			    	int port = ConfigHandler.instance.getIntegerOrCrash("https_port");
			    	
			    	HttpConfiguration http_config = new HttpConfiguration();
			        http_config.setSecureScheme("https");
			        http_config.setSecurePort(port);
			        
			    	SslContextFactory sslContextFactory = new SslContextFactory();
		        	sslContextFactory.setKeyStorePath(ConfigHandler.instance.getStringOrCrash("https_keystore_path"));
		        	sslContextFactory.setKeyStorePassword(ConfigHandler.instance.getStringOrCrash("https_keystore_password"));
		        	
		        	HttpConfiguration https_config = new HttpConfiguration(http_config);
		            https_config.addCustomizer(new SecureRequestCustomizer());
		        	
		        	ServerConnector sslConnector = new ServerConnector(server,
		        			new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
		        			new HttpConnectionFactory(https_config));
		        	sslConnector.setHost(ConfigHandler.instance.getStringOrCrash("srv_address"));
		            sslConnector.setPort(port);
		            server.addConnector(sslConnector);
		            MFLogger.log(this, "HTTPS ingeschakeld op " + sslConnector.getPort());
			    }
			    
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
		instance.start();
		for (IResource iter : resources) {
			iter.registerResource(instance);
		}
		MFLogger.log(this, "Gereed voor gebruik.");
	}
}
