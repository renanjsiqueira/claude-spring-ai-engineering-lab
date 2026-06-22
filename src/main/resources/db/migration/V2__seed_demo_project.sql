-- Seed the demo project used by the tool-using agent (see ProjectContextTool / AgentBacklogService),
-- so createBacklogItem can persist against an existing project out of the box.
insert into project (id, name, description, created_at)
values ('brabrix-dev',
        'Brabrix',
        'SaaS platform for managing customers, transactions and billing.',
        now());
