package Uber.context;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * replace
 * {@link org.springframework.mobile.device.DeviceWebArgumentResolver DeviceWebArgumentResolver}
 * for testing. Sufix url with &quot;device=mobile&quot; to simulate mobile
 * device.<br>
 * 替代
 * {@link org.springframework.mobile.device.DeviceWebArgumentResolver DeviceWebArgumentResolver}
 * 可作測試用，url串接可模擬手持裝置連線
 *
 * @author Kent Yeh
 */
public class DeviceWebArgumentResolver implements WebArgumentResolver, InitializingBean {

    private static final Logger logger = LogManager.getLogger(DeviceWebArgumentResolver.class);
    private static final MobileDevice mobileDevice = new MobileDevice();
    private String requestName = "device";
    private String mobileValue = "mobile";

    public DeviceWebArgumentResolver() {

    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public String getMobileValue() {
        return mobileValue;
    }

    public void setMobileValue(String mobileValue) {
        this.mobileValue = mobileValue;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.hasText(requestName)) {
            requestName = "device";
        }
        if (!StringUtils.hasText(mobileValue)) {
            mobileValue = "mobile";
        }
    }

    @Override
    public Object resolveArgument(MethodParameter param, NativeWebRequest request) throws Exception {
        for (Map.Entry entry : request.getParameterMap().entrySet()) {
            logger.info("parameter[\"{}\"]={}", entry.getKey(), entry.getValue());
        }
        if (Device.class.isAssignableFrom(param.getParameterType())) {
            if (mobileValue.equals(request.getParameter(requestName))) {
                return mobileDevice;
            } else {
                return DeviceUtils.getCurrentDevice(request);
            }
        } else {
            return WebArgumentResolver.UNRESOLVED;
        }
    }

    static class MobileDevice implements Device {

        @Override
        public boolean isMobile() {
            return true;
        }

        @Override
        public boolean isNormal() {
            return false;
        }

        @Override
        public boolean isTablet() {
            return false;
        }

        @Override
        public DevicePlatform getDevicePlatform() {
            return DevicePlatform.ANDROID;
        }
    }
}
