package com.bitcoin_payment_gateway;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.bitcoinj.core.ECKey;
import org.slf4j.LoggerFactory;

public class Main {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class);
    private static void setLoggingLevel(Level l){
        ch.qos.logback.classic.Logger r = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        r.setLevel(l);
    }

    public static void main(String[] args) throws Exception {
        setLoggingLevel(Level.INFO);

        // TODO: Have BitcoinNetworkProvider initialize with MainNetParams or with TestNetParams
        BitcoinNetworkProvider b = new BitcoinNetworkProvider("localhost", "inhash", "user", "kp5g6d", 1000);


        // Make sure we have at least 120 keys to watch
        while(b.getKeyCount() < 120){
            ECKey key = ECKeyUtils.getRandomKey();
            String address = ECKeyUtils.ECKeyToString(key);
            log.debug("Importing: {}", address);
            b.addKey(key, 0);
        }

        log.info("Following {} addresses", b.getKeyCount());
        b.start();
    }

}
