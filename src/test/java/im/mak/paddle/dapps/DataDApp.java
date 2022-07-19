package im.mak.paddle.dapps;

import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.invocation.*;
import im.mak.paddle.dapp.DApp;
import im.mak.paddle.dapp.DAppCall;

public class DataDApp extends DApp {

    public DataDApp(long initialBalance, String script) {
        super(initialBalance, script);
    }

    public DAppCall setData(int intArg, String binArg, boolean boolArg, String strArg) {
        Function function = Function.as(
                "setData",
                IntegerArg.as(intArg),
                BinaryArg.as(binArg),
                BooleanArg.as(boolArg),
                StringArg.as(strArg));
        return new DAppCall(address(), function);
    }

    public DAppCall setData(int intArg) {
        Function function = Function.as(
                "setData",
                IntegerArg.as(intArg));
        return new DAppCall(address(), function);
    }

    public DAppCall setData(byte[] base58String) {
        Function function = Function.as("setData", BinaryArg.as(base58String));
        return new DAppCall(address(), function);
    }

    public DAppCall setData(byte[] dApp2, int a, String key1, String key2, byte[] base58String) {
        Function function = Function.as(
                "setData",
                BinaryArg.as(dApp2),
                IntegerArg.as(a),
                StringArg.as(key1),
                StringArg.as(key2),
                BinaryArg.as(base58String)
        );
        return new DAppCall(address(), function);
    }
}
