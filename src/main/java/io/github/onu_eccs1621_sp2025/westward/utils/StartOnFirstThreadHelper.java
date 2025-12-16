package io.github.onu_eccs1621_sp2025.westward.utils;

import org.lwjgl.system.macosx.LibC;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adds some utilities to ensure that the JVM was started with the
 * {@code -XstartOnFirstThread} argument, which is required on macOS for LWJGL 3
 * to function. Also helps on Windows when users have names with characters from
 * outside the Latin alphabet, a common cause of startup crashes.
 * 
 * @author damios
 */
public final class StartOnFirstThreadHelper {
    /**
     * System Environmental Variable to check if the JVM has restarted or not
     */
    private static final String JVM_RESTARTED_ARG = "jvmIsRestarted";
    private StartOnFirstThreadHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * Starts a new JVM if the application was started on macOS without the
     * {@code -XstartOnFirstThread} argument. This also includes some code for
     * Windows, for the case where the user's home directory includes certain
     * non-Latin-alphabet characters (without this code, most LWJGL3 apps fail
     * immediately for those users). Returns whether a new JVM was started and
     * thus no code should be executed.
     * <p>
     * <u>Usage:</u>
     *
     * <pre><code>
     * public static void main(String... args) {
     * 	if (StartOnFirstThreadHelper.startNewJvmIfRequired()) return; // don't execute any code
     * 	// after this is the actual main method code
     * }
     * </code></pre>
     *
     * @param redirectOutput
     *            whether the output of the new JVM should be rerouted to the
     *            old JVM, so it can be accessed in the same place; keeps the
     *            old JVM running if enabled
     * @return whether a new JVM was started and thus no code should be executed
     *         in this one
     */
    public static boolean startNewJvmIfRequired(final boolean redirectOutput) {
        final String osName = System.getProperty("os.name").toLowerCase(Locale.US);
        if (!osName.contains("mac")) {
            if (osName.contains("windows")) {
                /*
                 Here, we are trying to work around an issue with how LWJGL3 loads its extracted .dll files.
                 By default, LWJGL3 extracts to the directory specified by "java.io.tmpdir", which is usually the user's home.
                 If the user's name has non-ASCII (or some non-alphanumeric) characters in it, that would fail.
                 By extracting to the relevant "ProgramData" folder, which is usually "C:\ProgramData", we avoid this.
                 */
                System.setProperty("java.io.tmpdir", System.getenv("ProgramData") + "/libGDX-temp");
            }
            return false;
        }

        final long pid = LibC.getpid();

        // check whether -XstartOnFirstThread is enabled
        if ("1".equals(System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid))) {
            return false;
        }

        // check whether the JVM was previously restarted
        if ("true".equals(System.getProperty(JVM_RESTARTED_ARG))) {
            DebugLogger.error("There was a problem evaluating whether the JVM was started with the -XstartOnFirstThread argument.");
            return false;
        }

        // Restart the JVM with -XstartOnFirstThread
        final List<String> jvmArgs = new ArrayList<>();
        final String javaExecPath = System.getProperty("java.home") + File.separator
                + "bin" + File.separator + "java";
        if (!new File(javaExecPath).exists()) {
            DebugLogger.error("A Java installation could not be found. If you are distributing this app with a bundled JRE, be sure to set the -XstartOnFirstThread argument manually!");
            return false;
        }
        jvmArgs.add(javaExecPath);
        jvmArgs.add("-XstartOnFirstThread");
        jvmArgs.add("-D" + JVM_RESTARTED_ARG + "=true");
        jvmArgs.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        jvmArgs.add("-cp");
        jvmArgs.add(System.getProperty("java.class.path"));
        String mainClass = System.getenv("JAVA_MAIN_CLASS_" + pid);
        if (mainClass == null) {
            final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            if (trace.length > 0) {
                mainClass = trace[trace.length - 1].getClassName();
            } else {
                DebugLogger.error("The main class could not be determined.");
                return false;
            }
        }
        jvmArgs.add(mainClass);

        try {
            if (!redirectOutput) {
                final ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
                processBuilder.start();
            } else {
                final Process process = new ProcessBuilder(jvmArgs)
                        .redirectErrorStream(true).start();
                final BufferedReader processOutput = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = processOutput.readLine()) != null) {
                    DebugLogger.info(line);
                }

                processOutput.close();
                process.waitFor();
            }
        } catch (Exception e) {
            DebugLogger.error("There was a problem restarting the JVM", e);
        }

        return true;
    }

    /**
     * Starts a new JVM if the application was started on macOS without the
     * {@code -XstartOnFirstThread} argument. Returns whether a new JVM was
     * started and thus no code should be executed. Redirects the output of the
     * new JVM to the old one.
     * <p>
     * <u>Usage:</u>
     *
     * <pre>
     * public static void main(String... args) {
     * 	if (StartOnFirstThreadHelper.startNewJvmIfRequired()) return; // don't execute any code
     * 	// the actual main method code
     * }
     * </pre>
     *
     * @return whether a new JVM was started and thus no code should be executed
     *         in this one
     */
    public static boolean startNewJvmIfRequired() {
        return startNewJvmIfRequired(true);
    }
}