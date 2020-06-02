package l2s.gameserver.handler.voicecommands.impl.BotReport;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;

public class CaptchaTimer
{
	private final List<CaptchaEvent> captchaEventList;
	private final List<FailedBotReporter> failedBotReporters;
  
	protected CaptchaTimer()
	{
		captchaEventList = new CopyOnWriteArrayList<CaptchaEvent>();
		failedBotReporters = new CopyOnWriteArrayList<FailedBotReporter>();
		ThreadPoolManager.getInstance().execute(new CaptchaTimerThread());
	}
  
	public void addBotReporter(Player player)
	{
		FailedBotReporter reporter = getBotReporter(player);
		if(reporter != null)
			reporter.setLastReportTime(System.currentTimeMillis());
		else
			failedBotReporters.add(new FailedBotReporter(player.getAccountName(), player.getIP(), player.getHWID(), System.currentTimeMillis()));
	}
  
	public void removeBotReporter(Player player)
	{
		FailedBotReporter reporter = getBotReporter(player);
		if(reporter != null)
			failedBotReporters.remove(reporter);
	}
  
	public boolean canReportBotAgain(Player player)
	{
		FailedBotReporter reporter = getBotReporter(player);
		return reporter == null || reporter.canReportAgain();
	}
  
	private FailedBotReporter getBotReporter(Player player)
	{
		for(FailedBotReporter reporter : failedBotReporters)
			if(reporter.isBotReporter(player))
				return reporter;
		return null;
	}
  
	public void addCaptchaTimer(Player actor, Player target, String correctCaptcha)
	{
		captchaEventList.add(new CaptchaEvent(actor, target, correctCaptcha, System.currentTimeMillis()));
	}
  
	public void removeCaptchaTimer(CaptchaEvent event)
	{
		captchaEventList.remove(event);
	}
  
	public CaptchaEvent getMyEvent(Player target)
	{
		for(CaptchaEvent event : captchaEventList)
			if(event.getTargetName().equals(target.getName()))
				return event;
		return null;
	}
  
	protected Iterable<CaptchaEvent> getCaptchaEventList()
	{
		return captchaEventList;
	}
  
	protected class CaptchaTimerThread implements Runnable
	{
		private static final long CAPTCHA_TIMER_DELAY = 500L;
    
		public void run() 
		{
			long currentTime = System.currentTimeMillis();
			for(CaptchaEvent event : getCaptchaEventList())
			{
				if(event.getStartDate() + Config.CAPTCHA_ANSWER_SECONDS * 1000L <= currentTime)
					CaptchaHandler.onFailedCaptcha(event);
			}
			ThreadPoolManager.getInstance().schedule(this, 500L);
		}
	}
  
	private static class FailedBotReporter
	{
		private final String accountName;
		private final String ip;
		private final String hwid;
		private long lastReportTime;
    
		private FailedBotReporter(String accountName, String ip, String hwid, long lastReportTime)
		{
			this.accountName = accountName;
			this.ip = ip;
			this.hwid = hwid;
			this.lastReportTime = lastReportTime;
		}
    
		public void setLastReportTime(long lastReportTime)
		{
			this.lastReportTime = lastReportTime;
		}
    
		private boolean isBotReporter(Player player)
		{
			if(player.getAccountName().equals(accountName))
				return true;
			if(player.getIP().equals(ip))
				return true;
			return false;
		}
    
		private boolean canReportAgain()
		{
			return lastReportTime + Config.CAPTCHA_TIME_BETWEEN_REPORTS_SECONDS * 1000L < System.currentTimeMillis();
		}
	}
  
	public static CaptchaTimer getInstance()
	{
		return CaptchaTimerHolder.instance;
	}
  
	private static class CaptchaTimerHolder
	{
		protected static final CaptchaTimer instance = new CaptchaTimer();
	}
}
