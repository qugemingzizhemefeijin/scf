package cg.zz.scf.test.server.main;

import java.util.ArrayList;
import java.util.List;

import cg.zz.scf.server.bootstrap.Main;

public class Bootstrap {

	public static void main(String[] args) throws Exception {
		String path = Bootstrap.class.getClassLoader().getResource("").getPath();
		if(path.startsWith("/")) {
			path = path.substring(1);
		}
		
		System.setProperty("user.dir", path);
		
		List<String> argsList = new ArrayList<>();
		if(args != null && args.length > 0) {
			for(String s : args) {
				argsList.add(s);
			}
		}
		
		argsList.add("-Dscf.service.name=mimi");
		
		Main.main(argsList.toArray(new String[0]));
	}

}
