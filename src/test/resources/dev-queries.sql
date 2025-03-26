set search_path to jarvis,public;

select * from BOOK;

select * from BOOK_PAGE where book_id = 103 order by page_num;

select b.title, count(bp.page_num) from book_page bp join book b on b.id = bp.book_id
    group by b.title;

select * from book_page where content ilike '%pg_stat_statements';

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


select * from book_page where to_tsvector('english', content) @@ to_tsquery('transaction & repeatable & read');

select * from book_page where to_tsvector('english', content) @@ plainto_tsquery('transactional serializable');

select * from book_page where to_tsvector('english', content) @@ to_tsquery('isorhamnetin');

---
create table book_page_test as
select generate_series / 100 as id, mod(generate_series, 100) as page_num, 'sample test content ' || generate_series || ' ' || gen_random_uuid() as content from generate_series(1, 1000);

select * from book_page_test;
drop table book_page_test;

select id, count(id) from book_page_test where content ilike '%0b%'
    group by id having count(id) > 10 order by 1;

select * from book_page_test where content ilike '%0b%';


with pages as (
    select * from book_page_test where content ilike '%0b%')
SELECT *
FROM (
         SELECT *, ROW_NUMBER() OVER (PARTITION BY id ORDER BY page_num) AS n
         FROM pages
     ) AS x
WHERE n <= 4

---
with pages as (
    select * from book_page_test where content ilike '%0b%')
SELECT *, ROW_NUMBER() OVER (PARTITION BY id ORDER BY page_num) AS n
FROM pages order by id;

with pages_ranked as (
    select book_page_test.*, ts_rank_cd(textsearch, query, 32) as rank
    from book_page_test, to_tsvector('english', content) textsearch, to_tsquery('sample') query
    where textsearch @@ query
    order by rank desc)
select *
from (
         select *, row_number() over (partition by id) as n
         from pages_ranked
     ) as x
where n <= 10;

select book_page.*, ts_rank_cd(textsearch, query, 32) as rank
    from book_page, to_tsvector('english', content) textsearch, to_tsquery('transact') query
         where textsearch @@ query
         order by rank desc;


with pages_ranked as
         (select book_page.*, ts_rank_cd(fts, query) as rank
          from book_page,
               to_tsvector('english', content) fts,
               websearch_to_tsquery('transaction serializable') query
          where fts @@ query)
select *,
       ts_headline('english', content, websearch_to_tsquery('english', 'transaction serializable'),
                      'MaxFragments=5, MaxWords=15, MinWords=3, StartSel=<b>, StopSel=</b>') as content_highlighted
from (select *,
             row_number() over (partition by book_id order by rank desc) as rownum_per_book,
             max(rank) over (partition by book_id)                       as max_rank_per_book
      from pages_ranked) as x
where rownum_per_book <= 5
order by max_rank_per_book desc, book_id limit 20;

SELECT ts_headline('english',
                   'The most common type of search
                 is to find all documents containing given query terms
                 and return them in order of their similarity to the
                 query.',
                   to_tsquery('english', 'query & similarity'));


with pages_ranked as
         (select bp.*, b.title, ts_rank_cd(fts, query) as rank
          from book_page bp join book b on b.id = bp.book_id,
                           to_tsvector('english', content) fts,
                           websearch_to_tsquery('transaction') query
          where fts @@ query)
select *,
       ts_headline('english', content, websearch_to_tsquery('english', 'transaction'),
                   'MaxFragments=5, MaxWords=15, MinWords=3, StartSel=<b>, StopSel=</b>') as content_highlighted
from (select *,
             row_number() over (partition by book_id order by rank desc) as rownum_per_book,
             max(rank) over (partition by book_id)                       as max_rank_per_book
      from pages_ranked) as x
where rownum_per_book <= :max_per_book
order by max_rank_per_book desc, book_id limit :max_total;