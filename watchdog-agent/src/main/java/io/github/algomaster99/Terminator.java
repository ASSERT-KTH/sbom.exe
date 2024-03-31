package io.github.algomaster99;

import static io.github.algomaster99.terminator.commons.fingerprint.classfile.HashComputer.computeHash;

import io.github.algomaster99.terminator.commons.fingerprint.classfile.RuntimeClass;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.Bomi;
import io.github.algomaster99.terminator.commons.fingerprint.protobuf.ClassFile;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Terminator {
    private static Options options;

    public static void premain(String agentArgs, Instrumentation inst) {
        options = new Options(agentArgs);
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    ClassLoader loader,
                    String className,
                    Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain,
                    byte[] classfileBuffer) {

                return isLoadedClassAllowlisted(className, classfileBuffer);
            }
        });
    }

    private static byte[] isLoadedClassAllowlisted(String className, byte[] classfileBuffer) {
        Bomi fingerprints = options.getSbom();
        if (RuntimeClass.isProxyClass(classfileBuffer) || RuntimeClass.isBoundMethodHandle(classfileBuffer)) {
            return classfileBuffer;
        }
        for (ClassFile classFile : fingerprints.getClassFileList()) {
            if (classFile.getClassName().equals(className)) {
                String hash = computeHash(classfileBuffer);
                for (ClassFile.Attribute attribute : classFile.getAttributeList()) {
                    if (hash.equals(attribute.getHash())) {
                        return classfileBuffer;
                    }
                }
                if (options.shouldSkipShutdown()) {
                    System.err.println("[MODIFIED]: " + className);
                    return classfileBuffer;
                } else {
                    blueScreenOfDeath("[MODIFIED]: " + className);
                    System.exit(1);
                    return null;
                }
            }
        }
        if (options.shouldSkipShutdown()) {
            System.err.println("[NOT ALLOWLISTED]: " + className);
            return classfileBuffer;
        } else {
            blueScreenOfDeath("[NOT ALLOWLISTED]: " + className);
            System.exit(1);
            return null;
        }
    }

    private static void blueScreenOfDeath(String classViolation) {
        final String WHITE = "\u001B[97m";
        final String BOLD = "\u001B[1m";
        final String BACKGROUND_LIGHT_BLUE = "\u001B[104m";
        final String RESET = "\u001B[0m";

        String message = "                \n" + "             _  \n"
                + "           .' ) \n"
                + " ,.--.    / .'  \n"
                + "//    \\  / /    \n"
                + "\\\\    / / /     \n"
                + " `'--' . '      \n"
                + " ,.--. | |      \n"
                + "//   \\' '       \n"
                + "\\\\    / \\ \\     \n"
                + " `'--'   \\ \\    \n"
                + "          \\ '.  \n"
                + "           '._) \n"
                + "\n"
                + "\n"
                + "A fatal error has been detected by the Java Runtime Environment:\n"
                + "\n"
                + classViolation
                + "\n";

        System.out.println(BACKGROUND_LIGHT_BLUE + BOLD + WHITE);
        System.out.println(message);
        System.out.println(RESET);
    }
}
