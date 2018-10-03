package nl.hypothermic.mfsrv;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.javalin.Javalin;
import nl.hypothermic.api.NexmoHooks;
import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.database.IDatabaseHandler;
import nl.hypothermic.mfsrv.database.TempDatabase;
import nl.hypothermic.mfsrv.resources.AccountResource;
import nl.hypothermic.mfsrv.resources.AuthResource;
import nl.hypothermic.mfsrv.resources.IResource;

public class MFServer {

	public static final long SESSION_TIMEOUT = 300000; // ms

	private final Javalin instance;
	public final NexmoHooks nexmo;
	private final IResource[] resources = {
			(IResource) new AuthResource(this),
			(IResource) new AccountResource(this)
	};

	public final IDatabaseHandler database = new TempDatabase(this);
	private final ConfigHandler cfg;

	private static final AtomicInteger counter = new AtomicInteger();
	public static final ExecutorService threadpool = Executors.newCachedThreadPool(new ThreadFactory() {
		public Thread newThread(Runnable r) {
			return new Thread(r, "MFSRV-" + counter.incrementAndGet());
		}
	});

	public static void main(String[] args) throws IOException {
		new MFServer(args).start();
	}

	public MFServer(String[] args) throws IOException {
		instance = Javalin.create();
		cfg = new ConfigHandler();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				database.eventServletStop();
				instance.stop();
			}
		});
		nexmo = new NexmoHooks(ConfigHandler.instance.getStringOrCrash("nexmoKey"),
							   ConfigHandler.instance.getStringOrCrash("nexmoSecret"));
	}

	public void start() {
		database.eventServletStart();
		instance.start(7000);
		for (IResource iter : resources) {
			iter.registerResource(instance);
		}
	}
}
