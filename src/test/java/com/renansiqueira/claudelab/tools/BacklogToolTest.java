package com.renansiqueira.claudelab.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.renansiqueira.claudelab.application.BacklogItemResponse;
import com.renansiqueira.claudelab.application.BacklogService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BacklogToolTest {

    @Test
    void persistsViaServiceAndMapsResult() {
        BacklogService service = Mockito.mock(BacklogService.class);
        UUID id = UUID.randomUUID();
        when(service.createItem("devbacklog-ai-assistant", "Import CSV", "Import customers via CSV"))
                .thenReturn(new BacklogItemResponse(id, "devbacklog-ai-assistant", null, "Import CSV",
                        "Import customers via CSV", null, null, List.of(), List.of(), Instant.now()));

        BacklogTool tool = new BacklogTool(service);
        BacklogItem item = tool.createBacklogItem("devbacklog-ai-assistant", "Import CSV", "Import customers via CSV");

        assertThat(item.id()).isEqualTo(id.toString());
        assertThat(item.projectId()).isEqualTo("devbacklog-ai-assistant");
        assertThat(item.title()).isEqualTo("Import CSV");
        assertThat(item.description()).isEqualTo("Import customers via CSV");
        verify(service).createItem("devbacklog-ai-assistant", "Import CSV", "Import customers via CSV");
    }
}
