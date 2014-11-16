package com.bitcoin_payment_gateway;

import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.PostgresFullPrunedBlockStore;
import org.bitcoinj.utils.Threading;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;

public class BitcoinNetworkProvider {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BitcoinNetworkProvider.class);

    private BitcoinNetworkEventListener eventListener;
    private BitcoinNetworkPaymentListener paymentListener;
    private PeerGroup vPeerGroup;
    private PostgresFullPrunedBlockStore s;
    private FollowedAddressStore fas;
    private BlockChain bc;
    private Wallet w;

    public BitcoinNetworkProvider(String DB_HOST, String DB_NAME, String DB_USER, String DB_PASSWD, int DB_DEPTH) throws Exception {
        fas = new FollowedAddressStore(DB_HOST, DB_NAME, DB_USER, DB_PASSWD);
        s = new PostgresFullPrunedBlockStore(MainNetParams.get(), DB_DEPTH, DB_HOST, DB_NAME, DB_USER, DB_PASSWD);
        bc = new BlockChain(MainNetParams.get(), s);

        vPeerGroup = new PeerGroup(MainNetParams.get(), bc);
        vPeerGroup.setUserAgent("Satoshi", "0.9.3");
        vPeerGroup.addPeerDiscovery(new DnsDiscovery(MainNetParams.get()));
        vPeerGroup.setMaxConnections(16);

        eventListener = new BitcoinNetworkEventListener(vPeerGroup, bc, s, true);
        paymentListener = new BitcoinNetworkPaymentListener(vPeerGroup, bc, s);

        vPeerGroup.addEventListener(eventListener, Threading.THREAD_POOL);

        w = new Wallet(MainNetParams.get());
        w.addEventListener(paymentListener);
        ArrayList<String> addresses = fas.getAddresses();
        for(String addr : addresses){
            ECKey key = ECKeyUtils.getECKey(addr);
            if(key == null)
                continue;
            w.importKey(key);
        }
        vPeerGroup.addWallet(w);
    }

    public boolean addKey(ECKey key, int user_id) {
        String serialized = ECKeyUtils.ECKeyToString(key);
        boolean res = true;
        try {
            res = res && fas.addAddress(user_id, serialized) <= 1;
        } catch (SQLException e){
            e.printStackTrace();
        }

        return w.importKey(key) && res;
    }

    public int getKeyCount() throws SQLException {
        ArrayList<String> addr = fas.getAddresses();
        if(fas == null || addr == null){
            log.error("Failed to get addresses from DB");
            return -1;
        }
        return addr.size();
    }

    public boolean removeKey(ECKey key){
        boolean res = true;
        try {
            String priv_key = key.getPrivateKeyEncoded(MainNetParams.get()).toString();
            res = res && fas.removeAddressByAnything(priv_key);
        } catch (SQLException e) {
            e.printStackTrace();
            res = res && false;
        }

        if(w.removeKey(key))
            return res;
        log.error("Failed to load the key into wallet");
        return false;
    }

    public void start() throws Exception {
        vPeerGroup.startAsync();

        log.info("Waiting for at least 6 peers...");
        vPeerGroup.waitForPeers(6).get();

        log.info("Initiating blockchain sync");
        vPeerGroup.downloadBlockChain();
    }

}
