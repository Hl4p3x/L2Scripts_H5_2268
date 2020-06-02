package events.Viktorina;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.ItemFunctions;
import scriptconfig.ScriptConfig;

public class Viktorina extends Functions implements ScriptFile, IVoicedCommandHandler, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(Viktorina.class);
	private String[] _commandList = new String[] { "o", "voff", "von", "vhelp", "vtop", "v", "vo" };
	private ArrayList<String> questions = new ArrayList<String>();
	private static ArrayList<Player> playerList = new ArrayList<Player>();
	static ScheduledFuture<?> _taskViktorinaStart;
	private static ArrayList<RewardList> _items = new ArrayList<RewardList>();
	static ScheduledFuture<?> _taskStartQuestion;
	static ScheduledFuture<?> _taskStopQuestion;
	long _timeStopViktorina = 0;
	private static boolean status = false;
	private static boolean _questionStatus = false;
	private static int index;
	private static String question;
	private static String answer;
	private final static String GET_LIST_FASTERS = "SELECT `obj_id`,`value` FROM `character_variables` WHERE `name`='viktorinafirst' ORDER BY `value` DESC LIMIT 0,10";
	private final static String GET_LIST_TOP = "SELECT `obj_id`,`value` FROM `character_variables` WHERE `name`='viktorinaschet' ORDER BY `value` DESC LIMIT 0,10";;
	private static Viktorina instance;
	private static boolean DEBUG_VIKROINA = true;
	//Перменные ниже, перенес в конфиг.
	private static boolean VIKTORINA_ENABLED = false;// false;
	private static boolean VIKTORINA_REMOVE_QUESTION = false;//false;;
	private static boolean VIKTORINA_REMOVE_QUESTION_NO_ANSWER = false;//= false;
	private static int VIKTORINA_START_TIME_HOUR;// 16;
	private static int VIKTORINA_START_TIME_MIN;// 16;
	private static int VIKTORINA_WORK_TIME;//2;
	private static int VIKTORINA_TIME_ANSER;//1;
	private static int VIKTORINA_TIME_PAUSE;//1;

	//private static String REWARD_FERST = ScriptConfig.get("Victorina_Reward_Ferst");//"57,1,100;57,2,100;";
	//private static String REWARD_OTHER = ScriptConfig.get("Victorina_Reward_Other");//"57,1,100;57,2,100;";

	public static Viktorina getInstance()
	{
		if(instance == null)
			instance = new Viktorina();
		return instance;
	}

	/**
	 * Загружаем базу вопросов.
	 */
	public void loadQuestions()
	{
		File file = new File(Config.DATAPACK_ROOT + "/data/scripts/events/Viktorina/questions.txt");

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str;
			while((str = br.readLine()) != null)
				questions.add(str);
			br.close();
			_log.info("Viktorina Event: Questions loaded");
		}
		catch(Exception e)
		{
			_log.info("Viktorina Event: Error parsing questions file. " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Сохраняем вопросы обратно в файл.
	 */
	public void saveQuestions()
	{
		if(!VIKTORINA_REMOVE_QUESTION)
			return;
		File file = new File(Config.DATAPACK_ROOT + "/data/scripts/events/Viktorina/questions.txt");

		try
		{
			BufferedWriter br = new BufferedWriter(new FileWriter(file));
			for(String str : questions)
				br.write(str + "\r\n");
			br.close();
			_log.info("Viktorina Event: Questions saved");
		}
		catch(Exception e)
		{
			_log.info("Viktorina Event: Error save questions file. " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Готовим вопрос, вытягиваем рандомно любой вопрос с ответом.
	 */
	public void parseQuestion()
	{
		index = Rnd.get(questions.size());
		String str = questions.get(index);
		StringTokenizer st = new StringTokenizer(str, "|");
		question = st.nextToken();
		answer = st.nextToken();
	}

	public void checkAnswer(String chat, Player player)
	{
		if(chat.equalsIgnoreCase(answer) && isQuestionStatus())
		{
			if(!playerList.contains(player))
				playerList.add(player);
			_log.info("Viktorina: игрок - " + player.getName() + " дал правильный ответ. Был добавлен в список.");
			player.sendMessage("Это правильный ответ");
		}
	}

	/**
	 * Анонс вопроса викторины.
	 * @param text
	 */
	public void announseViktorina(String text)
	{
		SayPacket2 cs = new SayPacket2(0, ChatType.TELL, "Викторина", text);
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			if(player.getVar("viktorina") == "on")
				player.sendPacket(cs);
	}

	public void checkPlayers()
	{
		SayPacket2 cs = new SayPacket2(0, ChatType.TELL, "Викторина", "чтобы отказаться от участия в викторине введите .voff , для справки введите .vhelp");
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			if(player.getVar("viktorina") == null)
			{
				player.sendPacket(cs);
				player.setVar("viktorina", "on", -1);
			}
	}

	public void viktorinaSay(Player player, String text)
	{
		SayPacket2 cs = new SayPacket2(0, ChatType.TELL, "Викторина", text);
		if(player.getVar("viktorina") == "on")
			player.sendPacket(cs);
	}

	/**
	 * Подсчет правильно ответивших
	 */
	public void winners()
	{
		if(!isStatus())
		{
			_log.info("Пытался объявить победителя, но викторина оказалась выключена", "Viktorina");
			return;
		}
		if(isQuestionStatus())
		{
			_log.info("Пытался объявить победителя, когда действовал вопрос.", "Viktorina");
			return;
		}
		if(ServerVariables.getString("viktorinaq") == null)
			ServerVariables.set("viktorinaq", 0);
		if(ServerVariables.getString("viktorinaa") == null)
			ServerVariables.set("viktorinaa", 0);
		if(playerList.size() > 0)
		{
			announseViktorina(" правильных ответов: " + playerList.size() + ", первый ответил: " + playerList.get(0).getName() + ", правильны ответ: " + answer + "");
			ServerVariables.set("viktorinaq", ServerVariables.getInt("viktorinaq") + 1);
			ServerVariables.set("viktorinaa", ServerVariables.getInt("viktorinaa") + 1);
			if(VIKTORINA_REMOVE_QUESTION)
				questions.remove(index);
			_log.info("" + playerList.get(0).getName() + "|" + playerList.size() + "|" + question + "|" + answer, "Viktorina");
		}
		else
		{
			if(VIKTORINA_REMOVE_QUESTION_NO_ANSWER)
				announseViktorina(" правильного ответа не поступило, правильный ответ был:" + answer + "");
			if(!VIKTORINA_REMOVE_QUESTION_NO_ANSWER)
				announseViktorina(" правильного ответа не поступило, правильный ответ был:" + answer + "");
			ServerVariables.set("viktorinaq", ServerVariables.getInt("viktorinaq") + 1);
			if(VIKTORINA_REMOVE_QUESTION && VIKTORINA_REMOVE_QUESTION_NO_ANSWER)
				questions.remove(index);
		}
	}

	/**
	 * Считам через сколько стартуем викторину, создаем пул.
	 */
	public void Start()
	{
		if(_taskViktorinaStart != null)
			_taskViktorinaStart.cancel(true);
		Calendar _timeStartViktorina = Calendar.getInstance();
		_timeStartViktorina.set(Calendar.HOUR_OF_DAY, VIKTORINA_START_TIME_HOUR);
		_timeStartViktorina.set(Calendar.MINUTE, VIKTORINA_START_TIME_MIN);
		_timeStartViktorina.set(Calendar.SECOND, 0);
		_timeStartViktorina.set(Calendar.MILLISECOND, 0);
		Calendar _timeStopViktorina = Calendar.getInstance();
		_timeStopViktorina.setTimeInMillis(_timeStartViktorina.getTimeInMillis());
		_timeStopViktorina.add(Calendar.HOUR_OF_DAY, VIKTORINA_WORK_TIME);
		long currentTime = System.currentTimeMillis();
		// Если время виторины еще не наступило
		if(_timeStartViktorina.getTimeInMillis() >= currentTime)
			_taskViktorinaStart = ThreadPoolManager.getInstance().schedule(new ViktorinaStart(_timeStopViktorina.getTimeInMillis()), _timeStartViktorina.getTimeInMillis() - currentTime);
		else if(currentTime > _timeStartViktorina.getTimeInMillis() && currentTime < _timeStopViktorina.getTimeInMillis())
			_taskViktorinaStart = ThreadPoolManager.getInstance().schedule(new ViktorinaStart(_timeStopViktorina.getTimeInMillis()), 1000);
		else
		{
			_timeStartViktorina.add(Calendar.HOUR_OF_DAY, 24);
			_timeStopViktorina.add(Calendar.HOUR_OF_DAY, 24);
			_taskViktorinaStart = ThreadPoolManager.getInstance().schedule(new ViktorinaStart(_timeStopViktorina.getTimeInMillis()), _timeStartViktorina.getTimeInMillis() - currentTime);
		}

		if(DEBUG_VIKROINA)
			_log.info("Start Viktorina: " + _timeStartViktorina.getTime());
		_log.info("Stop Viktorina: " + _timeStopViktorina.getTime());

	}

	/**
	 * Функция продолжения таймера викторины, нужна при ручной остановке викторины.
	 * Назначает старт викторины на след день
	 */
	public void Continue()
	{
		if(_taskViktorinaStart != null)
			_taskViktorinaStart.cancel(true);
		Calendar _timeStartViktorina = Calendar.getInstance();
		_timeStartViktorina.set(Calendar.HOUR_OF_DAY, VIKTORINA_START_TIME_HOUR);
		_timeStartViktorina.set(Calendar.MINUTE, VIKTORINA_START_TIME_MIN);
		_timeStartViktorina.set(Calendar.SECOND, 0);
		_timeStartViktorina.set(Calendar.MILLISECOND, 0);
		Calendar _timeStopViktorina = Calendar.getInstance();
		_timeStopViktorina.setTimeInMillis(_timeStartViktorina.getTimeInMillis());
		_timeStopViktorina.add(Calendar.HOUR_OF_DAY, VIKTORINA_WORK_TIME);
		_timeStartViktorina.add(Calendar.HOUR_OF_DAY, 24);
		_timeStopViktorina.add(Calendar.HOUR_OF_DAY, 24);
		long currentTime = System.currentTimeMillis();
		_taskViktorinaStart = ThreadPoolManager.getInstance().schedule(new ViktorinaStart(_timeStopViktorina.getTimeInMillis()), _timeStartViktorina.getTimeInMillis() - currentTime);
		if(DEBUG_VIKROINA)
			_log.info("Continue Viktorina: " + _timeStartViktorina.getTime() + "|Stop Viktorina: " + _timeStopViktorina.getTime());

	}

	/**
	 * Запуск викторины в ручную!!
	 * запускается на время указанное в настройках.
	 */
	public void ForseStart()
	{
		if(_taskViktorinaStart != null)
			_taskViktorinaStart.cancel(true);
		Calendar _timeStartViktorina = Calendar.getInstance();
		Calendar _timeStopViktorina = Calendar.getInstance();
		_timeStopViktorina.setTimeInMillis(_timeStartViktorina.getTimeInMillis());
		_timeStopViktorina.add(Calendar.HOUR_OF_DAY, VIKTORINA_WORK_TIME);
		_log.info("Viktorina Started");
		_taskViktorinaStart = ThreadPoolManager.getInstance().schedule(new ViktorinaStart(_timeStopViktorina.getTimeInMillis()), 1000);
		if(DEBUG_VIKROINA)
			_log.info("Start Viktorina: " + _timeStartViktorina.getTime());
		_log.info("Stop Viktorina: " + _timeStopViktorina.getTime());

	}

	/**
	 * Стартуем викторину
	 * @author Sevil
	 *
	 */
	public class ViktorinaStart implements Runnable
	{

		public ViktorinaStart(long timeStopViktorina)
		{
			_timeStopViktorina = timeStopViktorina;
		}

		@Override
		public void run()
		{
			try
			{
				//if(isStatus())
				//{
				//	if(DEBUG_VIKROINA)
				//		_log.info("Viktoryna is already starter, WTF ??? \n" + Util.dumpStack());
				//	return;
				//}
				if(_taskStartQuestion != null)
					_taskStartQuestion.cancel(true);
				_taskStartQuestion = ThreadPoolManager.getInstance().schedule(new startQuestion(_timeStopViktorina), 5000);
				Announcements.getInstance().announceToAll("Викторина началась!");
				Announcements.getInstance().announceToAll("Для справки введите .vhelp");
				loadQuestions();
				setStatus(true);

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Задаем вопрос, ждем время, запускаем стоп вопроса.
	 * @author Sevil
	 *
	 */
	public class startQuestion implements Runnable
	{
		long _timeStopViktorina = 0;

		public startQuestion(long timeStopViktorina)
		{
			_timeStopViktorina = timeStopViktorina;
		}

		@Override
		public void run()
		{
			long currentTime = Calendar.getInstance().getTimeInMillis();
			if(currentTime > _timeStopViktorina)
			{
				_log.info("Viktorina time off...", "Viktorina");
				playerList.clear();
				setStatus(false);
				setQuestionStatus(false);
				announseViktorina("Время работы викторины истекло, Всем участникам приятного времяпрепровождения!");
				Announcements.getInstance().announceToAll("Время викторины закончилось.!");
				return;
			}
			if(!playerList.isEmpty())
			{
				_log.info("Что за чорт??? почему когда я задаю вопрос, лист правильно ответивших не пуст!?!?", "Viktorina");
				playerList.clear();
				return;
			}
			if(!isStatus())
			{
				_log.info("Что за чорт??? Почему я должен задавать вопрос, когда викторина не запущена???", "Viktorina");
				return;
			}
			if(!isQuestionStatus())
			{
				parseQuestion();
				checkPlayers();
				announseViktorina(question);
				if(_taskStopQuestion != null)
					_taskStopQuestion.cancel(true);
				_taskStopQuestion = ThreadPoolManager.getInstance().schedule(new stopQuestion(_timeStopViktorina), VIKTORINA_TIME_ANSER * 1000);
				setQuestionStatus(true);
			}
			else
				_log.info("Что за чорт???? ПОчему статус вопроса true?? когда быть должен false!!!!", "Viktorina");
		}
	}

	/**
	 * Стоп вопроса: подсчитываем правильные ответы, и кто дал правильный ответ быстрее всех.
	 * запускаем следующий вопрос.
	 * @author Sevil
	 *
	 */
	public class stopQuestion implements Runnable
	{
		long _timeStopViktorina = 0;

		public stopQuestion(long timeStopViktorina)
		{
			_timeStopViktorina = timeStopViktorina;
		}

		@Override
		public void run()
		{
			if(!isStatus())
			{
				_log.info("Что за чорт??? Почему я должен считать победителей и выдавать награды когда викторина не запущена???", "Viktorina");
				return;
			}
			setQuestionStatus(false);
			winners();
			rewarding();
			playerList.clear();
			if(_taskStartQuestion != null)
				_taskStartQuestion.cancel(true);
			_taskStartQuestion = ThreadPoolManager.getInstance().schedule(new startQuestion(_timeStopViktorina), VIKTORINA_TIME_PAUSE * 1000);
		}
	}

	/**
	 * Останавливаем эвент.
	 */
	public void stop()
	{
		playerList.clear();
		if(_taskStartQuestion != null)
			_taskStartQuestion.cancel(true);
		if(_taskStopQuestion != null)
			_taskStopQuestion.cancel(true);
		setQuestionStatus(false);
		_log.info("Viktorina Stoped.", "Viktorina");
		if(isStatus())
			Announcements.getInstance().announceToAll("Викторина остановлена!");
		setStatus(false);
		Continue();
	}

	/**
	 * Формируем окно справки. вызывается если игрок не разу не учавствовал в викторине
	 * или командой .vhelp
	 * @param player
	 */
	public void help(Player player)
	{
		int schet;
		int first;
		int vq;
		int va;
		String vstatus;
		if(player.getVar("viktorinaschet") == null)
			schet = 0;
		else
			schet = Integer.parseInt(player.getVar("viktorinaschet"));

		if(player.getVar("viktorinafirst") == null)
			first = 0;
		else
			first = Integer.parseInt(player.getVar("viktorinafirst"));

		if(ServerVariables.getString("viktorinaq", "0") == "0")
		{
			ServerVariables.set("viktorinaq", 0);
			vq = 0;
		}
		else
			vq = Integer.parseInt(ServerVariables.getString("viktorinaq"));

		if(ServerVariables.getString("viktorinaa", "0") == "0")
		{
			ServerVariables.set("viktorinaa", 0);
			va = 0;
		}
		else
			va = Integer.parseInt(ServerVariables.getString("viktorinaa"));

		if(player.getVar("viktorina") == "on")
			vstatus = "<font color=\"#00FF00\">Вы учавствуете в \"Викторине\"</font><br>";
		else
			vstatus = "<font color=\"#FF0000\">Вы не учавствуете в \"Викторине\"</font><br>";

		StringBuffer help = new StringBuffer("<html><body>");
		help.append("<center>Помошь по Викторине<br></center>");
		help.append(vstatus);
		help.append("Время начала викторины: " + VIKTORINA_START_TIME_HOUR + ":" + VIKTORINA_START_TIME_MIN + "<br>");
		help.append("Длительность работы викторины " + VIKTORINA_WORK_TIME + " ч.<br>");
		help.append("Время в течении которого можно дать ответ: " + VIKTORINA_TIME_ANSER + " сек.<br>");
		help.append("Время между вопросами: " + (VIKTORINA_TIME_ANSER + VIKTORINA_TIME_PAUSE) + " сек.<br>");
		help.append("Вопросов уже было заданно: " + vq + ".<br>");
		help.append("Верно ответили на : " + va + ".<br>");
		help.append("Вы верно ответили на : " + schet + ", в " + first + " вы были первым.<br>");
		help.append("<br>");
		help.append("<center>Команды викторины:<br></center>");
		help.append("<font color=\"LEVEL\">Ответ</font> - вводится в любой вид чата.<br>");
		help.append("<font color=\"LEVEL\">.von</font> - команда для включения викторины<br>");
		help.append("<font color=\"LEVEL\">.voff</font> - команда для выключения викторины<br>");
		help.append("<font color=\"LEVEL\">.vtop</font> - команда для просмотра результатов.<br>");
		help.append("<font color=\"LEVEL\">.vhelp</font> - команда для вызова этой страницы.<br>");
		help.append("<font color=\"LEVEL\">.v</font> - показывает текущий вопрос.<br>");
		help.append("</body></html>");
		show(help.toString(), player);
	}

	/**
	 * выводит топ
	 * @param player
	 */
	public void top(Player player)
	{
		StringBuffer top = new StringBuffer("<html><body>");
		top.append("<center>Топ Самых Быстрых");
		top.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");
		final List<Scores> fasters = getList(true);
		if(fasters.size() != 0)
		{
			top.append("<table width=300 border=0 bgcolor=\"000000\">");

			int index = 1;

			for(final Scores faster : fasters)
			{
				top.append("<tr>");
				top.append("<td><center>" + index + "<center></td>");
				top.append("<td><center>" + faster.getName() + "<center></td>");
				top.append("<td><center>" + faster.getScore() + "<center></td>");
				top.append("</tr>");
				index++;
			}

			top.append("<tr><td><br></td><td></td></tr>");

			top.append("</table>");
		}
		top.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
		top.append("</center>");

		top.append("<center>Общий топ");
		top.append("<img src=\"L2UI.SquareWhite\" width=270 height=1><img src=\"L2UI.SquareBlank\" width=1 height=3>");
		final List<Scores> top10 = getList(false);
		if(top10.size() != 0)
		{
			top.append("<table width=300 border=0 bgcolor=\"000000\">");

			int index = 1;

			for(final Scores top1 : top10)
			{
				top.append("<tr>");
				top.append("<td><center>" + index + "<center></td>");
				top.append("<td><center>" + top1.getName() + "<center></td>");
				top.append("<td><center>" + top1.getScore() + "<center></td>");
				top.append("</tr>");
				index++;
			}

			top.append("<tr><td><br></td><td></td></tr>");

			top.append("</table>");
		}
		top.append("<img src=\"L2UI.SquareWhite\" width=270 height=1> <img src=\"L2UI.SquareBlank\" width=1 height=3>");
		top.append("</center>");

		top.append("</body></html>");
		show(top.toString(), player);
	}

	public void setQuestionStatus(boolean b)
	{
		_questionStatus = b;
	}

	public boolean isQuestionStatus()
	{
		return _questionStatus;
	}

	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		executeTask("events.Viktorina.Viktorina", "preLoad", new Object[0], 20000);
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
		_log.info("Loaded Event: Viktorina");
	}

	@Override
	public void onReload()
	{
		stop();
	}

	@Override
	public void onShutdown()
	{
		stop();

	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{

		if(command.equals("o"))
		{
			if(args.equalsIgnoreCase(answer) && isQuestionStatus())
				if(!playerList.contains(player))
					playerList.add(player);
			//_log.info("preprepls " + playerList + "");
			if(!isQuestionStatus())
				viktorinaSay(player, "Возможно вопрос не был задан,или же время ответа истекло");
		}
		if(command.equals("von"))
		{
			player.setVar("viktorina", "on", -1);
			player.sendMessage("Вы принимаете участие в Викторине!");
			player.sendMessage("Ждите поступления Вам вопроса в ПМ!");
		}
		if(command.equals("voff"))
		{
			player.setVar("viktorina", "off", -1);
			player.sendMessage("Отказались от участия в Викторине!");
			player.sendMessage("До новых встреч!");
		}
		if(command.equals("vhelp"))
			help(player);
		if(command.equals("vtop"))
			top(player);
		if(command.equals("v"))
			viktorinaSay(player, question);
		if(command.equals("vo") && player.isGM())
			viktorinaSay(player, answer);
		return true;
	}

	/**
	 *выдача награды, начисление очков.
	 */
	private void rewarding()
	{
		if(!isStatus())
		{
			_log.info("Пытался вручить награды, но викторина оказалась выключена");
			return;
		}
		if(isQuestionStatus())
		{
			_log.info("Пытался вручить награды, когда действовал вопрос.");
			return;
		}

		parseReward();
		int schet;
		int first;
		for(Player player : playerList)
		{
			if(player.getVar("viktorinaschet") == null)
				schet = 0;
			else
				schet = Integer.parseInt(player.getVar("viktorinaschet"));
			if(player.getVar("viktorinafirst") == null)
				first = 0;
			else
				first = Integer.parseInt(player.getVar("viktorinafirst"));
			if(player == playerList.get(0))
			{
				giveItemByChance(player, true);
				player.setVar("viktorinafirst", "" + (first + 1) + "", -1);
			}
			else
				giveItemByChance(player, false);
			player.setVar("viktorinaschet", "" + (schet + 1) + "", -1);
		}
	}

	/**
	 * парсим конфиг наград
	 */
	private void parseReward()
	{
		_items.clear();
		StringTokenizer st = new StringTokenizer(ScriptConfig.get("Victorina_Reward_Ferst"), ";");
		StringTokenizer str = new StringTokenizer(ScriptConfig.get("Victorina_Reward_Other"), ";");
		while(st.hasMoreTokens())
		{
			String str1 = st.nextToken();
			StringTokenizer str2 = new StringTokenizer(str1, ",");
			final int itemId = Integer.parseInt(str2.nextToken());
			final int count = Integer.parseInt(str2.nextToken());
			final int chance = Integer.parseInt(str2.nextToken());
			final boolean first = true;
			final RewardList item = new RewardList();
			item.setProductId(itemId);
			item.setCount(count);
			item.setChance(chance);
			item.setFirst(first);
			_items.add(item);
		}
		while(str.hasMoreTokens())
		{
			String str1 = str.nextToken();
			StringTokenizer str2 = new StringTokenizer(str1, ",");
			final int itemId = Integer.parseInt(str2.nextToken());
			final int count = Integer.parseInt(str2.nextToken());
			final int chance = Integer.parseInt(str2.nextToken());
			final boolean first = false;
			final RewardList item = new RewardList();
			item.setProductId(itemId);
			item.setCount(count);
			item.setChance(chance);
			item.setFirst(first);
			_items.add(item);
		}
	}

	/**
	 * Выдаем приз на каторую укажет шанс + определяем выдавать приз для первого или для остальных
	 * @param player
	 * @param first
	 * @return
	 */
	private boolean giveItemByChance(Player player, boolean first)
	{
		int chancesumm = 0;
		int productId = 0;
		int chance = Rnd.get(0, 100);
		int count = 0;
		for(RewardList items : _items)
		{
			chancesumm = chancesumm + items.getChance();
			if(first == items.getFirst() && chancesumm > chance)
			{
				productId = items.getProductId();
				count = items.getCount();
				ItemFunctions.addItem(player, productId, count, false, "Viktorina reward");
				if(count > 1)
					player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S2_S1S).addItemName(productId).addNumber(count));
				else
					player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1).addItemName(productId));
				if(DEBUG_VIKROINA)
					_log.info("Игрок: " + player.getName() + " получил " + productId + ":" + count + " с шансом: " + items.getChance() + ":" + items.getFirst() + "", "Viktorina");
				return true;
			}
		}
		return true;
	}

	private class RewardList
	{
		public int _productId;
		public int _count;
		public int _chance;
		public boolean _first;

		private void setProductId(int productId)
		{
			_productId = productId;
		}

		private void setChance(int chance)
		{
			_chance = chance;
		}

		private void setCount(int count)
		{
			_count = count;
		}

		private void setFirst(boolean first)
		{
			_first = first;
		}

		private int getProductId()
		{
			return _productId;
		}

		private int getChance()
		{
			return _chance;
		}

		private int getCount()
		{
			return _count;
		}

		private boolean getFirst()
		{
			return _first;
		}
	}

	private boolean isStatus()
	{
		return status;
	}

	public static boolean isRunned()
	{
		return status;
	}

	private void setStatus(boolean status)
	{
		Viktorina.status = status;
	}

	/**
	 * Возвращаем имя чара по его obj_Id
	 * @param char_id
	 * @return
	 */
	private String getName(int char_id)
	{
		String name = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?");
			statement.setInt(1, char_id);
			rset = statement.executeQuery();
			rset.next();
			name = rset.getString("char_name");
			//return name;
		}
		catch(final SQLException e)
		{
			_log.info("ААА!!! ОПАСНОСТЬ, не могу найти игрока с таким obj_Id:" + e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return name;
	}

	/**
	 * Возвращаем лист имен.
	 * @param first
	 * @return
	 */
	private List<Scores> getList(final boolean first)
	{
		final List<Scores> names = new ArrayList<Scores>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		String GET_LIST = null;
		if(first)
			GET_LIST = GET_LIST_FASTERS;
		else
			GET_LIST = GET_LIST_TOP;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GET_LIST);
			rset = statement.executeQuery();

			while(rset.next())
			{
				final String name = getName(rset.getInt("obj_id"));
				final int score = rset.getInt("value");
				Scores scores = new Scores();
				scores.setName(name);
				scores.setScore(score);
				names.add(scores);
			}
			return names;
		}
		catch(final SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return names;
	}

	private class Scores
	{
		public String _name;
		public int _score;

		private void setName(String name)
		{
			_name = name;
		}

		private void setScore(int score)
		{
			_score = score;
		}

		private String getName()
		{
			return _name;
		}

		private int getScore()
		{
			return _score;
		}
	}

	public static void preLoad()
	{
		VIKTORINA_ENABLED = ScriptConfig.getBoolean("Victorina_Enabled");// false;
		VIKTORINA_REMOVE_QUESTION = ScriptConfig.getBoolean("Victorina_Remove_Question");//false;;
		VIKTORINA_REMOVE_QUESTION_NO_ANSWER = ScriptConfig.getBoolean("Victorina_Remove_Question_No_Answer");//= false;
		VIKTORINA_START_TIME_HOUR = ScriptConfig.getInt("Victorina_Start_Time_Hour");// 16;
		VIKTORINA_START_TIME_MIN = ScriptConfig.getInt("Victorina_Start_Time_Minute");// 16;
		VIKTORINA_WORK_TIME = ScriptConfig.getInt("Victorina_Work_Time");//2;
		VIKTORINA_TIME_ANSER = ScriptConfig.getInt("Victorina_Time_Answer");//1;
		VIKTORINA_TIME_PAUSE = ScriptConfig.getInt("Victorina_Time_Pause");//1;
		if(VIKTORINA_ENABLED)
			executeTask("events.Viktorina.Viktorina", "Start", new Object[0], 5000);
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		SayPacket2 cs = new SayPacket2(0, ChatType.CRITICAL_ANNOUNCE, "Викторина", "Активен ивент Викторина! Для участия наберите команду .von! Для справки .vhelp!");
		if(isStatus())
			player.sendPacket(cs);
	}
}
