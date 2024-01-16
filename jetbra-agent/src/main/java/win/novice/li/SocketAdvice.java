package win.novice.li;

import net.bytebuddy.asm.Advice;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class SocketAdvice {
    @Advice.OnMethodExit
    public static void intercept(@Advice.Argument(value = 0,readOnly = false) SocketAddress socketAddress) throws Exception {
        if (socketAddress instanceof InetSocketAddress){
            InetAddress address = ((InetSocketAddress) socketAddress).getAddress();
            if (address.getHostAddress().equals("116.62.33.138")){
                throw new ConnectException("拒绝连接");
            }
        }
    }
}
