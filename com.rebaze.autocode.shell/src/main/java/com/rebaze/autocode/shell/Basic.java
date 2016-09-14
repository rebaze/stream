package com.rebaze.autocode.shell;

import java.io.File;
import java.io.IOException;

import org.apache.felix.service.command.Descriptor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.rebaze.autocode.api.Autocode;

@Component(immediate=true, service = Basic.class, property = 
	{
			"osgi.command.scope=stream",
			"osgi.command.function=update"
	})
public class Basic {

	@Reference MavenRepoUtil mavenRepo; 
	
	@Descriptor("Runs an update against maven central.")
	public void update(@Descriptor("path to bom") String path) {
		try {
			
			mavenRepo.resolve(new File(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
