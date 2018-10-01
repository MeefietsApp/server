package nl.hypothermic.mfsrv;

import io.javalin.Javalin;
import nl.hypothermic.mfsrv.database.IDatabaseHandler;
import nl.hypothermic.mfsrv.database.TempDatabase;
import nl.hypothermic.mfsrv.resources.AuthResource;
import nl.hypothermic.mfsrv.resources.IResource;

public class MFServer {

	public static final long SESSION_TIMEOUT = 300000; // ms
	
	private final Javalin instance;
	private static final IResource[] resources = {(IResource) new AuthResource()};
	
	private static final IDatabaseHandler database = new TempDatabase();

	public static void main(String[] args) {
		new MFServer().start();
	}
	
	public MFServer() {
		instance = Javalin.create();
		instance.enableCaseSensitiveUrls();
	}
	
	public void start() {
		instance.start(7000);
		for (IResource iter : resources) {
			iter.registerResource(instance);
		}
	}
}
