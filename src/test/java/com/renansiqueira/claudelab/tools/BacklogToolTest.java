package com.renansiqueira.claudelab.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BacklogToolTest {

    private final BacklogTool tool = new BacklogTool();

    @Test
    void createsAndStoresItem() {
        BacklogItem item = tool.createBacklogItem(
                "brabrix-dev", "Import customers via CSV", "Allow bulk customer import from a CSV file.");

        assertThat(item.id()).isNotBlank();
        assertThat(item.projectId()).isEqualTo("brabrix-dev");
        assertThat(item.title()).isEqualTo("Import customers via CSV");

        assertThat(tool.count()).isEqualTo(1);
        assertThat(tool.findById(item.id())).contains(item);
    }

    @Test
    void generatesDistinctIdsAndAccumulates() {
        BacklogItem first = tool.createBacklogItem("p", "A", "desc a");
        BacklogItem second = tool.createBacklogItem("p", "B", "desc b");

        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(tool.count()).isEqualTo(2);
        assertThat(tool.findAll()).contains(first, second);
    }
}
