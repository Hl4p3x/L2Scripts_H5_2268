package l2s.gameserver.handler.voicecommands.impl.BotReport;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.utils.AutoBan;

public class CaptchaHandler
{
	private static final long TIME_BETWEEN_LAST_ATTACK = 5000L;
  
	private static boolean canReport(Player actor, Player target)
	{
		if(!Config.CAPTCHA_ALLOW)
		{
			actor.sendMessage("This option is currently under construction!");
			return false;
		}
		if(actor.equals(target))
		{
			actor.sendMessage("You cannot report yourself...");
			return false;
		}
		if(actor.getLevel() < Config.CAPTCHA_MIN_LEVEL)
		{
			actor.sendMessage("Your level is too low to report other Players!");
			return false;
		}
		if(!target.isInCombat() || target.isInZonePeace() || !target.isOnline())
		{
			actor.sendMessage("Cannot report Players that aren't fighting!");
			return false;
		}
		if(target.getPvpFlag() > 0 || target.getKarma() > 0 || target.isInOlympiadMode() || target.isInFightClub())
		{
			actor.sendMessage("Cannot report Players that are PvPing!");
			return false;
		}
		if(target.isInZoneBattle() || target.isInZone(Zone.ZoneType.epic))
		{
			actor.sendMessage("You cannot report players in this zone!");
			return false;
		}
		if(actor.isGM())
		{
			return true;
		}
		if(target.containsQuickVar("LastCaptchaTest") && target.getQuickVarL("LastCaptchaTest", new long[0]) + Config.CAPTCHA_TIME_BETWEEN_TESTED_SECONDS * 1000L > System.currentTimeMillis())
		{
			actor.sendMessage("This player answered Captcha test lately!");
			return false;
		}
		if(!CaptchaTimer.getInstance().canReportBotAgain(actor))
		{
			actor.sendMessage("You cannot report players so often!");
			return false;
		}
		Creature lastAttacker = target.getLastAttacker();
		if(lastAttacker == null || !lastAttacker.isMonster() || target.getLastAttackDate() + 5000L < System.currentTimeMillis())
		{
			if(target.getTarget() == null || !target.getTarget().isMonster())
			{
				actor.sendMessage("You cannot report players, that aren't fighting with monsters.");
				return false;
			}
			lastAttacker = (Creature)target.getTarget();
		}
		if(lastAttacker.isBoss() || lastAttacker.isSiegeGuard())
		{
			actor.sendMessage("You cannot report players, that aren't fighting with monsters!");
			return false;
		}
		return true;
	}
	
	public static boolean tryReportPlayer(Player actor, Player target)
	{
		if(!canReport(actor, target))
			return false;
		target.addQuickVar("LastCaptchaTest", Long.valueOf(System.currentTimeMillis()));
		CaptchaTimer.getInstance().addBotReporter(actor);
		actor.sendMessage("Thanks for reporting player, he is now being tested.");
		String correctCaptcha = Captcha.sendCaptcha(target);
		CaptchaTimer.getInstance().addCaptchaTimer(actor, target, correctCaptcha);
		return true;
	}
  
	public static void onAnswerCaptcha(Player actor, String answer)
	{
		CaptchaEvent event = CaptchaTimer.getInstance().getMyEvent(actor);
		if(event == null)
		{
			actor.sendMessage("You cannot answer Captcha at this time!");
			return;
		}
		if(answer == null || !event.getCorrectCaptcha().equalsIgnoreCase(answer.trim()))
			onFailedCaptcha(event);
		else
			onCorrectCaptcha(event);
	}
  
	public static void onFailedCaptcha(CaptchaEvent event)
	{
		CaptchaTimer.getInstance().removeCaptchaTimer(event);
		String targetName = event.getTargetName();
		Player target = GameObjectsStorage.getPlayer(targetName);
		if(target == null)
			AutoBan.doJailPlayer(targetName, (int) (Config.CAPTCHA_JAIL_SECONDS * 1000), true);
		else
		{
			target.sendMessage("You have failed Captcha Test!");
			target.toJail((int) (Config.CAPTCHA_JAIL_SECONDS * 1000));
		}
    
		Player actor = GameObjectsStorage.getPlayer(event.getActorName());
		if(actor != null)
		{
			actor.sendMessage("Reported player was put in Jail!");
			CaptchaTimer.getInstance().removeBotReporter(actor);
		}
	}
  
	private static void onCorrectCaptcha(CaptchaEvent event)
	{
		CaptchaTimer.getInstance().removeCaptchaTimer(event);
		Player target = GameObjectsStorage.getPlayer(event.getTargetName());
		if(target != null)
			target.sendMessage("Captcha is correct! Thank you!");
		Player actor = GameObjectsStorage.getPlayer(event.getActorName());
		if(actor != null)
		{
			actor.sendMessage("Target answered Captcha correctly. He is not bot.");
		}
	}
}
