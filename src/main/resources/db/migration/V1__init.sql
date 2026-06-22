create table project (
    id          varchar(64) primary key,
    name        varchar(255) not null,
    description varchar(2000),
    created_at  timestamp not null
);

create table backlog_item (
    id         uuid primary key,
    project_id varchar(64) not null references project (id),
    type       varchar(32),
    title      varchar(255) not null,
    summary    varchar(2000),
    priority   varchar(32),
    user_story varchar(2000),
    created_at timestamp not null
);

create index idx_backlog_item_project on backlog_item (project_id);

create table acceptance_criterion (
    id              uuid primary key,
    backlog_item_id uuid not null references backlog_item (id) on delete cascade,
    description     varchar(2000) not null,
    position        integer not null
);

create table technical_task (
    id              uuid primary key,
    backlog_item_id uuid not null references backlog_item (id) on delete cascade,
    description     varchar(2000) not null,
    position        integer not null
);

create index idx_acceptance_criterion_item on acceptance_criterion (backlog_item_id);
create index idx_technical_task_item on technical_task (backlog_item_id);
