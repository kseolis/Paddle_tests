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

import static com.wavesplatform.transactions.TransferTransaction.LATEST_VERSION;
import static com.wavesplatform.transactions.common.AssetId.WAVES;
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
            transferTransaction(amount, alice, bob, ADDRESS, MIN_FEE, v);
        }
    }

    @Test
    @DisplayName("min transfer WAVES on alias")
    void transferTransactionWavesByAliasTest() {
        Amount amount = Amount.of(MIN_TRANSFER_SUM, WAVES);
        for (int v = 1; v <= LATEST_VERSION; v++) {
            transferTransaction(amount, alice, bob, ALIAS, MIN_FEE, v);
        }
    }

    @Test
    @DisplayName("transfer all WAVES on address")
    void transferTransactionWavesByAddressTest() {
        node().faucet().transfer(alice, DEFAULT_FAUCET, WAVES);
        Amount amount = Amount.of(aliceBalance - MIN_FEE, WAVES);
        transferTransaction(amount, alice, bob, ADDRESS, MIN_FEE, 1);
    }

    @Test
    @DisplayName("transfer all issued asset on alias")
    void transferTransactionIssuedAssetByAliasTest() {
        Amount amount = Amount.of(alice.getBalance(issuedAssetId), issuedAssetId);
        transferTransaction(amount, alice, bob, ALIAS, MIN_FEE, 3);
    }

    @Test
    @DisplayName("transfer all WAVES on alias")
    void transferTransactionAllWavesByAliasTest() {
        Amount amount = Amount.of(bobBalance - MIN_FEE, WAVES);
        transferTransaction(amount, bob, alice, ALIAS, MIN_FEE, LATEST_VERSION);
    }

    @Test
    @DisplayName("transfer minimum smart asset on address")
    void transferMinSmartAsset() {
        long fee = MIN_FEE + EXTRA_FEE;
        Amount amount = Amount.of(MIN_TRANSFER_SUM, issuedSmartAssetId);
        transferTransaction(amount, acc, alice, ADDRESS, fee, 2);
    }

    @Test
    @DisplayName("transfer almost all smart asset on alias")
    void transferMaxSmartAsset() {
        long transferSum = acc.getBalance(issuedSmartAssetId) - MIN_TRANSFER_SUM;
        long fee = MIN_FEE + EXTRA_FEE;
        Amount amount = Amount.of(transferSum, issuedSmartAssetId);
        transferTransaction(amount, acc, alice, ALIAS, fee, 1);
    }

    private void transferTransaction
            (Amount amount, Account from, Account to, String addressOrAlias, long fee, int version) {
        AssetId asset = amount.assetId();
        long senderBalanceAfterTransaction = from.getBalance(asset) - amount.value() - (asset.isWaves() ? MIN_FEE : 0);
        long recipientBalanceAfterTransaction = to.getBalance(asset) + amount.value();
        var transferTo = addressOrAlias.equals(ADDRESS) ? to.address() : to.getAliases().get(0);

        TransferTransaction tx = TransferTransaction.builder(transferTo, amount)
                .attachment(base58StringAttachment)
                .version(version)
                .sender(from.publicKey())
                .fee(fee)
                .getSignedWith(from.privateKey());
        node().waitForTransaction(node().broadcast(tx).id());
        TransactionInfo transactionInfo = node().getTransactionInfo(tx.id());

        aliceBalance = alice.getWavesBalance();
        bobBalance = bob.getWavesBalance();

        assertAll(
                () -> assertThat(transactionInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(from.getBalance(asset)).isEqualTo(senderBalanceAfterTransaction),
                () -> assertThat(to.getBalance(asset)).isEqualTo(recipientBalanceAfterTransaction),
                () -> assertThat(tx.fee().value()).isEqualTo(fee),
                () -> assertThat(tx.fee().assetId()).isEqualTo(WAVES),
                () -> assertThat(tx.attachment()).isEqualTo(base58StringAttachment),
                () -> assertThat(tx.sender()).isEqualTo(from.publicKey()),
                () -> assertThat(tx.amount()).isEqualTo(amount),
                () -> assertThat(tx.type()).isEqualTo(4)
        );
    }
}
