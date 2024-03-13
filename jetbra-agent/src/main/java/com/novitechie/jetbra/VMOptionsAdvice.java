package com.novitechie.jetbra;

import net.bytebuddy.asm.Advice;

import java.nio.file.Path;

public class VMOptionsAdvice {

    @Advice.OnMethodExit
    public static void intercept(@Advice.Return(readOnly = false) Path r) throws Exception {
        RuntimeException e = new RuntimeException();
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            if (stackTraceElement.getFileName() == null){
                r = null;
                break;
            }
        }
    }
}
