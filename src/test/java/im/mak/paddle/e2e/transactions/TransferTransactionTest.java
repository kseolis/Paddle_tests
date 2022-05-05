package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.TransferTransaction;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class TransferTransactionTest {
    private static Account alice;
    private static long aliceBalance;

    private static Account bob;
    private static long bobBalance;

    private static Account acc;

    private static AssetId issuedAssetId;
    private static AssetId issuedSmartAssetId;
    private static Base58String base58StringAttachment;

    @BeforeAll
    static void before() {
        base58StringAttachment = new Base58String("attachment");
        async(
                () -> {
                    alice = new Account(DEFAULT_FAUCET);
                    alice.createAlias(randomNumAndLetterString(15));
                    issuedAssetId = alice.issue(i -> i.name("Test_Asset").quantity(1000_00000000L)).tx().assetId();
                    aliceBalance = alice.getWavesBalance();
                },
                () -> {
                    bob = new Account(DEFAULT_FAUCET);
                    bob.createAlias(randomNumAndLetterString(15));
                    bobBalance = bob.getWavesBalance() - MIN_FEE;
                },
                () -> {
                    acc = new Account(DEFAULT_FAUCET);
                    acc.createAlias(randomNumAndLetterString(15));
                    issuedSmartAssetId = acc.issue(i -> i.name("T_smart")
                            .quantity(1000)
                            .decimals(8)
                            .script("{-# SCRIPT_TYPE ASSET #-} true")).tx().assetId();
                }
        );
    }

    @Test
    @DisplayName("min transfer issued asset on address")
    void transferTransactionIssuedAssetByAddressTest() {
        transferTransaction(Amount.of(MIN_TRANSFER_SUM, issuedAssetId), alice, bob, ADDRESS, MIN_FEE);
    }

    @Test
    @DisplayName("min transfer WAVES on alias")
    void transferTransactionWavesByAliasTest() {
        transferTransaction(Amount.of(MIN_TRANSFER_SUM, AssetId.WAVES), alice, bob, ALIAS, MIN_FEE);
    }

    @Test
    @DisplayName("transfer all WAVES on address")
    void transferTransactionWavesByAddressTest() {
        Amount amount = Amount.of(aliceBalance - MIN_FEE, AssetId.WAVES);
        transferTransaction(amount, alice, bob, ADDRESS, MIN_FEE);
    }

    @Test
    @DisplayName("transfer all issued asset on alias")
    void transferTransactionIssuedAssetByAliasTest() {
        Amount amount = Amount.of(alice.getBalance(issuedAssetId), issuedAssetId);
        transferTransaction(amount, alice, bob, ALIAS, MIN_FEE);
    }

    @Test
    @DisplayName("transfer all WAVES on alias")
    void transferTransactionAllWavesByAliasTest() {
        Amount amount = Amount.of(bobBalance - MIN_FEE, AssetId.WAVES);
        transferTransaction(amount, bob, alice, ALIAS, MIN_FEE);
    }

    @Test
    @DisplayName("transfer minimum smart asset on address")
    void transferMinSmartAsset() {
        long fee = MIN_FEE + EXTRA_FEE;
        Amount amount = Amount.of(MIN_TRANSFER_SUM, issuedSmartAssetId);
        transferTransaction(amount, acc, alice, ADDRESS, fee);
    }

    @Test
    @DisplayName("transfer almost all smart asset on alias")
    void transferMaxSmartAsset() {
        long transferSum = acc.getBalance(issuedSmartAssetId) - MIN_TRANSFER_SUM;
        long fee = MIN_FEE + EXTRA_FEE;
        Amount amount = Amount.of(transferSum, issuedSmartAssetId);
        transferTransaction(amount, acc, alice, ALIAS, fee);
    }

    private void transferTransaction(Amount amount, Account from, Account to, String addressOrAlias, long fee) {
        AssetId asset = amount.assetId();
        long senderBalanceAfterTransaction = from.getBalance(asset) - amount.value() - (asset.isWaves() ? MIN_FEE : 0);
        long recipientBalanceAfterTransaction = to.getBalance(asset) + amount.value();
        var transferTo = addressOrAlias.equals(ADDRESS) ? to.address() : to.getAliases().get(0);

        TransferTransaction tx = from.transfer(transferTo, amount,
                i -> i.attachment(base58StringAttachment)
        ).tx();

        TransactionInfo transactionInfo = node().getTransactionInfo(tx.id());

        aliceBalance = alice.getWavesBalance();
        bobBalance = bob.getWavesBalance();

        assertAll(
                () -> assertThat(transactionInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(from.getBalance(asset)).isEqualTo(senderBalanceAfterTransaction),
                () -> assertThat(to.getBalance(asset)).isEqualTo(recipientBalanceAfterTransaction),
                () -> assertThat(tx.fee().value()).isEqualTo(fee),
                () -> assertThat(tx.fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(tx.attachment()).isEqualTo(base58StringAttachment),
                () -> assertThat(tx.sender()).isEqualTo(from.publicKey()),
                () -> assertThat(tx.amount()).isEqualTo(amount),
                () -> assertThat(tx.type()).isEqualTo(4)
        );
    }
}
