package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.AssetId;
import im.mak.paddle.Account;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.TransferTransaction.LATEST_VERSION;
import static com.wavesplatform.transactions.common.AssetId.WAVES;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.helpers.transaction_senders.BaseTransactionSender.getTxInfo;
import static im.mak.paddle.helpers.transaction_senders.TransferTransactionSender.*;
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

    @BeforeAll
    static void before() {
        async(
                () -> {
                    alice = new Account(DEFAULT_FAUCET);
                    alice.createAlias(randomNumAndLetterString(15));
                    issuedAssetId = alice.issue(i -> i.name("Test_Asset").quantity(1000).decimals(8)).tx().assetId();
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
                            .script(SCRIPT_PERMITTING_OPERATIONS)).tx().assetId();
                }
        );
    }

    @Test
    @DisplayName("min transfer issued asset on address")
    void transferTransactionIssuedAssetByAddressTest() {
        Amount amount = Amount.of(MIN_TRANSFER_SUM, issuedAssetId);
        for (int v = 1; v <= LATEST_VERSION; v++) {
            transferTransactionSender(amount, alice, bob, ADDRESS, MIN_FEE, v);
            checkTransferTransaction(amount, alice, bob, MIN_FEE);
        }
    }

    @Test
    @DisplayName("min transfer WAVES on alias")
    void transferTransactionWavesByAliasTest() {
        Amount amount = Amount.of(MIN_TRANSFER_SUM, WAVES);
        for (int v = 1; v <= LATEST_VERSION; v++) {
            transferTransactionSender(amount, alice, bob, ALIAS, MIN_FEE, v);
            checkTransferTransaction(amount, alice, bob, MIN_FEE);
        }
    }

    @Test
    @DisplayName("transfer all WAVES on address")
    void transferTransactionWavesByAddressTest() {
        node().faucet().transfer(alice, DEFAULT_FAUCET, WAVES);
        Amount amount = Amount.of(aliceBalance - MIN_FEE, WAVES);
        transferTransactionSender(amount, alice, bob, ADDRESS, MIN_FEE, 1);
        checkTransferTransaction(amount, alice, bob, MIN_FEE);
    }

    @Test
    @DisplayName("transfer all issued asset on alias")
    void transferTransactionIssuedAssetByAliasTest() {
        Amount amount = Amount.of(alice.getBalance(issuedAssetId), issuedAssetId);
        transferTransactionSender(amount, alice, bob, ALIAS, MIN_FEE, 3);
        checkTransferTransaction(amount, alice, bob, MIN_FEE);
    }

    @Test
    @DisplayName("transfer all WAVES on alias")
    void transferTransactionAllWavesByAliasTest() {
        Amount amount = Amount.of(bobBalance - MIN_FEE, WAVES);
        transferTransactionSender(amount, bob, alice, ALIAS, MIN_FEE, LATEST_VERSION);
        checkTransferTransaction(amount, bob, alice, MIN_FEE);
    }

    @Test
    @DisplayName("transfer minimum smart asset on address")
    void transferMinSmartAsset() {
        long fee = MIN_FEE + EXTRA_FEE;
        Amount amount = Amount.of(MIN_TRANSFER_SUM, issuedSmartAssetId);
        transferTransactionSender(amount, acc, alice, ADDRESS, fee, 2);
        checkTransferTransaction(amount, acc, alice, fee);
    }

    @Test
    @DisplayName("transfer almost all smart asset on alias")
    void transferMaxSmartAsset() {
        long transferSum = acc.getBalance(issuedSmartAssetId) - MIN_TRANSFER_SUM;
        long fee = MIN_FEE + EXTRA_FEE;
        Amount amount = Amount.of(transferSum, issuedSmartAssetId);
        transferTransactionSender(amount, acc, alice, ALIAS, fee, 1);
        checkTransferTransaction(amount, acc, alice, fee);
    }

    private void checkTransferTransaction(Amount amount, Account from, Account to, long fee) {
        aliceBalance = alice.getWavesBalance();
        bobBalance = bob.getWavesBalance();

        assertAll(
                () -> assertThat(getTxInfo().applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(from.getBalance(getAsset())).isEqualTo(getSenderBalanceAfterTransaction()),
                () -> assertThat(to.getBalance(getAsset())).isEqualTo(getRecipientBalanceAfterTransaction()),
                () -> assertThat(getTransferTx().fee().value()).isEqualTo(fee),
                () -> assertThat(getTransferTx().fee().assetId()).isEqualTo(WAVES),
                () -> assertThat(getTransferTx().attachment()).isEqualTo(getBase58StringAttachment()),
                () -> assertThat(getTransferTx().sender()).isEqualTo(from.publicKey()),
                () -> assertThat(getTransferTx().amount()).isEqualTo(amount),
                () -> assertThat(getTransferTx().type()).isEqualTo(4)
        );
    }
}
