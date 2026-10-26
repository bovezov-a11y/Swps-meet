-- The Looking for fun (dating) section was removed. Its matches (with their messages), likes and answers go with it.
delete from matches where intent = 'DATING';
delete from likes where intent = 'DATING';
update profiles set intents = array_remove(intents, 'DATING') where 'DATING' = any (intents);
alter table profiles drop column gender;
alter table profiles drop column interested_in;
