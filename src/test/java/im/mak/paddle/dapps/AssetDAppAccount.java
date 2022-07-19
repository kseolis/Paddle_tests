package im.mak.paddle.dapps;

import com.wavesplatform.transactions.invocation.BinaryArg;
import com.wavesplatform.transactions.invocation.Function;
import im.mak.paddle.dapp.DApp;
import im.mak.paddle.dapp.DAppCall;

public class AssetDAppAccount extends DApp {

    public AssetDAppAccount(long initialBalance, String script) {
        super(initialBalance, script);
    }

    public DAppCall setDataAssetId(byte[] arg) {
        return new DAppCall(address(), Function.as("setData", BinaryArg.as(arg)));
    }

    public DAppCall setDataAssetAndAddress(byte[] asset, byte[] address) {
        return new DAppCall(address(), Function.as(
                "setData",
                BinaryArg.as(asset), BinaryArg.as(address))
        );
    }
}
