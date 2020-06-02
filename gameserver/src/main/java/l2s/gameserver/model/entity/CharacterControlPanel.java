package l2s.gameserver.model.entity;

import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.CCPHelpers.CCPCWHPrivilages;
import l2s.gameserver.model.entity.CCPHelpers.CCPOffline;
import l2s.gameserver.model.entity.CCPHelpers.CCPPassword;
import l2s.gameserver.model.entity.CCPHelpers.CCPPasswordRecover;
import l2s.gameserver.model.entity.CCPHelpers.CCPPoll;
import l2s.gameserver.model.entity.CCPHelpers.CCPRepair;
import l2s.gameserver.model.entity.CCPHelpers.CCPSecondaryPassword;
import l2s.gameserver.model.entity.CCPHelpers.CCPSmallCommands;
import l2s.gameserver.model.quest.QuestState;




public class CharacterControlPanel
{
	private static CharacterControlPanel _instance;
	
	public String useCommand(Player activeChar, String text, String bypass)
	{
		if(activeChar.isBlocked())
			return null;
		String[] param = text.split(" ");
		if(param.length == 0)
			return "char.html";
    
		if(param[0].equalsIgnoreCase("grief"))
			CCPSmallCommands.setAntiGrief(activeChar);
		else if(param[0].equalsIgnoreCase("noe"))
		{
			if(activeChar.getVar("NoExp") == null)
				activeChar.setVar("NoExp", "1", -1L);
			else
				activeChar.unsetVar("NoExp");
		}
		else if(param[0].equalsIgnoreCase("online"))
			activeChar.sendMessage(CCPSmallCommands.showOnlineCount());
		else if(param[0].equalsIgnoreCase("changeLog"))
		{
			QuestState st = activeChar.getQuestState(255);
			if(st != null)
			{
				String change = ChangeLogManager.getInstance().getChangeLog(activeChar, ChangeLogManager.getInstance().getLatestChangeId());
				st.showTutorialHTML(change);
			}
		}
		else if(param[0].equalsIgnoreCase("notraders"))
		{
			if (activeChar.getVar("notraders") == null)
			{
				activeChar.setNotShowTraders(true);
				activeChar.setVar("notraders", "1", -1L);
			}
			else
			{
				activeChar.setNotShowTraders(false);
				activeChar.unsetVar("notraders");
			}
		}
		else if(param[0].equalsIgnoreCase("notShowBuffAnim"))
		{
			if(activeChar.getVar("notShowBuffAnim") == null)
			{
				activeChar.setNotShowBuffAnim(true);
				activeChar.setVar("notShowBuffAnim", "true", -1L);
			}
			else
			{
				activeChar.setNotShowBuffAnim(false);
				activeChar.unsetVar("notShowBuffAnim");
			}   
		}
		else if (param[0].equalsIgnoreCase("autoloot"))
			setAutoLoot(activeChar);
		else if(param[0].equalsIgnoreCase("repairCharacter"))
		{
			if(param.length > 1)
				CCPRepair.repairChar(activeChar, param[1]);
			else
				return null;
		} 
		else
		{
			if(param[0].equalsIgnoreCase("offlineStore"))
			{
				boolean result = CCPOffline.setOfflineStore(activeChar);
				if(result)
					return null;
				return "char.htm";
			}
			if(param[0].startsWith("poll") || param[0].startsWith("Poll"))
			{
				CCPPoll.bypass(activeChar, param);
				return null;
			}
			if(param[0].equals("combineTalismans"))
			{
				CCPSmallCommands.combineTalismans(activeChar);
				return null;
			}
			if(param[0].equals("otoad"))
			{
				CCPSmallCommands.openToad(activeChar, -1L);
				return null;
			}
			if(param[0].equals("hwidPage"))
			{
				if(activeChar.getHwidLock() != null)
					return "cfgUnlockHwid.htm";
				return "cfgLockHwid.htm";
			}
			if(param[0].equals("lockHwid"))
			{
				boolean shouldLock = Boolean.parseBoolean(param[1]);
				if(shouldLock)
				{
					activeChar.setHwidLock(activeChar.getHWID());
					activeChar.sendMessage("Character is now Locked!");
				}
				else
				{
					activeChar.setHwidLock(null);
					activeChar.sendMessage("Character is now Unlocked!");
				}
			}
			else
			{
				if(param[0].equalsIgnoreCase("setupPRecover"))
				{
					CCPPasswordRecover.startPasswordRecover(activeChar);
					return null;
				}
				if(param[0].startsWith("setupPRecover"))
				{
					CCPPasswordRecover.setup(activeChar, text);
					return null;
				}
				if(param[0].startsWith("cfgSPPassword") || param[0].startsWith("cfgSPRecover"))
				{
					CCPPasswordRecover.reset(activeChar, text);
					return null;
				}
				if(param[0].startsWith("secondaryPass"))
				{
					CCPSecondaryPassword.startSecondaryPasswordSetup(activeChar, text);
					return null;
				}
				if(param[0].equalsIgnoreCase("showPassword"))
					return "cfgPassword.htm";
				if(param[0].equals("changePassword"))
				{
					StringTokenizer st = new StringTokenizer(text, " | ");
					String[] passes = new String[st.countTokens() - 1];
					st.nextToken();
					for(int i = 0; i < passes.length; i++)
					{
						passes[i] = st.nextToken();
					}
					boolean newDialog = CCPPassword.setNewPassword(activeChar, passes);
					if(newDialog)
						return null;
					return "cfgPassword.htm";
				}
				if(param[0].equalsIgnoreCase("showRepair"))
					return "cfgRepair.htm";
				if(param[0].equalsIgnoreCase("ping"))
				{
					CCPSmallCommands.getPing(activeChar);
					return null;
				}
				if(param[0].equalsIgnoreCase("cwhPrivs"))
				{
					if(param.length > 1)
					{
						String args = param[1] + (param.length > 2 ? " " + param[2] : "");
						return CCPCWHPrivilages.clanMain(activeChar, args);
					}
					return "cfgClan.htm";
				}
				if(param[0].equals("delevel"))
				{
					if(param.length > 1 && StringUtils.isNumeric(param[1]))
					{
						boolean success = CCPSmallCommands.decreaseLevel(activeChar, Integer.parseInt(param[1]));
						if (success)
							return null;
					}
					return "cfgDelevel.htm";
				}
			}
		}
		return "char.htm";
	}
  
	public String replacePage(String currentPage, Player activeChar, String additionalText, String bypass)
	{
		currentPage = currentPage.replaceFirst("%online%", CCPSmallCommands.showOnlineCount());
		currentPage = currentPage.replaceFirst("%antigrief%", getEnabledDisabled(activeChar.getVarBoolean("antigrief")));
		currentPage = currentPage.replaceFirst("%noe%", getEnabledDisabled(activeChar.getVarBoolean("NoExp")));
		currentPage = currentPage.replaceFirst("%notraders%", getEnabledDisabled(activeChar.getVarBoolean("notraders")));
		currentPage = currentPage.replaceFirst("%notShowBuffAnim%", getEnabledDisabled(activeChar.getVarBoolean("notShowBuffAnim")));
		currentPage = currentPage.replaceFirst("%autoLoot%", getEnabledDisabled(activeChar.isAutoLootEnabled()));
		if(currentPage.contains("%charsOnAccount%"))
			currentPage = currentPage.replaceFirst("%charsOnAccount%", CCPRepair.getCharsOnAccount(activeChar.getName(), activeChar.getAccountName()));
		return currentPage;
	}
  
	private String getEnabledDisabled(boolean enabled)
	{
		if(enabled)
			return "Enabled";
		return "Disabled";
	}
  
	public void setAutoLoot(Player player)
	{
		if(Config.AUTO_LOOT_INDIVIDUAL)
			player.setAutoLoot(!player.isAutoLootEnabled());
	}
  
	public static CharacterControlPanel getInstance()
	{
		if(_instance == null)
			_instance = new CharacterControlPanel();
		return _instance;
	}
}
