package com.stockup.backend.domain.bharosa;

/**
 * The five measured dimensions, each on 0..1, before they are combined.
 *
 * Kept as a separate value so the score can always be explained: every sentence
 * a customer reads is derived from one of these numbers.
 *
 * @param responsiveness  Pratikriya — do they answer, and fast enough to matter
 * @param promiseKeeping  Vachan — once committed, do they deliver
 * @param consistency     Sthirata — steady, or good for some and hopeless for others
 * @param confidence      Vishwas — effective evidence, capped per customer
 * @param trajectory      Sudhaar — improving or slipping, signed, small
 * @param medianResponseSeconds  raw latency, for the explanation text
 * @param distinctCustomers      breadth, for the explanation text
 */
public record BharosaPillars(
        double responsiveness,
        double promiseKeeping,
        double consistency,
        double confidence,
        double trajectory,
        Double medianResponseSeconds,
        long distinctCustomers,
        long totalEvents
) {
    public boolean isUnproven() {
        return confidence < 1.0;
    }
}
