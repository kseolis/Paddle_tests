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

    private static AssetId issuedAsset;
    private static Base58String base58StringAttachment;

    @BeforeAll
    static void before() {
        base58StringAttachment = new Base58String("attachment");
        async(
            () -> {
                alice = new Account(DEFAULT_FAUCET);
                alice.createAlias(randomNumAndLetterString(15));
                issuedAsset = alice.issue(i -> i.name("Test_Asset").quantity(1000_00000000L)).tx().assetId();
                aliceBalance = alice.getBalance(AssetId.WAVES);
            },
            () -> {
                bob = new Account(DEFAULT_FAUCET);
                bob.createAlias(randomNumAndLetterString(15));
                bobBalance = bob.getBalance(AssetId.WAVES);
            }
        );
    }

    @Test
    @DisplayName("min transfer issued asset on address")
    void transferTransactionIssuedAssetByAddressTest() {
        transferTransaction(Amount.of(MIN_TRANSFER_SUM, issuedAsset), alice, bob, ADDRESS);
    }

    @Test
    @DisplayName("min transfer WAVES on alias")
    void transferTransactionWavesByAliasTest() {
        transferTransaction(Amount.of(MIN_TRANSFER_SUM, AssetId.WAVES), alice, bob, ALIAS);
    }

    @Test
    @DisplayName("transfer all WAVES on address")
    void transferTransactionWavesByAddressTest() {
        Amount transferFullAliceAssetsSum = Amount.of(aliceBalance - MIN_FEE, AssetId.WAVES);
        transferTransaction(transferFullAliceAssetsSum, alice, bob, ADDRESS);
    }

    @Test
    @DisplayName("transfer all issued asset on alias")
    void transferTransactionIssuedAssetByAliasTest() {
        Amount transferFullAliceAssetsSum = Amount.of(aliceBalance - MIN_FEE, issuedAsset);
        transferTransaction(transferFullAliceAssetsSum, alice, bob, ALIAS);
    }

    @Test
    @DisplayName("transfer all WAVES alias")
    void transferTransactionAllWavesByAliasTest() {
        Amount transferFullBobAssetsSum = Amount.of(bobBalance - MIN_FEE, AssetId.WAVES);
        transferTransaction(transferFullBobAssetsSum, bob, alice, ALIAS);
    }

    private void transferTransaction(Amount transferSum, Account from, Account to, String addressOrAlias) {
        AssetId asset = transferSum.assetId();
        long senderBalanceAfterTransaction = from.getBalance(asset) - transferSum.value() - (asset.isWaves() ? MIN_FEE : 0);
        long recipientBalanceAfterTransaction = to.getBalance(asset) + transferSum.value();
        var transferTo = addressOrAlias.equals(ADDRESS) ? to.address() : to.getAliases().get(0);

        TransferTransaction tx = from.transfer(transferTo, transferSum,
                i -> i.attachment(base58StringAttachment)
        ).tx();

        TransactionInfo transactionInfo = node().getTransactionInfo(tx.id());

        aliceBalance = alice.getWavesBalance();
        bobBalance = bob.getWavesBalance();

        assertAll("check 'full amount transfer transaction' result",
                () -> assertThat(from.getBalance(asset)).isEqualTo(senderBalanceAfterTransaction),
                () -> assertThat(to.getBalance(asset)).isEqualTo(recipientBalanceAfterTransaction),
                () -> assertThat(transactionInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat((Object) transactionInfo.tx().fee().value()).isEqualTo(MIN_FEE),
                () -> assertThat((Object) transactionInfo.tx().fee().assetId()).isEqualTo(AssetId.WAVES),
                () -> assertThat(tx.attachment()).isEqualTo(base58StringAttachment),
                () -> assertThat(tx.sender()).isEqualTo(from.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(4)
        );
    }
}
