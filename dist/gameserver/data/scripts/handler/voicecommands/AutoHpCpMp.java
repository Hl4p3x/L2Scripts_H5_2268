package handler.voicecommands;

import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.listener.actor.OnChangeCurrentCpListener;
import l2s.gameserver.listener.actor.OnChangeCurrentHpListener;
import l2s.gameserver.listener.actor.OnChangeCurrentMpListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.EffectList;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillType;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class AutoHpCpMp implements IVoicedCommandHandler, ScriptFile
{
	private static class ChangeCurrentCpListener implements OnChangeCurrentCpListener
	{
		public void onChangeCurrentCp(Creature actor, double oldCp, double newCp)
		{
			if(!actor.isPlayer() || actor.isDead())
				return;

			Player player = actor.getPlayer();

			if(!player.getVarBoolean("acp_enabled", true))
				return;

			int percent = player.getVarInt("autocp", 0);
			double currentPercent = newCp / (player.getMaxCp() / 100.);
			if(percent <= 0 || currentPercent <= 0 || currentPercent > percent)
				return;

			ItemInstance effectedItem = null;
			int effectedItemPower = 0;

			ItemInstance instantItem = null;
			int instantItemPower = 0;

			final List<Effect> effects = player.getEffectList().getEffectsByType(EffectType.CombatPointHealOverTime);
			loop: for(ItemInstance item : player.getInventory().getItems())
			{
				Skill skill = item.getTemplate().getFirstSkill();
				if(skill == null)
					continue;

				for(EffectTemplate et : skill.getEffectTemplates())
				{
					if(et.getEffectType() == EffectType.CombatPointHealOverTime)
					{
						for(Effect effect : effects)
						{
							if(EffectList.checkStackType(et, effect.getTemplate()) && et._stackOrder <= effect.getStackOrder())
							{
								// Не хиляем, если уже наложена какая-либо хилка.
								effectedItem = null;
								effectedItemPower = 0;
								continue loop;
							}
						}

						if(!ItemFunctions.checkUseItem(player, item, false))
							continue loop;

						int power = (int) et._value;
						if(power > effectedItemPower)
						{
							if(skill.checkCondition(player, player, false, false, true, true))
							{
								effectedItem = item;
								effectedItemPower = power;
								continue loop;
							}
						}
					}
				}
			}

			loop: for(ItemInstance item : player.getInventory().getItems())
			{
				Skill skill = item.getTemplate().getFirstSkill();
				if(skill == null)
					continue;

				if(!ItemFunctions.checkUseItem(player, item, false))
					continue;

				for(EffectTemplate et : skill.getEffectTemplates())
				{
					if(/*et.getEffectType() == EffectType.HealCP || */et.getEffectType() == EffectType.HealCPPercent)
					{
						int power = (int) et._value;
						if(et.getEffectType() == EffectType.HealCPPercent)
							power = power * (player.getMaxCp() / 100);
						if(power > instantItemPower)
						{
							if(skill.checkCondition(player, player, false, false, true, true))
							{
								instantItem = item;
								instantItemPower = power;
								continue loop;
							}
						}
					}
				}
				if(skill.getSkillType() == SkillType.COMBATPOINTHEAL/* || skill.getSkillType() == SkillType.COMBATPOINTHEAL_PERCENT*/)
				{
					int power = (int) skill.getPower();
					/*if(skill.getSkillType() == SkillType.COMBATPOINTHEAL_PERCENT)
						power = power * (player.getMaxCp() / 100);*/
					if(power > instantItemPower)
					{
						if(skill.checkCondition(player, player, false, false, true, true))
						{
							instantItem = item;
							instantItemPower = power;
							continue loop;
						}
					}
				}
			}

			if(instantItem != null)
				ItemFunctions.useItem(player, instantItem, false, false);

			if(effectedItem != null)
			{
				if(instantItemPower == 0 || percent >= (newCp + instantItemPower) / (player.getMaxCp() / 100.))
					ItemFunctions.useItem(player, effectedItem, false, false);
			}
		}
	}

	private static class ChangeCurrentHpListener implements OnChangeCurrentHpListener
	{
		public void onChangeCurrentHp(Creature actor, double oldHp, double newHp)
		{
			if(!actor.isPlayer() || actor.isDead())
				return;

			Player player = actor.getPlayer();

			if(!player.getVarBoolean("acp_enabled", true))
				return;

			int percent = player.getVarInt("autohp", 0);
			double currentPercent = newHp / (player.getMaxHp() / 100.);
			if(percent <= 0 || currentPercent <= 0 || currentPercent > percent)
				return;

			ItemInstance effectedItem = null;
			int effectedItemPower = 0;

			ItemInstance instantItem = null;
			int instantItemPower = 0;

			final List<Effect> effects = player.getEffectList().getEffectsByType(EffectType.HealOverTime);
			loop: for(ItemInstance item : player.getInventory().getItems())
			{
				Skill skill = item.getTemplate().getFirstSkill();
				if(skill == null)
					continue;

				for(EffectTemplate et : skill.getEffectTemplates())
				{
					if(et.getEffectType() == EffectType.HealOverTime)
					{
						for(Effect effect : effects)
						{
							if(EffectList.checkStackType(et, effect.getTemplate()) && et._stackOrder <= effect.getStackOrder())
							{
								// Не хиляем, если уже наложена какая-либо хилка.
								effectedItem = null;
								effectedItemPower = 0;
								continue loop;
							}
						}

						if(!ItemFunctions.checkUseItem(player, item, false))
							continue loop;

						int power = (int) et._value;
						if(power > effectedItemPower)
						{
							if(skill.checkCondition(player, player, false, false, true, true))
							{
								effectedItem = item;
								effectedItemPower = power;
								continue loop;
							}
						}
					}
				}
			}

			loop: for(ItemInstance item : player.getInventory().getItems())
			{
				Skill skill = item.getTemplate().getFirstSkill();
				if(skill == null)
					continue;

				if(!ItemFunctions.checkUseItem(player, item, false))
					continue;

				for(EffectTemplate et : skill.getEffectTemplates())
				{
					if(et.getEffectType() == EffectType.Heal || et.getEffectType() == EffectType.HealPercent)
					{
						int power = (int) et._value;
						if(et.getEffectType() == EffectType.HealPercent)
							power = power * (player.getMaxHp() / 100);
						if(power > instantItemPower)
						{
							if(skill.checkCondition(player, player, false, false, true, true))
							{
								instantItem = item;
								instantItemPower = power;
								continue loop;
							}
						}
					}
				}
				if(skill.getSkillType() == SkillType.HEAL || skill.getSkillType() == SkillType.HEAL_PERCENT)
				{
					int power = (int) skill.getPower();
					if(skill.getSkillType() == SkillType.HEAL_PERCENT)
						power = power * (player.getMaxHp() / 100);
					if(power > instantItemPower)
					{
						if(skill.checkCondition(player, player, false, false, true, true))
						{
							instantItem = item;
							instantItemPower = power;
							continue loop;
						}
					}
				}
			}

			if(instantItem != null)
				ItemFunctions.useItem(player, instantItem, false, false);

			if(effectedItem != null)
			{
				if(instantItemPower == 0 || percent >= (newHp + instantItemPower) / (player.getMaxHp() / 100.))
					ItemFunctions.useItem(player, effectedItem, false, false);
			}
		}
	}

	private static class ChangeCurrentMpListener implements OnChangeCurrentMpListener
	{
		public void onChangeCurrentMp(Creature actor, double oldMp, double newMp)
		{
			if(!actor.isPlayer() || actor.isDead())
				return;

			Player player = actor.getPlayer();

			if(!player.getVarBoolean("acp_enabled", true))
				return;

			int percent = player.getVarInt("automp", 0);
			double currentPercent = newMp / (player.getMaxMp() / 100.);
			if(percent <= 0 || currentPercent <= 0 || currentPercent > percent)
				return;

			ItemInstance effectedItem = null;
			int effectedItemPower = 0;

			ItemInstance instantItem = null;
			int instantItemPower = 0;

			final List<Effect> effects = player.getEffectList().getEffectsByType(EffectType.ManaHealOverTime);
			loop: for(ItemInstance item : player.getInventory().getItems())
			{
				Skill skill = item.getTemplate().getFirstSkill();
				if(skill == null)
					continue;

				for(EffectTemplate et : skill.getEffectTemplates())
				{
					if(et.getEffectType() == EffectType.ManaHealOverTime)
					{
						for(Effect effect : effects)
						{
							if(EffectList.checkStackType(et, effect.getTemplate()) && et._stackOrder <= effect.getStackOrder())
							{
								// Не хиляем, если уже наложена какая-либо хилка.
								effectedItem = null;
								effectedItemPower = 0;
								continue loop;
							}
						}

						if(!ItemFunctions.checkUseItem(player, item, false))
							continue loop;

						int power = (int) et._value;
						if(power > effectedItemPower)
						{
							if(skill.checkCondition(player, player, false, false, true, true))
							{
								effectedItem = item;
								effectedItemPower = power;
								continue loop;
							}
						}
					}
				}
			}

			loop: for(ItemInstance item : player.getInventory().getItems())
			{
				Skill skill = item.getTemplate().getFirstSkill();
				if(skill == null)
					continue;

				if(!ItemFunctions.checkUseItem(player, item, false))
					continue;

				for(EffectTemplate et : skill.getEffectTemplates())
				{
					if(et.getEffectType() == EffectType.ManaHeal || et.getEffectType() == EffectType.ManaHealPercent)
					{
						int power = (int) et._value;
						if(et.getEffectType() == EffectType.ManaHealPercent)
							power = power * (player.getMaxMp() / 100);
						if(power > instantItemPower)
						{
							if(skill.checkCondition(player, player, false, false, true, true))
							{
								instantItem = item;
								instantItemPower = power;
								continue loop;
							}
						}
					}
				}
				if(skill.getSkillType() == SkillType.MANAHEAL || skill.getSkillType() == SkillType.MANAHEAL_PERCENT)
				{
					int power = (int) skill.getPower();
					if(skill.getSkillType() == SkillType.MANAHEAL_PERCENT)
						power = power * (player.getMaxMp() / 100);
					if(power > instantItemPower)
					{
						if(skill.checkCondition(player, player, false, false, true, true))
						{
							instantItem = item;
							instantItemPower = power;
							continue loop;
						}
					}
				}
			}

			if(instantItem != null)
				ItemFunctions.useItem(player, instantItem, false, false);

			if(effectedItem != null)
			{
				if(instantItemPower == 0 || percent >= (newMp + instantItemPower) / (player.getMaxMp() / 100.))
					ItemFunctions.useItem(player, effectedItem, false, false);
			}
		}
	}

	private static class PlayerEnterListener implements OnPlayerEnterListener
	{
		public void onPlayerEnter(Player player)
		{
			if(!Config.ALLOW_VOICED_COMMANDS || !Config.ALLOW_AUTOHEAL_COMMANDS)
				return;

			int percent = player.getVarInt("autocp", 0);
			if(percent > 0)
			{
				player.addListener(CHANGE_CURRENT_CP_LISTENER);
				if(player.isLangRus())
					player.sendMessage("Вы используете систему автоматического восстановления CP. Ваше CP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
				else
					player.sendMessage("You are using an automatic CP recovery. Your CP will automatically recover at a value of " + percent + "% or less.");
			}
			percent = player.getVarInt("autohp", 0);
			if(percent > 0)
			{
				player.addListener(CHANGE_CURRENT_HP_LISTENER);
				if(player.isLangRus())
					player.sendMessage("Вы используете систему автоматического восстановления HP. Ваше HP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
				else
					player.sendMessage("You are using an automatic HP recovery. Your HP will automatically recover at a value of " + percent + "% or less.");
			}
			percent = player.getVarInt("automp", 0);
			if(percent > 0)
			{
				player.addListener(CHANGE_CURRENT_MP_LISTENER);
				if(player.isLangRus())
					player.sendMessage("Вы используете систему автоматического восстановления MP. Ваше MP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
				else
					player.sendMessage("You are using an automatic MP recovery. Your MP will automatically recover at a value of " + percent + "% or less.");
			}
		}
	}

	private static final OnChangeCurrentCpListener CHANGE_CURRENT_CP_LISTENER = new ChangeCurrentCpListener();
	private static final OnChangeCurrentHpListener CHANGE_CURRENT_HP_LISTENER = new ChangeCurrentHpListener();
	private static final OnChangeCurrentMpListener CHANGE_CURRENT_MP_LISTENER = new ChangeCurrentMpListener();
	private static final OnPlayerEnterListener PLAYER_ENTER_LISTENER = new PlayerEnterListener();

	private static final String[] COMMANDS = new String[] { "acp", "autocp", "autohp", "automp" };

	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS || !Config.ALLOW_AUTOHEAL_COMMANDS)
			return false;

		if(command.equalsIgnoreCase("acp"))
		{
			boolean enabled = activeChar.getVarBoolean("acp_enabled", true);
			int autoHp = activeChar.getVarInt("autohp", 0);
			int autoMp = activeChar.getVarInt("automp", 0);
			int autoCp = activeChar.getVarInt("autocp", 0);
			try
			{
				String[] params = args.split("\\s+");
				if(params[0].equalsIgnoreCase("enable"))
				{
					if(!enabled)
					{
						enabled = true;
						activeChar.unsetVar("acp_enabled");
						if(activeChar.isLangRus())
							activeChar.sendMessage("Система автоматического восстановления активирована.");
						else
							activeChar.sendMessage("The automatic recovery system is activated.");
					}
				}
				else if(params[0].equalsIgnoreCase("disable"))
				{
					if(enabled)
					{
						enabled = false;
						activeChar.setVar("acp_enabled", false);
						if(activeChar.isLangRus())
							activeChar.sendMessage("Система автоматического восстановления деактивирована.");
						else
							activeChar.sendMessage("The automatic recovery system is deactivated.");
					}
				}
				else if(params[0].equalsIgnoreCase("hp"))
				{
					int newAutoHp = Math.min(99, Integer.parseInt(params[1]));
					if(newAutoHp != autoHp)
					{
						if(newAutoHp > 0)
						{
							activeChar.setVar("autohp", newAutoHp, -1);
							if(autoHp > 0)
							{
								if(activeChar.isLangRus())
									activeChar.sendMessage("Ваше HP будет автоматически восстанавливаться при значении " + newAutoHp + "% и меньше.");
								else
									activeChar.sendMessage("Your HP will automatically recover at a value of " + newAutoHp + "% or less.");
							}
							else
							{
								activeChar.addListener(CHANGE_CURRENT_HP_LISTENER);
								if(activeChar.isLangRus())
									activeChar.sendMessage("Вы включили систему автоматического восстановления HP. Ваше HP будет автоматически восстанавливаться при значении " + newAutoHp + "% и меньше.");
								else
									activeChar.sendMessage("You have enabled an automatic HP recovery. Your HP will automatically recover at a value of " + newAutoHp + "% or less.");
							}
						}
						else
						{
							activeChar.unsetVar("autohp");
							activeChar.removeListener(CHANGE_CURRENT_HP_LISTENER);
							if(activeChar.isLangRus())
								activeChar.sendMessage("Система автоматического восстановления HP отключена.");
							else
								activeChar.sendMessage("HP automatic recovery system disabled.");
						}
						autoHp = newAutoHp;
					}
				}
				else if(params[0].equalsIgnoreCase("mp"))
				{
					int newAutoMp = Math.min(99, Integer.parseInt(params[1]));
					if(newAutoMp != autoMp)
					{
						if(newAutoMp > 0)
						{
							activeChar.setVar("automp", newAutoMp, -1);
							if(autoMp > 0)
							{
								if(activeChar.isLangRus())
									activeChar.sendMessage("Ваше MP будет автоматически восстанавливаться при значении " + newAutoMp + "% и меньше.");
								else
									activeChar.sendMessage("Your MP will automatically recover at a value of " + newAutoMp + "% or less.");
							}
							else
							{
								activeChar.addListener(CHANGE_CURRENT_MP_LISTENER);
								if(activeChar.isLangRus())
									activeChar.sendMessage("Вы включили систему автоматического восстановления MP. Ваше MP будет автоматически восстанавливаться при значении " + newAutoMp + "% и меньше.");
								else
									activeChar.sendMessage("You have enabled an automatic MP recovery. Your MP will automatically recover at a value of " + newAutoMp + "% or less.");
							}
						}
						else
						{
							activeChar.unsetVar("automp");
							activeChar.removeListener(CHANGE_CURRENT_MP_LISTENER);
							if(activeChar.isLangRus())
								activeChar.sendMessage("Система автоматического восстановления MP отключена.");
							else
								activeChar.sendMessage("MP automatic recovery system disabled.");
						}
						autoMp = newAutoMp;
					}
				}
				else if(params[0].equalsIgnoreCase("cp"))
				{
					int newAutoCp = Math.min(99, Integer.parseInt(params[1]));
					if(newAutoCp != autoCp)
					{
						if(newAutoCp > 0)
						{
							activeChar.setVar("autocp", newAutoCp, -1);
							if(autoCp > 0)
							{
								if(activeChar.isLangRus())
									activeChar.sendMessage("Ваше CP будет автоматически восстанавливаться при значении " + newAutoCp + "% и меньше.");
								else
									activeChar.sendMessage("Your CP will automatically recover at a value of " + newAutoCp + "% or less.");
							}
							else
							{
								activeChar.addListener(CHANGE_CURRENT_CP_LISTENER);
								if(activeChar.isLangRus())
									activeChar.sendMessage("Вы включили систему автоматического восстановления CP. Ваше CP будет автоматически восстанавливаться при значении " + newAutoCp + "% и меньше.");
								else
									activeChar.sendMessage("You have enabled an automatic CP recovery. Your CP will automatically recover at a value of " + newAutoCp + "% or less.");
							}
						}
						else
						{
							activeChar.unsetVar("autocp");
							activeChar.removeListener(CHANGE_CURRENT_CP_LISTENER);
							if(activeChar.isLangRus())
								activeChar.sendMessage("Система автоматического восстановления CP отключена.");
							else
								activeChar.sendMessage("CP automatic recovery system disabled.");
						}
						autoCp = newAutoCp;
					}
				}
			}
			catch(Exception e)
			{
				//
			}

			NpcHtmlMessagePacket htmlMsg = new NpcHtmlMessagePacket(0);
			htmlMsg.setFile("command/acp.htm");
			htmlMsg.addVar("acp_enabled", enabled);
			htmlMsg.addVar("auto_hp_percent", autoHp);
			htmlMsg.addVar("auto_mp_percent", autoMp);
			htmlMsg.addVar("auto_cp_percent", autoCp);
			activeChar.sendPacket(htmlMsg);
		}
		else if(command.equalsIgnoreCase("autocp"))
		{
			int percent;
			try
			{
				percent = Math.min(99, Integer.parseInt(args));
			}
			catch(NumberFormatException e)
			{
				if(activeChar.isLangRus())
					activeChar.sendMessage("Неверное использование комманды! Используйте: .autocp [ПРОЦЕНТ_CP_ДЛЯ_НАЧАЛА_ВОССТАНОВЛЕНИЯ]");
				else
					activeChar.sendMessage("Incorrect use commands! Use: .autocp [CP_PERCENT_FOR EARLY_RECOVERY]");
				return false;
			}
			if(percent <= 0)
			{
				if(activeChar.getVarInt("autocp", 0) > 0)
				{
					activeChar.removeListener(CHANGE_CURRENT_CP_LISTENER);
					activeChar.unsetVar("autocp");
					if(activeChar.isLangRus())
						activeChar.sendMessage("Система автоматического восстановления CP отключена.");
					else
						activeChar.sendMessage("CP automatic recovery system disabled.");
				}
				else
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Нельзя указать нулевое или отрицательное значение!");
					else
						activeChar.sendMessage("You can not specify zero or negative value!");
				}
				return false;
			}
			activeChar.addListener(CHANGE_CURRENT_CP_LISTENER);
			activeChar.setVar("autocp", percent, -1);
			if(activeChar.isLangRus())
				activeChar.sendMessage("Вы включили систему автоматического восстановления CP. Ваше CP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
			else
				activeChar.sendMessage("You have enabled an automatic CP recovery. Your CP will automatically recover at a value of " + percent + "% or less.");
			return true;
		}
		else if(command.equalsIgnoreCase("autohp"))
		{
			int percent;
			try
			{
				percent = Math.min(99, Integer.parseInt(args));
			}
			catch(NumberFormatException e)
			{
				if(activeChar.isLangRus())
					activeChar.sendMessage("Неверное использование комманды! Используйте: .autohp [ПРОЦЕНТ_HP_ДЛЯ_НАЧАЛА_ВОССТАНОВЛЕНИЯ]");
				else
					activeChar.sendMessage("Incorrect use commands! Use: .autohp [HP_PERCENT_FOR EARLY_RECOVERY]");
				return false;
			}
			if(percent <= 0)
			{
				if(activeChar.getVarInt("autohp", 0) > 0)
				{
					activeChar.removeListener(CHANGE_CURRENT_HP_LISTENER);
					activeChar.unsetVar("autohp");
					if(activeChar.isLangRus())
						activeChar.sendMessage("Система автоматического восстановления HP отключена.");
					else
						activeChar.sendMessage("HP automatic recovery system disabled.");
				}
				else
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Нельзя указать нулевое или отрицательное значение!");
					else
						activeChar.sendMessage("You can not specify zero or negative value!");
				}
				return false;
			}
			activeChar.addListener(CHANGE_CURRENT_HP_LISTENER);
			activeChar.setVar("autohp", percent, -1);
			if(activeChar.isLangRus())
				activeChar.sendMessage("Вы включили систему автоматического восстановления HP. Ваше HP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
			else
				activeChar.sendMessage("You have enabled an automatic HP recovery. Your HP will automatically recover at a value of " + percent + "% or less.");
			return true;
		}
		else if(command.equalsIgnoreCase("automp"))
		{
			int percent;
			try
			{
				percent = Math.min(99, Integer.parseInt(args));
			}
			catch(NumberFormatException e)
			{
				if(activeChar.isLangRus())
					activeChar.sendMessage("Неверное использование комманды! Используйте: .automp [ПРОЦЕНТ_MP_ДЛЯ_НАЧАЛА_ВОССТАНОВЛЕНИЯ]");
				else
					activeChar.sendMessage("Incorrect use commands! Use: .automp [MP_PERCENT_FOR EARLY_RECOVERY]");
				return false;
			}
			if(percent <= 0)
			{
				if(activeChar.getVarInt("automp", 0) > 0)
				{
					activeChar.removeListener(CHANGE_CURRENT_MP_LISTENER);
					activeChar.unsetVar("automp");
					if(activeChar.isLangRus())
						activeChar.sendMessage("Система автоматического восстановления MP отключена.");
					else
						activeChar.sendMessage("MP automatic recovery system disabled.");
				}
				else
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Нельзя указать нулевое или отрицательное значение!");
					else
						activeChar.sendMessage("You can not specify zero or negative value!");
					return false;
				}
			}
			activeChar.addListener(CHANGE_CURRENT_MP_LISTENER);
			activeChar.setVar("automp", percent, -1);
			if(activeChar.isLangRus())
				activeChar.sendMessage("Вы включили систему автоматического восстановления MP. Ваше MP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
			else
				activeChar.sendMessage("You have enabled an automatic MP recovery. Your MP will automatically recover at a value of " + percent + "% or less.");
			return true;
		}
		return false;
	}

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(PLAYER_ENTER_LISTENER);
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}
}