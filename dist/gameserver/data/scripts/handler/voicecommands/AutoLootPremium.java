package handler.voicecommands;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.ItemFunctions;

public class AutoLootPremium extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private String[] _commandList = new String[] { "loot" };

	private static String _itemName = "Adena";
	private static int _premiumLootItemId = 57;
	private static int _premiumLootDayCount = 1000;


	@Override
	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;

		if(command.equals("loot") && args != null && activeChar.getVar("AutoLoot") == null)
		{
			String[] param = args.split(" ");
			if(param.length == 2)
			{
				if(param[0].equalsIgnoreCase("add"))
				{
					String _dayS = param[1];
					if(!checkInteger(_dayS))
					{
						activeChar.sendMessage(""+ activeChar.getName() +", Пишите только цифры!");
						return false;
					}
					int _dayD = Integer.valueOf(_dayS);

					if(activeChar.getInventory().getCountOf(_premiumLootItemId) < _dayD * _premiumLootDayCount)
					{
						activeChar.sendMessage(""+ activeChar.getName() +", Не хватает вещей для покупки авто-лута");
						return false;
					}

					ItemFunctions.deleteItem(activeChar, _premiumLootItemId, _dayD * _premiumLootDayCount);
					activeChar.setAutoLoot(true);
					activeChar.unsetVar("AutoLoot");
					activeChar.setVar("AutoLoot", "true", System.currentTimeMillis() + (86400000 * _dayD));
					activeChar.sendMessage(""+ activeChar.getName() +", Вы успешно купили премиальный авто-лоот на "+_dayD+" дней");
					return true;
				}
			}
			String append = "<center>Премиум авто-лут:<br>";
			append += "Стоимость на 1 день авто-лута "+_premiumLootDayCount+" "+_itemName+"<br>";
			append += "Укажите на сколько дней вы хотите <br> купить авто-лут:<br>";
			append += "<edit var=\"days\" width=150> <br>";
			append += "<button value=\"Купить\" action=\"bypass -h user_loot add $days \" width=150 height=15></center><br>";
			show(append, activeChar, null);
			activeChar.setAutoLoot(false);
			activeChar.unsetVar("AutoLoot");
		}
		else if(activeChar.getVar("AutoLoot") != null)
		{
			activeChar.sendMessage(""+ activeChar.getName() +", Вы уже используете премиум авто-лут!");
			return false;
		}
		return false;
	}
	public boolean checkInteger(String number)
	{
		try
		{
			int x = Integer.parseInt(number);
			number = Integer.toString(x);
			return true;
		}
		catch (NumberFormatException e)
		{
			//e.printStackTrace();
		}
		return false;
	}
}