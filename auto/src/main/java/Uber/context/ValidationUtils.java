package Uber.context;

import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author Kent Yeh
 */
@Component("validationUtils")
public class ValidationUtils {

    @Autowired(required = false)
    @Qualifier("validator")
    ValidatorFactory validator;

    /**
     * Throw errorType Exception when validate failed.<br>
     * 驗證不通過時拋出 errorType 例外
     *
     * @param <T> entity's type. entity的型別
     * @param <E> exception type to be thrown.例外型別
     * @param entity entity to be validated.要驗證的物件
     * @param errorType exception type to be thrown.例外型別
     * @throws E exception to be thrown. 拋出的例外
     */
    public <T extends Object, E extends Exception> void validateMessage(T entity, Class<E> errorType) throws E {
        Set<ConstraintViolation<T>> violations = validate(entity);
        if (!violations.isEmpty()) {
            try {
                String msg = toMessage(violations);
                throw errorType.getDeclaredConstructor(java.lang.String.class).newInstance(msg);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Validate entity.<br>
     * 關證物件
     *
     * @param <T> entity's type. entity的型別
     * @param entity entity to be validated.要驗證的物件
     * @return
     */
    public <T extends Object> Set<ConstraintViolation<T>> validate(T entity) {
        if (validator != null) {
            return validator.getValidator().validate(entity);
        } else {
            return Collections.<ConstraintViolation<T>>emptySet();
        }
    }

    /**
     * Convert violations to text message.<br>
     * 將違反驗證點以文字表示.
     *
     * @param <T> entity's type. entity的型別
     * @param violations
     * @return violations' message text, empty means no violation.無違反時回返空字串
     */
    public <T extends Object> String toMessage(Set<ConstraintViolation<T>> violations) {
        if (violations == null || violations.isEmpty()) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation violation : violations) {
                sb.append("\n\t").append("'").append(violation.getPropertyPath().toString()).append("':")
                        .append(violation.getMessage());
            }
            return sb.toString();
        }
    }
}
