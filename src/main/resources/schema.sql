create table product_price (
    id bigint not null,
    amount bigint,
    amount_us bigint,
    amount_eu bigint,
    primary key (id)
) engine = InnoDB;
