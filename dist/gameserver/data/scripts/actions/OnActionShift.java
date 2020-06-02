package actions;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.admincommands.impl.AdminEditChar;
import l2s.gameserver.model.AggroList.HateComparator;
import l2s.gameserver.model.AggroList.HateInfo;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.reward.RewardType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.PositionUtils;
import l2s.gameserver.utils.Util;

public class OnActionShift extends Functions
{
	public boolean OnActionShift_NpcInstance(Player player, GameObject object)
	{
		if(player == null || object == null)
			return false;

		if(!object.isNpc())
			return false;

		NpcInstance npc = (NpcInstance) object;
		if(npc.isPeaceNpc() && !player.isGM())
			return false;

		// Для мертвых мобов не показываем табличку, иначе спойлеры плачут
		if(npc.isDead())
			return false;

		if(!Config.ALLOW_NPC_SHIFTCLICK && !player.isGM())
		{
			if(Config.ALT_GAME_SHOW_DROPLIST)
			{
				droplist(player, npc, null, 1);
				return true;
			}
			return false;
		}

		String dialog;
		if(player.isGM() || Config.SHOW_FULL_NPC_SHIFTCLICK)
		{
			dialog = HtmCache.getInstance().getHtml("scripts/actions/player.L2NpcInstance.onActionShift.full.htm", player);
			dialog = dialog.replace("%class%", String.valueOf(npc.getClass().getSimpleName().replaceFirst("Instance", "")));
			dialog = dialog.replace("%id%", String.valueOf(npc.getNpcId()));
			dialog = dialog.replace("%respawn%", String.valueOf(npc.getSpawn() != null ? Util.formatTime(npc.getSpawn().getRespawnDelay()) : "0"));
			dialog = dialog.replace("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
			dialog = dialog.replace("%pevs%", String.valueOf(npc.getEvasionRate(null)));
			dialog = dialog.replace("%pacc%", String.valueOf(npc.getAccuracy()));
			dialog = dialog.replace("%pcrt%", String.valueOf(npc.getCriticalHit(null, null)));
			dialog = dialog.replace("%aspd%", String.valueOf(npc.getPAtkSpd()));
			dialog = dialog.replace("%cspd%", String.valueOf(npc.getMAtkSpd()));
			dialog = dialog.replace("%currentMP%", String.valueOf(npc.getCurrentMp()));
			dialog = dialog.replace("%currentHP%", String.valueOf(npc.getCurrentHp()));
			dialog = dialog.replace("%loc%", npc.getSpawn() == null ? "" : npc.getSpawn().getName());
			dialog = dialog.replace("%dist%", String.valueOf(npc.getDistance3D(player)));
			dialog = dialog.replace("%killed%", String.valueOf(0));//TODO [G1ta0] убрать
			dialog = dialog.replace("%spReward%", String.valueOf(npc.getSpReward()));
			dialog = dialog.replace("%xyz%", npc.getLoc().x + " " + npc.getLoc().y + " " + npc.getLoc().z);
			dialog = dialog.replace("%ai_type%", npc.getAI().getClass().getSimpleName());
			dialog = dialog.replace("%direction%", PositionUtils.getDirectionTo(npc, player).toString().toLowerCase());
			dialog = dialog.replace("%respawn%", String.valueOf(npc.getSpawn() != null ? Util.formatTime(npc.getSpawn().getRespawnDelay()) : "0"));
			dialog = dialog.replace("%factionId%", String.valueOf(npc.getFaction()));
			dialog = dialog.replace("%aggro%", String.valueOf(npc.getAggroRange()));
			dialog = dialog.replace("%pDef%", String.valueOf(npc.getPDef(null)));
			dialog = dialog.replace("%mDef%", String.valueOf(npc.getMDef(null, null)));
			dialog = dialog.replace("%pAtk%", String.valueOf(npc.getPAtk(null)));
			dialog = dialog.replace("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
			dialog = dialog.replace("%runSpeed%", String.valueOf(npc.getRunSpeed()));

			dialog = dialog.replace("%AI%", String.valueOf(npc.getAI()) + ",<br1>active: " + npc.getAI().isActive() + ",<br1>intention: " + npc.getAI().getIntention());

			StringBuilder b = new StringBuilder("");
			for(Event e : npc.getEvents())
				b.append(e.toString()).append(";");

			dialog = dialog.replace("%event%", b.toString());
		}
		else
			dialog = HtmCache.getInstance().getHtml("scripts/actions/player.L2NpcInstance.onActionShift.htm", player);

		dialog = dialog.replace("<?npc_name?>", nameNpc(npc));
		dialog = dialog.replace("<?id?>", String.valueOf(npc.getNpcId()));
		dialog = dialog.replace("<?level?>", String.valueOf(npc.getLevel()));
		dialog = dialog.replace("<?max_hp?>", String.valueOf(npc.getMaxHp()));
		dialog = dialog.replace("<?max_mp?>", String.valueOf(npc.getMaxMp()));
		dialog = dialog.replace("<?xp_reward?>", String.valueOf(npc.getExpReward()));
		dialog = dialog.replace("<?sp_reward?>", String.valueOf(npc.getSpReward()));
		dialog = dialog.replace("<?aggresive?>", new CustomMessage(npc.getAggroRange() > 0 ? "YES" : "NO", player).toString());

		show(dialog, player, npc);

		return true;
	}

	public String getNpcRaceById(int raceId)
	{
		switch(raceId)
		{
			case 1:
				return "Undead";
			case 2:
				return "Magic Creatures";
			case 3:
				return "Beasts";
			case 4:
				return "Animals";
			case 5:
				return "Plants";
			case 6:
				return "Humanoids";
			case 7:
				return "Spirits";
			case 8:
				return "Angels";
			case 9:
				return "Demons";
			case 10:
				return "Dragons";
			case 11:
				return "Giants";
			case 12:
				return "Bugs";
			case 13:
				return "Fairies";
			case 14:
				return "Humans";
			case 15:
				return "Elves";
			case 16:
				return "Dark Elves";
			case 17:
				return "Orcs";
			case 18:
				return "Dwarves";
			case 19:
				return "Others";
			case 20:
				return "Non-living Beings";
			case 21:
				return "Siege Weapons";
			case 22:
				return "Defending Army";
			case 23:
				return "Mercenaries";
			case 24:
				return "Unknown Creature";
			case 25:
				return "Kamael";
			default:
				return "Not defined";
		}
	}

	public void droplist()
	{
		droplist(new String[0]);
	}

	public void droplist(String[] param)
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		if(param.length == 0)
			droplist(player, npc, null, 1);
		else if(param.length == 1)
			droplist(player, npc, RewardType.valueOf(param[0]), 1);
		else if(param.length > 1)
			droplist(player, npc, RewardType.valueOf(param[0]), Integer.parseInt(param[1]));
	}

	public void droplist(Player player, NpcInstance npc, RewardType showType, int page)
	{
		if(player == null || npc == null)
			return;

		if(Config.ALT_GAME_SHOW_DROPLIST || player.isGM())
			RewardListInfo.showInfo(player, npc, showType, page);
	}

	public void quests()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center><br>");

		Map<QuestEventType, Quest[]> list = npc.getTemplate().getQuestEvents();
		for(Map.Entry<QuestEventType, Quest[]> entry : list.entrySet())
		{
			for(Quest q : entry.getValue())
				dialog.append(entry.getKey()).append(" ").append(q.getClass().getSimpleName()).append("<br1>");
		}

		dialog.append("</body></html>");
		show(dialog.toString(), player, npc);
	}

	public void skills()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center>");

		Collection<Skill> list = npc.getAllSkills();
		if(list != null && !list.isEmpty())
		{
			dialog.append("<br>Active:<br>");
			for(Skill s : list)
				if(s.isActive())
					dialog.append(s.getName()).append("<br1>");
			dialog.append("<br>Passive:<br>");
			for(Skill s : list)
				if(!s.isActive())
					dialog.append(s.getName()).append("<br1>");
		}

		dialog.append("</body></html>");
		show(dialog.toString(), player, npc);
	}

	public void effects()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center><br>");

		List<Effect> list = npc.getEffectList().getAllEffects();
		if(list != null && !list.isEmpty())
			for(Effect e : list)
				dialog.append(e.getSkill().getName()).append("<br1>");

		dialog.append("<br><center><button value=\"");
		dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
		dialog.append("\" action=\"bypass -h scripts_actions.OnActionShift:effects\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");

		show(dialog.toString(), player, npc);
	}

	public void servitorEffects(String[] args)
	{
		if(args == null || args.length == 0)
			return;

		Player player = getSelf();
		Servitor servitor = player.getServitor();
		if(servitor == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(HtmlUtils.htmlNpcName(servitor.getNpcId())).append("<br></font></center><br>");

		List<Effect> list = servitor.getEffectList().getAllEffects();
		if(list != null && !list.isEmpty())
			for(Effect e : list)
				dialog.append(e.getSkill().getName()).append("<br1>");

		dialog.append("<br><center><button value=\"");
		dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
		dialog.append("\" action=\"bypass -h scripts_actions.OnActionShift:servitorEffects " + servitor.getObjectId() + "\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");

		show(dialog.toString(), player);
	}

	public void stats()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		String dialog = HtmCache.getInstance().getHtml("scripts/actions/player.L2NpcInstance.stats.htm", player);
		dialog = dialog.replace("<?npc_name?>", nameNpc(npc));
		dialog = dialog.replace("%level%", String.valueOf(npc.getLevel()));
		dialog = dialog.replace("%factionId%", String.valueOf(npc.getFaction()));
		dialog = dialog.replace("%aggro%", String.valueOf(npc.getAggroRange()));
		dialog = dialog.replace("%race%", getNpcRaceById(npc.getTemplate().getRace()));
		dialog = dialog.replace("%maxHp%", String.valueOf(npc.getMaxHp()));
		dialog = dialog.replace("%maxMp%", String.valueOf(npc.getMaxMp()));
		dialog = dialog.replace("%pDef%", String.valueOf(npc.getPDef(null)));
		dialog = dialog.replace("%mDef%", String.valueOf(npc.getMDef(null, null)));
		dialog = dialog.replace("%pAtk%", String.valueOf(npc.getPAtk(null)));
		dialog = dialog.replace("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
		dialog = dialog.replace("%paccuracy%", String.valueOf(npc.getAccuracy()));
		dialog = dialog.replace("%pevasionRate%", String.valueOf(npc.getEvasionRate(null)));
		dialog = dialog.replace("%pcriticalHit%", String.valueOf(npc.getCriticalHit(null, null)));
		dialog = dialog.replace("%runSpeed%", String.valueOf(npc.getRunSpeed()));
		dialog = dialog.replace("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
		dialog = dialog.replace("%pAtkSpd%", String.valueOf(npc.getPAtkSpd()));
		dialog = dialog.replace("%mAtkSpd%", String.valueOf(npc.getMAtkSpd()));
		show(dialog, player, npc);
	}

	public void resists()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
		dialog.append(nameNpc(npc)).append("<br></font></center><table width=\"80%\">");

		boolean hasResist;

		hasResist = addResist(dialog, "Fire", npc.calcStat(Stats.DEFENCE_FIRE, 0, null, null));
		hasResist |= addResist(dialog, "Wind", npc.calcStat(Stats.DEFENCE_WIND, 0, null, null));
		hasResist |= addResist(dialog, "Water", npc.calcStat(Stats.DEFENCE_WATER, 0, null, null));
		hasResist |= addResist(dialog, "Earth", npc.calcStat(Stats.DEFENCE_EARTH, 0, null, null));
		hasResist |= addResist(dialog, "Light", npc.calcStat(Stats.DEFENCE_HOLY, 0, null, null));
		hasResist |= addResist(dialog, "Darkness", npc.calcStat(Stats.DEFENCE_UNHOLY, 0, null, null));
		hasResist |= addResist(dialog, "Bleed", npc.calcStat(Stats.BLEED_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Poison", npc.calcStat(Stats.POISON_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Stun", npc.calcStat(Stats.STUN_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Root", npc.calcStat(Stats.ROOT_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Sleep", npc.calcStat(Stats.SLEEP_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Paralyze", npc.calcStat(Stats.PARALYZE_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Mental", npc.calcStat(Stats.MENTAL_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Debuff", npc.calcStat(Stats.DEBUFF_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Cancel", npc.calcStat(Stats.CANCEL_RESIST, 0, null, null));
		hasResist |= addResist(dialog, "Sword", 100 - npc.calcStat(Stats.SWORD_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Dual Sword", 100 - npc.calcStat(Stats.DUAL_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Blunt", 100 - npc.calcStat(Stats.BLUNT_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Dagger", 100 - npc.calcStat(Stats.DAGGER_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Bow", 100 - npc.calcStat(Stats.BOW_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Crossbow", 100 - npc.calcStat(Stats.CROSSBOW_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Polearm", 100 - npc.calcStat(Stats.POLE_WPN_VULNERABILITY, null, null));
		hasResist |= addResist(dialog, "Fist", 100 - npc.calcStat(Stats.FIST_WPN_VULNERABILITY, null, null));

		if(!hasResist)
			dialog.append("</table>No resists</body></html>");
		else
			dialog.append("</table></body></html>");
		show(dialog.toString(), player, npc);
	}

	private boolean addResist(StringBuilder dialog, String name, double val)
	{
		if(val == 0)
			return false;

		dialog.append("<tr><td>").append(name).append("</td><td>");
		if(val == Double.POSITIVE_INFINITY)
			dialog.append("MAX");
		else if(val == Double.NEGATIVE_INFINITY)
			dialog.append("MIN");
		else
		{
			dialog.append(String.valueOf((int)val));
			dialog.append("</td></tr>");
			return true;
		}

		dialog.append("</td></tr>");
		return true;
	}

	public void aggro()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;

		StringBuilder dialog = new StringBuilder("<html><body><table width=\"80%\"><tr><td>Attacker</td><td>Damage</td><td>Hate</td></tr>");

		Set<HateInfo> set = new TreeSet<HateInfo>(HateComparator.getInstance());
		set.addAll(npc.getAggroList().getCharMap().values());
		for(HateInfo aggroInfo : set)
			dialog.append("<tr><td>" + aggroInfo.attacker.getName() + "</td><td>" + aggroInfo.damage + "</td><td>" + aggroInfo.hate + "</td></tr>");

		dialog.append("</table><br><center><button value=\"");
		dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
		dialog.append("\" action=\"bypass -h scripts_actions.OnActionShift:aggro\" width=100 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" /></center></body></html>");
		show(dialog.toString(), player, npc);
	}

	public boolean OnActionShift_DoorInstance(Player player, GameObject object)
	{
		if(player == null || object == null || !player.getPlayerAccess().Door || !object.isDoor())
			return false;

		String dialog;
		DoorInstance door = (DoorInstance) object;
		dialog = HtmCache.getInstance().getHtml("scripts/actions/admin.L2DoorInstance.onActionShift.htm", player);
		dialog = dialog.replace("%CurrentHp%", String.valueOf((int) door.getCurrentHp()));
		dialog = dialog.replace("%MaxHp%", String.valueOf(door.getMaxHp()));
		dialog = dialog.replace("%ObjectId%", String.valueOf(door.getObjectId()));
		dialog = dialog.replace("%doorId%", String.valueOf(door.getDoorId()));
		dialog = dialog.replace("%pdef%", String.valueOf(door.getPDef(null)));
		dialog = dialog.replace("%mdef%", String.valueOf(door.getMDef(null, null)));
		dialog = dialog.replace("%type%", door.getDoorType().name());
		dialog = dialog.replace("%upgradeHP%", String.valueOf(door.getUpgradeHp()));
		StringBuilder b = new StringBuilder("");
		for(Event e : door.getEvents())
			b.append(e.toString()).append(";");
		dialog = dialog.replace("%event%", b.toString());

		show(dialog, player);
		player.sendActionFailed();
		return true;
	}

	public boolean OnActionShift_Player(Player player, GameObject object)
	{
		if(player == null || object == null || !player.getPlayerAccess().CanViewChar)
			return false;
		if(object.isPlayer())
			AdminEditChar.showCharacterList(player, (Player) object);
		return true;
	}

	public boolean OnActionShift_PetInstance(Player player, GameObject object)
	{
		if(player == null || object == null || !player.getPlayerAccess().CanViewChar)
			return false;
		if(object.isPet())
		{
			PetInstance pet = (PetInstance) object;

			String dialog;

			dialog = HtmCache.getInstance().getHtml("scripts/actions/admin.L2PetInstance.onActionShift.htm", player);
			dialog = dialog.replace("<?npc_name?>", HtmlUtils.htmlNpcName(pet.getNpcId()));
			dialog = dialog.replace("%title%", String.valueOf(StringUtils.isEmpty(pet.getTitle()) ? "Empty" : pet.getTitle()));
			dialog = dialog.replace("%level%", String.valueOf(pet.getLevel()));
			dialog = dialog.replace("%class%", String.valueOf(pet.getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
			dialog = dialog.replace("%xyz%", pet.getLoc().x + " " + pet.getLoc().y + " " + pet.getLoc().z);
			dialog = dialog.replace("%heading%", String.valueOf(pet.getLoc().h));

			dialog = dialog.replace("%owner%", String.valueOf(pet.getPlayer().getName()));
			dialog = dialog.replace("%ownerId%", String.valueOf(pet.getPlayer().getObjectId()));
			dialog = dialog.replace("%npcId%", String.valueOf(pet.getNpcId()));
			dialog = dialog.replace("%controlItemId%", String.valueOf(pet.getControlItem().getItemId()));

			dialog = dialog.replace("%exp%", String.valueOf(pet.getExp()));
			dialog = dialog.replace("%sp%", String.valueOf(pet.getSp()));

			dialog = dialog.replace("%maxHp%", String.valueOf(pet.getMaxHp()));
			dialog = dialog.replace("%maxMp%", String.valueOf(pet.getMaxMp()));
			dialog = dialog.replace("%currHp%", String.valueOf((int) pet.getCurrentHp()));
			dialog = dialog.replace("%currMp%", String.valueOf((int) pet.getCurrentMp()));

			dialog = dialog.replace("%pDef%", String.valueOf(pet.getPDef(null)));
			dialog = dialog.replace("%mDef%", String.valueOf(pet.getMDef(null, null)));
			dialog = dialog.replace("%pAtk%", String.valueOf(pet.getPAtk(null)));
			dialog = dialog.replace("%mAtk%", String.valueOf(pet.getMAtk(null, null)));
			dialog = dialog.replace("%paccuracy%", String.valueOf(pet.getAccuracy()));
			dialog = dialog.replace("%pevasionRate%", String.valueOf(pet.getEvasionRate(null)));
			dialog = dialog.replace("%pcrt%", String.valueOf(pet.getCriticalHit(null, null)));
			dialog = dialog.replace("%runSpeed%", String.valueOf(pet.getRunSpeed()));
			dialog = dialog.replace("%walkSpeed%", String.valueOf(pet.getWalkSpeed()));
			dialog = dialog.replace("%pAtkSpd%", String.valueOf(pet.getPAtkSpd()));
			dialog = dialog.replace("%mAtkSpd%", String.valueOf(pet.getMAtkSpd()));
			dialog = dialog.replace("%dist%", String.valueOf(pet.getRealDistance(player)));

			dialog = dialog.replace("%STR%", String.valueOf(pet.getSTR()));
			dialog = dialog.replace("%DEX%", String.valueOf(pet.getDEX()));
			dialog = dialog.replace("%CON%", String.valueOf(pet.getCON()));
			dialog = dialog.replace("%INT%", String.valueOf(pet.getINT()));
			dialog = dialog.replace("%WIT%", String.valueOf(pet.getWIT()));
			dialog = dialog.replace("%MEN%", String.valueOf(pet.getMEN()));

			show(dialog, player);
		}
		return true;
	}

	public boolean OnActionShift_ItemInstance(Player player, GameObject object)
	{
		if(player == null || object == null || !player.getPlayerAccess().CanViewChar)
			return false;
		if(object.isItem())
		{
			String dialog;
			ItemInstance item = (ItemInstance) object;
			dialog = HtmCache.getInstance().getHtml("scripts/actions/admin.L2ItemInstance.onActionShift.htm", player);
			dialog = dialog.replace("<?npc_name?>", String.valueOf(item.getTemplate().getName()));
			dialog = dialog.replace("%objId%", String.valueOf(item.getObjectId()));
			dialog = dialog.replace("%itemId%", String.valueOf(item.getItemId()));
			dialog = dialog.replace("%grade%", String.valueOf(item.getCrystalType()));
			dialog = dialog.replace("%count%", String.valueOf(item.getCount()));

			Player owner = GameObjectsStorage.getPlayer(item.getOwnerId()); //FIXME [VISTALL] несовсем верно, может быть CCE при условии если овнер не игрок
			dialog = dialog.replace("%owner%", String.valueOf(owner == null ? "none" : owner.getName()));
			dialog = dialog.replace("%ownerId%", String.valueOf(item.getOwnerId()));

			for(Element e : Element.VALUES)
				dialog = dialog.replace("%" + e.name().toLowerCase() + "Val%", String.valueOf(item.getAttributeElementValue(e, true)));

			dialog = dialog.replace("%attrElement%", String.valueOf(item.getAttributeElement()));
			dialog = dialog.replace("%attrValue%", String.valueOf(item.getAttributeElementValue()));

			dialog = dialog.replace("%enchLevel%", String.valueOf(item.getEnchantLevel()));
			dialog = dialog.replace("%type%", String.valueOf(item.getItemType()));

			dialog = dialog.replace("%dropTime%", String.valueOf(item.getDropTimeOwner()));
			//dialog = dialog.replace("%dropOwner%", String.valueOf(item.getDropOwnerId()));
			//dialog = dialog.replace("%dropOwnerId%", String.valueOf(item.getDropOwnerId()));

			show(dialog, player);
			player.sendActionFailed();
		}
		return true;
	}

	private String nameNpc(NpcInstance npc)
	{
		if(npc.getNameNpcString() == NpcString.NONE)
			return HtmlUtils.htmlNpcName(npc.getNpcId());
		else
			return HtmlUtils.htmlNpcString(npc.getNameNpcString().getId(), npc.getName());
	}
}