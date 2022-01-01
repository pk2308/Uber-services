package Uber.model;

import java.util.List;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

/**
 *
 * @author Kent Yeh
 */
public interface TestDao extends Dao {

    @SqlQuery("SELECT count(8) FROM appmember")
    int countUsers();
    
    @SqlQuery("SELECT count(8) FROM appmember WHERE EXISTS(SELECT 1 FROM authorities"
            + " WHERE authorities.account=appmember.account AND ARRAY_CONTAINS(ARRAY[ <auths> ],authority))")
    int countAdminOrUser(@BindList("auths") List<String> auths);
    
    @SqlQuery("SELECT member FROM hzmembers")
    List<String> queryHzMembers();
}
