package fire.sushi.ui;

import android.app.Application;
import java.lang.reflect.Method;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam;

public class SushiLspModule extends XposedModule {
    private static boolean serverStarted = false;

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        XposedModule.log("SushiUI: onModuleLoaded");
        try {
            Method onCreate = Application.class.getDeclaredMethod("onCreate");
            getXposedInterface().hook(onCreate).intercept(chain -> {
                chain.proceed();
                if (serverStarted) return null;
                serverStarted = true;
                Application app = (Application) chain.getThisObject();
                XposedModule.log("SushiUI: starting server from Application.onCreate");
                SushiLspServer.start(app);
                return null;
            });
        } catch (NoSuchMethodException e) {
            XposedModule.log("SushiUI: failed to hook Application.onCreate: " + e);
        }
    }
}
