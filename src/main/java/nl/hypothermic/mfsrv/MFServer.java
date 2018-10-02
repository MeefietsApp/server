package nl.hypothermic.mfsrv;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.javalin.Javalin;
import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.database.IDatabaseHandler;
import nl.hypothermic.mfsrv.database.TempDatabase;
import nl.hypothermic.mfsrv.resources.AuthResource;
import nl.hypothermic.mfsrv.resources.IResource;

public class MFServer {

	public static final long SESSION_TIMEOUT = 15000; // ms // 300000

	private final Javalin instance;
	private final IResource[] resources = {
			(IResource) new AuthResource(this)
	};

	public final IDatabaseHandler database = new TempDatabase();
	private final ConfigHandler cfg;

	private static final AtomicInteger counter = new AtomicInteger();
	public static final ExecutorService threadpool = Executors.newCachedThreadPool(new ThreadFactory() {
		public Thread newThread(Runnable r) {
			return new Thread(r, "MFSRV-" + counter.incrementAndGet());
		}
	});

	public static void main(String[] args) {
		new MFServer(args).start();
	}

	public MFServer(String[] args) {
		instance = Javalin.create();
		instance.enableCaseSensitiveUrls();
		cfg = new ConfigHandler();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				database.eventServletStop();
				instance.stop();
			}
		});
	}

	public void start() {
		database.eventServletStart();
		instance.start(7000);
		for (IResource iter : resources) {
			iter.registerResource(instance);
		}
	}
}
