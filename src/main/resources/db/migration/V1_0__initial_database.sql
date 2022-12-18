create table company (
    id int not null auto_increment,
    code varchar(10) not null,
    name varchar(255),
    description text,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    primary key (id),
    constraint uq_code unique (code)
);

create table company_latest_share_price (
    id int not null auto_increment,
    company_id int not null,
    share_price decimal(20,2),
    currency varchar(3),
    updated_at timestamp,
    primary key (id),
    constraint fk_company_latest_share_price_company foreign key(company_id) references company(id)
);

create table company_share_price (
    id bigint unsigned not null auto_increment,
    company_id int not null,
    share_price decimal(20,2),
    currency varchar(3),
    created_at timestamp,
    primary key (id),
    constraint fk_company_share_price_company foreign key(company_id) references company(id)
);

create index idx_company_share_price_company_time on company_share_price(company_id, created_at) using btree;
