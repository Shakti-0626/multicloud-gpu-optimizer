package com.multicloud.optimizer.optimizer;

import com.multicloud.optimizer.domain.Price;
import com.multicloud.optimizer.domain.enums.PricingModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ExplanationGenerator {

    public String generateExplanation(
            OptionEvaluation chosen,
            OptionEvaluation onDemandEquivalent,
            String dataProvider,
            String dataRegion,
            boolean checkpointing
    ) {
        Price p = chosen.getPriceOption();
        StringBuilder sb = new StringBuilder();

        boolean isLocal = p.getInstanceType().getProvider().getName().equalsIgnoreCase(dataProvider)
                && p.getRegion().equalsIgnoreCase(dataRegion);

        if (isLocal) {
            sb.append(String.format("Data already resides in %s %s, eliminating cross-cloud egress costs. ",
                    dataProvider, dataRegion));
        } else {
            sb.append(String.format("Data is transferred from %s %s to %s %s with $%.2f egress cost. ",
                    dataProvider, dataRegion, p.getInstanceType().getProvider().getName(), p.getRegion(),
                    chosen.getEgressCost().doubleValue()));
        }

        if (p.getPricingModel() == PricingModel.SPOT) {
            if (onDemandEquivalent != null && onDemandEquivalent.getTotalCost() != null && onDemandEquivalent.getTotalCost().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal savingsPct = onDemandEquivalent.getTotalCost().subtract(chosen.getTotalCost())
                        .divide(onDemandEquivalent.getTotalCost(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(0, RoundingMode.HALF_UP);
                sb.append(String.format("Spot pricing delivers ~%d%% total savings versus On-Demand. ", savingsPct.intValue()));
            } else {
                sb.append("Spot pricing significantly minimizes hourly compute rate. ");
            }

            if (checkpointing) {
                sb.append("Periodic checkpointing restricts work loss risk to 10%, keeping interruption penalty low ($")
                        .append(chosen.getInterruptionCost().toPlainString()).append(").");
            } else {
                sb.append("Without checkpointing, interruption penalty reflects 50% re-compute overhead ($")
                        .append(chosen.getInterruptionCost().toPlainString()).append(").");
            }
        } else {
            sb.append("On-demand guarantees 0% interruption risk with immediate resource reservation.");
        }

        return sb.toString();
    }
}
