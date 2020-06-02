package l2s.gameserver.security.hwid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;

public class AutoBansCleaner
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AutoBansCleaner.class);
	private static final String PATH = "lameguard/";
	private static final String ORIGINAL_FILE_NAME = "banned_hwid.txt";
	private static final String TEMP_FILE_NAME = "banned_hwid_temp.txt";
	private static final String BORDER = "********************************************";
  
	public static void startFileCleaning()
	{
		if(!isAllowed())
			return;
		ThreadPoolManager.getInstance().execute(new BanCleanerThread());
	}
  
	private static boolean isAllowed()
	{
		return Config.ALLOW_CLEANING_AUTO_BANS;
	}
  
	private static class BanCleanerThread  implements Runnable
	{
		public void run()
		{
			if(!isAllowed())
				return;
			cleanBans();
			ThreadPoolManager.getInstance().schedule(this, Config.SECONDS_BETWEEN_AUTO_BAN_CLEANING * 1000L);
		}
    
		private static void cleanBans()
		{
			File originalFile = new File("lameguard/banned_hwid.txt");
			File newFile = new File("lameguard/banned_hwid_temp.txt");
			createTempFile(originalFile, newFile);
			switchFiles(originalFile, newFile);
		}
    
		private static void createTempFile(File originalFile, File tempFile)
		{
			try
			{
				BufferedReader br = new BufferedReader(new FileReader(originalFile));
				PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
				boolean foundBorder = false;
				String line = br.readLine();
				
				while(line != null)
				{
					if(!foundBorder)
					{
						pw.println(line);
						pw.flush();
						if(line.contains("********************************************"))
							foundBorder = true;
					}
					line = br.readLine();
				}
			}
			catch (IOException e)
			{
				AutoBansCleaner.LOGGER.error("Error while Creating new Auto Ban File: ", e);
			}
		}
    
		private static void switchFiles(File original, File newFile)
		{
			if(!original.delete())
				AutoBansCleaner.LOGGER.error("Error while Deleting old Auto Ban File");
			if(!newFile.renameTo(original))
				AutoBansCleaner.LOGGER.error("Error while Renaming old Auto Ban File to New One");
		}
	}
}
