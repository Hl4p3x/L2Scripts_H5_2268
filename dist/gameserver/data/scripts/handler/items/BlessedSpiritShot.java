package handler.items;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExAutoSoulShot;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.item.WeaponTemplate;

public class BlessedSpiritShot extends ScriptItemHandler
{
	// all the items ids that this handler knowns
	private static final int[] _itemIds = { 3947, 3948, 3949, 3950, 3951, 3952, 22072, 22073, 22074, 22075, 22076 };
	private static final int[] _skillIds = { 2061, 2160, 2161, 2162, 2163, 2164 };

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		ItemInstance weaponInst = player.getActiveWeaponInstance();
		WeaponTemplate weaponItem = player.getActiveWeaponTemplate();
		int SoulshotId = item.getItemId();
		boolean isAutoSoulShot = false;

		if(player.getAutoSoulShot().contains(SoulshotId))
			isAutoSoulShot = true;

		if(weaponInst == null)
		{
			if(!isAutoSoulShot)
				player.sendPacket(Msg.CANNOT_USE_SPIRITSHOTS);
			return false;
		}

		if(weaponInst.getChargedSpiritshot() == ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			// already charged by blessed spirit shot
			// btw we cant charge only when bsps is charged
			return false;

		int spiritshotId = item.getItemId();
		int grade = weaponItem.getCrystalType().externalOrdinal;
		int blessedsoulSpiritConsumption = weaponItem.getSpiritShotCount();

		if(blessedsoulSpiritConsumption == 0)
		{
			// Can't use Spiritshots
			if(isAutoSoulShot)
			{
				player.removeAutoSoulShot(SoulshotId);
				player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addItemName(spiritshotId));
				return false;
			}
			player.sendPacket(Msg.CANNOT_USE_SPIRITSHOTS);
			return false;
		}

		if(grade == 0 && spiritshotId != 3947 // NG
				|| grade == 1 && spiritshotId != 3948 && spiritshotId != 22072 // D
				|| grade == 2 && spiritshotId != 3949 && spiritshotId != 22073 // C
				|| grade == 3 && spiritshotId != 3950 && spiritshotId != 22074 // B
				|| grade == 4 && spiritshotId != 3951 && spiritshotId != 22075 // A
				|| grade == 5 && spiritshotId != 3952 && spiritshotId != 22076 // S
		)
		{
			if(isAutoSoulShot)
				return false;
			player.sendPacket(Msg.SPIRITSHOT_DOES_NOT_MATCH_WEAPON_GRADE);
			return false;
		}
		if(!Config.INFINITY_SHOT)
			if(!player.getInventory().destroyItem(item, blessedsoulSpiritConsumption))
			{
				if(isAutoSoulShot)
				{
					player.removeAutoSoulShot(SoulshotId);
					player.sendPacket(new ExAutoSoulShot(SoulshotId, false), new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addItemName(spiritshotId));
					return false;
				}
				player.sendPacket(Msg.NOT_ENOUGH_SPIRITSHOTS);
				return false;
			}

		player.checkAndRemoveInvisible();
		weaponInst.setChargedSpiritshot(ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
		player.sendPacket(Msg.POWER_OF_MANA_ENABLED);
		player.broadcastPacket(new MagicSkillUse(player, player, _skillIds[grade], 1, 0, 0));
		return true;
	}

	@Override
	public final int[] getItemIds()
	{
		return _itemIds;
	}

	@Override
	public boolean isAutoUse()
	{
		return true;
	}
}