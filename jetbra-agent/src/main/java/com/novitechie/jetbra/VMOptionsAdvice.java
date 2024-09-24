package com.novitechie.jetbra;

import net.bytebuddy.asm.Advice;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class VMOptionsAdvice {

    @Advice.OnMethodExit
    public static void intercept(@Advice.Return(readOnly = false) Path r) throws Exception {
        RuntimeException e = new RuntimeException();
        Pattern pattern = Pattern.compile("\\A\\p{ASCII}*\\z");
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            if (!pattern.matcher(stackTraceElement.getMethodName()).matches()){
                r = null;
                break;
            }
        }
    }
}
