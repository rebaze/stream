package com.rebaze.autocode.shell;

import java.io.File;
import java.io.IOException;

import org.apache.felix.service.command.Descriptor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.rebaze.autocode.api.Autocode;

@Component(immediate=true, service = Basic.class, property = 
	{
			"osgi.command.scope=autocode",
			"osgi.command.function=build"
	})
public class Basic {

	@Reference Autocode autocode; 

	@Descriptor("build")
	public void build(String path) {
		System.out.println("Hello World.");
		try {
			
			autocode.build(new File(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
