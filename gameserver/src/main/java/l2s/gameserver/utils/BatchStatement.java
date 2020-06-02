package l2s.gameserver.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BatchStatement
{
	public static PreparedStatement createPreparedStatement(Connection con, String query) throws SQLException
	{
		con.setAutoCommit(false);
		return con.prepareStatement(query);
	}
}