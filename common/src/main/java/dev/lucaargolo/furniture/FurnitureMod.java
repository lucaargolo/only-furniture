package dev.lucaargolo.furniture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FurnitureMod {

    public static final String MOD_ID = "furniture";
    public static final String MOD_NAME = "Furniture Mod";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static FurnitureMod INSTANCE;

    public final PlatformHelper platformHelper = loadPlatformClass(PlatformHelper.class);

    public void init() {
        INSTANCE = this;
        LOG.info("Hello from Common! I'm running on {} ({})", platformHelper.getPlatformName(), platformHelper.getEnvironmentName());
    }

    public abstract String getPlatform();

    @SuppressWarnings("unchecked")
    public <T> T loadPlatformClass(Class<T> clazz, Object... parameters) {
        String name = clazz.getName();
        String platformName = name.substring(0, name.lastIndexOf('.')) + "." + getPlatform() + name.substring(name.lastIndexOf('.') + 1);
        Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }
        try {
            return (T) clazz.getClassLoader().loadClass(platformName).getConstructor(parameterTypes).newInstance(parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
