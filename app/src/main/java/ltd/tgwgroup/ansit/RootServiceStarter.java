package ltd.tgwgroup.ansit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/** Credentials never enter shell arguments, process output or state queries. */
public final class RootServiceStarter {
    public enum State { RUNNING, STOPPED, UNKNOWN }
    public enum Hook { LOADED, ABSENT, UNKNOWN }
    public static final class Snapshot {
        final State state;
        final String version, apk, hookVersion, hookApk;
        final Hook hook;
        Snapshot(State state, String version, String apk, Hook hook, String hookVersion, String hookApk) {
            this.state = state; this.version = version; this.apk = apk;
            this.hook = hook; this.hookVersion = hookVersion; this.hookApk = hookApk;
        }
        boolean serviceCurrent(String expected) { return state == State.RUNNING && expected.equals(apk) && BuildConfig.VERSION_NAME.equals(version); }
        boolean hookCurrent(String expected) { return hook == Hook.LOADED && expected.equals(hookApk) && BuildConfig.VERSION_NAME.equals(hookVersion); }
    }
    private record Result(int exitCode, String output) { }
    static void start(String installedApk, File log) throws Exception {
        Result result = execute(installedApk, "bootstrap " + quote(installedApk) + " " + quote(log.getAbsolutePath()), 90);
        if (result.exitCode != 0 || !hasLine(result.output, "ANSIT_SERVICE_READY " + RootMain.SERVICE_VERSION))
            throw new IOException("Start failed. Grant AnsIt Root permission and try again.");
    }
    static State state(String installedApk) throws Exception {
        Result result = execute(installedApk, "manage state", 15);
        return parseState(result.output, result.exitCode);
    }
    static Snapshot inspect(String installedApk) throws Exception {
        Result result = execute(installedApk, "manage inspect", 60);
        return parseInspection(result.output, result.exitCode);
    }
    static void restartSystemUi(String installedApk) throws Exception {
        Result result = execute(installedApk, "restart-systemui", 20);
        if (result.exitCode != 0 || !hasLine(result.output, "ANSIT_SYSTEMUI_RESTART_SENT"))
            throw new IOException("Could not restart SystemUI. Check authorization in your Root manager or restart it manually.");
    }
    static void stop(String installedApk) throws Exception {
        Result result = execute(installedApk, "manage stop", 20);
        if (parseState(result.output, result.exitCode) != State.STOPPED) throw new IOException("The service did not confirm it stopped. Please try again.");
    }
    static void refreshOverlay(String installedApk) throws Exception {
        Result result = execute(installedApk, "refresh-overlay", 20);
        if (result.exitCode != 0 || !(hasLine(result.output, "ANSIT_OVERLAY_APPLIED") || hasLine(result.output, "ANSIT_OVERLAY_SAVED")))
            throw new IOException("Display settings were saved, but refreshing the current answer could not be confirmed.");
    }
    static void prepareAdjustment(String installedApk) throws Exception {
        Result result = execute(installedApk, "prepare-adjustment", 20);
        if (result.exitCode != 0 || !hasLine(result.output, "ANSIT_EDITOR_READY"))
            throw new IOException("Could not hide the current answer. Check the service and try again.");
    }
    static State parseState(String output, int exitCode) throws IOException {
        boolean running = hasLine(output, "ANSIT_SERVICE_RUNNING");
        boolean stopped = hasLine(output, "ANSIT_SERVICE_STOPPED");
        if (exitCode != 0 || running == stopped) throw new IOException("Could not confirm service state. Check Root authorization and try again.");
        return running ? State.RUNNING : State.STOPPED;
    }
    static Snapshot parseInspection(String output, int exitCode) throws IOException {
        State state = parseState(output, exitCode);
        if (!hasLine(output, "ANSIT_ROOT_OK")) throw new IOException("Grant AnsIt Root permission and check again.");
        String version = "", apk = "", hookVersion = "", hookApk = "";
        Hook hook = Hook.UNKNOWN;
        int identities = 0, hooks = 0;
        for (String line : output.split("\\R")) {
            if (line.startsWith("ANSIT_SERVICE_IDENTITY ")) {
                String[] values = line.split(" ");
                if (values.length != 3 || !values[1].matches("[A-Za-z0-9._-]+") || !values[2].matches("legacy|[a-f0-9]{64}"))
                    throw new IOException("Invalid service version response. Please try again.");
                identities++; version = values[1]; apk = values[2];
            } else if (line.startsWith("ANSIT_HOOK_LOADED ")) {
                String[] values = line.split(" ");
                if (values.length != 3 || !values[1].matches("[A-Za-z0-9._-]+") || !values[2].matches("[a-f0-9]{64}"))
                    throw new IOException("Invalid module status response. Please try again.");
                hooks++; hook = Hook.LOADED; hookVersion = values[1]; hookApk = values[2];
            } else if (line.equals("ANSIT_HOOK_ABSENT")) { hooks++; hook = Hook.ABSENT; }
            else if (line.equals("ANSIT_HOOK_UNKNOWN")) { hooks++; hook = Hook.UNKNOWN; }
        }
        if (hooks != 1 || identities != (state == State.RUNNING ? 1 : 0)) throw new IOException("The status check response was incomplete. Please check again.");
        return new Snapshot(state, version, apk, hook, hookVersion, hookApk);
    }
    private static boolean hasLine(String output, String expected) {
        for (String line : output.split("\\R")) if (line.equals(expected)) return true;
        return false;
    }
    private static Result execute(String installedApk, String arguments, int timeout) throws Exception {
        String command = "CLASSPATH=" + quote(installedApk)
            + " /system/bin/app_process /system/bin " + RootMain.class.getName() + " " + arguments;
        Process root = new ProcessBuilder("su", "-c", command).redirectErrorStream(true).start();
        StringBuilder output = new StringBuilder();
        Thread reader = new Thread(() -> {
            try (var lines = new java.io.BufferedReader(new java.io.InputStreamReader(root.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = lines.readLine()) != null) synchronized (output) {
                    if (output.length() < 8192) output.append(line).append('\n');
                }
            } catch (IOException ignored) { }
        }, "ansit-root-result");
        reader.setDaemon(true); reader.start();
        if (!root.waitFor(timeout, TimeUnit.SECONDS)) {
            root.destroy(); throw new IOException("Root authorization or service operation timed out. Please try again.");
        }
        reader.join(2000);
        synchronized (output) { return new Result(root.exitValue(), output.toString()); }
    }
    private static String quote(String value) { return "'" + value.replace("'", "'\"'\"'") + "'"; }
}
