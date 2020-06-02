package l2s.gameserver.webserver;

import java.io.File;
import java.io.FileWriter;
import java.util.logging.Logger;

import l2s.gameserver.Config;
import l2s.gameserver.utils.Files;

public class WebServer implements Runnable
{
	protected static Logger _log = Logger.getLogger(WebServer.class.getName());

	@Override
	public void run()
	{
		try
		{
			File workingDir = new File(Config.WEB_SERVER_ROOT);
			for(File f : workingDir.listFiles())
			{
				if(!f.getName().endsWith(".fst"))
					continue;

				String content = Files.read(f.getPath());

				if(content == null)
					continue;

				try
				{
					String text = PageParser.parse(content);
					String name = f.getPath();
					name = name.substring(0, name.length() - 4);
					name = new StringBuilder().append(name).append(text.startsWith("<!DOCTYPE") ? ".html" : ".txt").toString();
					File out = new File(name);
					out.delete();
					out.createNewFile();
					FileWriter fw = new FileWriter(out);
					fw.write(text);
					fw.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}