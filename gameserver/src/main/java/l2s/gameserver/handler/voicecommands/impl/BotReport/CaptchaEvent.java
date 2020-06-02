package l2s.gameserver.handler.voicecommands.impl.BotReport;

import l2s.gameserver.model.Player;

public class CaptchaEvent
{
	private final String actorName;
	private final String targetName;
	private final String correctCaptcha;
	private final long startDate;
  
	CaptchaEvent(Player actor, Player target, String correctCaptcha, long startDate)
	{
		actorName = actor.getName();
		targetName = target.getName();
		this.correctCaptcha = correctCaptcha;
		this.startDate = startDate;
	}
  
	public String getActorName()
	{
		return actorName;
	}
  
	public String getTargetName()
	{
		return targetName;
	}
  
	public String getCorrectCaptcha()
	{
		return correctCaptcha;
	}
  
	public long getStartDate()
	{
		return startDate;
	}
}
