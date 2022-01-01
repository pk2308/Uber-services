package Uber.context;

import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;

/**
 *
 * @author Kent Yeh
 */
public class JdbiLog implements SqlLogger {

    private static final Logger logger = LogManager.getLogger(JdbiLog.class);
    @Override
    public void logException(StatementContext context, SQLException ex) {
        logger.error("Execute SQL:\n" + context.getRenderedSql() + "\nwith " + context.getBinding().toString()
                + "\nFailed:" + ex.getMessage(), ex);
    }

    @Override
    public void logAfterExecution(StatementContext context) {
        logger.debug("run SQL:\n" + context.getRenderedSql() + "\nwith " + context.getBinding().toString());
    }

    @Override
    public void logBeforeExecution(StatementContext context) {

    }

}
