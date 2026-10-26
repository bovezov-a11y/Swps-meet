-- Expo push tokens, one per installed app. A token moves to whoever logged in on that phone last.
create table push_tokens (
    token      varchar(255) primary key,
    user_id    uuid        not null references users (id) on delete cascade,
    updated_at timestamptz not null
);

create index push_tokens_user_id_idx on push_tokens (user_id);
