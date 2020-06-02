package l2s.gameserver.network.l2.c2s;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.network.l2.s2c.CharacterCreateFailPacket;
import l2s.gameserver.network.l2.s2c.CharacterCreateSuccessPacket;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;
import l2s.gameserver.templates.item.StartItem;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.Util;

public class CharacterCreate extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterCreate.class);

	// cSdddddddddddd
	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;

	@Override
	protected void readImpl()
	{
		_name = readS();
		readD(); // race
		_sex = readD();
		_classId = readD();
		readD(); // int
		readD(); // str
		readD(); // con
		readD(); // men
		readD(); // dex
		readD(); // wit
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
	}

	@Override
	protected void runImpl()
	{
		ClassId cid = ClassId.valueOf(_classId);
		if(cid == null || !cid.isOfLevel(ClassLevel.NONE))
			return;

		final boolean hfClient = getClient().isHFClient();

		if(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0 && CharacterDAO.getInstance().accountCharNumber(getClient().getLogin()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT)
		{
			if(hfClient)
				sendPacket(CharacterCreateFailPacket.REASON_TOO_MANY_CHARACTERS);
			return;
		}

		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
		{
			if(hfClient)
				sendPacket(CharacterCreateFailPacket.REASON_16_ENG_CHARS);
			return;
		}

		if(CharacterDAO.getInstance().getObjectIdByName(_name) > 0)
		{
			if(hfClient)
				sendPacket(CharacterCreateFailPacket.REASON_NAME_ALREADY_EXISTS);
			return;
		}

		if(_face > 2 || _face < 0)
		{
			_log.warn("Character Creation Failure: Character face " + _face + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		if(_hairStyle < 0 || (_sex == 0 && _hairStyle > 4) || (_sex != 0 && _hairStyle > 6))
		{
			_log.warn("Character Creation Failure: Character hair style " + _hairStyle + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		if(_hairColor > 3 || _hairColor < 0)
		{
			_log.warn("Character Creation Failure: Character hair color " + _hairColor + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		Player newChar = Player.create(_classId, _sex, getClient().getLogin(), _name, _hairStyle, _hairColor, _face);
		if(newChar == null)
		{
			_log.warn("Character Creation Failure: Player.create returned null. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		sendPacket(CharacterCreateSuccessPacket.STATIC);

		initNewChar(newChar);
		getClient().setCharSelection(CharacterSelectionInfoPacket.loadCharacterSelectInfo(getClient().getLogin()));
	}

	public static void initNewChar(Player newChar)
	{
		if(!newChar.getSubClassList().restore())
			return;

		PlayerTemplate template = newChar.getTemplate();

		if(Config.STARTING_ADENA > 0)
			newChar.addAdena(Config.STARTING_ADENA);

		newChar.setLoc(template.getStartLocation());

		if(Config.CHAR_TITLE)
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		else
			newChar.setTitle("");

		for(StartItem i : template.getStartItems())
		{
			ItemInstance item = ItemFunctions.createItem(i.getId());
			long count = i.getCount();
			if(item.isStackable())
			{
				item.setCount(count);
				item.setEnchantLevel(i.getEnchantLevel());
				newChar.getInventory().addItem(item);
			}
			else
			{
				for(long n = 0; n < count; n++)
				{
					item = ItemFunctions.createItem(i.getId());
					item.setEnchantLevel(i.getEnchantLevel());
					newChar.getInventory().addItem(item);
				}
				if(item.isEquipable() && i.isEquiped())
					newChar.getInventory().equipItem(item);
			}

			if(item.getItemId() == 5588) // tutorial book
				newChar.registerShortCut(new ShortCut(11, 0, ShortCut.TYPE_ITEM, item.getObjectId(), -1, 1));
		}

		for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_CREATE))
			hook.onPlayerCreate(newChar);

		for(SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(newChar, AcquireType.NORMAL))
			newChar.addSkill(SkillHolder.getInstance().getSkill(skill.getId(), skill.getLevel()), true);

		if(newChar.getSkillLevel(1001) > 0) // Soul Cry
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1001, 1, 1));
		if(newChar.getSkillLevel(1177) > 0) // Wind Strike
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1177, 1, 1));
		if(newChar.getSkillLevel(1216) > 0) // Self Heal
			newChar.registerShortCut(new ShortCut(2, 0, ShortCut.TYPE_SKILL, 1216, 1, 1));

		// add attack, take, sit shortcut
		newChar.registerShortCut(new ShortCut(0, 0, ShortCut.TYPE_ACTION, 2, -1, 1));
		newChar.registerShortCut(new ShortCut(3, 0, ShortCut.TYPE_ACTION, 5, -1, 1));
		newChar.registerShortCut(new ShortCut(10, 0, ShortCut.TYPE_ACTION, 0, -1, 1));
		// понял как на панельке отобразить. нц софт 10-11 панели сделали(by VISTALL)
		// fly transform
		newChar.registerShortCut(new ShortCut(0, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 911, 1, 1));
		newChar.registerShortCut(new ShortCut(3, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 884, 1, 1));
		newChar.registerShortCut(new ShortCut(4, ShortCut.PAGE_FLY_TRANSFORM, ShortCut.TYPE_SKILL, 885, 1, 1));
		// air ship
		newChar.registerShortCut(new ShortCut(0, ShortCut.PAGE_AIRSHIP, ShortCut.TYPE_ACTION, 70, 0, 1));

		startTutorialQuest(newChar);

		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
		newChar.setCurrentCp(0); // retail
		newChar.setOnlineStatus(false);

		newChar.store(false);
		newChar.getInventory().store();
		newChar.deleteMe();
		
		new File("./log/chars/"+newChar.getName()+"/").mkdir();
		Log.LogEvent(newChar.getName(), newChar.getIP(), "charCreated", "created");
	}

	public static void startTutorialQuest(Player player)
	{
		Quest q = QuestHolder.getInstance().getQuest(255);
		if(q != null)
			q.newQuestState(player);
	}
}