package win.novice.li.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import win.novice.li.model.License;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LicenseController {
    private static final PrivateKey PRIVATE_KEY = getPrivateKey();
    private static final X509Certificate CRT = getCertificate();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @PostMapping("/generateLicense")
    @SneakyThrows
    public Map<String, Object> generateLicense(@RequestBody @Validated License license) {
        Map<String, Object> ans = new HashMap<>();

        String licenseId = generateLicenseId();
        license.setLicenseId(licenseId);

        String licensePart = MAPPER.writeValueAsString(license);
        byte[] licensePartBytes = licensePart.getBytes(StandardCharsets.UTF_8);
        String licensePartBase64 = Base64.getEncoder().encodeToString(licensePartBytes);


        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(PRIVATE_KEY);
        signature.update(licensePartBytes);
        byte[] signatureBytes = signature.sign();
        String sigResultsBase64 = Base64.getEncoder().encodeToString(signatureBytes);

        String result = licenseId + "-" + licensePartBase64 + "-" + sigResultsBase64 + "-" + Base64.getEncoder().encodeToString(CRT.getEncoded());

        ans.put("license",result);
        return ans;
    }


    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateLicenseId() {
        int licenseLength = 10;
        StringBuilder sb = new StringBuilder(licenseLength);
        for (int i = 0; i < licenseLength; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }


    @SneakyThrows
    static PrivateKey getPrivateKey() {
        ClassPathResource licenseKeyResource = new ClassPathResource("jetbra.pem");
        Security.addProvider(new BouncyCastleProvider());
        PEMParser pemParser = new PEMParser(new InputStreamReader(licenseKeyResource.getInputStream()));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        Object object = pemParser.readObject();
        KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
        return kp.getPrivate();
    }

    @SneakyThrows
    static X509Certificate getCertificate() {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        ClassPathResource crtResource = new ClassPathResource("jetbra.crt");
        return (X509Certificate) certificateFactory.generateCertificate(crtResource.getInputStream());
    }
}

