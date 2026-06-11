package fire.sushi.ui;

import android.app.Application;
import android.content.Context;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SushiLspModule implements IXposedHookLoadPackage {
    private static boolean serverStarted = false;

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        XposedBridge.log("SushiUI: SystemUI hooked successfully.");

        // Hook Application.onCreate to ensure we have a valid Context to pass to BeanShell
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (serverStarted) return;
                serverStarted = true;
                
                Context systemUiContext = (Context) param.thisObject;
                XposedBridge.log("SushiUI: Captured Context. Starting Socket Server...");
                
                SushiLspServer.start(systemUiContext);
            }
        });
    }
}
