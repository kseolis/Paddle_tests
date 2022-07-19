package im.mak.paddle.helpers;

import static im.mak.paddle.helpers.Randomizer.getRandomInt;

public class ConstructorRideFunctions {
    private final static int decimals = getRandomInt(0, 8);
    private final static int quantity = getRandomInt(10000, 100000);
    private final static int nonce = getRandomInt(0, 10);
    private final static String issuedAssetName = "\"issuedAsset\"";
    private final static String issuedAssetDescription = "\"asset from ride script\"";

    public static String assetsFunctionBuilder(int libVersion, String script, String functions, String args) {
        final StringBuilder sb = new StringBuilder();
        sb.append("{-# STDLIB_VERSION ").append(libVersion).append(" #-}\n")
                .append("{-# CONTENT_TYPE DAPP #-}\n")
                .append("{-# SCRIPT_TYPE ACCOUNT #-}\n")
                .append("@Callable(i)\n")
                .append("func setData(").append(args).append(")={\n");

        sb.append("let issueAsset = Issue(").append(issuedAssetName).append(",")
                .append(issuedAssetDescription).append(",")
                .append(quantity).append(",")
                .append(decimals).append(",")
                .append("true").append(",")
                .append(script).append(",")
                .append(nonce).append(")");

        sb.append("\nlet issueAssetId = issueAsset.calculateAssetId()\n");

        sb.append("[\n")
                .append("\tissueAsset,\n")
                .append("\t").append(functions).append("\n]\n")
                .append("}");

        System.out.println(sb);

        return sb.toString();
    }

    public static String defaultFunctionBuilder(String funcArgs, String functions, long libVersion) {
        final StringBuilder sb = new StringBuilder();
        sb.append("{-# STDLIB_VERSION ").append(libVersion).append(" #-}\n")
                .append("{-# CONTENT_TYPE DAPP #-}\n")
                .append("{-# SCRIPT_TYPE ACCOUNT #-}\n")
                .append("@Callable(i)\n")
                .append("func setData(").append(funcArgs).append(")={\n");

        sb.append(functions)
                .append("}");
        System.out.println(sb);
        return sb.toString();
    }
}
