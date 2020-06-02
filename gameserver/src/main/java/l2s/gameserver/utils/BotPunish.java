package l2s.gameserver.utils;

public class BotPunish
{
	// Kind of punish
	private Punish _botPunishment;
	// Time the punish will last
	private long _punishTime;
	// Punis time (in secs)
	private int _punishDuration;
	
	// Type of punishments
	public enum Punish
	{
		CHATBAN,
		MOVEBAN,
		PARTYBAN,
		ACTIONBAN
	}
	
	public BotPunish(Punish punish, int mins)
	{
		_botPunishment = punish;
		_punishTime = System.currentTimeMillis() + ( mins * 60 * 1000);
		_punishDuration = mins * 60;
	}
	
	/**
	* Returns the current punishment type
	* @return Punish (BotPunish enum)
	*/
	public Punish getBotPunishType()
	{
		return _botPunishment;
	}
	
	/**
	* Returns the time (in millis) when the player
	* punish started
	* @return long 
	*/
	public long getPunishStarterTime()
	{
		return _punishTime;
	}
	
	/**
	* Returns the duration (in seconds) of the applied
	* punish
	* @return int 
	*/
	public int getDuration()
	{
		return _punishDuration;
	}
	
	/**
	* Return the time left to end up this punish
	* @return long
	*/
	public long getPunishTimeLeft()
	{
		long left = System.currentTimeMillis() - _punishTime;
		return left;
	}
	
	/**
	* @return true if the player punishment has
	* expired 
	*/
	public boolean canWalk()
	{
		if(_botPunishment == Punish.MOVEBAN
				&& System.currentTimeMillis() - _punishTime <= 0)
			return false;
		return true;
	}
	
	/**
	* @return true if the player punishment has
	* expired 
	*/
	public boolean canTalk()
	{
		if(_botPunishment == Punish.CHATBAN
				&& System.currentTimeMillis() - _punishTime <= 0)
			return false;
		return true;
	}
	
	/**
	* @return true if the player punishment has
	* expired 
	*/
	public boolean canJoinParty()
	{
		if(_botPunishment == Punish.PARTYBAN
				&& System.currentTimeMillis() - _punishTime <= 0)
			return false;
		return true;
	}
	
	/**
	* @return true if the player punishment has
	* expired 
	*/
	public boolean canPerformAction()
	{
		if(_botPunishment == Punish.ACTIONBAN
				&& System.currentTimeMillis() - _punishTime <= 0)
			return false;
		return true;
	}
}
