package com.dveamer.batch.domains.price

class PriceSql {

    public static final String findAll = """
        select id, amount, amount_us, amount_eu
        from product_price
    """;

    public static final String findMaxAmount = """
        select max(amount) as max_amount
        from product_price
    """;

    public static final String updateAmount = """
        update product_price
        set  amount = :amount
            ,amount_us = :amountUs
            ,amount_eu = :amountEu
        where 1=1
        and id = :id
    """;

    public static final String clearForeignCurrency = """
        update product_price
        set  amount_us = 1
            ,amount_eu = 1
    """;

}
