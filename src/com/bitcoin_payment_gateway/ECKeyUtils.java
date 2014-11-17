package com.bitcoin_payment_gateway;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;


import java.io.UnsupportedEncodingException;

public class ECKeyUtils {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ECKeyUtils.class);
    public static ECKey getRandomKey(){
        ECKey key = new ECKey();
        return key;
    }

    public static String ECKeyToString(ECKey key){
        String human_addr = key.toAddress(MainNetParams.get()).toString();
        String human_pkey = key.getPrivateKeyEncoded(MainNetParams.get()).toString();
        byte[] export_pkey = key.getPrivKeyBytes();
        String string_export_pkey = null;

        byte[] b64_encoded_pkey = Base64.encodeBase64(export_pkey);
        try {
            string_export_pkey = new String(b64_encoded_pkey, "ASCII");
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode private key");
            e.printStackTrace();
        }

        if(string_export_pkey == null){
            log.error("Failed to encode private key");
            return null;
        }

        return human_addr + "," + human_pkey + "," + string_export_pkey;
    }

    public static ECKey getECKey(String str){
        String[] components = str.split(",");
        if(components.length != 3) {
            log.error("Cannot import '{}'", str);
            return null;
        }
        String b64_encoded_pkey = components[2];
        byte[] pkey = Base64.decodeBase64(b64_encoded_pkey.getBytes());
        return ECKey.fromPrivate(pkey);
    }

}
