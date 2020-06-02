package l2s.commons.geometry;

public class Rectangle extends AbstractShape
{
	protected final Point2D[] points = new Point2D[4];
	protected final int radius;

	public Rectangle(int x1, int y1, int x2, int y2)
	{
		min.x = Math.min(x1, x2);
		min.y = Math.min(y1, y2);
		max.x = Math.max(x1, x2);
		max.y = Math.max(y1, y2);

		points[0] = new Point2D(min.x, min.y);
		points[1] = new Point2D(min.x, max.y);
		points[2] = new Point2D(max.x, max.y);
		points[3] = new Point2D(max.x, min.y);

		int r = 0;
		Point2D center = getCenter();
		for(Point2D point : points)
			r = Math.max(r, GeometryUtils.calculateDistance(center, point));
		radius = r;
	}

	@Override
	public Rectangle setZmax(int z)
	{
		max.z = z;
		return this;
	}

	@Override
	public Rectangle setZmin(int z)
	{
		min.z = z;
		return this;
	}
	
	@Override
	public boolean isInside(int x, int y)
	{
		return (x >= min.x) && (x <= max.x) && (y >= min.y) && (y <= max.y);
	}

	@Override
	public boolean isOnPerimeter(int x, int y)
	{
		return GeometryUtils.isOnPolygonPerimeter(points, x, y);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(min).append(", ").append(max);
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Point2D getCenter()
	{
		return GeometryUtils.getLineCenter(min.x, min.y, max.x, max.y);
	}

	@Override
	public Point2D getNearestPoint(int x, int y)
	{
		return GeometryUtils.getNearestPointOnPolygon(points, x, y);
	}

	@Override
	public int getRadius()
	{
		return radius;
	}
}