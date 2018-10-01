package nl.hypothermic.mfsrv.resources;

import io.javalin.Javalin;

public interface IResource {
	
	void registerResource(Javalin instance);

}
