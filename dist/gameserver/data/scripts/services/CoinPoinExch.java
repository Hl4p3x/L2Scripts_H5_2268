package services;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExBR_GamePointPacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.ItemFunctions;

public class CoinPoinExch extends Functions
{
	private static final int _coinID = 4356;

	public void Show()
	{
		Player player = getSelf();
		if(player == null)
			return;

		String append = "Обмен валют<br>";
		append += "<br>";

		append += "Уважаемый игрок!<br>";
		append += "Тут вы можете обменять:<br>";
		append += "L2Game Coin на баланс в Item Mall<br>";
		append += "баланс в Item Mall на L2Game Coin.<br>";
		append += "Пожалуйста выберите направление:<br>";
		append += "<button value=\"Coin -> ItemMall\" action=\"bypass -h scripts_services.CoinPoinExch:ShowC2P \" width=250 height=15><br>";
		append += "<button value=\"ItemMall -> Coin\" action=\"bypass -h scripts_services.CoinPoinExch:ShowP2C \" width=250 height=15><br>";
		show(append, player, null);
	}

	public void ShowP2C()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(player.getPremiumPoints() < 30)
		{
			String append = "Ваш баланс слишком мал для исполнение данной функции!";
			show(append, player, null);
			return;
		}


		String append2 = "Курс обмена: 30 баланса в ItemMall = 1 L2GameCoin<br>";
		append2 += "Укажите количество которые вы обмениваете!<br>";
		append2 += "<edit var=\"exch2\" width=70> <br>";
		append2 += "<button value=\"Обменять\" action=\"bypass -h scripts_services.CoinPoinExch:DoP2C $exch2\" width=150 height=15><br> <br>";
		show(append2, player, null);

	}

	public void DoP2C(String[] param)
	{
		Player player = getSelf();
		if(player == null)
			return;

		String coinsToEx = param[0];
		if(!checkInteger(coinsToEx))
		{
			player.sendMessage(""+ player.getName() +", Пишите только цифры!");
			return;
		}
		int _coinsToEx = Integer.parseInt(param[0]);

		if(player.getPremiumPoints() < _coinsToEx || _coinsToEx < 30)
		{
			player.sendMessage(""+ player.getName() +", У вас не хватает баланса для обмена");
			return;
		}

		player.reducePremiumPoints(_coinsToEx);
		player.sendPacket(new ExBR_GamePointPacket(player));
		double _coinsToExDouble = _coinsToEx / 30;
		int _finalAmmount = (int) Math.ceil(_coinsToExDouble);
		ItemFunctions.addItem(player, _coinID, _finalAmmount, "Exchange Premium Points to L2Game Coin");
		player.sendMessage(""+ player.getName() +", Успешно добавились "+_finalAmmount+" L2Game Coin");
		player.sendChanges();
	}

	public void ShowC2P()
	{
		Player player = getSelf();
		if(player == null)
			return;

		if(player.getInventory().getCountOf(_coinID) <= 0)
		{
			String append = "У вас нету L2Game Coin в инвентаре!";
			show(append, player, null);
			return;
		}


		String append2 = "Курс обмена: 1 L2GameCoin = 30 баланса в ItemMall <br>";
		append2 += "Укажите количество которые вы обмениваете!<br>";
		append2 += "<edit var=\"exch1\" width=70> <br>";
		append2 += "<button value=\"Обменять\" action=\"bypass -h scripts_services.CoinPoinExch:DoC2P $exch1\" width=150 height=15><br> <br>";
		show(append2, player, null);

	}

	public void DoC2P(String[] param)
	{
		Player player = getSelf();
		if(player == null)
			return;

		String coinsToEx = param[0];
		if(!checkInteger(coinsToEx))
		{
			player.sendMessage(""+ player.getName() +", Пишите только цифры!");
			return;
		}
		int _coinsToEx = Integer.parseInt(param[0]);

		if(player.getInventory().getCountOf(_coinID) < _coinsToEx || _coinsToEx <= 0)
		{
			player.sendMessage(""+ player.getName() +", У вас не хватает вещей для обмена");
			return;
		}

		ItemFunctions.deleteItem(player, _coinID, _coinsToEx);
		int finPoint = (_coinsToEx*30);
		finPoint *= -1;
		player.reducePremiumPoints(finPoint);
		player.sendPacket(new ExBR_GamePointPacket(player));
		//player.getNetConnection().setPoints( (int) player.getPremiumPoints() + (_coinsToEx*30));
		player.sendMessage(""+ player.getName() +", Успешно добавились "+_coinsToEx*30+" баланса в ItemMall");
		player.sendChanges();
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