-- Seed the demo project used by the tool-using agent (see ProjectContextTool / AgentBacklogService),
-- so createBacklogItem can persist against an existing project out of the box.
insert into project (id, name, description, created_at)
values ('devbacklog-ai-assistant',
        'DevBacklog AI Assistant',
        'Turns ideas, bugs and feature requests into structured engineering backlog items.',
        now());
