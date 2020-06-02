package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.util.Rnd;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.tables.FishTable;
import l2s.gameserver.templates.FishTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.PositionUtils;


public class FishingSkill extends Skill
{
	public FishingSkill(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		Player player = (Player) activeChar;

		if(player.getSkillLevel(SKILL_FISHING_MASTERY) == -1)
			return false;

		if(player.isFishing())
		{
			player.stopFishing();
			player.sendPacket(Msg.CANCELS_FISHING);
			return false;
		}

		if(player.isInBoat())
		{
			activeChar.sendPacket(Msg.YOU_CANT_FISH_WHILE_YOU_ARE_ON_BOARD);
			return false;
		}

		if(player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE);
			return false;
		}

		Zone fishingZone = player.getZone(ZoneType.FISHING);
		if(fishingZone == null)
		{
			player.sendPacket(Msg.YOU_CANT_FISH_HERE);
			return false;
		}

		if(player.isInWater())
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_FISH_WHILE_UNDER_WATER);
			return false;
		}

		WeaponTemplate weaponItem = player.getActiveWeaponTemplate();
		if(weaponItem == null || weaponItem.getItemType() != WeaponType.ROD)
		{
			//Fishing poles are not installed
			player.sendPacket(Msg.FISHING_POLES_ARE_NOT_INSTALLED);
			return false;
		}

		ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(lure == null || lure.getCount() < 1)
		{
			player.sendPacket(Msg.BAITS_ARE_NOT_PUT_ON_A_HOOK);
			return false;
		}

		//Вычисляем координаты поплавка
		int rnd = Rnd.get(50) + 150;
		double angle = PositionUtils.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle - 90);
		double sin = Math.sin(radian);
		double cos = Math.cos(radian);
		int x1 = -(int) (sin * rnd);
		int y1 = (int) (cos * rnd);
		int x = player.getX() + x1;
		int y = player.getY() + y1;
		//z - уровень карты
		int z = GeoEngine.getLowerHeight(x, y, player.getZ(), player.getGeoIndex()) + 1;

		// Проверяем, что поплавок оказался в воде
		// в зоне типа 2 можно рыбачить без воды, но если вода есть то ставим поплавок на ее поверхность
		boolean isInWater = fishingZone.getParams().getInteger("fishing_place_type") == 2;
		LazyArrayList<Zone> zones = LazyArrayList.newInstance();
		World.getZones(zones, new Location(x, y, z), player.getReflection());
		for(Zone zone : zones)
			if (zone.getType() == ZoneType.water)
			{
				//z - уровень воды
				z = zone.getTerritory().getZmax();
				isInWater = true;
				break;
			}
		LazyArrayList.recycle(zones);

		if(!isInWater)
		{
			player.sendPacket(Msg.YOU_CANT_FISH_HERE);
			return false;
		}

		player.getFishing().setFishLoc(new Location(x, y, z));

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature caster, List<Creature> targets)
	{
		if(caster == null || !caster.isPlayer())
			return;

		Player player = (Player) caster;

		ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(lure == null || lure.getCount() < 1)
		{
			player.sendPacket(SystemMsg.YOU_MUST_PUT_BAIT_ON_YOUR_HOOK_BEFORE_YOU_CAN_FISH);
			return;
		}
		Zone zone = player.getZone(ZoneType.FISHING);
		if(zone == null)
			return;

		int distributionId = zone.getParams().getInteger("distribution_id");

		int lureId = lure.getItemId();

		int group = l2s.gameserver.model.Fishing.getFishGroup(lure.getItemId());
		int type = l2s.gameserver.model.Fishing.getRandomFishType(lureId);
		int lvl = l2s.gameserver.model.Fishing.getRandomFishLvl(player);

		List<FishTemplate> fishs = FishTable.getInstance().getFish(group, type, lvl);
		if(fishs == null || fishs.size() == 0)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		if(!player.getInventory().destroyItemByObjectId(player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1L))
		{
			player.sendPacket(Msg.NOT_ENOUGH_BAIT);
			return;
		}

		int check = Rnd.get(fishs.size());
		FishTemplate fish = fishs.get(check);

		player.startFishing(fish, lureId);
	}
}