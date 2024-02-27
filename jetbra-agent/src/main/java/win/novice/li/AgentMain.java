package win.novice.li;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.Set;

public class AgentMain {
    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        printLogo();
        AgentBuilder agentBuilder = newAgentBuilder();
        agentBuilder
                .type(ElementMatchers.named("java.security.cert.PKIXBuilderParameters"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                        .visit(Advice.to(PKIXBuilderParametersAdvice.class)
                                .on(ElementMatchers.isConstructor().and(ElementMatchers.takesArgument(0, Set.class)))))
                .asTerminalTransformation()

                .type(ElementMatchers.named("sun.net.www.http.HttpClient"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                        .visit(Advice.to(HttpClientAdvice.class)
                                .on(ElementMatchers.named("openServer").and(ElementMatchers.takesArgument(0, String.class)))))
                .asTerminalTransformation()

                .type(ElementMatchers.named("com.intellij.diagnostic.VMOptions"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                        .visit(Advice.to(VMOptionsAdvice.class)
                                .on(ElementMatchers.named("getUserOptionsFile"))))
                .asTerminalTransformation()

                .installOn(inst);

        agentBuilder.installOn(inst);
    }

    static AgentBuilder newAgentBuilder() {
        return new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."));
    }


    static void printLogo() {
        System.out.println("     _      _   _               \n" +
                "    | | ___| |_| |__  _ __ __ _ \n" +
                " _  | |/ _ \\ __| '_ \\| '__/ _` |\n" +
                "| |_| |  __/ |_| |_) | | | (_| |\n" +
                " \\___/ \\___|\\__|_.__/|_|  \\__,_|");
    }
}
