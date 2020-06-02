package l2s.gameserver.vote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;


public class VoteRead
{
  private static final Logger _log = LoggerFactory.getLogger(VoteRead.class);
  
  public static volatile long _siteBlockTime = 0L;
  




  public static long checkVotedIP(String IP)
  {
    if (IP == null) {
      return 0L;
    }
    long voteDate = 0L;
    
    try
    {
      URL url = new URL(Config.VOTE_ADDRESS + IP);
      InputStreamReader isr = new InputStreamReader(url.openStream());Throwable localThrowable2 = null;
      try
      {
        BufferedReader br = new BufferedReader(isr);
        String strLine;
        while ((strLine = br.readLine()) != null)
        {
          if (!strLine.trim().equalsIgnoreCase("FALSE"))
          {

            voteDate = System.currentTimeMillis() / 1000L;
          }
        }
      }
      catch (Throwable localThrowable1)
      {
        localThrowable2 = localThrowable1;throw localThrowable1;




      }
      finally
      {




        if (isr != null) if (localThrowable2 != null) try { isr.close(); } catch (Throwable x2) { localThrowable2.addSuppressed(x2); } else isr.close();
      }
    }
    catch (MalformedURLException e) {
      _log.error("MalformedURLException while reading votes, IP:" + IP + " Address:" + Config.VOTE_ADDRESS, e);
      _siteBlockTime = System.currentTimeMillis() + 1800000L;
      return 0L;
    }
    catch (IOException e)
    {
      _log.error("IOException while reading votes, IP:" + IP + " Address:" + Config.VOTE_ADDRESS + " " + e.toString());
      _siteBlockTime = System.currentTimeMillis() + 900000L;
      return 0L;
    }
    catch (Exception e)
    {
      return 0L;
    }
    
    return voteDate;
  }
}
