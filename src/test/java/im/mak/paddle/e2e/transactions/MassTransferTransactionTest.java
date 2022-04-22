package im.mak.paddle.e2e.transactions;

import com.wavesplatform.transactions.MassTransferTransaction;
import com.wavesplatform.transactions.common.AssetId;
import com.wavesplatform.transactions.common.Base58String;
import com.wavesplatform.transactions.common.Recipient;
import com.wavesplatform.transactions.mass.Transfer;
import com.wavesplatform.wavesj.info.TransactionInfo;
import im.mak.paddle.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.wavesplatform.transactions.common.AssetId.WAVES;
import static com.wavesplatform.wavesj.ApplicationStatus.SUCCEEDED;
import static im.mak.paddle.Node.node;
import static im.mak.paddle.helpers.Randomizer.randomNumAndLetterString;
import static im.mak.paddle.util.Async.async;
import static im.mak.paddle.util.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MassTransferTransactionTest {
    private static Account alice;
    private static long aliceWavesBalance;
    private static long aliceAssetBalance;

    private static Account accountName1;
    private static Recipient accountName1Recipient;
    private static long accountName1Balance;

    private static Account accountName2;
    private static Recipient accountName2Recipient;
    private static long accountName2Balance;

    private static Account accountName3;
    private static Recipient accountName3Recipient;
    private static long accountName3Balance;

    private static Account accountName4;
    private static Recipient accountName4Recipient;
    private static long accountName4Balance;

    private static Account accountName5;
    private static Recipient accountName5Recipient;
    private static long accountName5Balance;

    private static AssetId issuedAsset;
    private static Base58String base58StringAttachment;

    long transactionCommission;

    @BeforeAll
    static void before() {
        base58StringAttachment = new Base58String("attachment");
        async(
                () -> {
                    alice = new Account(DEFAULT_FAUCET);
                    alice.createAlias(randomNumAndLetterString(15));
                    issuedAsset = alice.issue(i -> i.name("Test_Asset").quantity(1000_00000000L)).tx().assetId();
                    aliceWavesBalance = alice.getBalance(WAVES);
                    aliceAssetBalance = alice.getBalance(issuedAsset);
                },
                () -> {
                    accountName1 = new Account();
                    accountName1Recipient = new Account(DEFAULT_FAUCET).address();
                    accountName1Balance = accountName1.getWavesBalance();
                },
                () -> {
                    accountName2 = new Account();
                    accountName2Recipient = new Account(DEFAULT_FAUCET).address();
                    accountName2Balance = accountName2.getWavesBalance();
                },
                () -> {
                    accountName3 = new Account();
                    accountName3Recipient = new Account(DEFAULT_FAUCET).address();
                    accountName3Balance = accountName3.getWavesBalance();
                },
                () -> {
                    accountName4 = new Account();
                    accountName4Recipient = new Account(DEFAULT_FAUCET).address();
                    accountName4Balance = accountName4.getWavesBalance();
                    },
                () -> {
                    accountName5 = new Account();
                    accountName5Recipient = new Account(DEFAULT_FAUCET).address();
                    accountName5Balance = accountName5.getWavesBalance();
                }
        );
    }

    @Test
    @DisplayName("one transfer in a 'mass transfer transaction'")
    void oneTransferInMassTransfer() {
        Transfer transfer = Transfer.to(accountName1Recipient, MIN_TRANSFER_SUM);
        massTransferTransaction(WAVES, MIN_TRANSFER_SUM, transfer);
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' for four recipients")
    void massTransferFourRecipients() {
        Transfer transfer = Transfer.to(accountName1Recipient, MIN_TRANSFER_SUM);
        Transfer transfer2 = Transfer.to(accountName2Recipient, MIN_TRANSFER_SUM);
        Transfer transfer3 = Transfer.to(accountName3Recipient, MIN_TRANSFER_SUM);
        Transfer transfer4 = Transfer.to(accountName4Recipient, MIN_TRANSFER_SUM);
        massTransferTransaction(WAVES, MIN_TRANSFER_SUM, transfer, transfer2, transfer3, transfer4);
    }

    @Test
    @DisplayName("transfer in a 'mass transfer transaction' for five recipients")
    void massTransfer() {
        Transfer transfer = Transfer.to(accountName1Recipient, MIN_TRANSFER_SUM);
        Transfer transfer2 = Transfer.to(accountName2Recipient, MIN_TRANSFER_SUM);
        Transfer transfer3 = Transfer.to(accountName3Recipient, MIN_TRANSFER_SUM);
        Transfer transfer4 = Transfer.to(accountName4Recipient, MIN_TRANSFER_SUM);
        Transfer transfer5 = Transfer.to(accountName5Recipient, MIN_TRANSFER_SUM);
        massTransferTransaction(WAVES, MIN_TRANSFER_SUM, transfer, transfer2, transfer3, transfer4, transfer5);
    }

    private void massTransferTransaction(AssetId assetId, long amount, Transfer... transfers) {
        long balanceAfterMassTransfer = balanceAfterMassTransferCalculation(assetId, amount, transfers.length);

        MassTransferTransaction tx = alice.massTransfer(i -> i
                .attachment(base58StringAttachment)
                .assetId(assetId)
                .transfers(transfers)
        ).tx();

        TransactionInfo txInfo = node().getTransactionInfo(tx.id());

        assertAll(
                () -> assertThat(alice.getBalance(assetId)).isEqualTo(balanceAfterMassTransfer),
                () -> assertThat(txInfo.applicationStatus()).isEqualTo(SUCCEEDED),
                () -> assertThat(tx.sender()).isEqualTo(alice.publicKey()),
                () -> assertThat(tx.type()).isEqualTo(11),
                () -> assertThat((Object) txInfo.tx().fee().value()).isEqualTo(transactionCommission)
        );
    }

    private long balanceAfterMassTransferCalculation(AssetId assetId, long amount, int numberOfRecipients) {
        long numForRoundCheck = 100000;
        long additionalFeeForMassTransfer = FEE_FOR_MASS_TRANSFER * numberOfRecipients;
        if (additionalFeeForMassTransfer % numForRoundCheck != 0) {
            additionalFeeForMassTransfer = (long) Math.ceil((float) additionalFeeForMassTransfer / numForRoundCheck) * numForRoundCheck;
        }

        transactionCommission = MIN_FEE + additionalFeeForMassTransfer;
        long transactionSum = amount * numberOfRecipients;
        long balanceAfterMassTransfer = alice.getBalance(assetId) - transactionCommission - transactionSum;
        long result = alice.getBalance(assetId) - transactionCommission - transactionSum;

        System.out.println("numberOfRecipients: " + numberOfRecipients);
        System.out.println("additionalFeeForMassTransfer: " + additionalFeeForMassTransfer);
        System.out.println("transactionCommission: " + transactionCommission);
        System.out.println("transactionSum: " + transactionSum);
        System.out.println("balanceAfterMassTransfer: " + balanceAfterMassTransfer);

        return result;
    }
}

