import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Enumeration;
import java.util.Properties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import sun.security.pkcs11.SunPKCS11;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_TOKEN_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;


import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.log.SysoLogger;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.CertificateUtil;
import com.itextpdf.text.pdf.security.CrlClient;
import com.itextpdf.text.pdf.security.CrlClientOnline;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;

public class SignWithPKCS11USB {

    public static void main(String[] args) throws IOException, GeneralSecurityException, DocumentException {
        LoggerFactory.getInstance().setLogger(new SysoLogger());
        String src = args[0];
        String dest = args[1];
        String DLL = args[2];
        char[] pass = args[3].toCharArray();
        String alias = args[4];
        String reason = args[5];
        String location = args[6];
        long slotsWithTokens[] = new long[10];
        slotsWithTokens = getSlotsWithTokens(DLL);
        String config = "name=safenet5100\n" +
        "library=" + DLL + "\n" +
        "slotListIndex = " + slotsWithTokens[0];
        ByteArrayInputStream bais = new ByteArrayInputStream(config.getBytes());
        Provider providerPKCS11 = new SunPKCS11(bais);
        Security.addProvider(providerPKCS11);
        System.out.println(providerPKCS11.getName());
        BouncyCastleProvider providerBC = new BouncyCastleProvider();
        Security.addProvider(providerBC);
        KeyStore ks = KeyStore.getInstance("PKCS11");
        ks.load(null, pass);
        PrivateKey pk = (PrivateKey)ks.getKey(alias, pass);
        Certificate[] chain = ks.getCertificateChain(alias);
        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "signature");
        // Creating the signature
        ExternalSignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, providerPKCS11.getName());
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, CryptoStandard.CMS);
    }


    public static long[] getSlotsWithTokens(String libraryPath) throws IOException {
        CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
        String functionList = "C_GetFunctionList";

        initArgs.flags = 0;
        PKCS11 tmpPKCS11 = null;
        long[] slotList = null;
        try {
            try {
                tmpPKCS11 = PKCS11.getInstance(libraryPath, functionList, initArgs, false);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw ex;
            }
        } catch (PKCS11Exception e) {
            try {
                initArgs = null;
                tmpPKCS11 = PKCS11.getInstance(libraryPath, functionList, initArgs, true);
            } catch (IOException ex) {
             ex.printStackTrace();
         } catch (PKCS11Exception ex) {
             ex.printStackTrace();
         }
     }

     try {
        slotList = tmpPKCS11.C_GetSlotList(true);

        for (long slot : slotList){
            CK_TOKEN_INFO tokenInfo = tmpPKCS11.C_GetTokenInfo(slot);
            System.out.println("slot: "+slot+"\nmanufacturerID: "
                + String.valueOf(tokenInfo.manufacturerID) + "\nmodel: "
                + String.valueOf(tokenInfo.model));
        }
    } catch (PKCS11Exception ex) {
        ex.printStackTrace();
    } catch (Throwable t) {
        t.printStackTrace();
    }

    return slotList;

    }
}