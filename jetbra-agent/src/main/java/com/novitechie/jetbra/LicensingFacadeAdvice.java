package com.novitechie.jetbra;

import net.bytebuddy.asm.Advice;

import java.util.Calendar;
import java.util.Date;

public class LicensingFacadeAdvice {

    @Advice.OnMethodExit
    public static void intercept(@Advice.Return(readOnly = false) Date expirationDate) {
        RuntimeException e = new RuntimeException();
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            if (stackTraceElement.getFileName() == null){
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 50);
                expirationDate = calendar.getTime();
                break;
            }
        }
    }
}
