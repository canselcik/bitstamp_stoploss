package com.bitcoin_payment_gateway;

import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.utils.Threading;

import java.util.List;

public class BitcoinNetworkPaymentListener implements WalletEventListener {
    private PeerGroup vPeerGroup;
    private BlockChain bc;
    private BlockStore bs;
    public BitcoinNetworkPaymentListener(PeerGroup vPeerGroup, BlockChain bc, BlockStore bs){
        super();
        this.bc = bc;
        this.bs = bs;
        this.vPeerGroup = vPeerGroup;
    }

    @Override
    // TODO: Find a way to know to which address TX has been sent
    // TODO: Make sure these happen in the given order:
    //        1. Commit to DB that the transaction has been sent for this tx inputs
    //        2. Broadcast the sweep tx
    //        3. Increment user balance ( receiving_addr -> user_id -> increment(balance) )
    //        4. Make sure to not increment balance if the TX has been noted to have been sent
    //        5. Despite not incrementing balance, broadcast a sweep tx anyway.
    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        try {
            // Gets the received amount - fees incurred for that transaction
            Coin netReceived = tx.getValueSentToMe(wallet);

            // Generate target addr
            Address toAddr = new Address(MainNetParams.get(), "1MzszV4PTEyK578hshMzx7Fqb1kdth9osx");

            // If transaction has more than 0.0001 for us, we will be using 0.0001 as the fee
            Coin fee = Coin.ZERO;
            if(netReceived.getValue() > 10000)
                fee = Coin.valueOf(10000);

            Coin afterFee = netReceived.subtract(fee);
            Logger.l("Net received: " + netReceived.getValue());
            Logger.l("Fee: " + fee.getValue());
            Logger.l("Cold wallet will receive: " + afterFee.getValue());

            // Generate a send request
            Wallet.SendRequest sr = Wallet.SendRequest.to(toAddr, afterFee);
            sr.emptyWallet = true;
            sr.signInputs = true;
            sr.fee = fee;
            sr.feePerKb = Coin.ZERO;

            // Add inputs to the request
            wallet.allowSpendingUnconfirmedTransactions();
            wallet.completeTx(sr);

            final String txHash = sr.tx.getHashAsString();
            Logger.l("Broadcasting tx: " + txHash);
            vPeerGroup.broadcastTransaction(sr.tx).addListener(new Runnable() {
                @Override
                public void run() {
                    Logger.l("Broadcast (" + txHash + ") was successful");
                }
            }, Threading.THREAD_POOL);

        } catch (AddressFormatException e) {
            Logger.l("AddressFormatException: " + e.toString());
            e.printStackTrace();
        } catch (InsufficientMoneyException e) {
            Logger.l("InsufficientMoneyException: " + e.toString());
            e.printStackTrace();
        }

    }

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        Logger.l("Sent " + prevBalance.subtract(newBalance).getValue());
    }

    @Override
    public void onReorganize(Wallet wallet) { }

    @Override
    public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) { }

    @Override
    public void onWalletChanged(Wallet wallet) { }

    @Override
    public void onScriptsAdded(Wallet wallet, List<Script> scripts) { }

    @Override
    public void onKeysAdded(List<ECKey> keys) { }
}