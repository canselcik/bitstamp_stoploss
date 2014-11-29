package com.bitcoin_payment_gateway;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.LoggerFactory;

public class Main {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class);
    private static void setGlobalLoggingLevel(Level l){
        ch.qos.logback.classic.Logger r =
                (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        r.setLevel(l);
    }

    private static void addKeysToStorage(BitcoinNetworkProvider p, int count){
        if(p == null || count == 0) return;
        while(count > 0){
            ECKey key = ECKeyUtils.getRandomKey();
            String address = ECKeyUtils.ECKeyToString(key, p.getNetworkParameters());
            log.debug("Creating: {}", address);
            p.addKey(key, 0);
            count--;
        }
    }

    public static void main(String[] args) throws Exception {
        setGlobalLoggingLevel(Level.ERROR);

        BitcoinNetworkProvider b = new BitcoinNetworkProvider("localhost", "inhash", "user", "kp5g6d",
                "/Users/user/Desktop/mainnet.bin", MainNetParams.get(), 0, "1MzszV4PTEyK578hshMzx7Fqb1kdth9osx");

        // Make sure we have at least 120 keys to watch
        int keysToGenerate = 120 - b.getKeyCount();
        addKeysToStorage(b, keysToGenerate);

        log.info("Following {} addresses", b.getKeyCount());
        b.start();
    }

}
