package com.bitcoin_payment_gateway;

import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.utils.Threading;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BitcoinNetworkEventListener implements BlockChainListener {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BitcoinNetworkEventListener.class);
    private PeerGroup vPeerGroup;
    private BlockChain bc;
    private BlockStore bs;
    private Wallet w;
    private NetworkParameters params;
    private Address coldWalletAddr;
    public BitcoinNetworkEventListener(PeerGroup vPeerGroup, BlockChain bc, BlockStore bs,
                                       Wallet w, NetworkParameters params, String coldWalletAddr) throws Exception{
        super();
        this.bc = bc;
        this.params = params;
        this.coldWalletAddr = new Address(params, coldWalletAddr);
        this.w = w;
        this.bs = bs;
        this.vPeerGroup = vPeerGroup;
    }

    @Override
    public void notifyNewBestBlock(StoredBlock block) throws VerificationException {
        log.error("notifyNewBestBlock ({})", block.getHeader().getHashAsString());
    }

    @Override
    public void reorganize(StoredBlock splitPoint, List<StoredBlock> oldBlocks, List<StoredBlock> newBlocks) throws VerificationException {
        log.error("Reorganize called");
    }

    @Override
    public boolean isTransactionRelevant(Transaction tx) throws ScriptException {
        boolean relevant = w.isTransactionRelevant(tx) &&
                           tx.getValueSentToMe(w).isGreaterThan(Coin.ZERO);
        log.error("isRelevant({}) = {}", tx.getHashAsString(), relevant);
        return relevant;
    }

    @Override
    public void receiveFromBlock(Transaction tx, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        log.error("recvFromBlock TX: " + tx.getHashAsString());
        log.error("Depth in blocks: {}", tx.getConfidence().getDepthInBlocks());
        log.error("Confidence: {}", tx.getConfidence().toString());

        if(tx.isEveryOutputSpent()){
            log.error("Transaction {} is relevant but all outputs are spent, ignoring.", tx.getHashAsString());
            return;
        }

        if(blockType != AbstractBlockChain.NewBlockType.BEST_CHAIN){
            log.error("This transaction is not a part of the longest chain, ignoring.");
            return;
        }
        send(tx);
    }

    // TODO: Make sure these happen in the given order:
    //        1. Commit to DB that the transaction has been sent for this tx inputs
    //        2. Broadcast the sweep tx
    //        3. Increment user balance ( receiving_addr -> user_id -> increment(balance) )
    //        4. Make sure to not increment balance if the TX has been noted to have been sent
    //        5. Despite not incrementing balance, broadcast a sweep tx anyway.
    public boolean send(Transaction tx){
        try {
            // Gets the received amount - fees incurred for that transaction
            Coin netReceived = tx.getValueSentToMe(w);

            // If transaction has more than 0.0001 for us, we will be using 0.0001 as the fee
            Coin fee = Coin.ZERO;
            if(netReceived.getValue() > 10000)
                fee = Coin.valueOf(10000);

            Coin afterFee = netReceived.subtract(fee);

            log.error("Received {} SAT", netReceived.getValue());
            log.error("Deducted {}, transferring: {}", fee.getValue(), afterFee.getValue());

            // Generate a send request
            Wallet.SendRequest sr = Wallet.SendRequest.to(this.coldWalletAddr, afterFee);
            sr.emptyWallet = true;
            sr.signInputs = true;
            sr.fee = fee;
            sr.feePerKb = Coin.ZERO;

            // Add inputs to the request
            w.allowSpendingUnconfirmedTransactions();
            w.completeTx(sr);

            final String txHash = sr.tx.getHashAsString();
            log.error("Broadcasting tx: {}", txHash);
            vPeerGroup.broadcastTransaction(sr.tx).addListener(new Runnable() {
                @Override
                public void run() {
                    log.error("Broadcast ({}) was successful", txHash);
                }
            }, Threading.THREAD_POOL);
            return true;
        } catch (InsufficientMoneyException e) {
            log.error("InsufficientMoneyException: {}", e.toString());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        return false;
    }
}