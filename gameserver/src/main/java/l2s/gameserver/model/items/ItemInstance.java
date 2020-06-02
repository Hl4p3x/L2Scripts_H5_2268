package l2s.gameserver.model.items;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.dao.JdbcEntity;
import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.dao.HidenItemsDAO;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.attachment.ItemAttachment;
import l2s.gameserver.model.items.listeners.ItemEnchantOptionsListener;
import l2s.gameserver.network.l2.s2c.DropItemPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SpawnItemPacket;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.taskmanager.ItemsAutoDestroy;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate.Grade;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;
import l2s.gameserver.templates.item.ItemType;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;

public final class ItemInstance extends GameObject implements JdbcEntity
{
	public static final int[] EMPTY_ENCHANT_OPTIONS = new int[3];

	private static final long serialVersionUID = 3162753878915133228L;

	private static final ItemsDAO _itemsDAO = ItemsDAO.getInstance();

	/** Enumeration of locations for item */
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		PET_INVENTORY,
		PET_PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		FREIGHT, // востановлен, используется в Dimension Manager
		@Deprecated
		LEASE,
		MAIL,
		AUCTION
	}

	public static final int CHARGED_NONE = 0;
	public static final int CHARGED_SOULSHOT = 1;
	public static final int CHARGED_SPIRITSHOT = 1;
	public static final int CHARGED_BLESSED_SPIRITSHOT = 2;

	public static final int FLAG_NO_DROP = 1 << 0;
	public static final int FLAG_NO_TRADE = 1 << 1;
	public static final int FLAG_NO_TRANSFER = 1 << 2;
	public static final int FLAG_NO_CRYSTALLIZE = 1 << 3;
	public static final int FLAG_NO_ENCHANT = 1 << 4;
	public static final int FLAG_NO_DESTROY = 1 << 5;
	public static final int FLAG_NO_UNEQUIP = 1 << 6;
	//public static final int FLAG_ALWAYS_DROP_ON_DIE = 1 << 7;
	public static final int FLAG_EQUIP_ON_PICKUP = 1 << 7;
	//public static final int FLAG_NO_RIDER_PICKUP = 1 << 9;
	//public static final int FLAG_PET_EQUIPPED = 1 << 10;

	/** ID of the owner */
	private int ownerId;
	/** ID of the item */
	private int itemId;
	/** Quantity of the item */
	private long count;
	/** Level of enchantment of the item */
	private int enchantLevel = -1;
	/** Location of the item */
	private ItemLocation loc;
	/** Slot where item is stored */
	private int locData;
	/** Custom item types (used loto, race tickets) */
	private int customType1;
	private int customType2;
	/** Время жизни временных вещей */
	private int lifeTime;
	/** Спецфлаги для конкретного инстанса */
	private int customFlags;
	/** Атрибуты вещи */
	private ItemAttributes attrs = new ItemAttributes();
	/** Аугментация вещи */
	private int[] _enchantOptions = EMPTY_ENCHANT_OPTIONS;

	/** Object L2Item associated to the item */
	private ItemTemplate template;
	/** Флаг, что вещь одета, выставляется в инвентаре **/
	private boolean isEquipped;

	/** Item drop time for autodestroy task */
	private long _dropTime;

	private IntSet _dropPlayers = Containers.EMPTY_INT_SET;
	private long _dropTimeOwner;

	private int _chargedSoulshot = CHARGED_NONE;
	private int _chargedSpiritshot = CHARGED_NONE;

	private boolean _chargedFishtshot = false;
	private int _agathionEnergy;
	private int _visualId;

	private int _variationStoneId = 0;
	private int _variation1Id = 0;
	private int _variation2Id = 0;

	private ItemAttachment _attachment;
	private JdbcEntityState _state = JdbcEntityState.CREATED;

	public ItemInstance(int objectId)
	{
		super(objectId);
	}

	/**
	 * Constructor<?> of the L2ItemInstance from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		setItemId(itemId);
		setLifeTime(getTemplate().isTemporal() ? (int) (System.currentTimeMillis() / 1000L) + getTemplate().getDurability() * 60 : getTemplate().getDurability());
		setAgathionEnergy(getTemplate().getAgathionEnergy());
		setLocData(-1);
		setEnchantLevel(0);
	}

	public int getOwnerId()
	{
		return ownerId;
	}

	public void setOwnerId(int ownerId)
	{
		this.ownerId = ownerId;
	}

	public int getItemId()
	{
		return itemId;
	}

	public void setItemId(int id)
	{
		itemId = id;
		template = ItemHolder.getInstance().getTemplate(id);
		setCustomFlags(getCustomFlags());
	}

	public long getCount()
	{
		return count;
	}

	public void setCount(long count)
	{
		if(count < 0)
			count = 0;

		if(!isStackable() && count > 1L)
		{
			this.count = 1L;
			//TODO audit
			return;
		}

		this.count = count;
	}

	public int getEnchantLevel()
	{
		return enchantLevel;
	}

	public void setEnchantLevel(int enchantLevel)
	{
		final int old = this.enchantLevel;

		this.enchantLevel = enchantLevel;

		if(old != this.enchantLevel && getTemplate().getEnchantOptions().size() > 0)
		{
			Player player = GameObjectsStorage.getPlayer(ownerId);

			ItemEquipResult result = player == null ? null : new ItemEquipResult(player);
			if(isEquipped() && result != null)
				ItemEnchantOptionsListener.getInstance().onUnequip(result, getEquipSlot(), this, player);

			int[] enchantOptions = getTemplate().getEnchantOptions().get(this.enchantLevel);

			_enchantOptions = enchantOptions == null ? EMPTY_ENCHANT_OPTIONS : enchantOptions;

			if(isEquipped() && result != null)
				ItemEnchantOptionsListener.getInstance().onEquip(result, getEquipSlot(), this, player);

			if(result != null)
				result.resultAction();
		}
	}

	public void setLocName(String loc)
	{
		this.loc = ItemLocation.valueOf(loc);
	}

	public String getLocName()
	{
		return loc.name();
	}

	public void setLocation(ItemLocation loc)
	{
		this.loc = loc;
	}

	public ItemLocation getLocation()
	{
		return loc;
	}

	public void setLocData(int locData)
	{
		this.locData = locData;
	}

	public int getLocData()
	{
		return locData;
	}

	public int getCustomType1()
	{
		return customType1;
	}

	public void setCustomType1(int newtype)
	{
		customType1 = newtype;
	}

	public int getCustomType2()
	{
		return customType2;
	}

	public void setCustomType2(int newtype)
	{
		customType2 = newtype;
	}

	public int getLifeTime()
	{
		return lifeTime;
	}

	public void setLifeTime(int lifeTime)
	{
		this.lifeTime = Math.max(0, lifeTime);
	}

	public int getCustomFlags()
	{
		return customFlags;
	}

	public void setCustomFlags(int flags)
	{
		customFlags = flags;
	}

	public ItemAttributes getAttributes()
	{
		return attrs;
	}

	public void setAttributes(ItemAttributes attrs)
	{
		this.attrs = attrs;
	}

	public int getShadowLifeTime()
	{
		if(!isShadowItem())
			return 0;
		return getLifeTime();
	}

	public int getTemporalLifeTime()
	{
		if(!isTemporalItem())
			return 0;
		return getLifeTime() - (int) (System.currentTimeMillis() / 1000L);
	}

	private ScheduledFuture<?> _timerTask;

	public void startTimer(Runnable r)
	{
		_timerTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(r, 0, 60000L);
	}

	public void stopTimer()
	{
		if(_timerTask != null)
		{
			_timerTask.cancel(false);
			_timerTask = null;
		}
	}

	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return template.isEquipable();
	}

	/**
	 * Returns if item is equipped
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return isEquipped;
	}

	public void setEquipped(boolean isEquipped)
	{
		this.isEquipped = isEquipped;
	}

	public int getBodyPart()
	{
		return template.getBodyPart();
	}

	/**
	 * Returns the slot where the item is stored
	 * @return int
	 */
	public int getEquipSlot()
	{
		return getLocData();
	}

	/**
	 * Returns the characteristics of the item
	 * @return L2Item
	 */
	public ItemTemplate getTemplate()
	{
		return template;
	}

	public void setDropTime(long time)
	{
		_dropTime = time;
	}

	public long getLastDropTime()
	{
		return _dropTime;
	}

	public long getDropTimeOwner()
	{
		return _dropTimeOwner;
	}

	/**
	 * Returns the type of item
	 * @return Enum
	 */
	public ItemType getItemType()
	{
		return template.getItemType();
	}

	public boolean isArmor()
	{
		return template.isArmor();
	}

	public boolean isAccessory()
	{
		return template.isAccessory();
	}
	public boolean isNoEnchant()
	{
		return template.isNoEnchant();
	}

	public boolean isShieldNoEnchant()
	{
		return template.isShieldNoEnchant();
	}

	public boolean isSigelNoEnchant()
	{
		return template.isSigelNoEnchant();
	}

	public boolean isWeapon()
	{
		return template.isWeapon();
	}
	public boolean isNotAugmented()
	{
		return template.isNotAugmented();
	}

	public boolean isArrow()
	{
		return template.isArrow();
	}

	public boolean isUnderwear()
	{
		return template.isUnderwear();
	}

	/**
	 * Returns the reference price of the item
	 * @return int
	 */
	public int getReferencePrice()
	{
		return template.getReferencePrice();
	}

	/**
	 * Returns if item is stackable
	 * @return boolean
	 */
	public boolean isStackable()
	{
		if(template == null)
			System.out.println("BAD ITEM ID "+itemId+" DELETE IT");
		return template.isStackable();
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;

		if(player.isCursedWeaponEquipped() && CursedWeaponsManager.getInstance().isCursed(itemId))
			return;

		player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this, null);
	}

	public boolean isAugmented()
	{
		return getVariation1Id() != 0 || getVariation2Id() != 0;
	}

	public int getVariation1Id()
	{
		return _variation1Id;
	}

	public void setVariation1Id(int val)
	{
		_variation1Id = val;
	}

	public int getVariation2Id()
	{
		return _variation2Id;
	}

	public void setVariation2Id(int val)
	{
		_variation2Id = val;
	}

	public void setVariationStoneId(int id)
	{
		_variationStoneId = id;
	}

	public int getVariationStoneId()
	{
		return _variationStoneId;
	}

	/**
	 * Returns the type of charge with SoulShot of the item.
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public int getChargedSoulshot()
	{
		return _chargedSoulshot;
	}

	/**
	 * Returns the type of charge with SpiritShot of the item
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public int getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}

	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}

	/**
	 * Sets the type of charge with SoulShot of the item
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulshot(int type)
	{
		_chargedSoulshot = type;
	}

	/**
	 * Sets the type of charge with SpiritShot of the item
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritshot(int type)
	{
		_chargedSpiritshot = type;
	}

	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}

	public class FuncAttack extends Func
	{
		private final Element element;

		public FuncAttack(Element element, int order, Object owner)
		{
			super(element.getAttack(), order, owner);
			this.element = element;
		}

		@Override
		public void calc(Env env)
		{
			env.value += getAttributeElementValue(element, true);
		}
	}

	public class FuncDefence extends Func
	{
		private final Element element;

		public FuncDefence(Element element, int order, Object owner)
		{
			super(element.getDefence(), order, owner);
			this.element = element;
		}

		@Override
		public void calc(Env env)
		{
			env.value += getAttributeElementValue(element, true);
		}
	}

	/**
	 * This function basically returns a set of functions from
	 * L2Item/L2Armor/L2Weapon, but may add additional
	 * functions, if this particular item instance is enhanched
	 * for a particular player.
	 * @return Func[]
	 */
	public Func[] getStatFuncs()
	{
		Func[] result = Func.EMPTY_FUNC_ARRAY;

		LazyArrayList<Func> funcs = LazyArrayList.newInstance();

		if(template.getAttachedFuncs().length > 0)
			for(FuncTemplate t : template.getAttachedFuncs())
			{
				Func f = t.getFunc(this);
				if(f != null)
					funcs.add(f);
			}

		for(Element e : Element.VALUES)
		{
			if(isWeapon())
				funcs.add(new FuncAttack(e, 0x40, this));
			if(isArmor())
				funcs.add(new FuncDefence(e, 0x40, this));
		}

		if(!funcs.isEmpty())
			result = funcs.toArray(new Func[funcs.size()]);

		LazyArrayList.recycle(funcs);

		return result;
	}

	/**
	 * Return true if item is hero-item
	 * @return boolean
	 */
	public boolean isHeroWeapon()
	{
		return template.isHeroWeapon();
	}

	/**
	 * Return true if item can be destroyed
	 */
	public boolean canBeDestroyed(Player player)
	{
		if((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
			return false;

		if(isHeroWeapon())
			return false;

		if(PetDataTable.isPetControlItem(this) && player.isMounted())
			return false;

		if(player.getPetControlItem() == this)
			return false;

		if(player.getEnchantScroll() == this)
			return false;

		if(isCursed())
			return false;

		return template.isDestroyable();
	}

	/**
	 * Return true if item can be dropped
	 */
	public boolean canBeDropped(Player player, boolean pk)
	{
		if(player.isGM())
			return true;
		if(HidenItemsDAO.isHidden(this))
			return false;

		if((customFlags & FLAG_NO_DROP) == FLAG_NO_DROP)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented() && (!pk || !Config.DROP_ITEMS_AUGMENTED) && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if(!template.isDropable())
			return false;

		return true;
	}

	public boolean canBeTraded(Player player)
	{
		if(isEquipped())
			return false;

		if(player.isGM())
			return true;
		if(HidenItemsDAO.isHidden(this))
			return false;

		if((customFlags & FLAG_NO_TRADE) == FLAG_NO_TRADE)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if(!template.isTradeable() && !Config.CAN_BE_TRADED_NO_TARADEABLE)
			return false;

		if(!template.isSellable() && !Config.CAN_BE_TRADED_NO_SELLABLE)
			return false;

		if(!template.isStoreable() && !Config.CAN_BE_TRADED_NO_STOREABLE)
			return false;

		if(isShadowItem() && !Config.CAN_BE_TRADED_SHADOW_ITEM)
			return false;

		if(isHeroWeapon() && !Config.CAN_BE_TRADED_HERO_WEAPON)
			return false;

		return true;
	}

	/**
	 * Можно ли продать в магазин NPC
	 */
	public boolean canBeSold(Player player)
	{
		if(player.isGM())
			return true;

		if(HidenItemsDAO.isHidden(this))
			return false;
		if((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
			return false;

		if(getItemId() == ItemTemplate.ITEM_ID_ADENA)
			return false;

		if(template.getReferencePrice() == 0)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(isEquipped())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		if(!template.isTradeable())
			return false;

		if(!template.isSellable())
			return false;

		if(!template.isStoreable())
			return false;

		return true;
	}

	/**
	 * Можно ли положить на клановый склад
	 */
	public boolean canBeStored(Player player, boolean privatewh)
	{
		if(player.isGM())
			return true;

		if(HidenItemsDAO.isHidden(this))
			return false;
		if((customFlags & FLAG_NO_TRANSFER) == FLAG_NO_TRANSFER)
			return false;

		if(!getTemplate().isStoreable())
			return false;

		if(!privatewh && (isShadowItem() || isTemporalItem()))
			return false;

		if(!privatewh && isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(isEquipped())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;
		if(!privatewh && isAugmented() && !Config.CAN_BE_CWH_IS_AUGMENTED)
			return false;

		return privatewh || template.isTradeable();
	}

	public boolean canBeCrystallized(Player player)
	{
		if(isFlagNoCrystallize())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		return template.isCrystallizable();
	}

	public boolean canBeEnchanted()
	{
		if((customFlags & FLAG_NO_ENCHANT) == FLAG_NO_ENCHANT)
			return false;

		return template.canBeEnchanted();
	}

	public boolean canBeAugmented(Player player)
	{
		if(!getTemplate().isAugmentable())
			return false;

		if(isAugmented())
			return false;

		if(isHeroWeapon())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isCommonItem())
			return false;

		if(template.isPvP())
			return false;

		return true;
	}

	public boolean canBeExchanged(Player player)
	{
		if(player.isGM())
			return true;

		if(HidenItemsDAO.isHidden(this))
			return false;
		if((customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(!ItemFunctions.checkIfCanDiscard(player, this))
			return false;

		return template.isDestroyable();
	}

	public boolean isTerritoryAccessory()
	{
		return template.isTerritoryAccessory();
	}

	public boolean isShadowItem()
	{
		return template.isShadowItem();
	}

	public boolean isTemporalItem()
	{
		return template.isTemporal();
	}

	public boolean isCommonItem()
	{
		return template.isCommonItem();
	}

	public boolean isAltSeed()
	{
		return template.isAltSeed();
	}
	public boolean isAdena()
	{
		return template.isAdena();
	}

	public boolean isCursed()
	{
		return template.isCursed();
	}
	public boolean isHiddenItem()
	{
		return HidenItemsDAO.isHidden(this);
	}	

	/**
	 * Бросает на землю лут с NPC
	 */
	 
	 
	public void dropToTheGround(Player lastAttacker, NpcInstance fromNpc)
	{
		dropToTheGround(lastAttacker, fromNpc, false);
	}
	 
	public void dropToTheGround(Player lastAttacker, NpcInstance fromNpc, boolean isPvPZone)
	{
		Creature dropper = fromNpc;
		if(dropper == null)
			dropper = lastAttacker;

		Location pos = Location.findAroundPosition(dropper, 100);

		// activate non owner penalty
		if(lastAttacker != null) // lastAttacker в данном случае top damager
		{
			_dropPlayers = new HashIntSet(1, 2);
			for(Player $member : lastAttacker.getPlayerGroup())
				_dropPlayers.add($member.getObjectId());
			if(!isPvPZone)
				_dropTimeOwner = System.currentTimeMillis() + Config.NONOWNER_ITEM_PICKUP_DELAY + (fromNpc != null && fromNpc.isRaid() ? 285000 : 0);
			else
				_dropTimeOwner = System.currentTimeMillis() + Config.PVP_SPECIAL_NONOWNER_ITEM_PICKUP_DELAY; //ms
		}

		// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
		dropMe(dropper, pos);
	}

	/**
	 * Бросает вещь на землю туда, где ее можно поднять
	 */
	public void dropToTheGround(Creature dropper, Location dropPos)
	{
		if(GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
			dropMe(dropper, dropPos);
		else
			dropMe(dropper, dropper.getLoc());
	}

	/**
	 * Бросает вещь на землю из инвентаря туда, где ее можно поднять
	 */
	public void dropToTheGround(Playable dropper, Location dropPos)
	{
		setLocation(ItemLocation.VOID);
		if(getJdbcState().isPersisted())
		{
			setJdbcState(JdbcEntityState.UPDATED);
			update();
		}

		if(GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, dropper.getGeoIndex()))
			dropMe(dropper, dropPos);
		else
			dropMe(dropper, dropper.getLoc());
	}

	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion </li>
	 * <li>Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects of L2World </B></FONT><BR><BR>
	 *
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2ItemInstance</li>
	 * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Drop item</li>
	 * <li> Call Pet</li><BR>
	 *
	 * @param dropper Char that dropped item
	 * @param loc drop coordinates
	 */
	public void dropMe(Creature dropper, Location loc)
	{
		if(dropper != null)
			setReflection(dropper.getReflection());

		spawnMe0(loc, dropper);
		// Add drop to auto destroy item task
		if(isHerb())
			ItemsAutoDestroy.getInstance().addHerb(this);
		else if(Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed())
			ItemsAutoDestroy.getInstance().addItem(this);		
	}

	public final void pickupMe()
	{
		decayMe();
		setReflection(ReflectionManager.DEFAULT);
	}

	public ItemClass getItemClass()
	{
		return template.getItemClass();
	}

	/**
	 * Возвращает защиту от элемента.
	 * @return значение защиты
	 */
	private int getDefence(Element element)
	{
		return isArmor() ? getAttributeElementValue(element, true) : 0;
	}

	/**
	 * Возвращает защиту от элемента: огонь.
	 * @return значение защиты
	 */
	public int getDefenceFire()
	{
		return getDefence(Element.FIRE);
	}

	/**
	 * Возвращает защиту от элемента: вода.
	 * @return значение защиты
	 */
	public int getDefenceWater()
	{
		return getDefence(Element.WATER);
	}

	/**
	 * Возвращает защиту от элемента: воздух.
	 * @return значение защиты
	 */
	public int getDefenceWind()
	{
		return getDefence(Element.WIND);
	}

	/**
	 * Возвращает защиту от элемента: земля.
	 * @return значение защиты
	 */
	public int getDefenceEarth()
	{
		return getDefence(Element.EARTH);
	}

	/**
	 * Возвращает защиту от элемента: свет.
	 * @return значение защиты
	 */
	public int getDefenceHoly()
	{
		return getDefence(Element.HOLY);
	}

	/**
	 * Возвращает защиту от элемента: тьма.
	 * @return значение защиты
	 */
	public int getDefenceUnholy()
	{
		return getDefence(Element.UNHOLY);
	}

	/**
	 * Возвращает значение элемента.
	 * @return
	 */
	public int getAttributeElementValue(Element element, boolean withBase)
	{
		return attrs.getValue(element) + (withBase ? template.getBaseAttributeValue(element) : 0);
	}

	/**
	 * Возвращает элемент атрибуции предмета.<br>
	 */
	public Element getAttributeElement()
	{
		return attrs.getElement();
	}

	public int getAttributeElementValue()
	{
		return attrs.getValue();
	}

	public Element getAttackElement()
	{
		Element element = isWeapon() ? getAttributeElement() : Element.NONE;
		if(element == Element.NONE)
			for(Element e : Element.VALUES)
				if(template.getBaseAttributeValue(e) > 0)
					return e;
		return element;
	}

	public int getAttackElementValue()
	{
		return isWeapon() ? getAttributeElementValue(getAttackElement(), true) : 0;
	}

	/**
	 * Устанавливает элемент атрибуции предмета.<br>
	 * Element (0 - Fire, 1 - Water, 2 - Wind, 3 - Earth, 4 - Holy, 5 - Dark, -1 - None)
	 * @param element элемент
	 * @param value
	 */
	public void setAttributeElement(Element element, int value)
	{
		attrs.setValue(element, value);
	}

	/**
	 * Проверяет, является ли данный инстанс предмета хербом
	 * @return true если предмет является хербом
	 */
	public boolean isHerb()
	{
		return getTemplate().isHerb();
	}

	public Grade getCrystalType()
	{
		return template.getCrystalType();
	}

	@Override
	public String getName()
	{
		return getTemplate().getName();
	}

	public String getName(Player player)
	{
		return getTemplate().getName(player);
	}

	@Override
	public void save()
	{
		_itemsDAO.save(this);
	}
	
	@Override
	public void update()
	{
		_itemsDAO.update(this);
	}

	@Override
	public void delete()
	{
		_itemsDAO.delete(this);
	}

	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		//FIXME кажись дроппер у нас есть в итеме как переменная, ток проверить время? [VISTALL]
		L2GameServerPacket packet = null;
		if(dropper != null)
			packet = new DropItemPacket(this, dropper.getObjectId());
		else
			packet = new SpawnItemPacket(this);

		return Collections.singletonList(packet);
	}

	/**
	 * Returns the item in String format
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(getTemplate().getItemId());
		sb.append(" ");
		if(getEnchantLevel() > 0)
		{
			sb.append("+");
			sb.append(getEnchantLevel());
			sb.append(" ");
		}
		sb.append(getTemplate().getName());
		if(!getTemplate().getAdditionalName().isEmpty())
		{
			sb.append(" ");
			sb.append("\\").append(getTemplate().getAdditionalName()).append("\\");
		}
		sb.append(" ");
		sb.append("(");
		sb.append(getCount());
		sb.append(")");
		sb.append("[");
		sb.append(getObjectId());
		sb.append("]");

		return sb.toString();

	}

	@Override
	public void setJdbcState(JdbcEntityState state)
	{
		_state = state;
	}

	@Override
	public JdbcEntityState getJdbcState()
	{
		return _state;
	}
	
	@Override
	public boolean isItem()
	{
		return true;
	}

	public ItemAttachment getAttachment()
	{
		return _attachment;
	}

	public void setAttachment(ItemAttachment attachment)
	{
		ItemAttachment old = _attachment;
		_attachment = attachment;
		if(_attachment != null)
			_attachment.setItem(this);
		if(old != null)
			old.setItem(null);
	}

	public int getAgathionEnergy()
	{
		return _agathionEnergy;
	}

	public void setAgathionEnergy(int agathionEnergy)
	{
		_agathionEnergy = agathionEnergy;
	}

	public int getVisualId()
	{
		return _visualId;
	}

	public void setVisualId(int val)
	{
		_visualId = val;
	}

	public int[] getEnchantOptions()
	{
		return _enchantOptions;
	}

	public IntSet getDropPlayers()
	{
		return _dropPlayers;
	}

	public int getCrystalCountOnCrystallize()
	{
		int crystalsAdd = ItemFunctions.getCrystallizeCrystalAdd(this);
		return template.getCrystalCount() + crystalsAdd;
	}

	public int getCrystalCountOnEchant()
	{
		int defaultCrystalCount = template.getCrystalCount();
		if(defaultCrystalCount > 0)
		{
			int crystalsAdd = ItemFunctions.getCrystallizeCrystalAdd(this);
			return (int) Math.ceil(defaultCrystalCount / 2.) + crystalsAdd;
		}
		return 0;
	}

	public boolean isFlagNoCrystallize()
	{
		return (customFlags & FLAG_NO_CRYSTALLIZE) == FLAG_NO_CRYSTALLIZE;
	}
}