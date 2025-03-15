set search_path to jarvis,public;

select * from BOOK;

select * from BOOK_PAGE;
select count(*) from BOOK_PAGE;
delete from BOOK where 1=1;

CREATE EXTENSION IF NOT EXISTS PG_STAT_STATEMENTS;

select pg_stat_statements_reset();

select * from pg_stat_statements order by total_exec_time desc;

select * from pg_stat_statements
    where query ilike '%book%' and query ilike '%insert%';



insert into book(id, title, content) values
    (1, 'Test book 0', null);

insert into book_page (page_num, book_id, title, content) VALUES
   (0, 1, 'page title wut?', 'Sample text with some code @Transactional  and also ComplerableFuture.allOf');


select * from book_page where to_tsvector('english', content) @@ to_tsquery('missing');

select to_tsvector('english', content) from book_page where book_id = 1;
select to_tsquery('''Transa''');

select * from book_page where to_tsvector('english', content) @@ to_tsquery('ComplerableFuture.allOf');

select * from book_page where to_tsvector('english', content) @@ to_tsquery('''@Transactional''');


select * from book_page where to_tsvector('english', content) @@ to_tsquery('transaction');
select * from book_page where to_tsvector('english', content) @@ to_tsquery('isorhamnetin');