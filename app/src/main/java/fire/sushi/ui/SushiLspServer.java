package fire.sushi.ui;

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import bsh.Interpreter;
import de.robv.android.xposed.XposedBridge;

public class SushiLspServer {
    private static final String SOCKET_NAME = "sushi-ui";
    private static Interpreter sBshInterpreter;
    private static final Map<String, Object> sGlobalVariables = new HashMap<>();
    private static Handler sMainHandler;

    public static void start(Context context) {
        sMainHandler = new Handler(Looper.getMainLooper());
        
        try {
            sBshInterpreter = new Interpreter();
            sBshInterpreter.set("context", context);
            sBshInterpreter.set("handler", sMainHandler);
            sBshInterpreter.set("vars", sGlobalVariables);
            XposedBridge.log("SushiUI: BeanShell Interpreter initialized.");
        } catch (Exception e) {
            XposedBridge.log("SushiUI: Failed to init BeanShell: " + e);
        }

        new Thread(() -> {
            ExecutorService executor = Executors.newCachedThreadPool();
            try (LocalServerSocket server = new LocalServerSocket(SOCKET_NAME)) {
                XposedBridge.log("SushiUI: Listening on @" + SOCKET_NAME);
                while (true) {
                    final LocalSocket client = server.accept();
                    executor.submit(() -> handleClient(client));
                }
            } catch (Exception e) {
                XposedBridge.log("SushiUI: Server socket error: " + e);
            }
        }).start();
    }

    private static void handleClient(LocalSocket client) {
        PrintWriter out = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            
            String line = in.readLine();
            if (line != null) {
                String[] args = line.split(" ");
                if (args.length >= 2 && "java".equals(args[0])) {
                    byte[] d = Base64.getDecoder().decode(args[1]);
                    String code = new String(d, "UTF-8");
                    Object res = sBshInterpreter.eval(code);
                    out.println(res != null ? res.toString() : "OK");
                } else if (args.length >= 2 && "java-raw".equals(args[0])) {
                    String code = line.substring("java-raw".length()).trim();
                    Object res = sBshInterpreter.eval(code);
                    out.println(res != null ? res.toString() : "OK");
                } else if ("hello".equals(args[0])) {
                    out.println("Hello from SushiUI (SystemUI Hooked!)");
                } else {
                    out.println("Unknown UI command");
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            XposedBridge.log("SushiUI eval error:\n" + sw.toString());
            if (out != null) {
                out.println("ERROR: " + e.getMessage() + "\n" + sw.toString());
            }
        } finally {
            try { client.close(); } catch (Exception e) {}
        }
    }
}
