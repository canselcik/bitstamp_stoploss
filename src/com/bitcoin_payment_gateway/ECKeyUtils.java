package com.bitcoin_payment_gateway;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.apache.commons.codec.binary.Base64;


import java.io.UnsupportedEncodingException;

public class ECKeyUtils {
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
            Logger.l("Failed to encode private key");
            e.printStackTrace();
        }

        if(string_export_pkey == null){
            Logger.l("Failed to encode private key");
            return null;
        }

        return human_addr + "," + human_pkey + "," + string_export_pkey;
    }

    public static ECKey getECKey(String str){
        String[] components = str.split(",");
        if(components.length != 3) {
            Logger.l("Cannot import '" + str + "'");
            return null;
        }
        String b64_encoded_pkey = components[2];
        byte[] pkey = Base64.decodeBase64(b64_encoded_pkey);
        return ECKey.fromPrivate(pkey);
    }

}
