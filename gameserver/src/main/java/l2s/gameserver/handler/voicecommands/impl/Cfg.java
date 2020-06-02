package l2s.gameserver.handler.voicecommands.impl;

import org.apache.commons.lang3.math.NumberUtils;

import l2s.commons.map.hash.TIntStringHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Language;

public class Cfg extends Functions implements IVoicedCommandHandler
{
	private String[] _commandList = new String[] { "lang", "cfg" };

	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;

		if(command.equals("cfg"))
			if(args != null)
			{
				String[] param = args.split(" ");
				if(param.length == 2)
				{
					if(param[0].equalsIgnoreCase("lang"))
					{
						if(!Config.USE_CLIENT_LANG && Config.CAN_SELECT_LANGUAGE)
							activeChar.setLanguage(param[1]);
						else
							activeChar.sendMessage(new CustomMessage("l2s.gameserver.handler.voicecommands.impl.Cfg.useVoicedCommand.Lang", activeChar));
					}

					if(param[0].equalsIgnoreCase("noe"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("NoExp", "1", -1);
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("NoExp");

					if(param[0].equalsIgnoreCase(Player.NO_TRADERS_VAR))
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setNotShowTraders(true);
							activeChar.setVar(Player.NO_TRADERS_VAR, "1", -1);
						}
						else if(param[1].equalsIgnoreCase("of"))
						{
							activeChar.setNotShowTraders(false);
							activeChar.unsetVar(Player.NO_TRADERS_VAR);
						}

					if(param[0].equalsIgnoreCase(Player.NO_ANIMATION_OF_CAST_VAR))
						if(param[1].equalsIgnoreCase("on"))
						{
							activeChar.setNotShowBuffAnim(true);
							activeChar.setVar(Player.NO_ANIMATION_OF_CAST_VAR, "1", -1);
						}
						else if(param[1].equalsIgnoreCase("of"))
						{
							activeChar.setNotShowBuffAnim(false);
							activeChar.unsetVar(Player.NO_ANIMATION_OF_CAST_VAR);
						}

					if(param[0].equalsIgnoreCase("noShift"))
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("noShift", "1", -1);
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("noShift");

					if(Config.SERVICES_ENABLE_NO_CARRIER && param[0].equalsIgnoreCase("noCarrier"))
					{
						int time = NumberUtils.toInt(param[1], Config.SERVICES_NO_CARRIER_DEFAULT_TIME);

						if(time > Config.SERVICES_NO_CARRIER_MAX_TIME)
							time = Config.SERVICES_NO_CARRIER_MAX_TIME;
						else if(time < Config.SERVICES_NO_CARRIER_MIN_TIME)
							time = Config.SERVICES_NO_CARRIER_MIN_TIME;

						activeChar.setVar("noCarrier", String.valueOf(time), -1);
					}

					if(param[0].equalsIgnoreCase("translit"))
					{
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("translit", "tl", -1);
						else if(param[1].equalsIgnoreCase("la"))
							activeChar.setVar("translit", "tc", -1);
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("translit");
					}

					if(Config.AUTO_LOOT_INDIVIDUAL)
					{
						if(param[0].equalsIgnoreCase("autoloot"))
							activeChar.setAutoLoot(Boolean.parseBoolean(param[1]));
						if(param[0].equalsIgnoreCase("autolooth"))
							activeChar.setAutoLootHerbs(Boolean.parseBoolean(param[1]));
					}

					if(Config.ENABLE_CERTAIN_DROP_INVIDUAL && param[0].equalsIgnoreCase("certaindrop"))
						activeChar.setCertainDropIndividual(Boolean.parseBoolean(param[1]));

					if(param[0].equalsIgnoreCase("lfc"))
					{
						if(param[1].equalsIgnoreCase("on"))
							activeChar.setVar("lfcNotes", "on", -1);
						else if(param[1].equalsIgnoreCase("of"))
							activeChar.unsetVar("lfcNotes");
					}

					if(Config.ENABLE_DAM_ON_SCREEN)
					{
						if(param[0].equalsIgnoreCase("damtxt"))
						{
							if(param[1].equalsIgnoreCase("on"))
								activeChar.setDmgOnScreenEnable(true);
							else if(param[1].equalsIgnoreCase("of"))
								activeChar.setDmgOnScreenEnable(false);
						}
					}
				}
			}

		TIntStringHashMap templates = HtmCache.getInstance().getTemplates("command/cfg.htm", activeChar);

		String langBlock = "";
		if(!Config.USE_CLIENT_LANG && Config.CAN_SELECT_LANGUAGE)
		{
			boolean haveMoreLanguages = false;
			StringBuilder languagesButtons = new StringBuilder();

			final String langButton = templates.get(2);
			for(Language lang : Config.AVAILABLE_LANGUAGES)
			{
				if(activeChar.getLanguage() == lang)
					continue;

				haveMoreLanguages = true;

				String button = langButton;
				button = button.replace("<?short_lang_name?>", lang.getShortName());
				button = button.replace("<?lang_name?>", StringsHolder.getInstance().getString("LangFull", lang));

				languagesButtons.append(button);
			}

			if(haveMoreLanguages)
			{
				langBlock = templates.get(1);
				langBlock = langBlock.replace("<?current_lang?>", new CustomMessage("LangFull", activeChar).toString());
				langBlock = langBlock.replace("<?available_languages?>", languagesButtons.toString());
			}
		}

		final String disableMsg = new CustomMessage("common.Disable", activeChar).toString();
		final String enableMsg = new CustomMessage("common.Enable", activeChar).toString();

		String certainDropBlock = "";
		if(Config.ENABLE_CERTAIN_DROP_INVIDUAL)
		{
			certainDropBlock = templates.get(3);

			certainDropBlock = certainDropBlock.replace("<?value_certain?>", String.valueOf(!activeChar.isCertainDropEnabled()));
			if(activeChar.isCertainDropEnabled())
				certainDropBlock = certainDropBlock.replace("<?value_name_certain?>", disableMsg);
			else
				certainDropBlock = certainDropBlock.replace("<?value_name_certain?>", enableMsg);
		}

		String autolootBlock = "";
		if(Config.AUTO_LOOT_INDIVIDUAL)
		{
			autolootBlock = templates.get(4);

			autolootBlock = autolootBlock.replace("<?value_items?>", String.valueOf(!activeChar.isAutoLootEnabled()));
			if(activeChar.isAutoLootEnabled())
				autolootBlock = autolootBlock.replace("<?value_name_items?>", disableMsg);
			else
				autolootBlock = autolootBlock.replace("<?value_name_items?>", enableMsg);

			autolootBlock = autolootBlock.replace("<?value_herbs?>", String.valueOf(!activeChar.isAutoLootHerbsEnabled()));
			if(activeChar.isAutoLootHerbsEnabled())
				autolootBlock = autolootBlock.replace("<?value_name_herbs?>", disableMsg);
			else
				autolootBlock = autolootBlock.replace("<?value_name_herbs?>", enableMsg);
		}

		String noCarrierBlock = "";
		if(Config.SERVICES_ENABLE_NO_CARRIER)
		{
			noCarrierBlock = templates.get(5);
			noCarrierBlock = noCarrierBlock.replace("<?no_carrier_time?>", Config.SERVICES_ENABLE_NO_CARRIER ? (activeChar.getVarBoolean("noCarrier") ? activeChar.getVar("noCarrier") : "0") : "N/A");
		}

		String dialog = templates.get(0);
		dialog = dialog.replace("<?lang_block?>", langBlock);
		dialog = dialog.replace("<?certaindrop_block?>", certainDropBlock);
		dialog = dialog.replace("<?autoloot_block?>", autolootBlock);
		dialog = dialog.replace("<?no_carrier_block?>", noCarrierBlock);

		if(activeChar.getVarBoolean("NoExp"))
		{
			dialog = dialog.replace("<?value_noe?>", "of");
			dialog = dialog.replace("<?value_name_noe?>", new CustomMessage("common.Disable", activeChar).toString());
		}
		else
		{
			dialog = dialog.replace("<?value_noe?>", "on");
			dialog = dialog.replace("<?value_name_noe?>", new CustomMessage("common.Enable", activeChar).toString());
		}

		if(activeChar.getVarBoolean("notraders"))
		{
			dialog = dialog.replace("<?value_notraders?>", "of");
			dialog = dialog.replace("<?value_name_notraders?>", new CustomMessage("common.Disable", activeChar).toString());
		}
		else
		{
			dialog = dialog.replace("<?value_notraders?>", "on");
			dialog = dialog.replace("<?value_name_notraders?>", new CustomMessage("common.Enable", activeChar).toString());
		}

		if(activeChar.isDmgOnScreenEnable())
		{
			dialog = dialog.replace("<?value_damtxt?>", "of");
			dialog = dialog.replace("<?value_name_damtxt?>", new CustomMessage("common.Disable", activeChar).toString());
		}
		else
		{
			dialog = dialog.replace("<?value_damtxt?>", "on");
			dialog = dialog.replace("<?value_name_damtxt?>", new CustomMessage("common.Enable", activeChar).toString());
		}

		if(activeChar.getVarBoolean("notShowBuffAnim"))
		{
			dialog = dialog.replace("<?value_notShowBuffAnim?>", "of");
			dialog = dialog.replace("<?value_name_notShowBuffAnim?>", new CustomMessage("common.Disable", activeChar).toString());
		}
		else
		{
			dialog = dialog.replace("<?value_notShowBuffAnim?>", "on");
			dialog = dialog.replace("<?value_name_notShowBuffAnim?>", new CustomMessage("common.Enable", activeChar).toString());
		}

		if(activeChar.getVarBoolean("noShift"))
		{
			dialog = dialog.replace("<?value_noShift?>", "of");
			dialog = dialog.replace("<?value_name_noShift?>", new CustomMessage("common.Disable", activeChar).toString());
		}
		else
		{
			dialog = dialog.replace("<?value_noShift?>", "on");
			dialog = dialog.replace("<?value_name_noShift?>", new CustomMessage("common.Enable", activeChar).toString());
		}

		if(activeChar.getVarBoolean("lfcNotes"))
		{
			dialog = dialog.replace("<?value_lfc?>", "of");
			dialog = dialog.replace("<?value_name_lfc?>", new CustomMessage("common.Disable", activeChar).toString());
		}
		else
		{
			dialog = dialog.replace("<?value_lfc?>", "on");
			dialog = dialog.replace("<?value_name_lfc?>", new CustomMessage("common.Enable", activeChar).toString());
		}

		String tl = activeChar.getVar("translit");
		if(tl == null)
		{
			dialog = dialog.replace("<?value_translit?>", "on");
			dialog = dialog.replace("<?value_name_translit?>", new CustomMessage("common.Enable", activeChar).toString());
		}
		else if(tl.equals("tl"))
		{
			dialog = dialog.replace("<?value_translit?>", "la");
			dialog = dialog.replace("<?value_name_translit?>", "Lt");
		}
		else if(tl.equals("tc"))
		{
			dialog = dialog.replace("<?value_translit?>", "of");
			dialog = dialog.replace("<?value_name_translit?>", new CustomMessage("common.Disable", activeChar).toString());
		}

		show(dialog, activeChar);

		return true;
	}

	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}