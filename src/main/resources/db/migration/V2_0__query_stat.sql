create table query (
    id bigserial primary key,
    query text not null,
    created timestamp default current_timestamp,
    metadata jsonb
);

create index query_query_idx on query using gin(query gin_trgm_ops);
---

create table github_user (
     id text primary key,
     constraint id_lowercase check (id = lower(id))
);
---

create table query_github_user (
    query_id bigint not null references query(id) on delete cascade,
    github_user_id text not null references github_user(id) on delete cascade,
    count int not null check(count > 0),
    primary key (query_id, github_user_id)
);

create index qgu_ghu_id_fk_idx on query_github_user(github_user_id);
