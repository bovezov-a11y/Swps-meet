create table users (
    id                         uuid primary key,
    email                      varchar(255) not null unique,
    password_hash              varchar(255) not null,
    first_name                 varchar(50)  not null,
    role                       varchar(20)  not null,
    email_verified             boolean      not null default false,
    banned                     boolean      not null default false,
    privacy_policy_version     varchar(20),
    privacy_policy_accepted_at timestamptz,
    created_at                 timestamptz  not null
);

-- The current emailed six-digit code of an unconfirmed account. Only a hash is stored.
create table verification_codes (
    user_id    uuid primary key references users (id) on delete cascade,
    code_hash  varchar(64) not null,
    attempts   integer     not null default 0,
    expires_at timestamptz not null,
    created_at timestamptz not null
);

create table profiles (
    user_id         uuid primary key references users (id) on delete cascade,
    birth_date      date         not null,
    campus          varchar(20)  not null,
    program         varchar(100) not null,
    study_year      integer      not null,
    degree_level    varchar(20)  not null,
    bio             varchar(500),
    interests       varchar(40)[] not null default '{}',
    languages       varchar(40)[] not null default '{}',
    -- Instagram username without the @; shown only to matches.
    instagram       varchar(30),
    intents         varchar(20)[] not null,
    internship_role varchar(20),
    industry        varchar(100),
    workplace       varchar(100),
    gender          varchar(20),
    interested_in   varchar(20)[] not null default '{}',
    -- Hidden after enough reports until a moderator reviews the profile.
    hidden          boolean      not null default false,
    updated_at      timestamptz  not null
);

create table photos (
    id           uuid primary key,
    user_id      uuid        not null references users (id) on delete cascade,
    position     integer     not null,
    content_type varchar(50) not null,
    created_at   timestamptz not null
);

create index photos_user_id_idx on photos (user_id);

-- "I would like to meet this person" for one intent. Two likes in opposite directions make a match.
create table likes (
    id         uuid primary key,
    from_id    uuid        not null references users (id) on delete cascade,
    to_id      uuid        not null references users (id) on delete cascade,
    intent     varchar(20) not null,
    created_at timestamptz not null,
    unique (from_id, to_id, intent)
);

create index likes_to_id_idx on likes (to_id);

-- One match per pair of students; the application stores the pair in a fixed order so it is unique.
create table matches (
    id         uuid primary key,
    user_low   uuid        not null references users (id) on delete cascade,
    user_high  uuid        not null references users (id) on delete cascade,
    intent     varchar(20) not null,
    created_at timestamptz not null,
    unique (user_low, user_high)
);

create index matches_user_high_idx on matches (user_high);

create table messages (
    id         uuid primary key,
    match_id   uuid          not null references matches (id) on delete cascade,
    sender_id  uuid          not null references users (id) on delete cascade,
    body       varchar(2000) not null,
    created_at timestamptz   not null
);

create index messages_match_id_created_at_idx on messages (match_id, created_at);

create table blocks (
    id         uuid primary key,
    blocker_id uuid        not null references users (id) on delete cascade,
    blocked_id uuid        not null references users (id) on delete cascade,
    created_at timestamptz not null,
    unique (blocker_id, blocked_id)
);

create index blocks_blocked_id_idx on blocks (blocked_id);

create table reports (
    id          uuid primary key,
    reporter_id uuid        not null references users (id) on delete cascade,
    target_id   uuid        not null references users (id) on delete cascade,
    reason      varchar(30) not null,
    details     varchar(1000),
    created_at  timestamptz not null,
    resolved_at timestamptz,
    unique (reporter_id, target_id)
);

create index reports_target_id_idx on reports (target_id);
