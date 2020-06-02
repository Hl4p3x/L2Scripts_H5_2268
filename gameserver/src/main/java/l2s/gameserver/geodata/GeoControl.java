package l2s.gameserver.geodata;

import org.napile.primitive.pair.ByteObjectPair;

import gnu.trove.map.TIntObjectMap;
import l2s.commons.geometry.Shape;
import l2s.gameserver.geodata.GeoEngine.CeilGeoControlType;

public interface GeoControl
{
	Shape getGeoShape();

	TIntObjectMap<ByteObjectPair<CeilGeoControlType>> getGeoAround();

	void setGeoAround(TIntObjectMap<ByteObjectPair<CeilGeoControlType>> value);

	int getGeoControlIndex();

	boolean isHollowGeo();
}