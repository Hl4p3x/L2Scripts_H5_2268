package l2s.gameserver.security.hwid;

import l2s.gameserver.model.Player;

public class HwidGamer
{
	private final String hwid;
	private final java.util.List<Player> onlineChars;
	private long firstTimePlayed;
	private long totalTimePlayed;
  
	public static enum PLAYER_THREAT
	{
		NONE, 
		FRIENDLY, 
		KEEP_EYE_ON, 
		CRITICAL;
	}
  

	private int pollAnswer;
  
	private int warnings;
	private int seenChangeLog;
	private final PLAYER_THREAT threat;
	private long bannedToDate;
	public HwidGamer(String hwid, long firstTimePlayed, long totalTimePlayed, int pollAnswer, int warnings, int seenChangeLog, PLAYER_THREAT threat, long bannedToDate)
	{
		this.hwid = hwid;
		this.firstTimePlayed = firstTimePlayed;
		this.totalTimePlayed = totalTimePlayed;
		this.pollAnswer = pollAnswer;
		this.seenChangeLog = seenChangeLog;
		this.warnings = warnings;
		this.threat = threat;
		onlineChars = new java.util.concurrent.CopyOnWriteArrayList<Player>();
    
		this.bannedToDate = bannedToDate;
		installHandler();
	}
  
	public void addPlayer(Player player)
	{
		onlineChars.add(player);
	}
  
	public void removePlayer(Player player)
	{
		onlineChars.remove(player);
		if(onlineChars.isEmpty())
		{
			closeLogger();
			calculateTotalTimePlayed();
		}
	}
  
	public java.util.List<Player> getOnlineChars()
	{
		return onlineChars;
	}
  
	public String getHwid()
	{
		return hwid;
	}
  
	public long getFirstTimePlayed()
	{
		return firstTimePlayed;
	}
  
	public void calculateTotalTimePlayed()
	{
		totalTimePlayed += System.currentTimeMillis() - firstTimePlayed;
		firstTimePlayed = System.currentTimeMillis();
	}
  
	public void incTotalTimePlayed(long timeToAdd)
	{
		totalTimePlayed += timeToAdd;
	}
  
	public long getTotalTimePlayed()
	{
		return totalTimePlayed;
	}
  
	public void setPollAnswer(int answer, boolean updateDb)
	{
		pollAnswer = answer;
		if(updateDb)
			HwidEngine.getInstance().updateGamerInDb(this);
	}
  
	public int getPollAnswer()
	{
		return pollAnswer;
	}
  
	public PLAYER_THREAT getThreat()
	{
		return threat;
	}
  
	public void setHwidBanned(long toDate)
	{
		bannedToDate = toDate;
	}
  
	public long getBannedToDate()
	{
		return bannedToDate;
	}
  
	public void setWarnings(int newWarnings)
	{
		warnings = newWarnings;
		HwidEngine.getInstance().updateGamerInDb(this);
	}
  
	public int getWarnings()
	{
		return warnings;
	}
  
	public void setSeenChangeLog(int changeLogIndex, boolean updateInDb)
	{
		seenChangeLog = changeLogIndex;
		if(updateInDb)
			HwidEngine.getInstance().updateGamerInDb(this);
	}
  
	public int getSeenChangeLog()
	{
		return seenChangeLog;
	}
	
	private void installHandler() {}
  
	private void closeLogger() {}
}
