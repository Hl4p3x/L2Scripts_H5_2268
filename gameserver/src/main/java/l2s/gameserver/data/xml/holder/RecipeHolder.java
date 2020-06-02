package l2s.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.Collection;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.item.RecipeTemplate;

/**
 * @author Bonux
**/
public final class RecipeHolder extends AbstractHolder
{
	private static final RecipeHolder _instance = new RecipeHolder();

	private final TIntObjectMap<RecipeTemplate> _listByRecipeId = new TIntObjectHashMap<RecipeTemplate>();
	private final TIntObjectMap<RecipeTemplate> _listByRecipeItem = new TIntObjectHashMap<RecipeTemplate>();

	public static RecipeHolder getInstance()
	{
		return _instance;
	}

	public void addRecipe(RecipeTemplate recipe)
	{
		_listByRecipeId.put(recipe.getId(), recipe);
		_listByRecipeItem.put(recipe.getItemId(), recipe);
	}

	public RecipeTemplate getRecipeByRecipeId(int id)
	{
		return _listByRecipeId.get(id);
	}

	public RecipeTemplate getRecipeByRecipeItem(int id)
	{
		return _listByRecipeItem.get(id);
	}

	public Collection<RecipeTemplate> getRecipes()
	{
		Collection<RecipeTemplate> result = new ArrayList<RecipeTemplate>(size());
		for(int key : _listByRecipeId.keys())
		{
			result.add(_listByRecipeId.get(key));
		}
		return result;
	}

	@Override
	public int size()
	{
		return _listByRecipeId.size();
	}

	@Override
	public void clear()
	{
		_listByRecipeId.clear();
		_listByRecipeItem.clear();
	}
}
