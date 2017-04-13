import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
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
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            System.out.println((String)aliases.nextElement() + "\n");
        }
        String alias = (String)ks.aliases().nextElement();
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