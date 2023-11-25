package win.novice.li;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.PKIXBuilderParameters;
import java.util.HashSet;

@SpringBootApplication
public class JetbraServerApplication {
    public static void main(String[] args) throws InvalidAlgorithmParameterException {
        SpringApplication.run(JetbraServerApplication.class,args);
    }
}
