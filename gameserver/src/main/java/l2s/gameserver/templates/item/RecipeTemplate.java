package l2s.gameserver.templates.item;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;

/**
 * @author Bonux
**/
public final class RecipeTemplate
{
	public static class RecipeComponent
	{
		private int _itemId;
		private long _count;
		private int _chance;

		public RecipeComponent(int itemId, long count, int chance)
		{
			_itemId = itemId;
			_count = count;
			_chance = chance;
		}

		public RecipeComponent(int itemId, long count)
		{
			this(itemId, count, 0);
		}

		public int getItemId()
		{
			return _itemId;
		}

		public long getCount()
		{
			return _count;
		}

		public int getChance()
		{
			return _chance;
		}
	}

	private final int _id;
	private final int _level;
	private final int _mpConsume;
	private final int _successRate;
	private final int _itemId;
	private final boolean _isCommon;
	private final Collection<RecipeComponent> _materials;
	private final Collection<RecipeComponent> _products;
	private final Collection<RecipeComponent> _npcFee;

	public RecipeTemplate(int id, int level, int mpConsume, int successRate, int itemId, boolean isCommon)
	{
		_materials = new ArrayList<RecipeComponent>();
		_products = new ArrayList<RecipeComponent>();
		_npcFee = new ArrayList<RecipeComponent>();

		_id = id;
		_level = level;
		_mpConsume = mpConsume;
		_successRate = successRate;
		_itemId = itemId;
		_isCommon = isCommon;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMpConsume()
	{
		return _mpConsume;
	}

	public int getSuccessRate()
	{
		return _successRate;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public boolean isCommon()
	{
		return _isCommon;
	}

	public void addMaterial(RecipeComponent material)
	{
		_materials.add(material);
	}

	public RecipeComponent[] getMaterials()
	{
		return _materials.toArray(new RecipeComponent[_materials.size()]);
	}

	public void addProduct(RecipeComponent product)
	{
		_products.add(product);
	}

	public RecipeComponent[] getProducts()
	{
		return _products.toArray(new RecipeComponent[_products.size()]);
	}

	public RecipeComponent getRandomProduct()
	{
		int chancesAmount = 0;
		for(RecipeComponent product : _products)
			chancesAmount += product.getChance();

		if(Rnd.chance(chancesAmount))
		{
			RecipeComponent[] successProducts = new RecipeComponent[0];
			while(successProducts.length == 0)
			{
				for(RecipeComponent product : _products)
				{
					if(Rnd.chance(product.getChance()))
						successProducts = ArrayUtils.add(successProducts, product);
				}
			}

			return successProducts[Rnd.get(successProducts.length)];
		}
		return null;
	}

	public void addNpcFee(RecipeComponent fee)
	{
		_npcFee.add(fee);
	}

	public RecipeComponent[] getNpcFee()
	{
		return _npcFee.toArray(new RecipeComponent[_npcFee.size()]);
	}
}