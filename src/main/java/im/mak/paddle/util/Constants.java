package im.mak.paddle.util;

public class Constants {
    public static final byte ASSET_QUANTITY_MIN = 1;
    public static final long ASSET_QUANTITY_MAX = 9_223_372_036_854_775_807L;

    public static final long MIN_FEE = 100_000L;
    public static final long EXTRA_FEE = 400_000L;
    public static final long SUM_FEE = MIN_FEE + EXTRA_FEE;

    public static final long MIN_FEE_FOR_SET_SCRIPT = 1_000_000L;
    public static final long EXTRA_FEE_FOR_SET_SCRIPT = 4_000_000L;
    public static final long ONE_WAVES = 100_000_000L;

    public static final long MIN_FEE_FOR_EXCHANGE = 300_000L;
    public static final long EXCHANGE_FEE_FOR_SMART_ASSETS = EXTRA_FEE * 2;
    public static final long FEE_FOR_MASS_TRANSFER = 50000L ;

    public static final int DEFAULT_DECIMALS = 8;
    public static final byte ASSET_DECIMALS_MIN = 0;
    public static final byte ASSET_DECIMALS_MAX = 8;
    public static final int DEFAULT_FAUCET = 10_00000000;

    public static final byte MIN_TRANSFER_SUM = 1;
    public static final byte ORDER_V_3 = 3;
    public static final byte ORDER_V_4 = 4;

    public static int MIN_NUM_ACCOUNT_FOR_MASS_TRANSFER = 1;
    public static int MAX_NUM_ACCOUNT_FOR_MASS_TRANSFER = 100;

    public static final String ADDRESS = "Address";
    public static final String ALIAS = "Alias";

    public static final String SCRIPT_PERMITTING_OPERATIONS = "{-# STDLIB_VERSION 5 #-} {-# SCRIPT_TYPE ASSET #-} true";

    public static final long DEVNET_CHAIN_ID = 68;
    public static final String ACTIVE_STATUS_LEASE = "ACTIVE";
    public static final String INACTIVE_STATUS_LEASE = "INACTIVE";
}
