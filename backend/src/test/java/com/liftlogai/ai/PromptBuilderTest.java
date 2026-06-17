package com.liftlogai.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.liftlogai.ai.dto.AiFeedbackRequest;
import com.liftlogai.ai.prompt.PromptBuildResult;
import com.liftlogai.ai.prompt.PromptBuilder;
import com.liftlogai.ai.prompt.PromptContext;
import com.liftlogai.nutrition.dto.DailyNutritionSummaryResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    void promptSummaryDoesNotStoreRawQuestionOrSecretLikeText() {
        AiFeedbackRequest request = new AiFeedbackRequest(
                "weekly_review",
                "My password hunter2 and token abc123 should not be stored.",
                true,
                true,
                true
        );

        PromptBuildResult result = promptBuilder.build(request, emptyContext());

        assertThat(result.promptSummary()).contains("weekly_review feedback");
        assertThat(result.promptSummary()).contains("Custom question included.");
        assertThat(result.promptSummary()).doesNotContain("hunter2");
        assertThat(result.promptSummary()).doesNotContain("abc123");
        assertThat(result.prompt()).contains("password [REDACTED]");
        assertThat(result.prompt()).contains("token [REDACTED]");
        assertThat(result.prompt()).doesNotContain("hunter2");
        assertThat(result.prompt()).doesNotContain("abc123");
    }

    @Test
    void promptCanExcludeRequestedDataSources() {
        AiFeedbackRequest request = new AiFeedbackRequest(
                "goal_review",
                null,
                false,
                false,
                false
        );

        PromptBuildResult result = promptBuilder.build(request, emptyContext());

        assertThat(result.prompt()).contains("Recent workouts:\nNot included.");
        assertThat(result.prompt()).contains("Today's nutrition:\nNot included.");
        assertThat(result.prompt()).contains("Active goals:\nNot included.");
        assertThat(result.promptSummary()).contains("no fitness data sources");
    }

    private PromptContext emptyContext() {
        return new PromptContext(
                List.of(),
                new DailyNutritionSummaryResponse(LocalDate.of(2026, 6, 16), 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                List.of()
        );
    }
}
