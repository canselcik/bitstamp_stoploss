package com.bitcoin_payment_gateway;


import org.bitcoinj.core.ECKey;
import org.slf4j.LoggerFactory;

public class Main {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        BitcoinNetworkProvider b = new BitcoinNetworkProvider("localhost", "inhash", "user", "kp5g6d", 1000);

        // Make sure we have at least 120 keys to watch
        while(b.getKeyCount() < 120){
            ECKey key = ECKeyUtils.getRandomKey();
            String address = ECKeyUtils.ECKeyToString(key);
            log.info("Importing: {}", address);
            b.addKey(key, 0);
        }

        log.info("Following {} addresses", b.getKeyCount());
        b.start();
    }

}
