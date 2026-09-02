package com.stockup.backend.domain.bharosa;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Turns pillar values into things a shopkeeper's customer can actually read.
 *
 * Two rules shape this:
 *  - never show the maths;
 *  - pick the most *informative* tags, not the most flattering. A banner that
 *    only ever praises is advertising, and shoppers learn to ignore it.
 */
@Component
public class BharosaExplainer {

    /** The two strongest signals, for the compact store banner. */
    public List<BharosaTag> bannerTags(BharosaPillars p) {
        List<Scored> candidates = new ArrayList<>();

        if (p.isUnproven()) {
            // A new shop's most honest signal is that it is new.
            candidates.add(new Scored(BharosaTag.info("🌱", "New shop"), 1.0));
        }

        Double secs = p.medianResponseSeconds();
        if (secs != null) {
            if (secs <= 300) {
                candidates.add(new Scored(BharosaTag.positive("⚡", "Replies fast"), 0.9));
            } else if (secs <= 720) {
                candidates.add(new Scored(BharosaTag.info("💬", "Replies"), 0.4));
            } else {
                candidates.add(new Scored(BharosaTag.attention("🕐", "Slow to reply"), 0.85));
            }
        } else if (p.responsiveness() < 0.3 && !p.isUnproven()) {
            candidates.add(new Scored(BharosaTag.attention("🕐", "Rarely replies"), 0.9));
        }

        if (!p.isUnproven()) {
            double v = p.promiseKeeping();
            if (v >= 0.9) {
                candidates.add(new Scored(BharosaTag.positive("📦", "Keeps promises"), 0.95));
            } else if (v >= 0.7) {
                candidates.add(new Scored(BharosaTag.attention("📦", "Usually reliable"), 0.6));
            } else if (v > 0) {
                candidates.add(new Scored(BharosaTag.urgent("⚠", "Often cancels"), 1.0));
            }
        }

        if (p.trajectory() >= 0.03) {
            candidates.add(new Scored(BharosaTag.positive("📈", "Improving"), 0.7));
        } else if (p.trajectory() <= -0.03) {
            candidates.add(new Scored(BharosaTag.attention("📉", "Slipping lately"), 0.75));
        }

        // Rare by design — it should mean something when a shopper sees it.
        if (p.confidence() >= 12 && p.promiseKeeping() >= 0.95 && p.consistency() >= 0.97) {
            candidates.add(new Scored(BharosaTag.special("🛡", "Highly trusted"), 1.2));
        }

        return candidates.stream()
                .sorted(Comparator.comparingDouble(Scored::informativeness).reversed())
                .limit(2)
                .map(Scored::tag)
                .toList();
    }

    /** The fuller list shown when someone taps the score. */
    public List<String> reasons(BharosaPillars p) {
        List<String> out = new ArrayList<>();

        if (p.isUnproven()) {
            out.add("🌱 New to StockUp — not enough history yet to judge fairly");
            out.add("🛡 Starts at a neutral score rather than a flattering one");
            return out;
        }

        Double secs = p.medianResponseSeconds();
        if (secs != null) {
            long mins = Math.round(secs / 60.0);
            out.add(mins <= 5
                    ? "⚡ Answers quickly — usually within " + Math.max(1, mins) + " min"
                    : "🕐 Takes about " + mins + " min to answer");
        }

        double pct = Math.round(p.promiseKeeping() * 100);
        if (pct >= 90) {
            out.add("📦 Hands over what they promise — " + (int) pct + "% of reservations completed");
        } else if (pct >= 70) {
            out.add("📦 Usually completes reservations — " + (int) pct + "%");
        } else {
            out.add("⚠ Only completes " + (int) pct + "% of reservations");
        }

        if (p.distinctCustomers() >= 5) {
            out.add("🔄 Trusted by " + p.distinctCustomers() + " different shoppers, not a handful of regulars");
        } else if (p.distinctCustomers() > 0) {
            out.add("👥 " + p.distinctCustomers() + " shopper" + (p.distinctCustomers() == 1 ? "" : "s") + " so far");
        }

        if (p.consistency() >= 0.97) {
            out.add("✅ Dependable across different shoppers, not just some");
        } else if (p.consistency() <= 0.9) {
            out.add("〰 Experience varies between shoppers");
        }

        // Deliberately included: a score that only explains itself in praise
        // reads as marketing rather than information.
        if (p.trajectory() >= 0.03) {
            out.add("📈 Getting better recently");
        } else if (p.trajectory() <= -0.03) {
            out.add("📉 Slightly less reliable this month than before");
        }

        return out;
    }

    private record Scored(BharosaTag tag, double informativeness) { }
}
