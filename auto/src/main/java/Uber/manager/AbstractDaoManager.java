package Uber.manager;

import java.beans.PropertyEditorSupport;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 *
 * @author Kent Yeh
 * @param <K> Key Class
 * @param <E> Entity Class
 */
@Transactional(readOnly = true)
public abstract class AbstractDaoManager<K, E> extends PropertyEditorSupport implements ApplicationContextAware {

    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = Exception.class)
    public abstract E findByPrimaryKey(K key);

    private org.springframework.context.ApplicationContext context;

    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public void setApplicationContext(org.springframework.context.ApplicationContext ctx) throws BeansException {
        this.context = ctx;
    }

    /**
     * Convert String to key
     *
     * @param text primary key string.
     * @return primary key
     */
    public abstract K text2Key(String text);

    @Override
    public String getAsText() {
        return getValue() == null ? "" : getValue().toString();
    }

    /**
     *
     * @param text primary key string.
     * @throws IllegalArgumentException
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(StringUtils.hasText(text) ? findByPrimaryKey(text2Key(text)) : null);
        } catch (RuntimeException e) {
            setValue(null);
        }
    }
}
